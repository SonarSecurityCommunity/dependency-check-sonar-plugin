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

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.api.scanner.ScannerSide;

@ScannerSide
public final class DependencyCheckMetrics implements Metrics {

    private static final String DOMAIN = "OWASP-Dependency-Check";

    private static final String INHERITED_RISK_SCORE_KEY = "inherited_risk_score";
    private static final String VULNERABLE_COMPONENT_RATIO_KEY = "vulnerable_component_ratio";

    private static final String TOTAL_DEPENDENCIES_KEY = "total_dependencies";
    private static final String VULNERABLE_DEPENDENCIES_KEY = "vulnerable_dependencies";
    private static final String TOTAL_VULNERABILITIES_KEY = "total_vulnerabilities";
    private static final String CRITICAL_SEVERITY_VULNS_KEY = "critical_severity_vulns";
    private static final String HIGH_SEVERITY_VULNS_KEY = "high_severity_vulns";
    private static final String MEDIUM_SEVERITY_VULNS_KEY = "medium_severity_vulns";
    private static final String LOW_SEVERITY_VULNS_KEY = "low_severity_vulns";

    private static final String REPORT_KEY = "report";

    public static final Metric<Integer> INHERITED_RISK_SCORE = new Metric.Builder(DependencyCheckMetrics.INHERITED_RISK_SCORE_KEY, "Inherited Risk Score", Metric.ValueType.INT)
            .setDescription("Inherited Risk Score")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.TRUE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .create();

    public static final Metric<Double> VULNERABLE_COMPONENT_RATIO = new Metric.Builder(DependencyCheckMetrics.VULNERABLE_COMPONENT_RATIO_KEY, "Vulnerable Component Ratio", Metric.ValueType.PERCENT)
            .setDescription("Vulnerable Component Ratio")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.TRUE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .create();

    public static final Metric<Integer> CRITICAL_SEVERITY_VULNS = new Metric.Builder(CRITICAL_SEVERITY_VULNS_KEY, "Critical Severity Vulnerabilities", Metric.ValueType.INT)
            .setDescription("Critical Severity Vulnerabilities")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .setHidden(false)
            .create();

    public static final Metric<Integer> HIGH_SEVERITY_VULNS = new Metric.Builder(HIGH_SEVERITY_VULNS_KEY, "High Severity Vulnerabilities", Metric.ValueType.INT)
            .setDescription("High Severity Vulnerabilities")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .setHidden(false)
            .create();

    public static final Metric<Integer> MEDIUM_SEVERITY_VULNS = new Metric.Builder(MEDIUM_SEVERITY_VULNS_KEY, "Medium Severity Vulnerabilities", Metric.ValueType.INT)
            .setDescription("Medium Severity Vulnerabilities")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .setHidden(false)
            .create();

    public static final Metric<Integer> LOW_SEVERITY_VULNS = new Metric.Builder(LOW_SEVERITY_VULNS_KEY, "Low Severity Vulnerabilities", Metric.ValueType.INT)
            .setDescription("Low Severity Vulnerabilities")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .setHidden(false)
            .create();

    public static final Metric<Integer> TOTAL_DEPENDENCIES = new Metric.Builder(TOTAL_DEPENDENCIES_KEY, "Total Dependencies", Metric.ValueType.INT)
            .setDescription("Total Dependencies")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setHidden(false)
            .create();

    public static final Metric<Integer> VULNERABLE_DEPENDENCIES = new Metric.Builder(VULNERABLE_DEPENDENCIES_KEY, "Vulnerable Dependencies", Metric.ValueType.INT)
            .setDescription("Vulnerable Dependencies")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .setHidden(false)
            .create();

    public static final Metric<Integer> TOTAL_VULNERABILITIES = new Metric.Builder(TOTAL_VULNERABILITIES_KEY, "Total Vulnerabilities", Metric.ValueType.INT)
            .setDescription("Total Vulnerabilities")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setBestValue(0.0)
            .setHidden(false)
            .create();

    public static final Metric<String> REPORT = new Metric.Builder(REPORT_KEY, "Dependency-Check Report", Metric.ValueType.DATA)
            .setDescription("Report HTML")
            .setQualitative(Boolean.FALSE)
            .setDomain(DependencyCheckMetrics.DOMAIN)
            .setHidden(false)
            .setDeleteHistoricalData(true)
            .create();

    public static double vulnerableComponentRatio(int vulnerabilities, int vulnerableComponents) {
        double ratio = 0.0;
        if(vulnerableComponents > 0) {
            ratio = (double) vulnerabilities / vulnerableComponents;
        }
        return ratio;
    }

    public static int inheritedRiskScore(int blocker, int high, int medium, int low) {
        return (blocker * 7) + (high * 5) + (medium * 3) + (low);
    }


    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(
                DependencyCheckMetrics.INHERITED_RISK_SCORE,
                DependencyCheckMetrics.VULNERABLE_COMPONENT_RATIO,
                DependencyCheckMetrics.CRITICAL_SEVERITY_VULNS,
                DependencyCheckMetrics.HIGH_SEVERITY_VULNS,
                DependencyCheckMetrics.MEDIUM_SEVERITY_VULNS,
                DependencyCheckMetrics.LOW_SEVERITY_VULNS,
                DependencyCheckMetrics.TOTAL_DEPENDENCIES,
                DependencyCheckMetrics.VULNERABLE_DEPENDENCIES,
                DependencyCheckMetrics.TOTAL_VULNERABILITIES,
                DependencyCheckMetrics.REPORT
        );
    }
}