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
package org.sonar.dependencycheck.base;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.Severity;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.CvssV2;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Identifier;
import org.sonar.dependencycheck.parser.element.Vulnerability;
import org.sonar.dependencycheck.reason.GradleDependencyReason;
import org.sonar.dependencycheck.reason.MavenDependencyReason;
import org.sonar.dependencycheck.reason.NPMDependencyReason;

class DependencyCheckUtilsTest {

    public static Stream<Object[]> severities() {
        return Stream.of(new Object[][]{
                // defaults
                {Float.valueOf("10.0"), Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.BLOCKER},
                {Float.valueOf("7.0"),  Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("6.9"),  Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("4.0"),  Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"),  Severity.MAJOR},
                {Float.valueOf("3.9"),  Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("0.0"),  Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.MINOR},

                // custom
                {Float.valueOf("10.0"), Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.BLOCKER},
                {Float.valueOf("9.0"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.BLOCKER},
                {Float.valueOf("7.0"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.CRITICAL},
                {Float.valueOf("6.9"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.CRITICAL},
                {Float.valueOf("4.0"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("3.9"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("1.9"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("0.0"),  Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.INFO},

                // custom, blocker deactivated
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.CRITICAL},
                {Float.valueOf("9.0"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.CRITICAL},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.CRITICAL},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.CRITICAL},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.INFO},

                // custom, critical deactivated
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MAJOR},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Severity.INFO},

                // custom, critical and major deactivated
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.MINOR},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Severity.INFO},

                // all vulnerabilites are critical
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Severity.CRITICAL},

                // all vulnerabilites are MAJOR, critical and blocker is deactivated
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Float.valueOf("0.0"), Severity.MAJOR},

                // all vulnerabilites are MINOR, blocker, critical  and major are deactivated
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0"), Severity.MINOR},

                // all vulnerabilities are INFO, blocker, critical, major and minor deactivated
                {Float.valueOf("10.0"), Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO},
                {Float.valueOf("7.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO},
                {Float.valueOf("6.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO},
                {Float.valueOf("4.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO},
                {Float.valueOf("3.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO},
                {Float.valueOf("1.9"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO},
                {Float.valueOf("0.0"),  Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"),  Float.valueOf("-1"), Severity.INFO}
        });
    }

    @ParameterizedTest(name = "{index} => cvssSeverity={0}, blocker={1}, critical={2}, major={3}, minor={4}, expectedSeverity={5}")
    @MethodSource("severities")
    void testCvssToSonarQubeSeverity(Float cvssSeverity, Float blocker, Float critical, Float major, Float minor, Severity expectedSeverity) {
        assertEquals(expectedSeverity, DependencyCheckUtils.cvssToSonarQubeSeverity(cvssSeverity, blocker, critical, major, minor));
    }

    public static Stream<Object[]> severitiestoscore() {
        return Stream.of(new Object[][]{
                // defaults
                {"critical", Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Float.valueOf("9.0")},
                {"high",     Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Float.valueOf("7.0")},
                {"moderate", Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Float.valueOf("4.0")},
                {"medium",   Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Float.valueOf("4.0")},
                {"low",      Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Float.valueOf("0.0")},
                {"dummy",    Float.valueOf("9.0"), Float.valueOf("7.0"), Float.valueOf("4.0"), Float.valueOf("0.0"), Float.valueOf("0.0")},

                // custom
                {"critical", Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("9.0")},
                {"high",     Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("5.0")},
                {"medium",   Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("2.0")},
                {"low",      Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"info",     Float.valueOf("9.0"), Float.valueOf("5.0"), Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("0.0")},

                // custom, blocker deactivated
                {"critical", Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("5.0")},
                {"high",     Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("5.0")},
                {"medium",   Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("2.0")},
                {"low",      Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"info",     Float.valueOf("-1"), Float.valueOf("5.0"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("0.0")},

                // custom, blocker, critical deactivated
                {"critical", Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("2.0")},
                {"high",     Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("2.0")},
                {"medium",   Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("2.0")},
                {"low",      Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"info",     Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("2.0"), Float.valueOf("1.0"), Float.valueOf("0.0")},

                // custom, blocker, critical and major deactivated
                {"critical", Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"high",     Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"medium",   Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"low",      Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Float.valueOf("1.0")},
                {"info",     Float.valueOf("-1"), Float.valueOf("-1"),  Float.valueOf("-1"), Float.valueOf("1.0"), Float.valueOf("0.0")},

                // custom, blocker, critical, major and minor deactivated
                {"critical", Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0")},
                {"high",     Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0")},
                {"medium",   Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0")},
                {"low",      Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0")},
                {"info",     Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("-1"), Float.valueOf("0.0")},
        });
    }
    @ParameterizedTest(name = "{index} => severity={0}, blocker={1}, critical={2}, major={3}, minor={4}, expectedScore={5}")
    @MethodSource("severitiestoscore")
    void testSeverityToScore(String severity, Float blocker, Float critical, Float major, Float minor, Float expectedScore) {
        assertEquals(expectedScore, DependencyCheckUtils.severityToScore(severity, blocker, critical, major, minor));
    }

    @Test
    void testBestDependencyReasonRootConfigurationFileOrder() {
        Path path = new File("root").toPath();
        InputFile pom = new TestInputFileBuilder("moduleKey", "pom.xml").setContents("123456").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        InputFile subpom = new TestInputFileBuilder("moduleKey", "submodule/pom.xml").setContents("132").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        InputFile subpom2 = new TestInputFileBuilder("moduleKey", "submodule2/pom.xml").setContents("123").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        MavenDependencyReason pomReason = new MavenDependencyReason(pom);
        MavenDependencyReason submodulepomReason = new MavenDependencyReason(subpom);
        MavenDependencyReason submodule2pomReason = new MavenDependencyReason(subpom2);

        // when
        Dependency dependency = mock(Dependency.class);
        when(dependency.isJavaDependency()).thenReturn(true);
        when(dependency.isJavaScriptDependency()).thenReturn(false);

        // then
        assertEquals(pomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(pomReason, submodulepomReason, submodule2pomReason)).get());
        assertEquals(pomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(submodulepomReason, submodule2pomReason, pomReason)).get());
        assertEquals(pomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(submodule2pomReason, pomReason, submodulepomReason)).get());
    }

    @Test
    void testBestDependencyReasonJavaDependency() {
        Path path = new File("root").toPath();
        InputFile packagLock = new TestInputFileBuilder("moduleKey", "package-lock.json").setContents("123456").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        InputFile subpom = new TestInputFileBuilder("moduleKey", "submodule/pom.xml").setContents("132").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        NPMDependencyReason npmReason = new NPMDependencyReason(packagLock);
        MavenDependencyReason submodulepomReason = new MavenDependencyReason(subpom);

        // when
        Dependency dependency = mock(Dependency.class);
        when(dependency.isJavaDependency()).thenReturn(true);
        when(dependency.isJavaScriptDependency()).thenReturn(false);

        // then
        assertEquals(submodulepomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(submodulepomReason, npmReason)).get());
        // we have only a NPM DependencyReason, but a Java-Dependency
        assertEquals(npmReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(npmReason)).get());
    }

    @Test
    void testBestDependencyReasonNPMDependency() {
        Path path = new File("root").toPath();
        InputFile packagLock = new TestInputFileBuilder("moduleKey", "submodule/package-lock.json").setContents("123456").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        InputFile subpom = new TestInputFileBuilder("moduleKey", "pom.xml").setContents("132").setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        NPMDependencyReason npmReason = new NPMDependencyReason(packagLock);
        MavenDependencyReason submodulepomReason = new MavenDependencyReason(subpom);

        // when
        Dependency dependency = mock(Dependency.class);
        when(dependency.isJavaDependency()).thenReturn(false);
        when(dependency.isJavaScriptDependency()).thenReturn(true);

        // then
        assertEquals(npmReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(submodulepomReason, npmReason)).get());
        // we have only a Java DependencyReason, but a NPM-Dependency
        assertEquals(submodulepomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(submodulepomReason)).get());
    }

    @Test
    void testBestDependencyReasonSubModule() throws IOException {
        Path path = new File("root").toPath();
        String pomContent = new String(Files.readAllBytes(new File("src/test/resources/reason", "pom.xml").toPath()), StandardCharsets.UTF_8);
        String gradleContent = new String(Files.readAllBytes(new File("src/test/resources/reason", "build.gradle").toPath()), StandardCharsets.UTF_8);
        InputFile gradle = new TestInputFileBuilder("moduleKey", "build.gradle").setContents(gradleContent).setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        InputFile subpom = new TestInputFileBuilder("moduleKey", "submodule/pom.xml").setContents(pomContent).setCharset(StandardCharsets.UTF_8).setModuleBaseDir(path).build();
        GradleDependencyReason pomReason = new GradleDependencyReason(gradle);
        MavenDependencyReason submodulepomReason = new MavenDependencyReason(subpom);

        // when
        Identifier identifier1 = new Identifier("pkg:maven/struts/struts@1.2.8", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers1 = new ArrayList<>();
        packageidentifiers1.add(identifier1);
        CvssV2 cvssV2 = new CvssV2(5.0f, "HIGH");
        Vulnerability vulnerability1 = new Vulnerability("Test name", "NVD", "MyDescription", null, cvssV2, null, null);
        List<Vulnerability> vulnerabilities1 = new ArrayList<>();
        vulnerabilities1.add(vulnerability1);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(), vulnerabilities1, packageidentifiers1, Collections.emptyList(), null);

        // then
        assertEquals(submodulepomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(pomReason, submodulepomReason)).get());
        assertEquals(submodulepomReason, DependencyCheckUtils.getBestDependencyReason(dependency, Arrays.asList(submodulepomReason, pomReason)).get());
    }
}
