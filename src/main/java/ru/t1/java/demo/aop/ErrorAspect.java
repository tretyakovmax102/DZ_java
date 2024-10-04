package ru.t1.java.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Реализация аспекта, отправляющего информацию о методах, в которых возникло исключение
 *
 * @Lazy, чтобы не было циклической зависимости
 */
@Aspect
@Component
public class ErrorAspect {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ErrorAspect(@Lazy KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Around("execution(* ru.t1.java.demo..*(..))")
    public Object logAndSendErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("Method name", joinPoint.getSignature().toShortString());
            errorMessage.put("Arguments", joinPoint.getArgs());
            errorMessage.put("Exception", throwable.getClass().getSimpleName());
            errorMessage.put("Message", throwable.getMessage());
            errorMessage.put("StackTrace", getStackTraceAsString(throwable));

            kafkaTemplate.send("t1_demo_error_trace", errorMessage);

            throw throwable;
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}

