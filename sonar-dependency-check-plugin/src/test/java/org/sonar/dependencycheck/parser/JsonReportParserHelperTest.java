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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.sonar.dependencycheck.parser.element.Analysis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

class JsonReportParserHelperTest extends ReportParserTest {

    @Override
    public Analysis parseReport(String dir) throws Exception {
        Instant startTime = Instant.now();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dir + "/dependency-check-report.json");
        Analysis analysis = JsonReportParserHelper.parse(inputStream);
        assertNotNull(analysis);
        Instant endTime = Instant.now();
        System.out.println("Duration JSON-Report-Parser: " + Duration.between(startTime, endTime));
        return analysis;
    }

    @Test
    void parseReportJsonParseException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(JsonParseException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> JsonReportParserHelper.parse(inputStream), "No JsonParseException thrown");
        assertEquals("Could not parse JSON-Report", exception.getMessage());
    }

    @Test
    void parseReportJsonMappingException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(JsonMappingException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> JsonReportParserHelper.parse(inputStream), "No JsonMappingException thrown");
        assertEquals("Problem with JSON-Report-Mapping", exception.getMessage());
    }

    @Test
    void parseReportJsonIOException() {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream);
        ReportParserException exception = assertThrows(ReportParserException.class, () -> JsonReportParserHelper.parse(inputStream), "No IOException thrown");
        assertEquals("IO Problem with JSON-Report", exception.getMessage());
    }
}
