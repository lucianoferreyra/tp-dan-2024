package isi.dan.ms_productos.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(isi.dan.ms_productos.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        logger.info("{};{}", joinPoint.getSignature().getDeclaringType()
        +"."+
        joinPoint.getSignature().getName(), executionTime);
        MDC.put("metodo", joinPoint.getSignature().toShortString());
        MDC.put("duracion", executionTime+"");

        return proceed;
    }

}