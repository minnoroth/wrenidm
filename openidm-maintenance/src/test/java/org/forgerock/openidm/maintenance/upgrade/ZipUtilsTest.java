/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2023-2024 Wren Security. All rights reserved.
 */
package org.forgerock.openidm.maintenance.upgrade;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.testng.annotations.Test;

public class ZipUtilsTest {

    @Test
    public void testUnzipFile() throws IOException {
        Path workDir = Files.createTempDirectory("zipUtilsTest");

        // Prepare test files
        Path sourceDir = Files.createDirectory(workDir.resolve("source"));
        Path rootFilename = Paths.get("root.txt");
        Files.write(sourceDir.resolve(rootFilename), "Root zip file".getBytes());
        Path nestedFilename = Paths.get("folder/nested.txt");
        Files.createDirectories(sourceDir.resolve(nestedFilename).getParent());
        Files.write(sourceDir.resolve(nestedFilename), "Nested zip file".getBytes());
        Path zipFile = workDir.resolve("test.zip");
        createZipArchive(zipFile, sourceDir);
        Path targetDir = Files.createDirectory(workDir.resolve("target"));

        // Check extraction of all files
        ZipUtils.unzipFile(zipFile, targetDir);
        assertTrue(Files.exists(targetDir.resolve(rootFilename)));
        assertTrue(Files.exists(targetDir.resolve(nestedFilename)));
        assertEquals(Files.readString(targetDir.resolve(rootFilename)),
                Files.readString(sourceDir.resolve(rootFilename)));
        assertEquals(Files.readString(targetDir.resolve(nestedFilename)),
                Files.readString(sourceDir.resolve(nestedFilename)));

        // Check extraction of a specific file
        cleanDirectory(targetDir);
        ZipUtils.unzipFile(zipFile, FileSystems.getDefault().getPathMatcher("glob:folder/nested.txt"), targetDir);
        assertFalse(Files.exists(targetDir.resolve(rootFilename)));
        assertTrue(Files.exists(targetDir.resolve(nestedFilename)));
        assertEquals(Files.readString(targetDir.resolve(nestedFilename)),
                Files.readString(sourceDir.resolve(nestedFilename)));
    }

    private void cleanDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .filter(Predicate.not(path::equals))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private void createZipArchive(Path targetFile, Path sourceFolder) {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(targetFile)))) {
            Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!Files.isDirectory(file)) {
                        zos.putNextEntry(new ZipEntry(sourceFolder.relativize(file).toString()));
                        Files.copy(file, zos);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            zos.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create zip archive.", e);
        }
    }

}
