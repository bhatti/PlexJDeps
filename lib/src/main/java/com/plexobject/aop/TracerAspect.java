package com.plexobject.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.ArrayList;
import java.util.List;

@Aspect
public class TracerAspect {
    @Pointcut("@annotation(wrap)")
    public void callAt(Tracer wrap) {
    }

    @Around("callAt(wrap)")
    public Object around(ProceedingJoinPoint pjp, Tracer wrap) throws Throwable {
        try {
            Object result = pjp.proceed();
            if ("method-execution".equals(pjp.getKind())) {
                TraceCollector.getInstance().add(getTrace(pjp, result));
            }
            return result;
        } catch (Throwable e) {
            if ("method-execution".equals(pjp.getKind())) {
                TraceCollector.getInstance().add(getTrace(pjp, null));
            }
            throw e;
        }
    }

    static String getPackage(ProceedingJoinPoint pjp) {
        String[] toks = pjp.getSignature().getDeclaringTypeName().split("\\.");
        if (toks.length >= 2) {
            return toks[0] + "." + toks[1];
        }
        return pjp.getSignature().getDeclaringTypeName();
    }

    static Trace getTrace(ProceedingJoinPoint pjp, Object result) {
        String pkg = getPackage(pjp);
        final StackTraceElement[] threadStack = Thread.currentThread().getStackTrace();
        List<TraceMethod> methods = new ArrayList<>();
        for (int i = threadStack.length - 1; i >= 0; i--) {
            if (!threadStack[i].getClassName().startsWith("com.plexobject.aop") &&
                    !threadStack[i].getMethodName().contains("_aroundBod") &&
                    !threadStack[i].getClassName().contains("$AjcClosure") &&
                    threadStack[i].getClassName().startsWith(pkg)) {
                methods.add(new TraceMethod(threadStack[i]));
            }
        }
        return new Trace(pjp.getSignature().toLongString(), pjp.getArgs(), result, methods);
    }
}