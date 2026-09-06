package org.quartz.processor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes("org.quartz.annotation.QuartzTask")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class QuartzTaskDocumentationProcessor extends AbstractProcessor {

    private final List<Map<String, Object>> tasks = new ArrayList<>();
    private final Set<String> processedClasses = new HashSet<>();
    private final Diagnostics diagnostics = new Diagnostics();
    private boolean artifactsWritten;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        diagnostics.init(processingEnv.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            new TaskCollector(processingEnv, diagnostics, processedClasses, tasks)
                    .collect(roundEnv);
        }

        if (roundEnv.processingOver() && !artifactsWritten) {
            new YamlConfigWriter(processingEnv, diagnostics).write(tasks);
            new ReflectConfigWriter(processingEnv, diagnostics).write(tasks);
            artifactsWritten = true;
        }

        return !annotations.isEmpty();
    }
}