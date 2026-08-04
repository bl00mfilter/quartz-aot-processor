package org.quartz.properties;

public enum DailyTriggerProperties implements QuartzTriggerProperties {
    INTERVAL,
    INTERVAL_UNIT,
    START_TIME_OF_DAY,
    END_TIME_OF_DAY,
    DAYS_OF_WEEK,
    REPEAT_COUNT,
    START_TIME,
    END_TIME,
    PRIORITY,
    MISFIRE_STRATEGY,
    CALENDAR_NAME;

    public enum MisfireStrategy {
        FIRE_ONCE_NOW,
        DO_NOTHING,
        SMART_POLICY
    }
}
