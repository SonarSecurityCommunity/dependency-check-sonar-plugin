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

import java.io.IOException;
import java.io.InputStream;

import org.sonar.dependencycheck.reason.maven.MavenPomModel;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class PomParserHelper {

    private PomParserHelper() {
        // Do nothing
    }

    public static MavenPomModel parse(InputStream pom) throws ReportParserException {
        ObjectMapper mapper = new XmlMapper();
        try {
            return mapper.readValue(pom, MavenPomModel.class);
        } catch (JsonParseException e) {
            throw new ReportParserException("Could not parse pom.xml", e);
        } catch (JsonMappingException e) {
            throw new ReportParserException("Problem with pom.xml-Mapping", e);
        } catch (IOException e) {
            throw new ReportParserException("IO Problem with pom.xml", e);
        }
    }
}
