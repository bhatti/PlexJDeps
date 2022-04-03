package com.plexobject.aop;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Trace {
    private final long time = System.currentTimeMillis();
    private final String threadName = Thread.currentThread().getName();
    private final String signature;
    private final Object[] args;
    private final Object result;
    private final List<TraceMethod> methods;

    public Trace(String signature, Object[] args, Object result, List<TraceMethod> methods) {
        this.signature = signature;
        this.args = args;
        this.result = result;
        this.methods = methods;
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

    public Object getResult() {
        return result;
    }

    public List<TraceMethod> getMethods() {
        return methods;
    }


    public String buildSequenceConfig() {
        StringBuilder sb = new StringBuilder("@startuml\nautoactivate on\n");
        buildSequenceConfig(sb, 0);
        sb.append("@enduml\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Trace{" +
                "time=" + new Date(getTime()) +
                ", threadName='" + getThreadName() + '\'' +
                ", signature='" + getSignature() + '\'' +
                ", args=" + Arrays.toString(getArgs()) +
                ", result=" + getResult() +
                ", methods=" + methods +
                '}';
    }

    private void buildSequenceConfig(StringBuilder sb, int n) {
        if (getMethods().size() < 2 || n >= getMethods().size() - 1) {
            return;
        }
        TraceMethod cur = getMethods().get(n);
        TraceMethod next = getMethods().get(n + 1);
        sb.append(cur.getClassMethodName()).append(" -> ").append(next.getClassMethodName()).append(" ++ #333333");
        if (cur.getArgTypes() != null && cur.getArgTypes().length > 0) {
            sb.append(" : ").append(cur.getArgTypes()[0]);
            if (cur.getArgTypes().length > 1) {
                sb.append("...");
            }
        }
        sb.append("\n");
        buildSequenceConfig(sb, n + 1);
        sb.append(next.getClassMethodName()).append(" --> ").append(cur.getClassMethodName()).append(" -- ");
        if (next.getResultType() != null) {
            sb.append(" : ").append(next.getResultType());
        }
        sb.append("\n");
    }
}
