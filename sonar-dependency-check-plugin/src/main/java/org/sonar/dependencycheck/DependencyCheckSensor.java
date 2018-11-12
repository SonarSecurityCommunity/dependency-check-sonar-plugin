/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2017 Steve Springett
 * steve.springett@owasp.org
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
package org.sonar.dependencycheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.log.Profiler;
import org.sonar.dependencycheck.base.DependencyCheckConstants;
import org.sonar.dependencycheck.base.DependencyCheckMetrics;
import org.sonar.dependencycheck.base.DependencyCheckUtils;
import org.sonar.dependencycheck.parser.ReportParser;
import org.sonar.dependencycheck.parser.XmlReportFile;
import org.sonar.dependencycheck.parser.element.Analysis;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Vulnerability;

public class DependencyCheckSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(DependencyCheckSensor.class);
    private static final String SENSOR_NAME = "Dependency-Check";

    private final FileSystem fileSystem;
    private final PathResolver pathResolver;

    private int totalDependencies;
    private int vulnerableDependencies;
    private int vulnerabilityCount;
    private int blockerIssuesCount;
    private int criticalIssuesCount;
    private int majorIssuesCount;
    private int minorIssuesCount;

    public DependencyCheckSensor(FileSystem fileSystem, PathResolver pathResolver) {
        this.fileSystem = fileSystem;
        this.pathResolver = pathResolver;
    }

    private void addIssue(SensorContext context, Dependency dependency, Vulnerability vulnerability) {
        Float severityBlocker = context.config().getFloat(DependencyCheckConstants.SEVERITY_BLOCKER).orElse(DependencyCheckConstants.SEVERITY_BLOCKER_DEFAULT);
        Float severityCritical = context.config().getFloat(DependencyCheckConstants.SEVERITY_CRITICAL).orElse(DependencyCheckConstants.SEVERITY_CRITICAL_DEFAULT);
        Float severityMajor = context.config().getFloat(DependencyCheckConstants.SEVERITY_MAJOR).orElse(DependencyCheckConstants.SEVERITY_MAJOR_DEFAULT);
        Float severityMinor = context.config().getFloat(DependencyCheckConstants.SEVERITY_MINOR).orElse(DependencyCheckConstants.SEVERITY_MINOR_DEFAULT);
        Severity severity = DependencyCheckUtils.cvssToSonarQubeSeverity(vulnerability.getCvssScore(), severityBlocker ,severityCritical, severityMajor, severityMinor);

        context.newIssue()
                .forRule(RuleKey.of(DependencyCheckPlugin.REPOSITORY_KEY, DependencyCheckPlugin.RULE_KEY))
                .at(new DefaultIssueLocation()
                        .on(context.module())
                        .message(formatDescription(dependency, vulnerability))
                )
                .overrideSeverity(severity)
                .save();

        incrementCount(severity);
    }

    /**
     * TODO: Add Markdown formatting if and when Sonar supports it
     * https://jira.sonarsource.com/browse/SONAR-4161
     */
    private String formatDescription(Dependency dependency, Vulnerability vulnerability) {
        StringBuilder sb = new StringBuilder();
        sb.append("Filename: ").append(dependency.getFileName()).append(" | ");
        sb.append("Reference: ").append(vulnerability.getName()).append(" | ");
        sb.append("CVSS Score: ").append(vulnerability.getCvssScore()).append(" | ");
        if (StringUtils.isNotBlank(vulnerability.getCwe())) {
            sb.append("Category: ").append(vulnerability.getCwe()).append(" | ");
        }
        sb.append(vulnerability.getDescription());
        return sb.toString();
    }

    private void incrementCount(Severity severity) {
        switch (severity) {
            case BLOCKER:
                this.blockerIssuesCount++;
                break;
            case CRITICAL:
                this.criticalIssuesCount++;
                break;
            case MAJOR:
                this.majorIssuesCount++;
                break;
            case MINOR:
                this.minorIssuesCount++;
                break;
            default:
                LOGGER.debug("Unknown severity {}", severity);
        }
    }

    private void addIssues(SensorContext context, Analysis analysis) {
        if (analysis.getDependencies() == null) {
            return;
        }
        for (Dependency dependency : analysis.getDependencies()) {
            InputFile testFile = fileSystem.inputFile(
                    fileSystem.predicates().hasPath(
                            escapeReservedPathChars(dependency.getFilePath())
                    )
            );

            int depVulnCount = dependency.getVulnerabilities().size();

            if (depVulnCount > 0) {
                vulnerableDependencies++;
                saveMetricOnFile(context, testFile, DependencyCheckMetrics.VULNERABLE_DEPENDENCIES, depVulnCount);
            }
            saveMetricOnFile(context, testFile, DependencyCheckMetrics.TOTAL_VULNERABILITIES, depVulnCount);
            saveMetricOnFile(context, testFile, DependencyCheckMetrics.TOTAL_DEPENDENCIES, depVulnCount);

            for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                addIssue(context, dependency, vulnerability);
                vulnerabilityCount++;
            }
        }
    }

    private void saveMetricOnFile(SensorContext context, @Nullable InputFile inputFile, Metric<Integer> metric, int value) {
        if (inputFile != null) {
            context.<Integer>newMeasure().on(inputFile).forMetric(metric).withValue(value);
        }
    }

    private Analysis parseAnalysis(SensorContext context) throws IOException, XMLStreamException {
        XmlReportFile report = new XmlReportFile(context.config(), fileSystem, this.pathResolver);

        try (InputStream stream = report.getInputStream(DependencyCheckConstants.REPORT_PATH_PROPERTY)) {
            return new ReportParser().parse(stream);
        }
    }

    private String getHtmlReport(SensorContext context) {
        XmlReportFile report = new XmlReportFile(context.config(), fileSystem, this.pathResolver);
        File reportFile = report.getFile(DependencyCheckConstants.HTML_REPORT_PATH_PROPERTY);
        if (reportFile == null || !reportFile.exists() || !reportFile.isFile() || !reportFile.canRead()) {
            return null;
        }
        int len = (int) reportFile.length();
        String htmlReport = null;
        try (InputStream reportFileInputStream = Files.newInputStream(reportFile.toPath())){
            byte[] readBuffer = new byte[len];
            reportFileInputStream.read(readBuffer, 0, len);
            htmlReport = new String(readBuffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Could not read HTML-Report", e);
        }
        return htmlReport;
    }

    private void saveMeasures(SensorContext context) {
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.CRITICAL_SEVERITY_VULNS).on(context.module()).withValue(blockerIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.HIGH_SEVERITY_VULNS).on(context.module()).withValue(criticalIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.MEDIUM_SEVERITY_VULNS).on(context.module()).withValue(majorIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.LOW_SEVERITY_VULNS).on(context.module()).withValue(minorIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.TOTAL_DEPENDENCIES).on(context.module()).withValue(totalDependencies).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.VULNERABLE_DEPENDENCIES).on(context.module()).withValue(vulnerableDependencies).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.TOTAL_VULNERABILITIES).on(context.module()).withValue(vulnerabilityCount).save();

        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.INHERITED_RISK_SCORE).on(context.module())
            .withValue(DependencyCheckMetrics.inheritedRiskScore(blockerIssuesCount, criticalIssuesCount, majorIssuesCount, minorIssuesCount)).save();
        context.<Double>newMeasure().forMetric(DependencyCheckMetrics.VULNERABLE_COMPONENT_RATIO).on(context.module())
            .withValue(DependencyCheckMetrics.vulnerableComponentRatio(vulnerabilityCount, vulnerableDependencies)).save();

        String htmlReport = getHtmlReport(context);
        if (htmlReport != null) {
            context.<String>newMeasure().forMetric(DependencyCheckMetrics.REPORT).on(context.module()).withValue(htmlReport).save();
        }
    }

    @Override
    public String toString() {
        return SENSOR_NAME;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.name(SENSOR_NAME);
    }

    @Override
    public void execute(SensorContext sensorContext) {
        Profiler profiler = Profiler.create(LOGGER);
        if (sensorContext.config().getBoolean(DependencyCheckConstants.SKIP_PLUGIN).orElse(DependencyCheckConstants.SKIP_PLUGIN_DEFAULT)) {
            LOGGER.debug("Skipping DependencyCheck Plugin");
        } else {
            profiler.startInfo("Process Dependency-Check report");
            try {
                Analysis analysis = parseAnalysis(sensorContext);
                this.totalDependencies = analysis.getDependencies().size();
                addIssues(sensorContext, analysis);
            } catch (FileNotFoundException e) {
                LOGGER.debug("Analysis aborted due to missing report file", e);
            } catch (IOException e) {
                LOGGER.warn("Analysis aborted due to: IO Errors", e);
            } catch (XMLStreamException e) {
                LOGGER.warn("Analysis aborted due to: XML is not valid", e);
            } finally {
                profiler.stopInfo();
            }
            saveMeasures(sensorContext);
        }
    }

    /**
     * The following characters are reserved on Windows systems.
     * Some are also reserved on Unix systems.
     *
     * < (less than)
     * > (greater than)
     * : (colon)
     * " (double quote)
     * / (forward slash)
     * \ (backslash)
     * | (vertical bar or pipe)
     * ? (question mark)
     * (asterisk)
     */
    private String escapeReservedPathChars(String path) {
        /*
         * TODO: For the time being, only try to replace ? (question mark) since that is the only reserved character
         * intentionally used by Dependency-Check.
         */
        String replacement = path.contains("/") ? "/" : "\\";
        return path.replace("?", replacement);
    }
}
