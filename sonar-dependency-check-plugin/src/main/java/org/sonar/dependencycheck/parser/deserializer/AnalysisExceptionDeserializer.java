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
import org.sonar.dependencycheck.parser.element.AnalysisException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.umd.cs.findbugs.annotations.Nullable;

public class AnalysisExceptionDeserializer extends StdDeserializer<List<AnalysisException>> {

    /**
     *
     */
    private static final long serialVersionUID = -6223608705188322988L;

    protected AnalysisExceptionDeserializer() {
        this(null);
    }

    protected AnalysisExceptionDeserializer(@Nullable Class<?> vc) {
        super(vc);
    }

    @Override
    public List<AnalysisException> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ArrayList<AnalysisException> exceptions = new ArrayList<>();
        // For JSON
        if (JsonToken.START_ARRAY.equals(jsonParser.getCurrentToken())) {
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonToken jsonToken = jsonParser.currentToken();
                if (JsonToken.START_OBJECT.equals(jsonToken) && StringUtils.isNotBlank(jsonParser.getCurrentName())) {
                    AnalysisException ex = jsonParser.readValueAs(AnalysisException.class);
                    exceptions.add(ex);
                }
            }
        }
        // For XML
        if (JsonToken.START_OBJECT.equals(jsonParser.getCurrentToken())) {
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                if (JsonToken.START_OBJECT.equals(jsonParser.getCurrentToken())) {
                    AnalysisException ex = jsonParser.readValueAs(AnalysisException.class);
                    exceptions.add(ex);
                }
            }
        }
        return exceptions;
    }
}
