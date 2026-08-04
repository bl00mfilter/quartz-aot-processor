package org.quartz.properties;

public enum SimpleTriggerProperties implements QuartzTriggerProperties{
    INTERVAL_MS,
    REPEAT_COUNT,
    START_TIME,
    END_TIME,
    PRIORITY,
    MISFIRE_STRATEGY,
    CALENDAR_NAME;

    public enum MisfireStrategy{
        FIRE_NOW,
        RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT,
        RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT,
        RESCHEDULE_NEXT_WITH_EXISTING_COUNT,
        SMART_POLICY
    }
}
