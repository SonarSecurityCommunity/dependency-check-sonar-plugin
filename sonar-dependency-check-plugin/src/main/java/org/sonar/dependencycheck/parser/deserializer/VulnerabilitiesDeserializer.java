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
package org.sonar.dependencycheck.parser.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.dependencycheck.parser.element.Vulnerability;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.umd.cs.findbugs.annotations.Nullable;

public class VulnerabilitiesDeserializer extends StdDeserializer<List<Vulnerability>>{


    /**
     *
     */
    private static final long serialVersionUID = -2364903734334590597L;

    protected VulnerabilitiesDeserializer() {
        this(null);
    }

    protected VulnerabilitiesDeserializer(@Nullable Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Vulnerability> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ArrayList<Vulnerability> vulnerabilities = new ArrayList<>();
        // for JSON
        if (JsonToken.START_ARRAY.equals(jsonParser.currentToken())) {
            parseJson(jsonParser, vulnerabilities);
        }
        // For XML
        else if (JsonToken.START_OBJECT.equals(jsonParser.currentToken())) {
            parseXML(jsonParser, vulnerabilities);
        }
        return vulnerabilities;
    }

    private void parseXML(JsonParser jsonParser, List<Vulnerability> vulnerabilities) throws IOException {
        while (!JsonToken.END_OBJECT.equals(jsonParser.nextToken())) {
            if (JsonToken.START_OBJECT.equals(jsonParser.currentToken())) {
                Vulnerability vul = jsonParser.readValueAs(Vulnerability.class);
                // skip suppressedVulnerabilities
                if (StringUtils.equals(jsonParser.getCurrentName(), "vulnerability")) {
                    vulnerabilities.add(vul);
                }
            }
        }
    }

    private void parseJson(JsonParser jsonParser, List<Vulnerability> vulnerabilities) throws IOException {
        while (!JsonToken.END_ARRAY.equals(jsonParser.nextToken())) {
            JsonToken jsonToken = jsonParser.currentToken();
            if (JsonToken.START_OBJECT.equals(jsonToken)) {
                Vulnerability vul = jsonParser.readValueAs(Vulnerability.class);
                vulnerabilities.add(vul);
            }
        }
    }
}
