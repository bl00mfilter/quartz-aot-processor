package org.quartz.builder;

import org.quartz.*;
import org.quartz.properties.DailyTriggerProperties;
import org.quartz.type.BaseTriggerType;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DailyTimeIntervalBuilder implements QuartzTriggerBuilder<DailyTimeIntervalTrigger> {

    @Override
    public String getSupportedType() {
        return BaseTriggerType.DAILY.name();
    }

    @Override
    public Set<String> getValidKeys() {
        return Arrays.stream(DailyTriggerProperties.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public void configure(TriggerBuilder<DailyTimeIntervalTrigger> builder, Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Trigger properties map for DAILY_TIME_INTERVAL cannot be empty or null");
        }

        int interval = properties.containsKey(DailyTriggerProperties.INTERVAL.name())
                ? Integer.parseInt(properties.get(DailyTriggerProperties.INTERVAL.name())) : 1;

        String unitStr = properties.get(DailyTriggerProperties.INTERVAL_UNIT.name());
        DateBuilder.IntervalUnit quartzUnit = DateBuilder.IntervalUnit.MINUTE;
        if (unitStr != null && !unitStr.isBlank()) {
            quartzUnit = DateBuilder.IntervalUnit.valueOf(unitStr.toUpperCase());
        }

        DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                .withInterval(interval, quartzUnit);

        String startWindowStr = properties.get(DailyTriggerProperties.START_TIME_OF_DAY.name());
        if (startWindowStr != null && !startWindowStr.isBlank()) {
            String[] start = startWindowStr.split(":");
            scheduleBuilder.startingDailyAt(TimeOfDay.hourAndMinuteOfDay(Integer.parseInt(start[0]), Integer.parseInt(start[1])));
        }
        String endWindowStr = properties.get(DailyTriggerProperties.END_TIME_OF_DAY.name());
        if (endWindowStr != null && !endWindowStr.isBlank()) {
            String[] end = endWindowStr.split(":");
            scheduleBuilder.endingDailyAt(TimeOfDay.hourAndMinuteOfDay(Integer.parseInt(end[0]), Integer.parseInt(end[1])));
        }

        String daysStr = properties.get(DailyTriggerProperties.DAYS_OF_WEEK.name());
        if (daysStr != null && !daysStr.isBlank()) {
            Set<Integer> quartzDays = new HashSet<>();
            for (String day : daysStr.split(",")) {
                switch (day.trim().toUpperCase()) {
                    case "MON": quartzDays.add(DateBuilder.MONDAY); break;
                    case "TUE": quartzDays.add(DateBuilder.TUESDAY); break;
                    case "WED": quartzDays.add(DateBuilder.WEDNESDAY); break;
                    case "THU": quartzDays.add(DateBuilder.THURSDAY); break;
                    case "FRI": quartzDays.add(DateBuilder.FRIDAY); break;
                    case "SAT": quartzDays.add(DateBuilder.SATURDAY); break;
                    case "SUN": quartzDays.add(DateBuilder.SUNDAY); break;
                    default:
                        throw new IllegalArgumentException("Unknown day of week: " + day);
                }
            }
            if (!quartzDays.isEmpty()) {
                scheduleBuilder.onDaysOfTheWeek(quartzDays);
            }
        }

        String repeatStr = properties.get(DailyTriggerProperties.REPEAT_COUNT.name());
        if (repeatStr != null) {
            int repeats = Integer.parseInt(repeatStr);
            if (repeats != -1) {
                scheduleBuilder.withRepeatCount(repeats);
            }
        }

        String misfireStr = properties.get(DailyTriggerProperties.MISFIRE_STRATEGY.name());
        if (misfireStr != null && !misfireStr.isBlank()) {
            DailyTriggerProperties.MisfireStrategy strategy = DailyTriggerProperties.MisfireStrategy.valueOf(misfireStr.toUpperCase());
            switch (strategy) {
                case DO_NOTHING:
                    scheduleBuilder.withMisfireHandlingInstructionDoNothing();
                    break;
                case FIRE_ONCE_NOW:
                case SMART_POLICY:
                default:
                    scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                    break;
            }
        }
        builder.withSchedule(scheduleBuilder);

        String startStr = properties.get(DailyTriggerProperties.START_TIME.name());
        if (startStr != null && !startStr.isBlank()) {
            builder.startAt(Date.from(ZonedDateTime.parse(startStr).toInstant()));
        }
        String endStr = properties.get(DailyTriggerProperties.END_TIME.name());
        if (endStr != null && !endStr.isBlank()) {
            builder.endAt(Date.from(ZonedDateTime.parse(endStr).toInstant()));
        }

        String calName = properties.get(DailyTriggerProperties.CALENDAR_NAME.name());
        if (calName != null && !calName.isBlank()) {
            builder.modifiedByCalendar(calName);
        }
    }
}
