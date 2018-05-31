/*
 * Dependency-Check Plugin for SonarQube
 * Copyright (C) 2015-2017 Steve Springett
 * steve.springett@owasp.org
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

import org.codehaus.staxmate.SMInputFactory;
import org.sonar.api.batch.rule.Severity;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;

public final class DependencyCheckUtils {

	private DependencyCheckUtils() {
	}

	public static SMInputFactory newStaxParser()
		throws FactoryConfigurationError {
		XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
		xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
		xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
		xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		return new SMInputFactory(xmlFactory);
	}

	public static Severity cvssToSonarQubeSeverity(String cvssScore, Double blocker, Double critical, Double major) {
		double score = Double.parseDouble(cvssScore);
		if (blocker.doubleValue() > 0 && score >= blocker.doubleValue()) {
			return Severity.BLOCKER;
		}
		if (critical.doubleValue() > 0 && score >= critical.doubleValue()) {
			return Severity.CRITICAL;
		} else if (major.doubleValue() > 0 && score >= major.doubleValue()) {
			return Severity.MAJOR;
		} else {
			return Severity.MINOR;
		}
	}

}
