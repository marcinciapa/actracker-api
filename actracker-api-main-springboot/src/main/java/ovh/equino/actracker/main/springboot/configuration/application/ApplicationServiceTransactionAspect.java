package ovh.equino.actracker.main.springboot.configuration.application;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import ovh.equino.actracker.main.springboot.configuration.instrumentation.MetricsCollector;

@Aspect
@Order(200)
class ApplicationServiceTransactionAspect {

    @Around("execution(* ovh.equino.actracker.application.*.*ApplicationService.*(..))")
    Object measureAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Starting transaction");
        return joinPoint.proceed();
    }
}
