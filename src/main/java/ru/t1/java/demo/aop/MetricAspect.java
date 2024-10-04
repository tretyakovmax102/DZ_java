package ru.t1.java.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация аспекта, вычисляющего время исполнения методов
 */
@Aspect
@Component
public class MetricAspect {

    @Qualifier("kafkaTemplateObject")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${metric.threshold}")
    private long threshold;

    public MetricAspect(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Around("@annotation(Metric)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        if (duration > threshold) {
            Map<String, Object> message = new HashMap<>();
            message.put("Method name", joinPoint.getSignature().toShortString());
            message.put("Duration", duration);
            message.put("Arguments", joinPoint.getArgs());

            kafkaTemplate.send("t1_demo_metric_trace", message);
        }

        return result;
    }
}