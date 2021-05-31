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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.NonNull;

@JsonIgnoreProperties({"attackVector",
                       "attackComplexity",
                       "privilegesRequired",
                       "userInteraction",
                       "scope",
                       "confidentialityImpact",
                       "integrityImpact",
                       "availabilityImpact",
                       "exploitabilityScore",
                       "impactScore",
                       "version"})
public class CvssV3 implements Cvss{
    private final Float score;
    private final String severity;

    /**
     * @param score
     * @param severity
     */
    @JsonCreator
    public CvssV3(@JsonProperty(value = "baseScore", required = true) @NonNull Float score,
                  @JsonProperty(value = "baseSeverity", required = true) @NonNull String severity) {
        this.score = score;
        this.severity = severity;
    }

    @NonNull
    @Override
    public Float getScore() {
        return score;
    }
    @NonNull
    @Override
    public String getSeverity() {
        return severity;
    }
}
