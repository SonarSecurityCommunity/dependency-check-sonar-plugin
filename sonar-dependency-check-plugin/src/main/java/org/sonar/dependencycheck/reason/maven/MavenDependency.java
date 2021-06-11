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

package org.sonar.dependencycheck.reason.maven;

public class MavenDependency {

    private final String groupId;
    private final String artifactId;
    private final int startLineNr;
    private final int endLineNr;

    /**
     * @param groupId
     * @param artifactId
     * @param startLineNr
     * @param endLineNr
     */
    public MavenDependency(String groupId, String artifactId, int startLineNr, int endLineNr) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.startLineNr = startLineNr;
        this.endLineNr = endLineNr;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return the startLineNr
     */
    public int getStartLineNr() {
        return startLineNr;
    }

    /**
     * @return the endLineNr
     */
    public int getEndLineNr() {
        return endLineNr;
    }
}
