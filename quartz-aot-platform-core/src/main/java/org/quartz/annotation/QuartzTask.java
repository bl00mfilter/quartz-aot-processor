package org.quartz.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface QuartzTask {
    String typeTrigger();

    String jobName();

    String jobGroup();

    String description() default "";

    @interface Property{
        String key();
        String value();
    }

    Property[] jobProperties() default {};

    Property[] triggerProperties() default {};
}
