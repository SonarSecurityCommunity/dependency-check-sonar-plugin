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
package org.sonar.dependencycheck.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.dependencycheck.base.DependencyCheckUtils;
import org.sonar.dependencycheck.parser.element.Analysis;
import org.sonar.dependencycheck.parser.element.Confidence;
import org.sonar.dependencycheck.parser.element.CvssV2;
import org.sonar.dependencycheck.parser.element.CvssV3;
import org.sonar.dependencycheck.parser.element.Dependency;
import org.sonar.dependencycheck.parser.element.Evidence;
import org.sonar.dependencycheck.parser.element.Identifier;
import org.sonar.dependencycheck.parser.element.ProjectInfo;
import org.sonar.dependencycheck.parser.element.ScanInfo;
import org.sonar.dependencycheck.parser.element.Vulnerability;

public class XMLReportParser {

    private static final Logger LOGGER = Loggers.get(XMLReportParser.class);
    private static final String IDENTIFIER_VULNERABILITY_IDS = "vulnerabilityIds";
    private static final String IDENTIFIER_PACKAGE = "package";

    private XMLReportParser() {
        // do nothing
    }

    public static Analysis parse(InputStream inputStream) throws ReportParserException {
        try {
            SMInputFactory inputFactory = DependencyCheckUtils.newStaxParser();
            SMHierarchicCursor rootC = inputFactory.rootElementCursor(inputStream);
            rootC.advance(); // <analysis>
            SMInputCursor childCursor = rootC.childCursor();
            ScanInfo scanInfo = null;
            ProjectInfo projectInfo = null;
            Collection<Dependency> dependencies = Collections.emptyList();
            while (childCursor.getNext() != null) {
                String nodeName = childCursor.getLocalName();
                if ("scanInfo".equals(nodeName)) {
                    scanInfo = processScanInfo(childCursor);
                } else if ("projectInfo".equals(nodeName)) {
                    projectInfo = processProjectInfo(childCursor);
                } else if ("dependencies".equals(nodeName)) {
                    dependencies = processDependencies(childCursor);
                }
            }
            scanInfo = Optional.ofNullable(scanInfo).orElseThrow(() -> new ReportParserException("Analysis - scanInfo not found"));
            return new Analysis(scanInfo, projectInfo, dependencies);
        } catch (XMLStreamException e) {
            throw new ReportParserException("Analysis aborted due to: XML is not valid", e);
        }
    }

    private static Collection<Dependency> processDependencies(SMInputCursor depC) throws XMLStreamException, ReportParserException {
        Collection<Dependency> dependencies = new ArrayList<>();
        SMInputCursor cursor = depC.childElementCursor("dependency");
        while (cursor.getNext() != null) {
            dependencies.add(processDependency(cursor));
        }
        return dependencies;
    }

