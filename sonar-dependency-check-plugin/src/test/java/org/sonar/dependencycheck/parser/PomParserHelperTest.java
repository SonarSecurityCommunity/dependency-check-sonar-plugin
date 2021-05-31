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
package org.sonar.dependencycheck.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.sonar.dependencycheck.reason.maven.MavenDependency;
import org.sonar.dependencycheck.reason.maven.MavenPomModel;

class PomParserHelperTest {

    @Test
    void parsePom() throws Exception {
        InputStream pom = getClass().getClassLoader().getResourceAsStream("reason/pom.xml");
        MavenPomModel pomModel = PomParserHelper.parse(pom);
        assertNotNull(pomModel);
        // check some dependencies
        checkMavenDependency(pomModel, "struts", "struts", 46, 50);
        checkMavenDependency(pomModel, "javax.mail", "com.sun.mail", 51, 55);
        checkMavenDependency(pomModel, "spring", "org.springframework", 56, 60);
        checkMavenDependency(pomModel, "commons-io", "commons-io", 61, 65);
        // check parent
        checkMavenParent(pomModel, "dummy-parent", 18, 21);
    }

    private void checkMavenDependency(MavenPomModel pomModel, String artifactId, String groupId, int startLineNr, int endLineNr) {
        boolean found = false;
        for (MavenDependency mavenDependency : pomModel.getDependencies()) {
            if (artifactId.equals(mavenDependency.getArtifactId())) {
                found = true;
                assertEquals(groupId, mavenDependency.getGroupId());
                assertEquals(startLineNr, mavenDependency.getStartLineNr());
                assertEquals(endLineNr, mavenDependency.getEndLineNr());
            }
        }
        assertTrue(found, "We haven't found dependency " + artifactId);
    }

    private void checkMavenParent(MavenPomModel pomModel, String groupId, int startLineNr, int endLineNr) {
        assertEquals(groupId, pomModel.getParent().get().getGroupId());
        assertEquals(startLineNr, pomModel.getParent().get().getStartLineNr());
        assertEquals(endLineNr, pomModel.getParent().get().getEndLineNr());
    }


    @Test
    void parsePomIOException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> PomParserHelper.parse(inputStream), "No IOException thrown");
        assertEquals("Could not parse pom.xml", exception.getMessage());
    }

}
