package org.quartz.task;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.builder.QuartzTriggerBuilder;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuartzTriggerToolbox {

    private final Map<String, QuartzTriggerBuilder<? extends Trigger>> registry;

    public QuartzTriggerToolbox(List<QuartzTriggerBuilder<? extends Trigger>> configurators) {
        this.registry = configurators.stream()
                .collect(Collectors.toMap(QuartzTriggerBuilder::getSupportedType, c -> c));
    }

    @SuppressWarnings("unchecked")
    public Trigger build(QuartzTasksProperties.TaskConfig config, JobDetail jobDetail) {

        QuartzTriggerBuilder<Trigger> configurator =
                (QuartzTriggerBuilder<Trigger>) registry.get(config.getTriggerType());

        if (configurator == null) {
            throw new IllegalArgumentException("No trigger configurator found in toolbox registry for type: "
                    + config.getTriggerType());
        }

        Map<String, String> rawProps = config.getProperties();

        if (rawProps != null && !rawProps.isEmpty()) {
            Set<String> validKeys = configurator.getValidKeys();

            for (String yamlKey : rawProps.keySet()) {
                if (!validKeys.contains(yamlKey.toUpperCase())) {
                    throw new IllegalStateException(String.format(
                            "AOT validation failed! Unknown parameter [%s] provided for trigger type [%s]. Expected keys: %s",
                            yamlKey, config.getTriggerType(), validKeys
                    ));
                }
            }
        }

        TriggerBuilder<Trigger> baseBuilder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(config.getJobName() + "_Trigger", config.getJobGroup());

        if (rawProps != null && !rawProps.isEmpty()) {
            String priorityVal = rawProps.get("PRIORITY");
            if (priorityVal != null && !priorityVal.isBlank()) {
                baseBuilder.withPriority(Integer.parseInt(priorityVal));
            }

            String startTimeVal = rawProps.get("START_TIME");
            if (startTimeVal != null && !startTimeVal.isBlank()) {
                baseBuilder.startAt(Date.from(ZonedDateTime.parse(startTimeVal).toInstant()));
            }

            String endTimeVal = rawProps.get("END_TIME");
            if (endTimeVal != null && !endTimeVal.isBlank()) {
                baseBuilder.endAt(Date.from(ZonedDateTime.parse(endTimeVal).toInstant()));
            }
        }

        configurator.configure(baseBuilder, rawProps);

        return baseBuilder.build();
    }
}
