package org.quartz.builder;

import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.properties.QuartzTriggerProperties;

import java.util.Map;
import java.util.Set;

public interface QuartzTriggerBuilder<E extends Trigger> {
    String getSupportedType();

    Set<String> getValidKeys();

    void configure(TriggerBuilder<E> builder, Map<String, String> properties);
}
