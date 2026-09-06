package org.quartz.processor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.quartz.annotation.QuartzTask;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TaskCollector {

    private static final String JOB_TYPE = "org.quartz.Job";

    private final ProcessingEnvironment processingEnv;
    private final Diagnostics diagnostics;
    private final Set<String> processedClasses;
    private final List<Map<String, Object>> tasks;

    

    public void collect(RoundEnvironment roundEnv) {
        TypeMirror jobType = resolveJobType();
        if (jobType != null) {
            roundEnv.getElementsAnnotatedWith(QuartzTask.class)
                    .forEach(element -> collectElement(element, jobType));
        }
    }

    private TypeMirror resolveJobType() {
        TypeElement jobElement = processingEnv.getElementUtils().getTypeElement(JOB_TYPE);
        if (jobElement == null) {
            diagnostics.error("org.quartz.Job not found on classpath. Add the quartz dependency.");
            return null;
        }
        return jobElement.asType();
    }

    private void collectElement(Element element, TypeMirror jobType) {
        if (element.getKind() != ElementKind.CLASS) {
            diagnostics.error("@QuartzTask can be applied only to classes", element);
            return;
        }
        TypeElement classElement = (TypeElement) element;
        if (processedClasses.add(classElement.getQualifiedName().toString())
                && isJobImplementation(classElement, jobType)
                && validate(classElement, element)) {
            tasks.add(toTaskMap(classElement, classElement.getAnnotation(QuartzTask.class)));
        }
    }

    private boolean isJobImplementation(TypeElement classElement, TypeMirror jobType) {
        boolean assignable = processingEnv.getTypeUtils().isAssignable(classElement.asType(), jobType);
        if (!assignable) {
            diagnostics.error("@QuartzTask class must implement org.quartz.Job", classElement);
        }
        return assignable;
    }

    private boolean validate(TypeElement classElement, Element element) {
        QuartzTask annotation = classElement.getAnnotation(QuartzTask.class);
        boolean valid = annotation != null
                && !isBlank(annotation.jobName())
                && !isBlank(annotation.jobGroup())
                && !isBlank(annotation.typeTrigger());
        if (!valid) {
            diagnostics.error("@QuartzTask jobName, jobGroup and typeTrigger must not be blank", element);
        }
        return valid;
    }

    private Map<String, Object> toTaskMap(TypeElement classElement, QuartzTask annotation) {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("jobName", annotation.jobName());
        task.put("jobGroup", annotation.jobGroup());
        task.put("triggerType", annotation.typeTrigger());
        task.put("description", annotation.description());
        task.put("className", classElement.getQualifiedName().toString());
        putIfNotEmpty(task, "properties", toMap(annotation.triggerProperties()));
        putIfNotEmpty(task, "jobData", toMap(annotation.jobProperties()));
        return task;
    }

    private void putIfNotEmpty(Map<String, Object> task, String key, Map<String, String> value) {
        if (!value.isEmpty()) {
            task.put(key, value);
        }
    }

    private Map<String, String> toMap(QuartzTask.Property[] properties) {
        Map<String, String> result = new LinkedHashMap<>();
        for (QuartzTask.Property property : properties) {
            if (property != null && !isBlank(property.key())) {
                result.put(property.key(), property.value() == null ? "" : property.value());
            }
        }
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
