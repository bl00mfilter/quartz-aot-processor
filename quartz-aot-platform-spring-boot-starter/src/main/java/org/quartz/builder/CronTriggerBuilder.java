package org.quartz.builder;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.properties.CronTriggerProperties;
import org.quartz.type.BaseTriggerType;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CronTriggerBuilder implements QuartzTriggerBuilder<CronTrigger> {

    @Override
    public String getSupportedType() {
        return BaseTriggerType.CRON.name();
    }

    @Override
    public Set<String> getValidKeys() {
        return Arrays.stream(CronTriggerProperties.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public void configure(TriggerBuilder<CronTrigger> builder, Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Trigger properties map for CRON cannot be empty or null");
        }

        String cronExpr = properties.get(CronTriggerProperties.EXPRESSION.name());
        if (cronExpr == null || cronExpr.isBlank()) {
            throw new IllegalStateException("Missing mandatory trigger configuration property: " + CronTriggerProperties.EXPRESSION.name());
        }
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpr);

        String tzStr = properties.get(CronTriggerProperties.TIME_ZONE.name());
        if (tzStr != null && !tzStr.isBlank()) {
            scheduleBuilder.inTimeZone(TimeZone.getTimeZone(tzStr));
        }

        String misfireStr = properties.get(CronTriggerProperties.MISFIRE_STRATEGY.name());
        if (misfireStr != null && !misfireStr.isBlank()) {
            CronTriggerProperties.MisfireStrategy strategy = CronTriggerProperties.MisfireStrategy.valueOf(misfireStr.toUpperCase());
            switch (strategy) {
                case DO_NOTHING:
                    scheduleBuilder.withMisfireHandlingInstructionDoNothing();
                    break;
                case FIRE_AND_PROCEED:
                case SMART_POLICY:
                default:
                    scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                    break;
            }
        }
        builder.withSchedule(scheduleBuilder);

        String startStr = properties.get(CronTriggerProperties.START_TIME.name());
        if (startStr != null && !startStr.isBlank()) {
            builder.startAt(Date.from(ZonedDateTime.parse(startStr).toInstant()));
        }
        String endStr = properties.get(CronTriggerProperties.END_TIME.name());
        if (endStr != null && !endStr.isBlank()) {
            builder.endAt(Date.from(ZonedDateTime.parse(endStr).toInstant()));
        }

        String calName = properties.get(CronTriggerProperties.CALENDAR_NAME.name());
        if (calName != null && !calName.isBlank()) {
            builder.modifiedByCalendar(calName);
        }
    }
}
