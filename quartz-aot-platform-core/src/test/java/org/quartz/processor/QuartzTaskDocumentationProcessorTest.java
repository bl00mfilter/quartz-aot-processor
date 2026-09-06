package org.quartz.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.junit.jupiter.api.Test;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

class QuartzTaskDocumentationProcessorTest {

    @Test
    void generatesYamlAndReflectConfigForValidJob() throws Exception {
        Compilation compilation = compile(
                jobSource("com.example", "MyJob", "myJob", "myGroup", "cron"));

        assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);

        Optional<JavaFileObject> yaml = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT, "quartz-tasks.yaml");
        assertThat(yaml).isPresent();
        assertThat(yaml.get().getCharContent(true).toString())
                .contains("myJob")
                .contains("myGroup")
                .contains("cron")
                .contains("com.example.MyJob");

        Optional<JavaFileObject> reflect = compilation.generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "META-INF/native-image/org.quartz/quartz-aot/reflect-config.json");
        assertThat(reflect).isPresent();
        assertThat(reflect.get().getCharContent(true).toString())
                .contains("com.example.MyJob");
    }

    @Test
    void generatesYamlWithPropertiesAndDescription() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("com.example.PropJob", """
                package com.example;
                import org.quartz.Job;
                import org.quartz.JobExecutionContext;
                import org.quartz.annotation.QuartzTask;

                @QuartzTask(
                    jobName = "propJob",
                    jobGroup = "g1",
                    typeTrigger = "cron",
                    description = "test description",
                    triggerProperties = @QuartzTask.Property(key = "cronExpression", value = "0 * * * * ?"),
                    jobProperties = @QuartzTask.Property(key = "region", value = "eu")
                )
                public class PropJob implements Job {
                    @Override
                    public void execute(JobExecutionContext context) {}
                }
                """);

        Compilation compilation = compile(source);

        assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);

        String yaml = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, "quartz-tasks.yaml")
                .orElseThrow().getCharContent(true).toString();
        assertThat(yaml)
                .contains("test description")
                .contains("cronExpression")
                .contains("region");
    }

    @Test
    void collectsMultipleJobsIntoSingleYaml() throws Exception {
        Compilation compilation = compile(
                jobSource("com.example", "JobOne", "job1", "g1", "cron"),
                jobSource("com.example", "JobTwo", "job2", "g2", "simple"));

        assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);

        String yaml = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, "quartz-tasks.yaml")
                .orElseThrow().getCharContent(true).toString();
        assertThat(yaml)
                .contains("job1")
                .contains("job2")
                .contains("com.example.JobOne")
                .contains("com.example.JobTwo");
    }

    @Test
    void failsWhenJobNameIsBlank() {
        Compilation compilation = compile(
                jobSource("com.example", "BlankJob", "", "g1", "cron"));

        assertThat(compilation.status()).isEqualTo(Compilation.Status.FAILURE);
        assertThat(compilation.diagnostics())
                .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                        && d.getMessage(null).contains("must not be blank"));
    }

    @Test
    void failsWhenClassDoesNotImplementJob() {
        JavaFileObject source = JavaFileObjects.forSourceString("com.example.NotAJob", """
                package com.example;
                import org.quartz.annotation.QuartzTask;

                @QuartzTask(jobName = "x", jobGroup = "g", typeTrigger = "cron")
                public class NotAJob {
                }
                """);

        Compilation compilation = compile(source);

        assertThat(compilation.status()).isEqualTo(Compilation.Status.FAILURE);
        assertThat(compilation.diagnostics())
                .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                        && d.getMessage(null).contains("must implement org.quartz.Job"));
    }

    private Compilation compile(JavaFileObject... sources) {
        return Compiler.javac()
                .withProcessors(new QuartzTaskDocumentationProcessor())
                .compile(sources);
    }

    private JavaFileObject jobSource(String pkg, String name, String jobName,
                                     String jobGroup, String trigger) {
        return JavaFileObjects.forSourceString(pkg + "." + name, """
                package %s;
                import org.quartz.Job;
                import org.quartz.JobExecutionContext;
                import org.quartz.annotation.QuartzTask;

                @QuartzTask(jobName = "%s", jobGroup = "%s", typeTrigger = "%s")
                public class %s implements Job {
                    @Override
                    public void execute(JobExecutionContext context) {}
                }
                """.formatted(pkg, jobName, jobGroup, trigger, name));
    }
}