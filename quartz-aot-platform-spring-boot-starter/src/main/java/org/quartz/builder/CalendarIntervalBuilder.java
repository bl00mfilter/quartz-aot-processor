package org.quartz.builder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CalendarIntervalTrigger;
import org.quartz.DateBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.properties.CalendarTriggerProperties;
import org.quartz.type.BaseTriggerType;
import org.springframework.stereotype.Component;

@Component
public class CalendarIntervalBuilder implements QuartzTriggerBuilder<CalendarIntervalTrigger> {

    @Override
    public String getSupportedType() {
        return BaseTriggerType.CALENDAR.name();
    }

    @Override
    public Set<String> getValidKeys() {
        return Arrays.stream(CalendarTriggerProperties.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public void configure(TriggerBuilder<CalendarIntervalTrigger> builder, Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Trigger properties map for CALENDAR_INTERVAL cannot be empty or null");
        }

        int interval = properties.containsKey(CalendarTriggerProperties.INTERVAL.name())
                ? Integer.parseInt(properties.get(CalendarTriggerProperties.INTERVAL.name())) : 1;

        String unitStr = properties.get(CalendarTriggerProperties.INTERVAL_UNIT.name());
        DateBuilder.IntervalUnit quartzUnit = DateBuilder.IntervalUnit.MONTH;
        if (unitStr != null && !unitStr.isBlank()) {
            CalendarTriggerProperties.IntervalUnit customUnit = CalendarTriggerProperties.IntervalUnit.valueOf(unitStr.toUpperCase());
            quartzUnit = DateBuilder.IntervalUnit.valueOf(customUnit.name());
        }

        CalendarIntervalScheduleBuilder scheduleBuilder = CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                .withInterval(interval, quartzUnit);

        String preserveHourStr = properties.get(CalendarTriggerProperties.PRESERVE_HOUR_DST.name());
        if (preserveHourStr != null && Boolean.parseBoolean(preserveHourStr)) {
            scheduleBuilder.preserveHourOfDayAcrossDaylightSavings(Boolean.parseBoolean(preserveHourStr));
        }
        String skipDayStr = properties.get(CalendarTriggerProperties.SKIP_DAY_DST_OVERLAP.name());
        if (skipDayStr != null && Boolean.parseBoolean(skipDayStr)) {
            scheduleBuilder.skipDayIfHourDoesNotExist(Boolean.parseBoolean(skipDayStr));
        }

        String tzStr = properties.get(CalendarTriggerProperties.TIME_ZONE.name());
        if (tzStr != null && !tzStr.isBlank()) {
            scheduleBuilder.inTimeZone(TimeZone.getTimeZone(tzStr));
        }

        String misfireStr = properties.get(CalendarTriggerProperties.MISFIRE_STRATEGY.name());
        if (misfireStr != null && !misfireStr.isBlank()) {
            CalendarTriggerProperties.MisfireStrategy strategy = CalendarTriggerProperties.MisfireStrategy.valueOf(misfireStr.toUpperCase());
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

        String startStr = properties.get(CalendarTriggerProperties.START_TIME.name());
        if (startStr != null && !startStr.isBlank()) {
            builder.startAt(Date.from(ZonedDateTime.parse(startStr).toInstant()));
        }
        String endStr = properties.get(CalendarTriggerProperties.END_TIME.name());
        if (endStr != null && !endStr.isBlank()) {
            builder.endAt(Date.from(ZonedDateTime.parse(endStr).toInstant()));
        }

        String calName = properties.get(CalendarTriggerProperties.CALENDAR_NAME.name());
        if (calName != null && !calName.isBlank()) {
            builder.modifiedByCalendar(calName);
        }
    }
}
