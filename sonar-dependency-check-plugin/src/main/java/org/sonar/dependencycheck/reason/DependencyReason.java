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

import java.util.List;

import org.sonar.api.batch.fs.InputComponent;
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
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Vulnerability;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class DependencyReason {

    private static final Logger LOGGER = Loggers.get(DependencyReason.class);

    private final DependencyCheckMetric metrics;
    private final Language language;

    protected DependencyReason(InputComponent inputComponent, Language language) {
        metrics = new DependencyCheckMetric(inputComponent);
        this.language = language;
    }
    /**
     * @return true or false if this component is the reason for your dependency
     */
    public abstract boolean isReasonable();

    /**
     * @return InputComponent with includes the dependency
     */
    @NonNull
    public abstract InputComponent getInputComponent();

    /**
     * @return the metrics
     */
    public DependencyCheckMetric getMetrics() {
        return metrics;
    }

    /**
     * @return language
     */
    public Language getLanguage() {
        return language;
    }

    protected static TextRangeConfidence addDependencyToFirstLine(Dependency dependency, InputFile inputFile) {
        LOGGER.debug("We haven't found a TextRange for {} in {}. We link to first line with {} confidence", dependency.getFileName(), inputFile, Confidence.LOW);
        return new TextRangeConfidence(inputFile.selectLine(1), Confidence.LOW);
    }
    /**
     * Returns for a dependency the a TextRange, where the import is happen
     *
     * @param dependency The dependency to check
     * @return TextRange
     */
    @NonNull
    public abstract TextRangeConfidence getBestTextRange(Dependency dependency);

    public void addIssue(SensorContext context, Dependency dependency) {
        dependency.sortVulnerabilityBycvssScore(context.config());
        List<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
        Vulnerability highestVulnerability = vulnerabilities.get(0);
        Severity severity = DependencyCheckUtils.cvssToSonarQubeSeverity(highestVulnerability.getCvssScore(context.config()), context.config());

        TextRangeConfidence textRange = getBestTextRange(dependency);
        InputComponent inputComponent = getInputComponent();

        NewIssue sonarIssue = context.newIssue();

        NewIssueLocation location = sonarIssue.newLocation()
            .on(inputComponent)
            .at(textRange.getTextRange())
            .message(DependencyCheckUtils.formatDescription(dependency, vulnerabilities, highestVulnerability, context.config()));

        sonarIssue
            .at(location)
            .forRule(RuleKey.of(DependencyCheckConstants.REPOSITORY_KEY, DependencyCheckUtils.getRuleKey(context.config())))
            .overrideSeverity(severity)
            .save();
        metrics.incrementCount(severity);
    }

    public void addIssue(SensorContext context, Dependency dependency, Vulnerability vulnerability) {
        Severity severity = DependencyCheckUtils.cvssToSonarQubeSeverity(vulnerability.getCvssScore(context.config()), context.config());

        TextRangeConfidence textRange = getBestTextRange(dependency);
        InputComponent inputComponent = getInputComponent();

        NewIssue sonarIssue = context.newIssue();

        NewIssueLocation location = sonarIssue.newLocation()
            .on(inputComponent)
            .at(textRange.getTextRange())
            .message(DependencyCheckUtils.formatDescription(dependency, vulnerability, context.config()));

        sonarIssue
            .at(location)
            .forRule(RuleKey.of(DependencyCheckConstants.REPOSITORY_KEY, DependencyCheckUtils.getRuleKey(context.config())))
            .overrideSeverity(severity)
            .save();
        metrics.incrementCount(severity);
    }
}
