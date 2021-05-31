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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sonar.dependencycheck.parser.element.Evidence;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import edu.umd.cs.findbugs.annotations.Nullable;

public class EvidenceDeserializer extends StdDeserializer<Map<String, List<Evidence>>>{

    /**
     *
     */
    private static final long serialVersionUID = 4098037817805079428L;

    protected EvidenceDeserializer() {
        this(null);
    }

    protected EvidenceDeserializer(@Nullable Class<?> vc) {
        super(vc);
    }

    @Override
    public Map<String, List<Evidence>> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        List<Evidence> evidences = new ArrayList<>();
        // empty evidenceCollected in XML
        if (StringUtils.equals(jsonParser.getCurrentName(), "evidenceCollected") && JsonToken.VALUE_STRING.equals(jsonParser.getCurrentToken())) {
            return buildFinalEvidences(evidences);
        }
        while (!JsonToken.END_OBJECT.equals(jsonParser.nextToken())) {
            JsonToken jsonToken = jsonParser.currentToken();
            // For JSON
            if (JsonToken.START_ARRAY.equals(jsonToken)) {
                parseJson(jsonParser, evidences);
            }
            // For XML
            else if(JsonToken.START_OBJECT.equals(jsonToken)){
                String fieldName = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("evidence", fieldName)) {
                    evidences.add(jsonParser.readValueAs(Evidence.class));
                }
            }
        }
        return buildFinalEvidences(evidences);
    }

    private void parseJson(JsonParser jsonParser, List<Evidence> evidences) throws IOException {
        String fieldName = jsonParser.getCurrentName();
        if (StringUtils.equalsAnyIgnoreCase(fieldName, "vendorEvidence", "productEvidence", "versionEvidence")) {
            while (!JsonToken.END_ARRAY.equals(jsonParser.nextToken())) {
                Evidence ev = jsonParser.readValueAs(Evidence.class);
                evidences.add(ev);
            }
        }
    }

    private Map<String, List<Evidence>> buildFinalEvidences(List<Evidence> evidences) {
        Map<String, List<Evidence>> evidencesMap = new HashMap<>();
        for (Evidence evidence : evidences) {
            String mapKey = evidence.getType() + "Evidence";
            if (evidencesMap.containsKey(mapKey)) {
                evidencesMap.get(mapKey).add(evidence);
            } else {
                List<Evidence> list = new LinkedList<>();
                list.add(evidence);
                evidencesMap.put(mapKey, list);
            }
        }
        return evidencesMap;
    }
}
