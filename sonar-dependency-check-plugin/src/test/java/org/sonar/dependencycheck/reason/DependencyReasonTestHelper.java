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
package org.sonar.dependencycheck.reason;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

public abstract class DependencyReasonTestHelper {

    private static final File TEST_DIR = new File("src/test/resources/reason");
    public static final int LINE_NOT_FOUND = 1;

    protected DefaultInputFile inputFile(String fileName) throws IOException {
        File file = new File(TEST_DIR, fileName);
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        return TestInputFileBuilder.create("key", fileName).setModuleBaseDir(Paths.get(TEST_DIR.getAbsolutePath()))
                .setType(InputFile.Type.MAIN).setLanguage("mytest").setCharset(StandardCharsets.UTF_8)
                .initMetadata(content).build();
    }
}
