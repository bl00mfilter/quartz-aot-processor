package org.quartz.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public final class YamlConfigWriter {

    private final ProcessingEnvironment processingEnv;
    private final Diagnostics diagnostics;

    public YamlConfigWriter(ProcessingEnvironment processingEnv, Diagnostics diagnostics) {
        this.processingEnv = processingEnv;
        this.diagnostics = diagnostics;
    }

    public void write(List<Map<String, Object>> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        try {
            FileObject file = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", "quartz-tasks.yaml");
            try (Writer writer = new BufferedWriter(file.openWriter())) {
                yaml().dump(buildRoot(tasks), writer);
            }
            diagnostics.note("Quartz AOT: generated quartz-tasks.yaml with " + tasks.size() + " task(s)");
        } catch (FilerException e) {
            diagnostics.note("Quartz AOT: quartz-tasks.yaml already created, skipping");
        } catch (IOException e) {
            diagnostics.error("Quartz AOT Generator Error: " + e.getMessage());
        }
    }

    private Yaml yaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setAllowUnicode(true);
        return new Yaml(options);
    }

    private Map<String, Object> buildRoot(List<Map<String, Object>> tasks) {
        Map<String, Object> quartz = new LinkedHashMap<>();
        quartz.put("tasks", tasks);
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("quartz", quartz);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("app", app);
        return root;
    }
}
