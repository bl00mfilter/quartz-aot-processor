package org.quartz.properties;

public enum CalendarTriggerProperties implements QuartzTriggerProperties {
    INTERVAL,
    INTERVAL_UNIT,
    TIME_ZONE,
    START_TIME,
    END_TIME,
    PRIORITY,
    MISFIRE_STRATEGY,
    CALENDAR_NAME,
    PRESERVE_HOUR_DST,
    SKIP_DAY_DST_OVERLAP;

    public enum MisfireStrategy {
        FIRE_AND_PROCEED,
        DO_NOTHING,
        SMART_POLICY
    }

    public enum IntervalUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}
