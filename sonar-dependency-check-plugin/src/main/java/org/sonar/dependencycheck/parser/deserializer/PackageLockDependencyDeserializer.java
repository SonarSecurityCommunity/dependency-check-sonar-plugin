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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.dependencycheck.reason.npm.NPMDependency;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.umd.cs.findbugs.annotations.Nullable;

public class PackageLockDependencyDeserializer extends StdDeserializer<List<NPMDependency>> {
    /**
     *
     */
    private static final long serialVersionUID = -1856893963064763155L;

    protected PackageLockDependencyDeserializer() {
        this(null);
    }

    protected PackageLockDependencyDeserializer(@Nullable Class<?> vc) {
        super(vc);
    }

    @Override
    public List<NPMDependency> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        List<NPMDependency> npmDependencies = new LinkedList<>();
        while (!JsonToken.END_OBJECT.equals(jsonParser.nextToken())) {
            if (JsonToken.START_OBJECT.equals(jsonParser.currentToken())) {
                String name = jsonParser.getCurrentName();
                int startLineNr = jsonParser.getCurrentLocation().getLineNr();
                String version = scanWholeDependencyForVersion(jsonParser);
                int endLineNr = jsonParser.getCurrentLocation().getLineNr();
                npmDependencies.add(new NPMDependency(name, version, startLineNr, endLineNr));
            }
        }
        return npmDependencies;
    }

    private String scanWholeDependencyForVersion(JsonParser jsonParser) throws IOException {
        String version = "";
        int depth = 0;
        while (!JsonToken.END_OBJECT.equals(jsonParser.nextToken()) || depth > 0) {
            // Check for version only at first level
            if (StringUtils.equalsIgnoreCase("version", jsonParser.getCurrentName()) && depth == 0) {
                version = jsonParser.getValueAsString();
            }
            if (JsonToken.END_OBJECT.equals(jsonParser.getCurrentToken())) {
                --depth;
            }
            if (JsonToken.START_OBJECT.equals(jsonParser.getCurrentToken())) {
                ++depth;
            }
        }
        return version;
    }

}
