package org.quartz.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.task.QuartzTasksProperties;
import org.quartz.task.QuartzTriggerToolbox;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskOrchestratorFactory {
    private final Scheduler scheduler;
    private final QuartzTasksProperties tasksProperties;
    private final QuartzTriggerToolbox toolbox;

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws SchedulerException {
        if (tasksProperties.getTasks() == null) {
            return;
        }
        for (QuartzTasksProperties.TaskConfig config : tasksProperties.getTasks()) {
            try {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(
                        config.getClassName(),
                        true,
                        contextClassLoader != null ? contextClassLoader : TaskOrchestratorFactory.class.getClassLoader()
                );

                JobKey jobKey = JobKey.jobKey(config.getJobName(), config.getJobGroup());
                JobDataMap jobDataMap = new JobDataMap(config.getJobData() != null ? config.getJobData() : Map.of());

                JobDetail jobDetail = JobBuilder.newJob(jobClass)
                        .withIdentity(jobKey)
                        .withDescription(config.getDescription())
                        .usingJobData(jobDataMap)
                        .storeDurably()
                        .build();

                Trigger customTrigger = toolbox.build(config, jobDetail);

                if (!scheduler.checkExists(jobKey)) {
                    scheduler.scheduleJob(jobDetail, customTrigger);
                    log.info("[QUARTZ_CLUSTER] Task [{}] successfully registered in group [{}] on PostgreSQL database storage.",
                            config.getJobName(), config.getJobGroup());
                } else {
                    TriggerKey triggerKey = TriggerKey.triggerKey(config.getJobName() + "_Trigger", config.getJobGroup());
                    scheduler.rescheduleJob(triggerKey, customTrigger);
                    log.info("[QUARTZ_CLUSTER] Task [{}] execution schedule updated dynamically from AOT configurations.", config.getJobName());
                }

            } catch (ClassNotFoundException e) {
                log.error("[QUARTZ_CLUSTER_CRITICAL] Failed to resolve target compiled Java class: {}", config.getClassName(), e);
                throw new IllegalStateException("Cluster stabilization aborted due to missing infrastructure dependencies", e);
            }
        }
        log.info("[QUARTZ_CLUSTER] Active synchronization layer stabilized. Total active workers deployed: {}", tasksProperties.getTasks().size());
    }
}
