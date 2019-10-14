/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2019 dependency-check
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sonar.api.config.Configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.NonNull;

@JsonIgnoreProperties({"isVirtual", "sha256", "description", "projectReferences", "license"})
public class Dependency {

    private final String fileName;
    private final String filePath;
    private final String md5;
    private final String sha1;
    private final Map<String, List<Evidence>> evidenceCollected;
    private final List<Vulnerability> vulnerabilities;
    private final Collection<Identifier> packages;
    private final Collection<Identifier> vulnerabilityIds;

    @JsonCreator
    public Dependency(@JsonProperty("fileName") @NonNull String fileName,
                      @JsonProperty("filePath") @NonNull String filePath,
                      @JsonProperty("md5") @NonNull String md5Hash,
                      @JsonProperty("sha1") @NonNull String sha1Hash,
                      @JsonProperty("evidenceCollected") Map<String, List<Evidence>> evidenceCollected,
                      @JsonProperty("vulnerabilities") List<Vulnerability> vulnerabilities,
                      @JsonProperty("packages") Collection<Identifier> packages,
                      @JsonProperty("vulnerabilityIds") Collection<Identifier> vulnerabilityIds) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.md5 = md5Hash;
        this.sha1 = sha1Hash;
        this.evidenceCollected = evidenceCollected;
        this.vulnerabilities = vulnerabilities;
        this.packages = packages;
        this.vulnerabilityIds = vulnerabilityIds;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMd5Hash() {
        return md5;
    }

    public String getSha1Hash() {
        return sha1;
    }

    public Map<String, List<Evidence>> getEvidenceCollected() {
        return evidenceCollected;
    }

    public List<Vulnerability> getVulnerabilities() {
        return Optional.ofNullable(vulnerabilities).orElse(Collections.emptyList());
    }

    public void sortVulnerabilityBycvssScore(Configuration config) {
        final Comparator<Vulnerability> comp = (vul1, vul2) -> Float.compare( vul1.getCvssScore(config), vul2.getCvssScore(config));
        Collections.sort(this.vulnerabilities, comp.reversed());
    }

    public Collection<Identifier> getPackages() {
        return packages;
    }

    public Collection<Identifier> getVulnerabilityIds() {
        return vulnerabilityIds;
    }

}
