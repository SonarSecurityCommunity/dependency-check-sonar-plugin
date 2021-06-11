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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IdentifierTest {

    @Test
    void testMaven() {
        Identifier a = new Identifier("pkg:maven/struts/struts@1.2.8", Confidence.HIGH);
        assertTrue(Identifier.isMavenPackage(a));
        assertFalse(Identifier.isNPMPackage(a));
        assertFalse(Identifier.isJavaScriptPackage(a));
        assertEquals("struts/struts@1.2.8", Identifier.getPackageArtifact(a).get());
        assertEquals("maven", Identifier.getPackageType(a).get());
    }

    @Test
    void testNode() {
        Identifier a = new Identifier("pkg:npm/braces@1.8.5", Confidence.HIGHEST);
        assertFalse(Identifier.isMavenPackage(a));
        assertTrue(Identifier.isNPMPackage(a));
        assertFalse(Identifier.isJavaScriptPackage(a));
        assertEquals("braces@1.8.5", Identifier.getPackageArtifact(a).get());
        assertEquals("npm", Identifier.getPackageType(a).get());
    }

    @Test
    void testNodeWithOutVersion() {
        Identifier a = new Identifier("pkg:npm/mime", Confidence.HIGHEST);
        assertFalse(Identifier.isMavenPackage(a));
        assertTrue(Identifier.isNPMPackage(a));
        assertFalse(Identifier.isJavaScriptPackage(a));
        assertEquals("mime", Identifier.getPackageArtifact(a).get());
        assertEquals("npm", Identifier.getPackageType(a).get());
    }

    @Test
    void testJavaScript() {
        Identifier a = new Identifier("pkg:javascript/jquery@2.2.0", Confidence.HIGHEST);
        assertFalse(Identifier.isMavenPackage(a));
        assertFalse(Identifier.isNPMPackage(a));
        assertTrue(Identifier.isJavaScriptPackage(a));
        assertEquals("jquery@2.2.0", Identifier.getPackageArtifact(a).get());
        assertEquals("javascript", Identifier.getPackageType(a).get());
    }
}
