package ru.t1.java.demo.aop;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Client;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Aspect
@Component
@Transactional(propagation = NOT_SUPPORTED)
@RequiredArgsConstructor
public class TransactionAspect {

    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;

    @Pointcut("@annotation(ru.t1.java.demo.aop.Transaction)")
    public void join() {

    }

    @Around("@annotation(ru.t1.java.demo.aop.Transaction)")
    public void wrapMethod(final ProceedingJoinPoint joinPoint) throws Throwable {

        var transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {

            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Client client) {
                    entityManager.persist(client);
                }
            }

            transactionManager.commit(transactionStatus);
        } catch (Exception exception) {
            transactionManager.rollback(transactionStatus);
        }
    }

}
