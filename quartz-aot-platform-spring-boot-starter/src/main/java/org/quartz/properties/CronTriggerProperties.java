package org.quartz.properties;

public enum CronTriggerProperties implements QuartzTriggerProperties{
    EXPRESSION,
    TIME_ZONE,
    START_TIME,
    END_TIME,
    PRIORITY,
    MISFIRE_STRATEGY,
    CALENDAR_NAME;

    public enum MisfireStrategy {
        FIRE_AND_PROCEED,
        DO_NOTHING,
        SMART_POLICY
    }
}
