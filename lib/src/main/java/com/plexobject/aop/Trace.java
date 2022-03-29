package com.plexobject.aop;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Trace {
    private final long time = System.currentTimeMillis();
    private final String threadName = Thread.currentThread().getName();
    private final String signature;
    private final Object[] args;
    private final List<String> stack;

    public Trace(String signature, Object[] args, List<String> stack) {
        this.signature = signature;
        this.args = args;
        this.stack = stack;
    }

    public long getTime() {
        return time;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getSignature() {
        return signature;
    }

    public Object[] getArgs() {
        return args;
    }

    public List<String> getStack() {
        return stack;
    }

    @Override
    public String toString() {
        return "Trace{" +
                "time=" + new Date(getTime()) +
                ", threadName='" + getThreadName() + '\'' +
                ", signature='" + getSignature() + '\'' +
                ", args=" + Arrays.toString(getArgs()) +
                ", stack=" + getStack() +
                '}';
    }
}
