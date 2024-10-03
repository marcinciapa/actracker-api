package ovh.equino.actracker.main.springboot.configuration.application;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import ovh.equino.actracker.main.springboot.configuration.instrumentation.MetricsCollector;

@Aspect
@Order(100)
class ApplicationServiceMetricAspect {

    @Autowired
    private MetricsCollector metricsCollector;

    @Around("execution(* ovh.equino.actracker.application.*.*ApplicationService.*(..))")
    Object measureAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Starting measuring");
        return metricsCollector.measureAndExecute(joinPoint);
    }
}
