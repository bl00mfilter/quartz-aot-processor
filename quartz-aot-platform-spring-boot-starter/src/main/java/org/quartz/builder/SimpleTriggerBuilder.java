package org.quartz.builder;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.properties.SimpleTriggerProperties;
import org.quartz.type.BaseTriggerType;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SimpleTriggerBuilder implements QuartzTriggerBuilder<SimpleTrigger> {
    @Override
    public String getSupportedType() {
        return BaseTriggerType.SIMPLE.name();
    }

    @Override
    public Set<String> getValidKeys() {
        return Arrays.stream(SimpleTriggerProperties.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public void configure(TriggerBuilder<SimpleTrigger> builder, Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Trigger properties map for SIMPLE cannot be empty or null");
        }

        String intervalStr = properties.get(SimpleTriggerProperties.INTERVAL_MS.name());
        long interval = intervalStr != null ? Long.parseLong(intervalStr) : 1000L;
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval);

        String repeatStr = properties.get(SimpleTriggerProperties.REPEAT_COUNT.name());
        int repeats = repeatStr != null ? Integer.parseInt(repeatStr) : -1;
        if (repeats == -1) {
            scheduleBuilder.repeatForever();
        } else {
            scheduleBuilder.withRepeatCount(repeats);
        }

        String misfireStr = properties.get(SimpleTriggerProperties.MISFIRE_STRATEGY.name());
        if (misfireStr != null && !misfireStr.isBlank()) {
            SimpleTriggerProperties.MisfireStrategy strategy = SimpleTriggerProperties.MisfireStrategy.valueOf(misfireStr.toUpperCase());
            switch (strategy) {
                case FIRE_NOW:
                    scheduleBuilder.withMisfireHandlingInstructionFireNow();
                    break;
                case RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT:
                    scheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount();
                    break;
                case RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT:
                    scheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
                    break;
                case RESCHEDULE_NEXT_WITH_EXISTING_COUNT:
                    scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
                    break;
                case SMART_POLICY:
                default:
                    break;
            }
        }
        builder.withSchedule(scheduleBuilder);

        String startStr = properties.get(SimpleTriggerProperties.START_TIME.name());
        if (startStr != null && !startStr.isBlank()) {
            builder.startAt(Date.from(ZonedDateTime.parse(startStr).toInstant()));
        }
        String endStr = properties.get(SimpleTriggerProperties.END_TIME.name());
        if (endStr != null && !endStr.isBlank()) {
            builder.endAt(Date.from(ZonedDateTime.parse(endStr).toInstant()));
        }

        String calName = properties.get(SimpleTriggerProperties.CALENDAR_NAME.name());
        if (calName != null && !calName.isBlank()) {
            builder.modifiedByCalendar(calName);
        }
    }
}
