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

@JsonIgnoreProperties({"accessVector",
                       "accessComplexity",
                       "authenticationr",
                       "confidentialImpact",
                       "integrityImpact",
                       "availabilityImpact",
                       "version",
                       "exploitabilityScore",
                       "impactScore",
                       "acInsufInfo",
                       "obtainAllPrivilege",
                       "obtainUserPrivilege",
                       "obtainOtherPrivilege",
                       "userInteractionRequired"})
public class CvssV2 implements Cvss {
    private final Float score;
    private final String severity;
    @JsonCreator
    public CvssV2(@JsonProperty(value = "score", required = true) @NonNull Float score,
                  @JsonProperty(value = "severity", required = true) @NonNull String severity) {
        this.score = score;
        this.severity = severity;
    }

    /**
     * @return the score
     */
    @Override
    @NonNull
    public Float getScore() {
        return score;
    }

    /**
     * @return the severity
     */
    @Override
    @NonNull
    public String getSeverity() {
        return severity;
    }
}
