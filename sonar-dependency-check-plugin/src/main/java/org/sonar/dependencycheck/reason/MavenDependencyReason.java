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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.dependencycheck.base.DependencyCheckUtils;
import org.sonar.dependencycheck.parser.PomParserHelper;
import org.sonar.dependencycheck.parser.ReportParserException;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Identifier;
import org.sonar.dependencycheck.reason.maven.MavenDependency;
import org.sonar.dependencycheck.reason.maven.MavenParent;
import org.sonar.dependencycheck.reason.maven.MavenPomModel;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MavenDependencyReason extends DependencyReason {

    private final InputFile pom;
    private final Map<Dependency, TextRangeConfidence> dependencyMap;
    private MavenPomModel pomModel;

    private static final Logger LOGGER = Loggers.get(MavenDependencyReason.class);

    public MavenDependencyReason(@NonNull InputFile pom) {
        super(pom, Language.JAVA);
        this.pom = pom;
        dependencyMap = new HashMap<>();
        pomModel = null;
        try {
            pomModel = PomParserHelper.parse(pom.inputStream());
        } catch (ReportParserException | IOException e) {
            LOGGER.warn("Parsing {} failed", pom);
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    @NonNull
    public TextRangeConfidence getBestTextRange(@NonNull Dependency dependency) {
        if (!dependencyMap.containsKey(dependency)) {
            Optional<Identifier> mavenIdentifier = DependencyCheckUtils.getMavenIdentifier(dependency);
            if (mavenIdentifier.isPresent()) {
                fillArtifactMatch(dependency, mavenIdentifier.get());
            } else {
                LOGGER.debug("No Identifier with type maven found for Dependency {}", dependency.getFileName());
            }
            dependencyMap.computeIfAbsent(dependency, k -> addDependencyToFirstLine(k, pom));
        }
        return dependencyMap.get(dependency);
    }

    /**
     *
     * This Methods fills a map for a dependency TODO: It would be nice to have
     * something similar to the command "mvn dependency:tree" At the moment a simple
     * pom line parser without transitive dependencies
     *
     * @param dependency
     * @param mavenIdentifier
     */
    private void fillArtifactMatch(@NonNull Dependency dependency, Identifier mavenIdentifier) {
        // Try to find in <dependency>
        for (MavenDependency mavenDependency : pomModel.getDependencies()) {
            checkPomDependency(mavenIdentifier, mavenDependency)
                    .ifPresent(textRange -> dependencyMap.put(dependency, textRange));
        }
        // Check Parent if present
        pomModel.getParent().ifPresent(parent -> checkPomParent(mavenIdentifier, parent)
                .ifPresent(textRange -> dependencyMap.put(dependency, textRange)));
    }

    private Optional<TextRangeConfidence> checkPomDependency(Identifier mavenIdentifier, MavenDependency dependency) {
        Optional<String> packageArtifact = Identifier.getPackageArtifact(mavenIdentifier);
        if (packageArtifact.isPresent()) {
            // packageArtifact has something like struts/struts@1.2.8
            String[] mavenIdentifierSplit = packageArtifact.get().split("@");
            mavenIdentifierSplit = mavenIdentifierSplit[0].split("/");
            String groupId = mavenIdentifierSplit[0];
            String artifactId = mavenIdentifierSplit[1];
            if (StringUtils.equals(artifactId, dependency.getArtifactId())
                    && StringUtils.equals(groupId, dependency.getGroupId())) {
                LOGGER.debug("Found a artifactId and groupId match in {}", pom);
                return Optional.of(new TextRangeConfidence(pom.newRange(pom.selectLine(dependency.getStartLineNr()).start(), pom.selectLine(dependency.getEndLineNr()).end()), Confidence.HIGHEST));
            }
            if (StringUtils.equals(artifactId, dependency.getArtifactId())) {
                LOGGER.debug("Found a artifactId match in {} for {}", pom, artifactId);
                return Optional.of(new TextRangeConfidence(pom.newRange(pom.selectLine(dependency.getStartLineNr()).start(), pom.selectLine(dependency.getEndLineNr()).end()), Confidence.HIGH));
            }
            if (StringUtils.equals(groupId, dependency.getGroupId())) {
                LOGGER.debug("Found a groupId match in {} for {}", pom, groupId);
                return Optional.of(new TextRangeConfidence(pom.newRange(pom.selectLine(dependency.getStartLineNr()).start(), pom.selectLine(dependency.getEndLineNr()).end()), Confidence.MEDIUM));
            }
        }
        return Optional.empty();
    }

    private Optional<TextRangeConfidence> checkPomParent(Identifier mavenIdentifier, MavenParent parent) {
        Optional<String> packageArtifact = Identifier.getPackageArtifact(mavenIdentifier);
        if (packageArtifact.isPresent()) {
            // packageArtifact has something like struts/struts@1.2.8
            String[] mavenIdentifierSplit = packageArtifact.get().split("@");
            mavenIdentifierSplit = mavenIdentifierSplit[0].split("/");
            String groupId = mavenIdentifierSplit[0];
            if (StringUtils.equals(groupId, parent.getGroupId())) {
                LOGGER.debug("Found a groupId match in {} for {}", pom, groupId);
                return Optional.of(new TextRangeConfidence(pom.newRange(pom.selectLine(parent.getStartLineNr()).start(), pom.selectLine(parent.getEndLineNr()).end()), Confidence.MEDIUM));
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if we have a pom File and this pom file is readable and has content
     * Pom Files contains the in maven builds the dependencies
     */
    @Override
    public boolean isReasonable() {
        return pom != null && pomModel != null;
    }

    /**
     * returns pom file
     */
    @NonNull
    @Override
    public InputComponent getInputComponent() {
        return pom;
    }
}