    private static Dependency processDependency(SMInputCursor depC) throws XMLStreamException, ReportParserException {
        SMInputCursor childCursor = depC.childCursor();
        String fileName = null;
        String filepath = null;
        String md5Hash = null;
        String sha1Hash = null;
        Map<String, List<Evidence>> evidences = Collections.emptyMap();
        Map<String, Collection<Identifier>> identifiers = Collections.emptyMap();
        List<Vulnerability> vulnerabilities = Collections.emptyList();
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if (nodeName == null) {
                continue;
            }
            switch (nodeName) {
                case "fileName":
                    fileName = StringUtils.trim(childCursor.collectDescendantText(false));
                    break;
                case "filePath":
                    filepath = StringUtils.trim(childCursor.collectDescendantText(false));
                    break;
                case "md5":
                    md5Hash = StringUtils.trim(childCursor.collectDescendantText(false));
                    break;
                case "sha1":
                    sha1Hash = StringUtils.trim(childCursor.collectDescendantText(false));
                    break;
                case "evidenceCollected":
                    evidences = processEvidenceCollected(childCursor);
                    break;
                case "vulnerabilities":
                    vulnerabilities = processVulnerabilities(childCursor);
                    break;
                case "identifiers":
                    identifiers = processIdentifiersCollected(childCursor);
                    break;
                default:
                    LOGGER.debug("Depedency Node {} is not used", nodeName);
                    break;
            }
        }
        fileName = Optional.ofNullable(fileName).orElseThrow(() -> new ReportParserException("Dependency - fileName not found"));
        filepath = Optional.ofNullable(filepath).orElseThrow(() -> new ReportParserException("Dependency - filePath not found"));
        md5Hash = Optional.ofNullable(md5Hash).orElseThrow(() -> new ReportParserException("Dependency - md5 not found"));
        sha1Hash = Optional.ofNullable(sha1Hash).orElseThrow(() -> new ReportParserException("Dependency - sha1 not found"));
        return new Dependency(fileName, filepath, md5Hash, sha1Hash, evidences, vulnerabilities, identifiers.get(IDENTIFIER_PACKAGE), identifiers.get(IDENTIFIER_VULNERABILITY_IDS));
    }

    private static List<Vulnerability> processVulnerabilities(SMInputCursor vulnC) throws XMLStreamException, ReportParserException {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        SMInputCursor cursor = vulnC.childElementCursor("vulnerability");
        while (cursor.getNext() != null) {
            vulnerabilities.add(processVulnerability(cursor));
        }
        return vulnerabilities;
    }

    private static Vulnerability processVulnerability(SMInputCursor vulnC) throws XMLStreamException, ReportParserException {
        String name = null;
        String source = null;
        String severity = null;
        String description = null;
        String[] cwes = null;
        CvssV2 cvssV2 = null;
        CvssV3 cvssV3 = null;
        // Attributes
        for (int i = 0; i < vulnC.getAttrCount(); ++i) {
            if (StringUtils.equalsAnyIgnoreCase("source", vulnC.getAttrLocalName(i))) {
                source = StringUtils.trim(vulnC.getAttrValue(i));
            }
        }
        SMInputCursor childCursor = vulnC.childCursor();
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if (nodeName == null) {
                continue;
            }
            switch (nodeName) {
            case "name":
                name = StringUtils.trim(childCursor.collectDescendantText(false));
                break;
            case "cvssV2":
                cvssV2 = processCvssv2(childCursor);
                break;
            case "cvssV3":
                cvssV3 = processCvssv3(childCursor);
                break;
            case "severity":
                severity = StringUtils.trim(childCursor.collectDescendantText(false));
                break;
            case "cwes":
                cwes = processCwes(childCursor);
                break;
            case "description":
                description = StringUtils.trim(childCursor.collectDescendantText(false));
                break;
            default:
                break;
            }
        }
        name = Optional.ofNullable(name).orElseThrow(() -> new ReportParserException("Vulnerability - name not found"));
        source = Optional.ofNullable(source).orElseThrow(() -> new ReportParserException("Vulnerability - source not found"));
        description = Optional.ofNullable(description).orElseThrow(() -> new ReportParserException("Vulnerability - description not found"));
        return new Vulnerability(name, source, description, cwes, cvssV2, cvssV3, severity);
    }

    private static String[] processCwes(SMInputCursor cwesC) throws XMLStreamException {
        List<String> cwes = new ArrayList<>();
        SMInputCursor cursor = cwesC.childElementCursor("cwe");
        while (cursor.getNext() != null) {
            cwes.add(cursor.collectDescendantText(false));
        }
        return cwes.toArray(new String[cwes.size()]);
    }

    private static CvssV3 processCvssv3(SMInputCursor cvssv3C) throws XMLStreamException, ReportParserException {
        SMInputCursor childCursor = cvssv3C.childCursor();
        Float baseScore = null;
        String baseSeverity = null;
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if ("baseScore".equals(nodeName)) {
                String cvssScoreTmp = StringUtils.trim(childCursor.collectDescendantText(false));
                try {
                    baseScore = Float.parseFloat(cvssScoreTmp);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Could not parse CVSSv3-Score {} to Float. Setting CVSS-Score to 0.0", cvssScoreTmp);
                    baseScore = 0.0f;
                }
            } else if ("baseSeverity".equals(nodeName)) {
                baseSeverity = StringUtils.trim(childCursor.collectDescendantText(false));
            }
        }
        baseScore = Optional.ofNullable(baseScore).orElseThrow(() -> new ReportParserException("CvssV3 - baseScore not found"));
        baseSeverity = Optional.ofNullable(baseSeverity).orElseThrow(() -> new ReportParserException("CvssV3 - baseSeverity not found"));
        return new CvssV3(baseScore, baseSeverity);
    }

    private static CvssV2 processCvssv2(SMInputCursor cvssv2C) throws XMLStreamException, ReportParserException {
        SMInputCursor childCursor = cvssv2C.childCursor();
        Float score = null;
        String severity = null;
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if ("score".equals(nodeName)) {
                String cvssScoreTmp = StringUtils.trim(childCursor.collectDescendantText(false));
                try {
                    score = Float.parseFloat(cvssScoreTmp);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Could not parse CVSSv2-Score {} to Float. Setting CVSS-Score to 0.0", cvssScoreTmp);
                    score = 0.0f;
                }
            } else if ("severity".equals(nodeName)) {
                severity = StringUtils.trim(childCursor.collectDescendantText(false));
            }
        }
        score = Optional.ofNullable(score).orElseThrow(() -> new ReportParserException("CvssV2 - score not found"));
        severity = Optional.ofNullable(severity).orElseThrow(() -> new ReportParserException("CvssV2 - severity not found"));
        return new CvssV2(score, severity);
    }

    private static Map<String, List<Evidence>> processEvidenceCollected(SMInputCursor ecC) throws XMLStreamException, ReportParserException {
        Map<String, List<Evidence>> evidences = new HashMap<>();
        SMInputCursor cursor = ecC.childElementCursor("evidence");
        while (cursor.getNext() != null) {
            Evidence evidence = processEvidence(cursor);
            String mapKey = evidence.getType() + "Evidence";
            if (evidences.containsKey(mapKey)) {
                evidences.get(mapKey).add(evidence);
            } else {
                List<Evidence> list = new LinkedList<>();
                list.add(evidence);
                evidences.put(mapKey, list);
            }
        }
        return evidences;
    }

    private static Evidence processEvidence(SMInputCursor ecC) throws XMLStreamException, ReportParserException {
        String type = null;
        Confidence confidence = null;
        for (int i = 0; i < ecC.getAttrCount(); ++i) {
            if (ecC.getAttrLocalName(i).equals("type")) {
                type = ecC.getAttrValue(i);
            }
            if (ecC.getAttrLocalName(i).equals("confidence")) {
                confidence = Confidence.valueOf(ecC.getAttrValue(i));
            }
        }
        SMInputCursor childCursor = ecC.childCursor();
        String source = null;
        String name = null;
        String value = null;
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if ("source".equals(nodeName)) {
                source = StringUtils.trim(childCursor.collectDescendantText(false));
            } else if ("name".equals(nodeName)) {
                name = StringUtils.trim(childCursor.collectDescendantText(false));
            } else if ("value".equals(nodeName)) {
                value = StringUtils.trim(childCursor.collectDescendantText(false));
            }
        }
        source = Optional.ofNullable(source).orElseThrow(() -> new ReportParserException("Evidence - source not found"));
        name = Optional.ofNullable(name).orElseThrow(() -> new ReportParserException("Evidence - name not found"));
        value = Optional.ofNullable(value).orElseThrow(() -> new ReportParserException("Evidence - source not found"));
        type = Optional.ofNullable(type).orElseThrow(() -> new ReportParserException("Evidence - type not found"));
        confidence = Optional.ofNullable(confidence).orElseThrow(() -> new ReportParserException("Evidence - confidence not found"));
        return new Evidence(source, name, value, type, confidence);
    }

    private static Map<String, Collection<Identifier>> processIdentifiersCollected(SMInputCursor ifC) throws XMLStreamException, ReportParserException {
        Map<String, Collection<Identifier>> identifierCollection = new HashMap<>();
        SMInputCursor childCursor = ifC.childCursor();
        while (childCursor.getNext() != null) {
            // identifierType can be one of package, vulnerabilityIds, suppressedVulnerabilityIds
            String identifierType = childCursor.getLocalName();
            if (StringUtils.isNotBlank(identifierType)) {
                if (identifierCollection.containsKey(identifierType)) {
                    identifierCollection.get(identifierType).add(processIdentifiers(childCursor));
                } else {
                    Collection<Identifier> list = new LinkedList<>();
                    list.add(processIdentifiers(childCursor));
                    identifierCollection.put(identifierType, list);
                }
            }
        }
        return identifierCollection;
    }

    private static Identifier processIdentifiers(SMInputCursor ifC) throws XMLStreamException, ReportParserException {
        Confidence confidence = null;
        String id = null;
        for (int i = 0; i < ifC.getAttrCount(); ++i) {
            if ("confidence".equals(ifC.getAttrLocalName(i))) {
                confidence = Confidence.valueOf(ifC.getAttrValue(i));
            }
        }
        SMInputCursor childCursor = ifC.childCursor();
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if ("id".equals(nodeName)) {
                id = StringUtils.trim(childCursor.collectDescendantText(false));
            }
        }
        id = Optional.ofNullable(id).orElseThrow(() -> new ReportParserException("Identifier - id not found"));
        return new Identifier(id, confidence);
    }

    private static ScanInfo processScanInfo(SMInputCursor siC) throws XMLStreamException, ReportParserException {
        SMInputCursor childCursor = siC.childCursor();
        String engineVersion = null;
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if (StringUtils.equalsIgnoreCase("engineVersion", nodeName)) {
                engineVersion = StringUtils.trim(childCursor.collectDescendantText(false));
            }
        }
        engineVersion = Optional.ofNullable(engineVersion).orElseThrow(() -> new ReportParserException("eningeVersion not found"));
        return new ScanInfo(engineVersion);
    }

    private static ProjectInfo processProjectInfo(SMInputCursor piC) throws XMLStreamException, ReportParserException {
        SMInputCursor childCursor = piC.childCursor();
        String projectName = null;
        String projectReportDate = null;
        while (childCursor.getNext() != null) {
            String nodeName = childCursor.getLocalName();
            if (nodeName == null) {
                continue;
            }
            switch (nodeName) {
            case "name":
                projectName = StringUtils.trim(childCursor.collectDescendantText(false));
                break;
            case "reportDate":
                projectReportDate = StringUtils.trim(childCursor.collectDescendantText(false));
                break;
            default:
                break;
            }
        }
        projectName = Optional.ofNullable(projectName).orElseThrow(() -> new ReportParserException("project - name not found"));
        projectReportDate = Optional.ofNullable(projectReportDate).orElseThrow(() -> new ReportParserException("project - reportDate not found"));
        return new ProjectInfo(projectName, projectReportDate);
    }

}
