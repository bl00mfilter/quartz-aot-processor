package org.quartz.configurator;

import org.quartz.builder.CalendarIntervalBuilder;
import org.quartz.builder.CronTriggerBuilder;
import org.quartz.builder.DailyTimeIntervalBuilder;
import org.quartz.builder.SimpleTriggerBuilder;
import org.quartz.factory.TaskOrchestratorFactory;
import org.quartz.task.QuartzTasksProperties;
import org.quartz.task.QuartzTriggerToolbox;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(QuartzTasksProperties.class)
@Import({
        TaskOrchestratorFactory.class,
        QuartzTriggerToolbox.class,
        CronTriggerBuilder.class,
        SimpleTriggerBuilder.class,
        CalendarIntervalBuilder.class,
        DailyTimeIntervalBuilder.class
})
public class QuartzAotAutoConfiguration {
}
