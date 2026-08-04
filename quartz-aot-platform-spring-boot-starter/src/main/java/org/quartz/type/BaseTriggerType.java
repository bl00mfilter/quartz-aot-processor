package org.quartz.type;

public enum BaseTriggerType implements TriggerType{
    CRON,
    SIMPLE,
    CALENDAR,
    DAILY
}
