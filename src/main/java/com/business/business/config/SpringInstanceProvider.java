package com.business.business.config;

import io.github.lekan128.aiagent.api.InstanceProvider;
import io.github.lekan128.aiagent.impl.method.caller.ReflectionCaller;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringInstanceProvider implements InstanceProvider {

    private final ApplicationContext context;

    public SpringInstanceProvider(ApplicationContext context) {
        this.context = context;
        // Register the provider globally once
        ReflectionCaller.setInstanceProvider(this);
    }

    @Override
    public Object getInstance(Class<?> clazz) {
        return context.getBean(clazz);
    }
}
