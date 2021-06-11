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
package org.sonar.dependencycheck.parser.element;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.NonNull;

@JsonIgnoreProperties({"reportSchema"})
public class Analysis {

    private final ScanInfo scanInfo;
    private final ProjectInfo projectInfo;
    private final Collection<Dependency> dependencies;

    @JsonCreator
    public Analysis(
        @JsonProperty(value = "scanInfo", required = true) @NonNull ScanInfo scanInfo,
        @JsonProperty(value = "projectInfo") @Nullable ProjectInfo projectInfo,
        @JsonProperty(value = "dependencies", required = true) @NonNull Collection<Dependency> dependencies) {
        this.scanInfo = scanInfo;
        this.projectInfo = projectInfo;
        this.dependencies = dependencies;
    }

    public ScanInfo getScanInfo() {
        return scanInfo;
    }

    public Optional<ProjectInfo> getProjectInfo() {
        return Optional.ofNullable(projectInfo);
    }

    public Collection<Dependency> getDependencies() {
        return dependencies;
    }

}
