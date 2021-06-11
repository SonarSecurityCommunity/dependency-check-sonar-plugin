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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Identifier;

class NPMDependencyReasonTest extends DependencyReasonTestHelper {

    @Test
    void isReasonable() throws IOException {
        NPMDependencyReason npm = new NPMDependencyReason(inputFile("package-lock.json"));
        assertTrue(npm.isReasonable());
        assertNotNull(npm.getInputComponent());
    }

    @Test
    void isReasonableWithoutContent() throws IOException {
        DefaultInputFile npmfile = mock(DefaultInputFile.class, RETURNS_DEEP_STUBS);
        when(npmfile.contents()).thenReturn("");
        NPMDependencyReason npm = new NPMDependencyReason(npmfile);
        assertFalse(npm.isReasonable());
        assertNotNull(npm.getInputComponent());
    }

    @Test
    void constructorWithIOException() throws IOException {
        DefaultInputFile npmfile = mock(DefaultInputFile.class, RETURNS_DEEP_STUBS);
        when(npmfile.contents()).thenThrow(new IOException());
        NPMDependencyReason npm = new NPMDependencyReason(npmfile);
        assertFalse(npm.isReasonable());
        assertNotNull(npm.getInputComponent());
    }

    @Test
    void foundDependencyJavascript() throws IOException {
        NPMDependencyReason npm = new NPMDependencyReason(inputFile("package-lock.json"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:javascript/jquery@2.2.0", Confidence.HIGHEST);
        Collection<Identifier> identifiersCollected = new ArrayList<>();
        identifiersCollected.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), identifiersCollected, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = npm.getBestTextRange(dependency);
        assertTrue(npm.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(335, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(339, textRangeConfidence.getTextRange().end().line());
        assertEquals(6, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGHEST, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(npm.getBestTextRange(dependency), npm.getBestTextRange(dependency));
    }

    @Test
    void foundDependencyNPM() throws IOException {
        NPMDependencyReason npm = new NPMDependencyReason(inputFile("package-lock.json"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:npm/arr-flatten@1.1.0", Confidence.HIGHEST);
        Collection<Identifier> identifiersCollected = new ArrayList<>();
        identifiersCollected.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), identifiersCollected, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = npm.getBestTextRange(dependency);
        assertTrue(npm.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(7, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(11, textRangeConfidence.getTextRange().end().line());
        assertEquals(6, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGHEST, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(npm.getBestTextRange(dependency), npm.getBestTextRange(dependency));
    }

    @Test
    void foundDependencyNPMOnlyWithName() throws IOException {
        NPMDependencyReason npm = new NPMDependencyReason(inputFile("package-lock.json"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:npm/arr-flatten@9.9.9", Confidence.HIGHEST);
        Collection<Identifier> identifiersCollected = new ArrayList<>();
        identifiersCollected.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), identifiersCollected, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = npm.getBestTextRange(dependency);
        assertTrue(npm.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(7, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(11, textRangeConfidence.getTextRange().end().line());
        assertEquals(6, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGH, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(npm.getBestTextRange(dependency), npm.getBestTextRange(dependency));
    }

    @Test
    void foundDependencyNPMWithoutVersion() throws IOException {
        NPMDependencyReason npm = new NPMDependencyReason(inputFile("package-lock.json"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:npm/arr-flatten", Confidence.HIGHEST);
        Collection<Identifier> identifiersCollected = new ArrayList<>();
        identifiersCollected.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), identifiersCollected, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = npm.getBestTextRange(dependency);
        assertTrue(npm.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(7, textRangeConfidence.getTextRange().start().line());
        assertEquals(0, textRangeConfidence.getTextRange().start().lineOffset());
        assertEquals(11, textRangeConfidence.getTextRange().end().line());
        assertEquals(6, textRangeConfidence.getTextRange().end().lineOffset());
        assertEquals(Confidence.HIGH, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(npm.getBestTextRange(dependency), npm.getBestTextRange(dependency));
    }

    @Test
    void foundNoDependency() throws IOException {
        NPMDependencyReason npm = new NPMDependencyReason(inputFile("package-lock.json"));
        // Create Dependency
        Identifier identifier = new Identifier("pkg:javascript/dummyname@2.2.0", Confidence.HIGHEST);
        Collection<Identifier> identifiersCollected = new ArrayList<>();
        identifiersCollected.add(identifier);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),Collections.emptyList(), identifiersCollected, Collections.emptyList(), null);
        TextRangeConfidence textRangeConfidence = npm.getBestTextRange(dependency);
        assertTrue(npm.isReasonable());
        assertNotNull(textRangeConfidence);
        assertEquals(LINE_NOT_FOUND, textRangeConfidence.getTextRange().start().line());
        assertEquals(Confidence.LOW, textRangeConfidence.getConfidence());
        // verify that same dependency points to the same TextRange, use of HashMap
        assertEquals(npm.getBestTextRange(dependency), npm.getBestTextRange(dependency));
    }

}
