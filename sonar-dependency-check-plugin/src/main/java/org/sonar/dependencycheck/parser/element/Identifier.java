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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@JsonIgnoreProperties({"url", "description","notes"})
public class Identifier {
    private final String id;
    private final Confidence confidence;

    @JsonCreator
    public Identifier(@JsonProperty(value = "id", required = true) @NonNull String id,
                      @JsonProperty(value = "confidence") @Nullable Confidence confidence) {
        this.id = id;
        this.confidence = confidence;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the confidence
     */
    public Optional<Confidence> getConfidence() {
        return Optional.ofNullable(confidence);
    }
    public static Optional<String> getPackageType(@NonNull Identifier identifier) {
        if (StringUtils.isNotBlank(identifier.getId())) {
            // pkg:maven/struts/struts@1.2.8 -> maven
            // pkg:javascript/jquery@2.2.0 -> javascript
            // pkg:npm/arr-flatten@1.1.0 -> npm
            // pkg:npm/mime -> npm
            return Optional.of(StringUtils.substringAfter(StringUtils.substringBefore(identifier.getId(), "/"), "pkg:"));
        }
        return Optional.empty();
    }
    public static Optional<String> getPackageArtifact(@NonNull Identifier identifier) {
        if (StringUtils.isNotBlank(identifier.getId())) {
            // pkg:maven/struts/struts@1.2.8 -> struts/struts@1.2.8
            // pkg:javascript/jquery@2.2.0 -> jquery@2.2.0
            // pkg:npm/arr-flatten@1.1.0 -> arr-flatten@1.1.0
            // pkg:npm/mime -> mime
            return Optional.of(StringUtils.substringAfter(identifier.getId(), "/"));
        }
        return Optional.empty();
    }

    public static boolean isMavenPackage(@NonNull Identifier identifier) {
        return "maven".equals(Identifier.getPackageType(identifier).orElse(""));
    }

    public static boolean isNPMPackage(@NonNull Identifier identifier) {
        return "npm".equals(Identifier.getPackageType(identifier).orElse(""));
    }

    public static boolean isJavaScriptPackage(@NonNull Identifier identifier) {
        return "javascript".equals(Identifier.getPackageType(identifier).orElse(""));
    }
}
