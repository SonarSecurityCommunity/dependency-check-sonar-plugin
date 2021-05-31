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
package org.sonar.dependencycheck.reason;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.dependencycheck.base.DependencyCheckConstants;
import org.sonar.dependencycheck.base.DependencyCheckMetric;
import org.sonar.dependencycheck.base.DependencyCheckUtils;
import org.sonar.dependencycheck.parser.element.Analysis;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Vulnerability;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DependencyReasonSearcher {

    private static final Logger LOGGER = Loggers.get(DependencyReasonSearcher.class);
    private final DependencyCheckMetric projectMetric;

    private final Collection<DependencyReason> dependencyReasons;

    public DependencyReasonSearcher(SensorContext context) {
        this.projectMetric = new DependencyCheckMetric(context.project());
        this.dependencyReasons = new LinkedList<>();
        Iterable<InputFile> poms = context.fileSystem().inputFiles(context.fileSystem().predicates().matchesPathPattern("**/pom.xml"));
        for (InputFile pom : poms) {
            DependencyReason pomReason = new MavenDependencyReason(pom);
            if (pomReason.isReasonable()) {
                dependencyReasons.add(pomReason);
                LOGGER.debug("Found reasonable pom.xml file {}", pom);
            } else {
                LOGGER.debug("Found unreasonable pom.xml file {}", pom);
            }
        }
        String[] gradlePathPatterns = {"**/*.gradle", "**/*.gradle.kts"};
        Iterable<InputFile> buildGradles = context.fileSystem().inputFiles(context.fileSystem().predicates().matchesPathPatterns(gradlePathPatterns));
        for (InputFile buildGradle : buildGradles) {
            DependencyReason gradleReason = new GradleDependencyReason(buildGradle);
            if (gradleReason.isReasonable()) {
                dependencyReasons.add(gradleReason);
                LOGGER.debug("Found reasonable gradle file {}", buildGradle);
            } else {
                LOGGER.debug("Found unreasonable gradle file {}", buildGradle);
            }
        }
        String[] npmPathPatterns = {"**/package-lock.json"};
        Iterable<InputFile> packageLocks = context.fileSystem().inputFiles(context.fileSystem().predicates().matchesPathPatterns(npmPathPatterns));
        for (InputFile packageLock : packageLocks) {
            DependencyReason npmReason = new NPMDependencyReason(packageLock);
            if (npmReason.isReasonable()) {
                dependencyReasons.add(npmReason);
                LOGGER.debug("Found reasonable npm file {}", npmReason);
            } else {
                LOGGER.debug("Found unreasonable npm file {}", npmReason);
            }
        }
    }

    /**
     * @return the all dependencyReasons
     */
    public Collection<DependencyReason> getDependencyReasons() {
        return dependencyReasons;
    }

    /**
     *
     * @param dependency - Dependency to go on Search
     * @return DependencyReason with a TextRange or the first DependencyReason in list or null if list is empty
     */
    @NonNull
    private Optional<DependencyReason> getBestDependencyReason(Dependency dependency) {
        LOGGER.debug("Get the best DependencyReason out of {} for {}", dependencyReasons.size(), dependency.getFileName());
        // Prefer root configuration file
        Optional<DependencyReason> dependencyReasonWinner = DependencyCheckUtils.getBestDependencyReason(dependency, dependencyReasons);
        dependencyReasonWinner.ifPresent(dependencyReason -> LOGGER.debug("DependencyReasonWinner: " + dependencyReason.getInputComponent()));
        return dependencyReasonWinner;
    }

    public void addDependenciesToInputComponents(@NonNull Analysis analysis,@NonNull SensorContext context) {
        if (analysis.getDependencies() == null || analysis.getDependencies().isEmpty()) {
            LOGGER.info("Analyse doesn't report any Dependencies");
            return;
        }
        if (dependencyReasons.isEmpty()) {
            LOGGER.info("No project configuration file, e.g. pom.xml, *.gradle, *.gradle.kts, package-lock.json found, therefore it isn't possible to correctly link dependencies with files.");
            linkIssues(analysis.getDependencies(), context);
            LOGGER.debug("Saving Metrics to project {}", projectMetric.toString());
            projectMetric.saveMeasures(context);
        } else {
            linkIssues(analysis.getDependencies(), context);
            for (DependencyReason reason : dependencyReasons) {
                LOGGER.debug("Saving Metrics to reason file {} ", reason.getMetrics().toString());
                reason.getMetrics().saveMeasures(context);
            }
        }
    }

    private void linkIssues(@NonNull Collection<Dependency> dependencies, @NonNull SensorContext context) {
        LOGGER.info("Linking {} dependencies", dependencies.size());
        for (Dependency dependency : dependencies) {
            Optional<DependencyReason> dependencyReason = getBestDependencyReason(dependency);
            if (dependencyReason.isPresent()) {
                linkDependencyToDependencyReasons(dependency, dependencyReason.get(), context);
            } else {
                linkDependencyToProject(dependency, context);
            }
        }
    }

    private void linkDependencyToProject(@NonNull Dependency dependency, @NonNull SensorContext context) {
        projectMetric.increaseTotalDependencies(1);
        if (!dependency.getVulnerabilities().isEmpty()) {
            projectMetric.increaseVulnerabilityCount(dependency.getVulnerabilities().size());
            projectMetric.increaseVulnerableDependencies(1);
            if (DependencyCheckUtils.summarizeVulnerabilities(context.config())) {
                // One issue per dependency
                addIssueToProject(context, dependency);
            } else {
                // One issue per vulnerability
                for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                    addIssueToProject(context, dependency, vulnerability);
                }
            }
        }
    }

    private void linkDependencyToDependencyReasons(@NonNull Dependency dependency, DependencyReason dependencyReason,
            @NonNull SensorContext context) {
        dependencyReason.getMetrics().increaseTotalDependencies(1);
        if (!dependency.getVulnerabilities().isEmpty()) {
            dependencyReason.getMetrics().increaseVulnerabilityCount(dependency.getVulnerabilities().size());
            dependencyReason.getMetrics().increaseVulnerableDependencies(1);
            if (DependencyCheckUtils.summarizeVulnerabilities(context.config())) {
                // One issue per dependency
                dependencyReason.addIssue(context, dependency);
            } else {
                // One issue per vulnerability
                for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                    dependencyReason.addIssue(context, dependency, vulnerability);
                }
            }
        }
    }

    public void addIssueToProject(SensorContext context, Dependency dependency, Vulnerability vulnerability) {
        Severity severity = DependencyCheckUtils.cvssToSonarQubeSeverity(vulnerability.getCvssScore(context.config()), context.config());
        NewIssue sonarIssue = context.newIssue();

        NewIssueLocation location = sonarIssue.newLocation()
            .on(context.project())
            .message(DependencyCheckUtils.formatDescription(dependency, vulnerability, context.config()));

        sonarIssue
            .at(location)
            .forRule(RuleKey.of(DependencyCheckConstants.REPOSITORY_KEY, DependencyCheckUtils.getRuleKey(context.config())))
            .overrideSeverity(severity)
            .save();
        projectMetric.incrementCount(severity);
    }

    private void addIssueToProject(SensorContext context, Dependency dependency) {
        dependency.sortVulnerabilityBycvssScore(context.config());
        List<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
        Vulnerability highestVulnerability = vulnerabilities.get(0);
        Severity severity = DependencyCheckUtils.cvssToSonarQubeSeverity(highestVulnerability.getCvssScore(context.config()), context.config());

        NewIssue sonarIssue = context.newIssue();

        NewIssueLocation location = sonarIssue.newLocation()
            .on(context.project())
            .message(DependencyCheckUtils.formatDescription(dependency, vulnerabilities, highestVulnerability, context.config()));

        sonarIssue
            .at(location)
            .forRule(RuleKey.of(DependencyCheckConstants.REPOSITORY_KEY, DependencyCheckUtils.getRuleKey(context.config())))
            .overrideSeverity(severity)
            .save();
        projectMetric.incrementCount(severity);
    }
}
