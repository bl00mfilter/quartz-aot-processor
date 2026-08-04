package org.quartz.processor;
import com.google.auto.service.AutoService;
import org.quartz.annotation.QuartzTask;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import javax.tools.Diagnostic;
import java.io.Writer;

@SupportedAnnotationTypes("org.quartz.annotation.QuartzTask")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class QuartzTaskDocumentationProcessor extends AbstractProcessor {

    private final List<Map<String, Object>> tasks = new ArrayList<>();
    private final Set<String> processedClasses = new HashSet<>();
    private boolean written = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            collectTasks(roundEnv);
        }

        if (roundEnv.processingOver() && !written) {
            writeConfiguration();
            written = true;
        }

        return !annotations.isEmpty();
    }

    private void collectTasks(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(QuartzTask.class)) {
            if (!element.getKind().isClass()) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@QuartzTask can be applied only to classes",
                        element
                );
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            String className = classElement.getQualifiedName().toString();

            if (!processedClasses.add(className)) {
                continue;
            }

            QuartzTask annotation = classElement.getAnnotation(QuartzTask.class);

            if (annotation == null) {
                continue;
            }

            if (isBlank(annotation.jobName()) || isBlank(annotation.jobGroup()) || isBlank(annotation.typeTrigger())) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@QuartzTask jobName, jobGroup and typeTrigger must not be blank",
                        element
                );
                continue;
            }

            Map<String, Object> task = new LinkedHashMap<>();

            task.put("jobName", annotation.jobName());
            task.put("jobGroup", annotation.jobGroup());
            task.put("triggerType", annotation.typeTrigger());
            task.put("description", annotation.description());
            task.put("className", className);

            Map<String, String> triggerProperties = toMap(annotation.triggerProperties());
            if (!triggerProperties.isEmpty()) {
                task.put("properties", triggerProperties);
            }

            Map<String, String> jobData = toMap(annotation.jobProperties());
            if (!jobData.isEmpty()) {
                task.put("jobData", jobData);
            }

            tasks.add(task);
        }
    }

    private Map<String, String> toMap(QuartzTask.Property[] properties) {
        Map<String, String> result = new LinkedHashMap<>();

        if (properties == null) {
            return result;
        }

        for (QuartzTask.Property property : properties) {
            if (property == null || isBlank(property.key())) {
                continue;
            }

            result.put(property.key(), property.value() == null ? "" : property.value());
        }

        return result;
    }

    private void writeConfiguration() {
        if (tasks.isEmpty()) {
            return;
        }

        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            options.setIndent(2);
            options.setAllowUnicode(true);

            Yaml yaml = new Yaml(options);

            Map<String, Object> quartz = new LinkedHashMap<>();
            quartz.put("tasks", tasks);

            Map<String, Object> app = new LinkedHashMap<>();
            app.put("quartz", quartz);

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("app", app);

            FileObject fileObject = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "quartz-tasks.yaml"
            );

            try (Writer writer = new BufferedWriter(fileObject.openWriter())) {
                yaml.dump(root, writer);
            }

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Quartz AOT: generated quartz-tasks.yaml with " + tasks.size() + " task(s)"
            );

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Quartz AOT Generator Error: " + e.getMessage()
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}