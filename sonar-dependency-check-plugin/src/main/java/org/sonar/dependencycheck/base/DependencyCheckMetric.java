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
package org.sonar.dependencycheck.base;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DependencyCheckMetric {

    private static final Logger LOGGER = Loggers.get(DependencyCheckMetric.class);
    private final InputComponent inputcomponent;

    private int totalDependencies;
    private int vulnerableDependencies;
    private int vulnerabilityCount;
    private int blockerIssuesCount;
    private int criticalIssuesCount;
    private int majorIssuesCount;
    private int minorIssuesCount;
    private int infoIssuesCount;

    public DependencyCheckMetric(@NonNull InputComponent inputComponent) {
        this.inputcomponent = inputComponent;
        this.totalDependencies = 0;
        this.vulnerableDependencies = 0;
        this.vulnerabilityCount = 0;
        this.blockerIssuesCount = 0;
        this.criticalIssuesCount = 0;
        this.majorIssuesCount = 0;
        this.minorIssuesCount = 0;
        this.infoIssuesCount = 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DependencyCheckMetric [inputcomponent=" + inputcomponent + ", totalDependencies=" + totalDependencies
                + ", vulnerableDependencies=" + vulnerableDependencies + ", vulnerabilityCount=" + vulnerabilityCount
                + ", blockerIssuesCount=" + blockerIssuesCount + ", criticalIssuesCount=" + criticalIssuesCount
                + ", majorIssuesCount=" + majorIssuesCount + ", minorIssuesCount=" + minorIssuesCount
                + ", infoIssuesCount=" + infoIssuesCount + "]";
    }

    public void incrementCount(Severity severity) {
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
            case INFO:
                this.infoIssuesCount++;
                break;
            default:
                LOGGER.debug("Unknown severity {}", severity);
                break;
        }
    }

    public void increaseVulnerabilityCount(int amount) {
        this.vulnerabilityCount += amount;
    }

    public void increaseVulnerableDependencies(int amount) {
        this.vulnerableDependencies += amount;
    }

    public void increaseTotalDependencies(int amount) {
        this.totalDependencies += amount;
    }

    public void saveMeasures(SensorContext context) {
        LOGGER.debug("Save measures on {}", inputcomponent);
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.CRITICAL_SEVERITY_VULNS).on(inputcomponent).withValue(blockerIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.HIGH_SEVERITY_VULNS).on(inputcomponent).withValue(criticalIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.MEDIUM_SEVERITY_VULNS).on(inputcomponent).withValue(majorIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.LOW_SEVERITY_VULNS).on(inputcomponent).withValue(minorIssuesCount).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.TOTAL_DEPENDENCIES).on(inputcomponent).withValue(totalDependencies).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.VULNERABLE_DEPENDENCIES).on(inputcomponent).withValue(vulnerableDependencies).save();
        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.TOTAL_VULNERABILITIES).on(inputcomponent).withValue(vulnerabilityCount).save();

        context.<Integer>newMeasure().forMetric(DependencyCheckMetrics.INHERITED_RISK_SCORE).on(inputcomponent)
            .withValue(DependencyCheckMetrics.inheritedRiskScore(blockerIssuesCount, criticalIssuesCount, majorIssuesCount, minorIssuesCount)).save();
        context.<Double>newMeasure().forMetric(DependencyCheckMetrics.VULNERABLE_COMPONENT_RATIO).on(inputcomponent)
            .withValue(DependencyCheckMetrics.vulnerableComponentRatio(vulnerabilityCount, vulnerableDependencies)).save();
    }
}
