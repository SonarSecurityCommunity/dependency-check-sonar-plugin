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
import org.sonar.dependencycheck.parser.PackageLockParserHelper;
import org.sonar.dependencycheck.parser.ReportParserException;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Identifier;
import org.sonar.dependencycheck.reason.npm.NPMDependency;
import org.sonar.dependencycheck.reason.npm.PackageLockModel;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class NPMDependencyReason extends DependencyReason {

    private final InputFile packageLock;
    private PackageLockModel packageLockModel;
    private final Map<Dependency, TextRangeConfidence> dependencyMap;

    private static final Logger LOGGER = Loggers.get(NPMDependencyReason.class);

    public NPMDependencyReason(InputFile packageLock) {
        super(packageLock, Language.JAVASCRIPT);
        this.packageLock = packageLock;
        dependencyMap = new HashMap<>();
        packageLockModel = null;
        try {
            packageLockModel = PackageLockParserHelper.parse(packageLock.inputStream());
        } catch (ReportParserException | IOException e) {
            LOGGER.warn("Parsing {} failed", packageLock);
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public boolean isReasonable() {
        return packageLock != null && packageLockModel != null;
    }

    @NonNull
    @Override
    public InputComponent getInputComponent() {
        return packageLock;
    }

    @NonNull
    @Override
    public TextRangeConfidence getBestTextRange(Dependency dependency) {
        if (!dependencyMap.containsKey(dependency)) {
            Optional<Identifier> javaScriptIdentifier = DependencyCheckUtils.getJavaScriptIdentifier(dependency);
            if (javaScriptIdentifier.isPresent()) {
                fillArtifactMatch(dependency, javaScriptIdentifier.get());
            } else {
                LOGGER.debug("No Identifier with type javascript found for Dependency {}", dependency.getFileName());
            }
            Optional<Identifier> npmIdentifier = DependencyCheckUtils.getNPMIdentifier(dependency);
            if (npmIdentifier.isPresent()) {
                fillArtifactMatch(dependency, npmIdentifier.get());
            } else {
                LOGGER.debug("No Identifier with type npm found for Dependency {}", dependency.getFileName());
            }
            dependencyMap.computeIfAbsent(dependency, k -> addDependencyToFirstLine(k, packageLock));
        }
        return dependencyMap.get(dependency);
    }

    private void fillArtifactMatch(@NonNull Dependency dependency, Identifier npmIdentifier) {
        String packageArtifact = Identifier.getPackageArtifact(npmIdentifier).orElse(null);
        if (StringUtils.isNotBlank(packageArtifact)) {
            String name;
            String version;
            if (packageArtifact.contains("@")) {
                // packageArtifact is something like jquery@2.2.0
                String[] npmIdentifierSplit = packageArtifact.split("@");
                name = npmIdentifierSplit[0];
                version = npmIdentifierSplit[1];
            } else {
                // It happens, that packageArtifact doesn't contain a version
                // https://github.com/dependency-check/dependency-check-sonar-plugin/issues/242#issuecomment-605521827
                name = packageArtifact;
                version = null;
            }

            // Try to find in <dependency>
            for (NPMDependency npmDependency : packageLockModel.getDependencies()) {
                checkNPMDependency(name, version , npmDependency)
                        .ifPresent(textrange -> dependencyMap.put(dependency, textrange));
            }
        }
    }

    private Optional<TextRangeConfidence> checkNPMDependency(String name, @Nullable String version, NPMDependency dependency) {
        if (StringUtils.equals(name, dependency.getName())
                && StringUtils.equals(version, dependency.getVersion())) {
            LOGGER.debug("Found a name and version match in {}", packageLock);
            return Optional.of(new TextRangeConfidence(packageLock.newRange(packageLock.selectLine(dependency.getStartLineNr()).start(), packageLock.selectLine(dependency.getEndLineNr()).end()), Confidence.HIGHEST));
        }
        if (StringUtils.equals(name, dependency.getName())) {
            LOGGER.debug("Found a name match in {} for {}", packageLock, name);
            return Optional.of(new TextRangeConfidence(packageLock.newRange(packageLock.selectLine(dependency.getStartLineNr()).start(), packageLock.selectLine(dependency.getEndLineNr()).end()), Confidence.HIGH));
        }
        return Optional.empty();
    }

}
