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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.dependencycheck.parser.element.Analysis;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Vulnerability;

class XMLReportParserHelperTest extends ReportParserTest {

    @Override
    public Analysis parseReport(String dir) throws Exception {
        Instant startTime = Instant.now();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dir + "/dependency-check-report.xml");
        Analysis analysis = XMLReportParserHelper.parse(inputStream);
        assertNotNull(analysis);
        Instant endTime = Instant.now();
        System.out.println("Duration XML-Report-Parser: " + Duration.between(startTime, endTime));
        return analysis;
    }

    @Test
    void parseReportXMLIOException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> XMLReportParserHelper.parse(inputStream), "No IOException thrown");
        assertEquals("Could not parse XML", exception.getMessage());
    }

    @Test
    void parseReportNode500() throws Exception {
        SensorContextTester context = SensorContextTester.create(new File(""));
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("reportNode.js/dependency-check-report.xml");
        Analysis analysis = XMLReportParserHelper.parse(inputStream);
        assertEquals("5.0.0-M2", analysis.getScanInfo().getEngineVersion());
        assertEquals("project", analysis.getProjectInfo().get().getName());
        assertEquals("2019-04-23T22:43:06.450+0000", analysis.getProjectInfo().get().getReportDate());

        Collection<Dependency> dependencies = analysis.getDependencies();
        assertEquals(3, dependencies.size());
        Iterator<Dependency> iterator = dependencies.iterator();

        // jquery
        Dependency dependency = iterator.next();

        assertEquals("jquery.js", dependency.getFileName());
        assertEquals("project/node_modules/moment-duration-format/test/vendor/jquery.js", dependency.getFilePath());
        assertEquals("cc41e74189d44a6169f56655e44ef69d", dependency.getMd5Hash().get());
        assertEquals("e0e77771c69fe4acd4cad20a0f20ce7a5086dc56", dependency.getSha1Hash().get());

        // Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
        // assertEquals(2, evidenceCollected.size());
        // for (Evidence evidence : evidenceCollected) {
        // assertFalse(evidence.getSource().isEmpty());
        // assertFalse(evidence.getName().isEmpty());
        // assertFalse(evidence.getValue().isEmpty());
        // }

        Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
        assertEquals(2, vulnerabilities.size());
        Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
        Vulnerability vulnerability = vulnIterator.next();
        assertEquals("CVE-2015-9251", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(4.3f, vulnerability.getCvssScore(false, context.config()), 0.0f);
        assertEquals(6.1f, vulnerability.getCvssScore(context.config()), 0.0f);
        assertEquals("MEDIUM", vulnerability.getSeverity());
        assertEquals("MEDIUM", vulnerability.getSeverity(false));
        assertTrue(vulnerability.getCwes().isPresent());
        assertEquals("CWE-79", vulnerability.getCwes().get()[0]);
        assertEquals(
            "jQuery before 3.0.0 is vulnerable to Cross-site Scripting (XSS) attacks when a cross-domain Ajax request is performed without the dataType option, causing text/javascript responses to be executed.",
            vulnerability.getDescription());

        vulnerability = vulnIterator.next();
        assertEquals("CVE-2019-11358", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(4.3f, vulnerability.getCvssScore(false, context.config()), 0.0f);
        assertEquals(6.1f, vulnerability.getCvssScore(context.config()), 0.0f);
        assertEquals("MEDIUM", vulnerability.getSeverity());
        assertTrue(vulnerability.getCwes().isPresent());
        assertEquals("CWE-79", vulnerability.getCwes().get()[0]);
        assertEquals(
            "jQuery before 3.4.0, as used in Drupal, Backdrop CMS, and other products, mishandles jQuery.extend(true, {}, ...) because of Object.prototype pollution. If an unsanitized source object contained an enumerable __proto__ property, it could extend the native Object.prototype.",
            vulnerability.getDescription());

        // kind-of -> no vulnerability
        dependency = iterator.next();

        assertEquals("index.js", dependency.getFileName());
        assertEquals("project/node_modules/jest-config/node_modules/is-number/node_modules/kind-of/index.js", dependency.getFilePath());
        assertEquals("974e0c2803e83c5bf65de52b98bf4f55", dependency.getMd5Hash().get());
        assertEquals("f9e8418f23f97452410088786d5e0c7a981ced74", dependency.getSha1Hash().get());

        // evidenceCollected = dependency.getEvidenceCollected();
        // assertEquals(0, evidenceCollected.size());

        vulnerabilities = dependency.getVulnerabilities();
        assertEquals(0, vulnerabilities.size());

        // braces -> vulnerability from NPM
        dependency = iterator.next();

        assertEquals("braces:1.8.5", dependency.getFileName());
        assertEquals("project/node_modules/braces/package.json", dependency.getFilePath());
        assertEquals("a5663cea473ad651ac4a00a504883a22", dependency.getMd5Hash().get());
        assertEquals("8082af62f94236e20503e76f0c94b0cf38bf0824", dependency.getSha1Hash().get());

        // evidenceCollected = dependency.getEvidenceCollected();
        // assertEquals(6, evidenceCollected.size());
        // for (Evidence evidence : evidenceCollected) {
        // assertFalse(evidence.getSource().isEmpty());
        // assertFalse(evidence.getName().isEmpty());
        // assertFalse(evidence.getValue().isEmpty());
        // }

        vulnerabilities = dependency.getVulnerabilities();
        assertEquals(1, vulnerabilities.size());
        vulnIterator = vulnerabilities.iterator();
        vulnerability = vulnIterator.next();
        assertEquals("786", vulnerability.getName());
        assertEquals("NPM", vulnerability.getSource());
        assertEquals(4.0f, vulnerability.getCvssScore(false, context.config()), 0.0f);
        assertEquals(4.0f, vulnerability.getCvssScore(context.config()), 0.0f);
        assertEquals("MEDIUM", vulnerability.getSeverity());
        assertEquals("MEDIUM", vulnerability.getSeverity(false));
        assertFalse(vulnerability.getCwes().isPresent());
        assertEquals("Versions of `braces` prior to 2.3.1 are vulnerable to Regular Expression Denial of\n" +
            "                        Service (ReDoS). Untrusted input may cause catastrophic backtracking while matching regular\n" +
            "                        expressions. This can cause the application to be unresponsive leading to Denial of Service.", vulnerability.getDescription());
    }

    @Test
    void parseBigReportNode500() throws Exception {
        SensorContextTester context = SensorContextTester.create(new File(""));
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("reportNode.js/big-dependency-check-report.xml");
        Analysis analysis = XMLReportParserHelper.parse(inputStream);
        assertEquals("5.0.0-M2", analysis.getScanInfo().getEngineVersion());
        assertEquals("project", analysis.getProjectInfo().get().getName());
        assertEquals("2019-04-23T22:43:06.450+0000", analysis.getProjectInfo().get().getReportDate());

        Collection<Dependency> dependencies = analysis.getDependencies();
        assertEquals(10765, dependencies.size());

        for (Dependency dependency : dependencies) {
            // check jquery
            if ("e0e77771c69fe4acd4cad20a0f20ce7a5086dc56".equals(dependency.getSha1Hash().orElse(""))) {
                assertEquals("jquery.js", dependency.getFileName());
                assertEquals("project/node_modules/moment-duration-format/test/vendor/jquery.js", dependency.getFilePath());
                assertEquals("cc41e74189d44a6169f56655e44ef69d", dependency.getMd5Hash().get());
                assertEquals("e0e77771c69fe4acd4cad20a0f20ce7a5086dc56", dependency.getSha1Hash().get());

                // Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
                // assertEquals(2, evidenceCollected.size());
                // for (Evidence evidence : evidenceCollected) {
                // assertFalse(evidence.getSource().isEmpty());
                // assertFalse(evidence.getName().isEmpty());
                // assertFalse(evidence.getValue().isEmpty());
                // }

                Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
                assertEquals(2, vulnerabilities.size());
                Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
                Vulnerability vulnerability = vulnIterator.next();
                assertEquals("CVE-2015-9251", vulnerability.getName());
                assertEquals("NVD", vulnerability.getSource());
                assertEquals(4.3f, vulnerability.getCvssScore(false, context.config()), 0.0f);
                assertEquals(6.1f, vulnerability.getCvssScore(context.config()), 0.0f);
                assertEquals("MEDIUM", vulnerability.getSeverity());
                assertEquals("MEDIUM", vulnerability.getSeverity(false));
                assertTrue(vulnerability.getCwes().isPresent());
                assertEquals("CWE-79", vulnerability.getCwes().get()[0]);
                assertEquals(
                    "jQuery before 3.0.0 is vulnerable to Cross-site Scripting (XSS) attacks when a cross-domain Ajax request is performed without the dataType option, causing text/javascript responses to be executed.",
                    vulnerability.getDescription());

                vulnerability = vulnIterator.next();
                assertEquals("CVE-2019-11358", vulnerability.getName());
                assertEquals("NVD", vulnerability.getSource());
                assertEquals(4.3f, vulnerability.getCvssScore(false, context.config()), 0.0f);
                assertEquals(6.1f, vulnerability.getCvssScore(context.config()), 0.0f);
                assertEquals("MEDIUM", vulnerability.getSeverity());
                assertTrue(vulnerability.getCwes().isPresent());
                assertEquals("CWE-79", vulnerability.getCwes().get()[0]);
                assertEquals(
                    "jQuery before 3.4.0, as used in Drupal, Backdrop CMS, and other products, mishandles jQuery.extend(true, {}, ...) because of Object.prototype pollution. If an unsanitized source object contained an enumerable __proto__ property, it could extend the native Object.prototype.",
                    vulnerability.getDescription());
            }
            // check kind-of
            if ("f9e8418f23f97452410088786d5e0c7a981ced74".equals(dependency.getSha1Hash().orElse(""))) {
                assertEquals("index.js", dependency.getFileName());
                assertEquals("project/node_modules/jest-config/node_modules/is-number/node_modules/kind-of/index.js", dependency.getFilePath());
                assertEquals("974e0c2803e83c5bf65de52b98bf4f55", dependency.getMd5Hash().get());
                assertEquals("f9e8418f23f97452410088786d5e0c7a981ced74", dependency.getSha1Hash().get());

                // Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
                // assertEquals(0, evidenceCollected.size());

                Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
                assertEquals(0, vulnerabilities.size());
            }
            // check brace:1.8.5 => NPM
            if ("8082af62f94236e20503e76f0c94b0cf38bf0824".equals(dependency.getSha1Hash().orElse(""))) {
                assertEquals("braces:1.8.5", dependency.getFileName());
                assertEquals("project/node_modules/braces/package.json", dependency.getFilePath());
                assertEquals("a5663cea473ad651ac4a00a504883a22", dependency.getMd5Hash().get());
                assertEquals("8082af62f94236e20503e76f0c94b0cf38bf0824", dependency.getSha1Hash().get());

                // Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
                // assertEquals(6, evidenceCollected.size());
                // for (Evidence evidence : evidenceCollected) {
                // assertFalse(evidence.getSource().isEmpty());
                // assertFalse(evidence.getName().isEmpty());
                // assertFalse(evidence.getValue().isEmpty());
                // }

                Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
                assertEquals(1, vulnerabilities.size());
                Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
                Vulnerability vulnerability = vulnIterator.next();
                assertEquals("786", vulnerability.getName());
                assertEquals("NPM", vulnerability.getSource());
                assertEquals(4.0f, vulnerability.getCvssScore(false, context.config()), 0.0f);
                assertEquals(4.0f, vulnerability.getCvssScore(context.config()), 0.0f);
                assertEquals("MEDIUM", vulnerability.getSeverity());
                assertEquals("MEDIUM", vulnerability.getSeverity(false));
                assertFalse(vulnerability.getCwes().isPresent());
                assertEquals(
                    "Versions of `braces` prior to 2.3.1 are vulnerable to Regular Expression Denial of Service (ReDoS). Untrusted input may cause catastrophic backtracking while matching regular expressions. This can cause the application to be unresponsive leading to Denial of Service.",
                    vulnerability.getDescription());
            }
        }
    }

}
