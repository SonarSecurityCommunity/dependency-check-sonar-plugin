/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2019 SonarSecurityCommunity
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.dependencycheck.parser.element.Analysis;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Evidence;
import org.sonar.dependencycheck.parser.element.Identifier;
import org.sonar.dependencycheck.parser.element.Vulnerability;

public class ReportParserTest {

    private ReportParser parser;

    @BeforeEach
    public void init() {
        final SensorContextTester context = SensorContextTester.create(new File(""));
        parser = new ReportParser(context);
    }

    @Test
    public void parseReport400() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("report/dependency-check-report-400.xml");
        Analysis analysis = parser.parse(inputStream);
        assertEquals("4.0.2", analysis.getScanInfo().getEngineVersion());
        assertEquals("Multi-Module Maven Example", analysis.getProjectInfo().get().getName());
        assertEquals("2019-04-18T11:30:25.206+0200", analysis.getProjectInfo().get().getReportDate());
        assertEquals("This report contains data retrieved from the National Vulnerability Database: https://nvd.nist.gov, NPM Public Advisories: https://www.npmjs.com/advisories, and the RetireJS community.", analysis.getProjectInfo().get().getCredits());

        // struts-1.2.8.jar
        Collection<Dependency> dependencies = analysis.getDependencies();
        assertEquals(20, dependencies.size());
        Iterator<Dependency> iterator = dependencies.iterator();
        Dependency dependency = (Dependency) iterator.next();

        assertEquals("struts-1.2.8.jar", dependency.getFileName());
        assertEquals("/to/path/struts/struts/1.2.8/struts-1.2.8.jar", dependency.getFilePath());
        assertEquals("8af31c3a406cfbfd991a6946102d583a", dependency.getMd5Hash());
        assertEquals("5919caff42c3f42fb251fd82a58af4a7880826dd", dependency.getSha1Hash());

        Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
        assertEquals(26, evidenceCollected.size());
        for (Evidence evidence : evidenceCollected) {
            assertFalse(evidence.getSource().isEmpty());
            assertFalse(evidence.getName().isEmpty());
            assertFalse(evidence.getValue().isEmpty());
        }

        Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
        assertEquals(8, vulnerabilities.size());
        Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
        Vulnerability vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2006-1546", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(7.5f, vulnerability.getCvssScore(), 0.0f);
        assertEquals("High", vulnerability.getSeverity());
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("Apache Software Foundation (ASF) Struts ...", vulnerability.getDescription());

        vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2006-1547", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(7.8f , vulnerability.getCvssScore(), 0.0f);
        assertEquals("High", vulnerability.getSeverity());
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("ActionForm in Apache Software Foundation (ASF) Struts before 1.2.9 ...", vulnerability.getDescription());

        // commons-beanutils-1.7.0.jar
        dependency = (Dependency) iterator.next();
        assertEquals(14, dependency.getEvidenceCollected().size());
        assertEquals(1, dependency.getVulnerabilities().size());

        // commons-digester-1.6.jar
        dependency = (Dependency) iterator.next();
        assertEquals(15, dependency.getEvidenceCollected().size());
        assertTrue(dependency.getVulnerabilities().isEmpty());

        // commons-collections-2.1.jar
        dependency = (Dependency) iterator.next();
        assertEquals(17, dependency.getEvidenceCollected().size());
        assertEquals(2, dependency.getVulnerabilities().size());
        assertEquals(2, dependency.getIdentifiersCollected().size());
        Collection<Identifier> identifiers = dependency.getIdentifiersCollected();
        Identifier identifier = identifiers.iterator().next();
        assertEquals(Confidence.HIGHEST, identifier.getConfidence().get());
        assertEquals("commons-collections:commons-collections:2.1", identifier.getName());
        assertEquals("maven", identifier.getType());
        vulnerabilities = dependency.getVulnerabilities();
        assertEquals(2, vulnerabilities.size());
        vulnIterator = vulnerabilities.iterator();
        vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2015-6420", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(7.5f, vulnerability.getCvssScore(), 0.0f);

