package org.quartz.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public final class ReflectConfigWriter {

    private static final String RESOURCE =
            "META-INF/native-image/org.quartz/quartz-aot/reflect-config.json";

    private final ProcessingEnvironment processingEnv;
    private final Diagnostics diagnostics;

    public ReflectConfigWriter(ProcessingEnvironment processingEnv, Diagnostics diagnostics) {
        this.processingEnv = processingEnv;
        this.diagnostics = diagnostics;
    }

    public void write(List<Map<String, Object>> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        try {
            FileObject file = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE);
            try (Writer writer = new BufferedWriter(file.openWriter())) {
                writer.write(toJson(tasks));
            }
            diagnostics.note("Quartz AOT: generated reflect-config.json for " + tasks.size() + " job class(es)");
        } catch (FilerException e) {
            diagnostics.note("Quartz AOT: reflect-config.json already created, skipping");
        } catch (IOException e) {
            diagnostics.error("Quartz AOT Generator Error (reflect-config): " + e.getMessage());
        }
    }

    private String toJson(List<Map<String, Object>> tasks) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            appendEntry(json, (String) tasks.get(i).get("className"), i == tasks.size() - 1);
        }
        return json.append("]\n").toString();
    }

    private void appendEntry(StringBuilder json, String className, boolean last) {
        json.append("  {\n")
            .append("    \"name\": \"").append(className).append("\",\n")
            .append("    \"allDeclaredConstructors\": true,\n")
            .append("    \"allPublicConstructors\": true,\n")
            .append("    \"allDeclaredMethods\": true,\n")
            .append("    \"allPublicMethods\": true\n")
            .append(last ? "  }\n" : "  },\n");
    }
}