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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.dependencycheck.base.DependencyCheckConstants;
import org.sonar.dependencycheck.parser.element.Analysis;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.CvssV2;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Identifier;
import org.sonar.dependencycheck.parser.element.ProjectInfo;
import org.sonar.dependencycheck.parser.element.ScanInfo;
import org.sonar.dependencycheck.parser.element.Vulnerability;

class DependencyReasonSearcherTest {

    private static final File TEST_DIR = new File("src/test/resources/reason");

    private DefaultInputFile inputFile(String fileName) throws IOException {
        File file = new File(TEST_DIR, fileName);
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        return TestInputFileBuilder.create("key", fileName).setModuleBaseDir(Paths.get(TEST_DIR.getAbsolutePath()))
                .setType(InputFile.Type.MAIN).setLanguage("mytest").setCharset(StandardCharsets.UTF_8)
                .initMetadata(content).build();
    }

    @Test
    void checkForDependencyReasons() throws IOException {
        SensorContextTester context = SensorContextTester.create(new File(""));
        MapSettings settings = new MapSettings();
        settings.setProperty(DependencyCheckConstants.XML_REPORT_PATH_PROPERTY, "dependency-check-report.xml");
        context.setSettings(settings);
        context.fileSystem().add(inputFile("pom.xml"));
        context.fileSystem().add(inputFile("build.gradle"));
        context.fileSystem().add(inputFile("build.gradle.kts"));
        context.fileSystem().add(inputFile("package-lock.json"));
        DependencyReasonSearcher searcher = new DependencyReasonSearcher(context);
        ScanInfo scanInfo = new ScanInfo("testengine", null);
        ProjectInfo projectInfo = new ProjectInfo("testproject", "testreportdate");
        Collection<Dependency> dependencies = new LinkedList<>();
        Analysis analysis = new Analysis(scanInfo, projectInfo, dependencies);
        searcher.addDependenciesToInputComponents(analysis, context);
        assertEquals(4, searcher.getDependencyReasons().size());
    }

    @Test
    void checkForDependencyReasonsMaven() throws IOException {
        SensorContextTester context = SensorContextTester.create(new File(""));
        MapSettings settings = new MapSettings();
        settings.setProperty(DependencyCheckConstants.XML_REPORT_PATH_PROPERTY, "dependency-check-report.xml");
        context.setSettings(settings);
        context.fileSystem().add(inputFile("pom.xml"));
        DependencyReasonSearcher searcher = new DependencyReasonSearcher(context);

        ScanInfo scanInfo = new ScanInfo("testengine", null);
        ProjectInfo projectInfo = new ProjectInfo("testproject", "testreportdate");
        Collection<Dependency> dependencies = new LinkedList<>();
        // First Identifier
        Identifier identifier1 = new Identifier("pkg:maven/struts/struts@1.2.8", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers1 = new ArrayList<>();
        packageidentifiers1.add(identifier1);
        CvssV2 cvssV2 = new CvssV2(5.0f, "HIGH");
        Vulnerability vulnerability1 = new Vulnerability("Test name", "NVD", "MyDescription", null, cvssV2, null, null);
        List<Vulnerability> vulnerabilities1 = new ArrayList<>();
        vulnerabilities1.add(vulnerability1);
        Dependency dependency1 = new Dependency(null, null, null, null, Collections.emptyMap(), vulnerabilities1, packageidentifiers1, Collections.emptyList(), null);
        // Second Identifier
        Identifier identifier2 = new Identifier("pkg:maven/org.springframework/spring@2.0.8", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers2 = new ArrayList<>();
        packageidentifiers2.add(identifier2);
        Vulnerability vulnerability2 = new Vulnerability("Test name", "NVD", "MyDescription", null, cvssV2, null, null);
        List<Vulnerability> vulnerabilities2 = new ArrayList<>();
        vulnerabilities2.add(vulnerability2);
        Dependency dependency2 = new Dependency(null, null, null, null, Collections.emptyMap(), vulnerabilities1, packageidentifiers2, Collections.emptyList(), null);

        // Add dependencies
        dependencies.add(dependency1);
        dependencies.add(dependency2);

        Analysis analysis = new Analysis(scanInfo, projectInfo, dependencies);
        searcher.addDependenciesToInputComponents(analysis, context);
        assertEquals(2, context.allIssues().size());
        assertEquals(1, searcher.getDependencyReasons().size());

        // check for org.springframework - spring dependency
        assertTrue(context.allIssues().stream().anyMatch(i -> i.primaryLocation().textRange().start().line() == 56 && i.primaryLocation().textRange().end().line() == 60));
        // check for struts - struts dependency
        assertTrue(context.allIssues().stream().anyMatch(i -> i.primaryLocation().textRange().start().line() == 46 && i.primaryLocation().textRange().end().line() == 50));
    }

    @Test
    void checkForDependencyReasonsGradle() throws IOException {
        SensorContextTester context = checkForDependencyReasonsGradleAbstract(inputFile("build.gradle"));
        assertTrue(context.allIssues().stream().anyMatch(i -> i.primaryLocation().textRange().start().line() == 24));
    }

    @Test
    void checkForDependencyReasonsGradleKotlinDsl() throws IOException {
        SensorContextTester context = checkForDependencyReasonsGradleAbstract(inputFile("build.gradle.kts"));
        assertTrue(context.allIssues().stream().anyMatch(i -> i.primaryLocation().textRange().start().line() == 15));
    }

    public SensorContextTester checkForDependencyReasonsGradleAbstract(InputFile inputFile) {
        SensorContextTester context = SensorContextTester.create(new File(""));
        MapSettings settings = new MapSettings();
        settings.setProperty(DependencyCheckConstants.XML_REPORT_PATH_PROPERTY, "dependency-check-report.xml");
        settings.setProperty(DependencyCheckConstants.SUMMARIZE_PROPERTY, Boolean.TRUE);
        context.setSettings(settings);
        context.fileSystem().add(inputFile);
        DependencyReasonSearcher searcher = new DependencyReasonSearcher(context);

        ScanInfo scanInfo = new ScanInfo("testengine", null);
        ProjectInfo projectInfo = new ProjectInfo("testproject", "testreportdate");
        Collection<Dependency> dependencies = new LinkedList<>();
        // Second Identifier
        Identifier identifier = new Identifier("pkg:maven/org.springframework/spring@2.0", Confidence.HIGHEST);
        Collection<Identifier> packageidentifiers = new ArrayList<>();
        packageidentifiers.add(identifier);
        CvssV2 cvssV2 = new CvssV2(5.0f, "HIGH");
        Vulnerability vulnerability = new Vulnerability("Test name", "NVD", "MyDescription", null, cvssV2, null, null);
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        vulnerabilities.add(vulnerability);
        Dependency dependency = new Dependency(null, null, null, null, Collections.emptyMap(),vulnerabilities, packageidentifiers, Collections.emptyList(), null);

        // Add dependencies
        dependencies.add(dependency);

        Analysis analysis = new Analysis(scanInfo, projectInfo, dependencies);
        searcher.addDependenciesToInputComponents(analysis, context);
        assertEquals(1, context.allIssues().size());
        assertEquals(1, searcher.getDependencyReasons().size());

        return context;
    }
}