        // xml-apis-1.0.b2.jar
        dependency = (Dependency) iterator.next();
        System.out.println(dependency.getFileName());
        assertEquals(32, dependency.getEvidenceCollected().size());
        assertTrue(dependency.getVulnerabilities().isEmpty());

    }
    @Test
    public void parseReport500() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("report/dependency-check-report-500.xml");
        Analysis analysis = parser.parse(inputStream);
        assertEquals("5.0.0-M2", analysis.getScanInfo().getEngineVersion());
        assertEquals("Multi-Module Maven Example", analysis.getProjectInfo().get().getName());
        assertEquals("2019-04-17T18:25:00.460+0200", analysis.getProjectInfo().get().getReportDate());
        assertEquals("This report contains data retrieved from the National Vulnerability Database: https://nvd.nist.gov, NPM Public Advisories: https://www.npmjs.com/advisories, and the RetireJS community.", analysis.getProjectInfo().get().getCredits());

        // struts-1.2.8.jar
        Collection<Dependency> dependencies = analysis.getDependencies();
        assertEquals(34, dependencies.size());
        Iterator<Dependency> iterator = dependencies.iterator();
        Dependency dependency = (Dependency) iterator.next();

        assertEquals("struts-1.2.8.jar", dependency.getFileName());
        assertEquals("/to/path/struts/struts/1.2.8/struts-1.2.8.jar", dependency.getFilePath());
        assertEquals("8af31c3a406cfbfd991a6946102d583a", dependency.getMd5Hash());
        assertEquals("5919caff42c3f42fb251fd82a58af4a7880826dd", dependency.getSha1Hash());

        Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
        assertEquals(30, evidenceCollected.size());
        for (Evidence evidence : evidenceCollected) {
            assertFalse(evidence.getSource().isEmpty());
            assertFalse(evidence.getName().isEmpty());
            assertFalse(evidence.getValue().isEmpty());
        }

        Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
        assertEquals(11, vulnerabilities.size());
        Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
        Vulnerability vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2006-1546", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(7.5f, vulnerability.getCvssScore(), 0.0f);
        assertEquals("HIGH", vulnerability.getSeverity());
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("Apache Software Foundation (ASF) Struts ...", vulnerability.getDescription());

        vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2006-1547", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(7.8f , vulnerability.getCvssScore(), 0.0f);
        assertEquals("HIGH", vulnerability.getSeverity());
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("ActionForm in Apache Software Foundation (ASF) Struts before 1.2.9 ...", vulnerability.getDescription());

        // commons-beanutils-1.7.0.jar
        dependency = (Dependency) iterator.next();
        assertEquals(20, dependency.getEvidenceCollected().size());
        assertEquals(1, dependency.getVulnerabilities().size());

        // commons-digester-1.6.jar
        dependency = (Dependency) iterator.next();
        assertEquals(20, dependency.getEvidenceCollected().size());
        assertTrue(dependency.getVulnerabilities().isEmpty());

        // commons-collections-2.1.jar
        dependency = (Dependency) iterator.next();
        assertEquals(21, dependency.getEvidenceCollected().size());
        assertEquals(2, dependency.getVulnerabilities().size());
        assertEquals(2, dependency.getIdentifiersCollected().size());
        Collection<Identifier> identifiers = dependency.getIdentifiersCollected();
        Identifier identifier = identifiers.iterator().next();
        assertEquals(Confidence.HIGH, identifier.getConfidence().get());
        assertEquals("commons-collections:commons-collections:2.1", identifier.getName());
        assertEquals("maven", identifier.getType());
        vulnerabilities = dependency.getVulnerabilities();
        assertEquals(2, vulnerabilities.size());
        vulnIterator = vulnerabilities.iterator();
        vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2015-6420", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(7.5f, vulnerability.getCvssScore(), 0.0f);

        // xml-apis-1.0.b2.jar
        dependency = (Dependency) iterator.next();
        assertEquals(47, dependency.getEvidenceCollected().size());
        assertTrue(dependency.getVulnerabilities().isEmpty());

    }

    @Test
    public void parseReportNode500() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("reportNode.js/dependency-check-report.xml");
        Analysis analysis = parser.parse(inputStream);
        assertEquals("5.0.0-M2", analysis.getScanInfo().getEngineVersion());
        assertEquals("project", analysis.getProjectInfo().get().getName());
        assertEquals("2019-04-23T22:43:06.450+0000", analysis.getProjectInfo().get().getReportDate());
        assertEquals("This report contains data retrieved from the National Vulnerability Database: https://nvd.nist.gov, NPM Public Advisories: https://www.npmjs.com/advisories, and the RetireJS community.", analysis.getProjectInfo().get().getCredits());

        Collection<Dependency> dependencies = analysis.getDependencies();
        assertEquals(3, dependencies.size());
        Iterator<Dependency> iterator = dependencies.iterator();

        // jquery
        Dependency dependency = (Dependency) iterator.next();

        assertEquals("jquery.js", dependency.getFileName());
        assertEquals("project/node_modules/moment-duration-format/test/vendor/jquery.js", dependency.getFilePath());
        assertEquals("cc41e74189d44a6169f56655e44ef69d", dependency.getMd5Hash());
        assertEquals("e0e77771c69fe4acd4cad20a0f20ce7a5086dc56", dependency.getSha1Hash());

        Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
        assertEquals(2, evidenceCollected.size());
        for (Evidence evidence : evidenceCollected) {
            assertFalse(evidence.getSource().isEmpty());
            assertFalse(evidence.getName().isEmpty());
            assertFalse(evidence.getValue().isEmpty());
        }

        Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
        assertEquals(2, vulnerabilities.size());
        Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
        Vulnerability vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2015-9251", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(4.3f, vulnerability.getCvssScore(false), 0.0f);
        assertEquals(6.1f, vulnerability.getCvssScore(), 0.0f);
        assertEquals("MEDIUM", vulnerability.getSeverity());
        assertEquals("MEDIUM", vulnerability.getSeverity(false));
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("jQuery before 3.0.0 is vulnerable to Cross-site Scripting (XSS) attacks when a cross-domain Ajax request is performed without the dataType option, causing text/javascript responses to be executed.", vulnerability.getDescription());

        vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("CVE-2019-11358", vulnerability.getName());
        assertEquals("NVD", vulnerability.getSource());
        assertEquals(4.3f, vulnerability.getCvssScore(false), 0.0f);
        assertEquals(6.1f , vulnerability.getCvssScore(), 0.0f);
        assertEquals("MEDIUM", vulnerability.getSeverity());
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("jQuery before 3.4.0, as used in Drupal, Backdrop CMS, and other products, mishandles jQuery.extend(true, {}, ...) because of Object.prototype pollution. If an unsanitized source object contained an enumerable __proto__ property, it could extend the native Object.prototype.", vulnerability.getDescription());

        // kind-of -> no vulnerability
        dependency = (Dependency) iterator.next();

        assertEquals("index.js", dependency.getFileName());
        assertEquals("project/node_modules/jest-config/node_modules/is-number/node_modules/kind-of/index.js", dependency.getFilePath());
        assertEquals("974e0c2803e83c5bf65de52b98bf4f55", dependency.getMd5Hash());
        assertEquals("f9e8418f23f97452410088786d5e0c7a981ced74", dependency.getSha1Hash());

        evidenceCollected = dependency.getEvidenceCollected();
        assertEquals(0, evidenceCollected.size());

        vulnerabilities = dependency.getVulnerabilities();
        assertEquals(0, vulnerabilities.size());


        // braces -> vulnerability from NPM
        dependency = (Dependency) iterator.next();

        assertEquals("braces:1.8.5", dependency.getFileName());
        assertEquals("project/node_modules/braces/package.json", dependency.getFilePath());
        assertEquals("a5663cea473ad651ac4a00a504883a22", dependency.getMd5Hash());
        assertEquals("8082af62f94236e20503e76f0c94b0cf38bf0824", dependency.getSha1Hash());

        evidenceCollected = dependency.getEvidenceCollected();
        assertEquals(6, evidenceCollected.size());
        for (Evidence evidence : evidenceCollected) {
            assertFalse(evidence.getSource().isEmpty());
            assertFalse(evidence.getName().isEmpty());
            assertFalse(evidence.getValue().isEmpty());
        }

        vulnerabilities = dependency.getVulnerabilities();
        assertEquals(1, vulnerabilities.size());
        vulnIterator = vulnerabilities.iterator();
        vulnerability = (Vulnerability) vulnIterator.next();
        assertEquals("786", vulnerability.getName());
        assertEquals("NPM", vulnerability.getSource());
        assertEquals(4.0f, vulnerability.getCvssScore(false), 0.0f);
        assertEquals(4.0f, vulnerability.getCvssScore(), 0.0f);
        assertEquals("MEDIUM", vulnerability.getSeverity());
        assertEquals("MEDIUM", vulnerability.getSeverity(false));
        assertFalse(vulnerability.getCwe().isPresent());
        assertEquals("Versions of `braces` prior to 2.3.1 are vulnerable to Regular Expression Denial of\n" +
                "                        Service (ReDoS). Untrusted input may cause catastrophic backtracking while matching regular\n" +
                "                        expressions. This can cause the application to be unresponsive leading to Denial of Service.", vulnerability.getDescription());
    }

    @Test
    public void parseBigReportNode500() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("reportNode.js/big-dependency-check-report.xml");
        Analysis analysis = parser.parse(inputStream);
        assertEquals("5.0.0-M2", analysis.getScanInfo().getEngineVersion());
        assertEquals("project", analysis.getProjectInfo().get().getName());
        assertEquals("2019-04-23T22:43:06.450+0000", analysis.getProjectInfo().get().getReportDate());
        assertEquals("This report contains data retrieved from the National Vulnerability Database: https://nvd.nist.gov, NPM Public Advisories: https://www.npmjs.com/advisories, and the RetireJS community.", analysis.getProjectInfo().get().getCredits());

        Collection<Dependency> dependencies = analysis.getDependencies();
        assertEquals(10765, dependencies.size());
        
        for (Dependency dependency : dependencies) {
            //check jquery
            if ( "e0e77771c69fe4acd4cad20a0f20ce7a5086dc56".equals(dependency.getSha1Hash())){
                assertEquals("jquery.js", dependency.getFileName());
                assertEquals("project/node_modules/moment-duration-format/test/vendor/jquery.js", dependency.getFilePath());
                assertEquals("cc41e74189d44a6169f56655e44ef69d", dependency.getMd5Hash());
                assertEquals("e0e77771c69fe4acd4cad20a0f20ce7a5086dc56", dependency.getSha1Hash());

                Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
                assertEquals(2, evidenceCollected.size());
                for (Evidence evidence : evidenceCollected) {
                    assertFalse(evidence.getSource().isEmpty());
                    assertFalse(evidence.getName().isEmpty());
                    assertFalse(evidence.getValue().isEmpty());
                }

                Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
                assertEquals(2, vulnerabilities.size());
                Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
                Vulnerability vulnerability = (Vulnerability) vulnIterator.next();
                assertEquals("CVE-2015-9251", vulnerability.getName());
                assertEquals("NVD", vulnerability.getSource());
                assertEquals(4.3f, vulnerability.getCvssScore(false), 0.0f);
                assertEquals(6.1f, vulnerability.getCvssScore(), 0.0f);
                assertEquals("MEDIUM", vulnerability.getSeverity());
                assertEquals("MEDIUM", vulnerability.getSeverity(false));
                assertFalse(vulnerability.getCwe().isPresent());
                assertEquals("jQuery before 3.0.0 is vulnerable to Cross-site Scripting (XSS) attacks when a cross-domain Ajax request is performed without the dataType option, causing text/javascript responses to be executed.", vulnerability.getDescription());

                vulnerability = (Vulnerability) vulnIterator.next();
                assertEquals("CVE-2019-11358", vulnerability.getName());
                assertEquals("NVD", vulnerability.getSource());
                assertEquals(4.3f, vulnerability.getCvssScore(false), 0.0f);
                assertEquals(6.1f , vulnerability.getCvssScore(), 0.0f);
                assertEquals("MEDIUM", vulnerability.getSeverity());
                assertFalse(vulnerability.getCwe().isPresent());
                assertEquals("jQuery before 3.4.0, as used in Drupal, Backdrop CMS, and other products, mishandles jQuery.extend(true, {}, ...) because of Object.prototype pollution. If an unsanitized source object contained an enumerable __proto__ property, it could extend the native Object.prototype.", vulnerability.getDescription());
            }
            //check kind-of
            if ( "f9e8418f23f97452410088786d5e0c7a981ced74".equals(dependency.getSha1Hash())){
                assertEquals("index.js", dependency.getFileName());
                assertEquals("project/node_modules/jest-config/node_modules/is-number/node_modules/kind-of/index.js", dependency.getFilePath());
                assertEquals("974e0c2803e83c5bf65de52b98bf4f55", dependency.getMd5Hash());
                assertEquals("f9e8418f23f97452410088786d5e0c7a981ced74", dependency.getSha1Hash());

                Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
                assertEquals(0, evidenceCollected.size());

                Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
                assertEquals(0, vulnerabilities.size());
            }
            //check brace:1.8.5 => NPM
            if ("8082af62f94236e20503e76f0c94b0cf38bf0824".equals(dependency.getSha1Hash())){
                assertEquals("braces:1.8.5", dependency.getFileName());
                assertEquals("project/node_modules/braces/package.json", dependency.getFilePath());
                assertEquals("a5663cea473ad651ac4a00a504883a22", dependency.getMd5Hash());
                assertEquals("8082af62f94236e20503e76f0c94b0cf38bf0824", dependency.getSha1Hash());

                Collection<Evidence> evidenceCollected = dependency.getEvidenceCollected();
                assertEquals(6, evidenceCollected.size());
                for (Evidence evidence : evidenceCollected) {
                    assertFalse(evidence.getSource().isEmpty());
                    assertFalse(evidence.getName().isEmpty());
                    assertFalse(evidence.getValue().isEmpty());
                }

                Collection<Vulnerability> vulnerabilities = dependency.getVulnerabilities();
                assertEquals(1, vulnerabilities.size());
                Iterator<Vulnerability> vulnIterator = vulnerabilities.iterator();
                Vulnerability vulnerability = (Vulnerability) vulnIterator.next();;
                assertEquals("786", vulnerability.getName());
                assertEquals("NPM", vulnerability.getSource());
                assertEquals(4.0f, vulnerability.getCvssScore(false), 0.0f);
                assertEquals(4.0f, vulnerability.getCvssScore(), 0.0f);
                assertEquals("MEDIUM", vulnerability.getSeverity());
                assertEquals("MEDIUM", vulnerability.getSeverity(false));
                assertFalse(vulnerability.getCwe().isPresent());
                assertEquals("Versions of `braces` prior to 2.3.1 are vulnerable to Regular Expression Denial of Service (ReDoS). Untrusted input may cause catastrophic backtracking while matching regular expressions. This can cause the application to be unresponsive leading to Denial of Service.", vulnerability.getDescription());
            }
        }
    }

}
