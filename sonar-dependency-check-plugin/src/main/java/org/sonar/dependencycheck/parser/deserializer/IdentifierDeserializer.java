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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sonar.dependencycheck.parser.element.Identifier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.umd.cs.findbugs.annotations.Nullable;

public class IdentifierDeserializer extends StdDeserializer<Map<String, List<Identifier>>>{

    /**
     *
     */
    private static final long serialVersionUID = 4098037817805079428L;

    protected IdentifierDeserializer() {
        this(null);
    }

    protected IdentifierDeserializer(@Nullable Class<?> vc) {
        super(vc);
    }

    @Override
    public Map<String, List<Identifier>> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Map<String, List<Identifier>> identifiers = new HashMap<>();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            JsonToken jsonToken = jsonParser.currentToken();
            if (JsonToken.START_OBJECT.equals(jsonToken)) {
                String currentName = jsonParser.getCurrentName();
                Identifier identifier = jsonParser.readValueAs(Identifier.class);
                if (identifiers.containsKey(currentName)) {
                    identifiers.get(currentName).add(identifier);
                } else {
                    List<Identifier> list = new LinkedList<>();
                    list.add(identifier);
                    identifiers.put(currentName, list);
                }
            }
        }
        return identifiers;
    }
}
