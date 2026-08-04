package org.quartz.task;

import lombok.Data;
import org.quartz.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@PropertySource(value = "classpath:quartz-tasks.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "app.quartz")
public class QuartzTasksProperties {

    private List<TaskConfig> tasks;

    @Data
    public static class TaskConfig {
        private String jobName;
        private String jobGroup;
        private String triggerType;
        private String description;
        private String className;
        private Map<String, String> properties;
        private Map<String, String> jobData;
    }
}
