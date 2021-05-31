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
import org.sonar.dependencycheck.reason.npm.NPMDependency;
import org.sonar.dependencycheck.reason.npm.PackageLockModel;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

class NPMParserHelperTest {

    @Test
    void parsePackageLock() throws Exception {
        InputStream packageLock = getClass().getClassLoader().getResourceAsStream("reason/package-lock.json");
        PackageLockModel packageLockModel = PackageLockParserHelper.parse(packageLock);
        assertNotNull(packageLockModel);
        // check some dependencies
        checkNPMDependency(packageLockModel, "arr-flatten", "1.1.0", 7, 11);
        checkNPMDependency(packageLockModel, "arr-union", "3.1.0", 12, 16);
        checkNPMDependency(packageLockModel, "base", "0.11.2", 32, 45);
        checkNPMDependency(packageLockModel, "is-number", "3.0.0", 299, 316);
        checkNPMDependency(packageLockModel, "yallist", "3.1.1", 927, 931);
    }

    private void checkNPMDependency(PackageLockModel packageLockModel, String name, String version, int startLineNr, int endLineNr) {
        boolean found = false;
        for (NPMDependency npmDependency : packageLockModel.getDependencies()) {
            if (name.equals(npmDependency.getName())) {
                found = true;
                assertEquals(version, npmDependency.getVersion());
                assertEquals(startLineNr, npmDependency.getStartLineNr());
                assertEquals(endLineNr, npmDependency.getEndLineNr());
            }
        }
        assertTrue(found, "We haven't found dependency " + name);
    }

    @Test
    void parseReportJsonParseException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(JsonParseException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> PackageLockParserHelper.parse(inputStream), "No JsonParseException thrown");
        assertEquals("Could not parse package-lock.json", exception.getMessage());
    }

    @Test
    void parseReportJsonMappingException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(JsonMappingException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> PackageLockParserHelper.parse(inputStream), "No JsonMappingException thrown");
        assertEquals("Problem with package-lock.json-Mapping", exception.getMessage());
    }

    @Test
    void parseReportJsonIOException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> PackageLockParserHelper.parse(inputStream), "No IOException thrown");
        assertEquals("IO Problem in package-lock.json parser", exception.getMessage());
    }

}
