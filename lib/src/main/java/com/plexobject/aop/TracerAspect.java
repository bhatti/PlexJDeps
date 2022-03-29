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
    public Object around(ProceedingJoinPoint pjp,
                         Tracer wrap) throws Throwable {
        if ("method-execution".equals(pjp.getKind())) {
            Trace trace = new Trace(
                    pjp.getSignature().toLongString(),
                    pjp.getArgs(),
                    getPackageStack(getPackage(pjp)));
            TraceCollector.getInstance().add(trace);
        }
        return pjp.proceed();
    }

    static String getPackage(ProceedingJoinPoint pjp) {
        String[] toks = pjp.getSignature().getDeclaringTypeName().split(".");
        if (toks.length >= 2) {
            return toks[0] + "." + toks[1];
        }
        return pjp.getSignature().getDeclaringTypeName();
    }

    static List<String> getPackageStack(String pkg) {
        final StackTraceElement[] threadStack = Thread.currentThread().getStackTrace();
        List<String> pkgStack = new ArrayList<>();
        for (int i = 0; i < threadStack.length; i++) {
            if (threadStack[i].getClassName().startsWith(pkg)) {
                pkgStack.add(threadStack[i].toString());
            }
        }
        return pkgStack;
    }
}