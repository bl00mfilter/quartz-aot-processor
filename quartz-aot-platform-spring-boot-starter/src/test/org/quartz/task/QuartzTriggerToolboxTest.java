package org.quartz.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TestJob;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.builder.QuartzTriggerBuilder;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuartzTriggerToolboxTest {

    private QuartzTriggerToolbox toolbox;
    private JobDetail jobDetail;

    @BeforeEach
    void setUp() {
        List<QuartzTriggerBuilder<? extends Trigger>> builders = List.of(new StubTriggerBuilder());
        toolbox = new QuartzTriggerToolbox(builders);
        jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity("job1", "group1")
                .build();
    }

    @Test
    void buildsTriggerWithPriorityAndStartTime() {
        QuartzTasksProperties.TaskConfig config = config(Map.of(
                "INTERVAL", "5",
                "PRIORITY", "7",
                "START_TIME", "2027-01-01T00:00:00Z"));

        Trigger trigger = toolbox.build(config, jobDetail);

        assertThat(trigger).isNotNull();
        assertThat(trigger.getJobKey()).isEqualTo(jobDetail.getKey());
        assertThat(trigger.getPriority()).isEqualTo(7);
        assertThat(trigger.getStartTime()).isEqualTo(
                Date.from(ZonedDateTime.parse("2027-01-01T00:00:00Z").toInstant()));
    }

    @Test
    void throwsOnUnknownTriggerType() {
        QuartzTasksProperties.TaskConfig config = config(Map.of());
        config.setTriggerType("unknown");

        assertThatThrownBy(() -> toolbox.build(config, jobDetail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No trigger configurator found");
    }

    @Test
    void throwsOnUnknownPropertyKey() {
        QuartzTasksProperties.TaskConfig config = config(Map.of("WRONG_KEY", "1"));

        assertThatThrownBy(() -> toolbox.build(config, jobDetail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown parameter");
    }

    private QuartzTasksProperties.TaskConfig config(Map<String, String> properties) {
        QuartzTasksProperties.TaskConfig config = new QuartzTasksProperties.TaskConfig();
        config.setJobName("job1");
        config.setJobGroup("group1");
        config.setTriggerType("stub");
        config.setProperties(properties);
        return config;
    }

    private static class StubTriggerBuilder implements QuartzTriggerBuilder<Trigger> {

        @Override
        public String getSupportedType() {
            return "stub";
        }

        @Override
        public Set<String> getValidKeys() {
            return Set.of("INTERVAL", "PRIORITY", "START_TIME");
        }

        @Override
        public void configure(TriggerBuilder<Trigger> builder, Map<String, String> properties) {
            int interval = Integer.parseInt(properties.getOrDefault("INTERVAL", "10"));
            builder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever());
        }
    }
}
