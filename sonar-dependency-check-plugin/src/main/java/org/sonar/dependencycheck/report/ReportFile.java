/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2021 dependency-check
 * philipp.dallig@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.dependencycheck.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class ReportFile {
    private static final Logger LOGGER = Loggers.get(ReportFile.class);

    protected final ReportFormat reportFormat;
    protected final String property;
    protected final File report;

    protected ReportFile(ReportFormat reportFormat, String property, File report) {
        this.reportFormat = reportFormat;
        this.property = property;
        this.report = report;
    }

    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(report.toPath());
    }

    @CheckForNull
    protected static File checkReport(@Nullable File report, ReportFormat reportFormat, String property) {
        if (report != null) {
            if (!report.exists()) {
                LOGGER.info("Dependency-Check {} report does not exists. Please check property {}:{}", reportFormat, property, report.getAbsolutePath());
                return null;
            }
            if (!report.isFile()) {
                LOGGER.info("Dependency-Check {} report is not a normal file", reportFormat);
                return null;
            }
            if (!report.canRead()) {
                LOGGER.info("Dependency-Check {} report is not readable", reportFormat);
                return null;
            }
        }
        return report;
    }

    @CheckForNull
    public String getReportContent() {
        String reportContent = null;
        try {
            reportContent = new String(Files.readAllBytes(report.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Could not read {}-Report", reportFormat, e);
        }
        return reportContent;
    }
}
