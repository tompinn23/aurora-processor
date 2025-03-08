package org.yonside.aurora.generator;

import org.junit.jupiter.api.Test;
import org.yonside.aurora.generator.ServiceProcessor;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessorTest {

    @Test
    void test() throws IOException {
        final String source = Paths.get("src/test/java/org/yonside/aurora/generator/model").toAbsolutePath().toString();

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        fileManager.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

        final Set<JavaFileObject.Kind> fileKinds = Collections.singleton(JavaFileObject.Kind.SOURCE);

        final Iterable<JavaFileObject> files = fileManager.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);

        final JavaCompiler.CompilationTask task =
                compiler.getTask(
                        new PrintWriter(System.out),
                        null,
                        null,
                        List.of("--release=" + Integer.getInteger("java.specification.version")),
                        null,
                        files);
        task.setProcessors(List.of(new ServiceProcessor()));

        assertTrue(task.call());
    }

}
