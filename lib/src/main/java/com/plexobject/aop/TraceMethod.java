package com.plexobject.aop;

import java.lang.reflect.Method;
import java.util.Arrays;

public class TraceMethod {
    private final String declaringClass;
    private final String methodName;
    private final String fileName;
    private final int lineNumber;
    private String[] argTypes;
    private String resultType;

    public TraceMethod(StackTraceElement elem) {
        this.declaringClass = elem.getClassName();
        this.methodName = elem.getMethodName();
        this.fileName = elem.getFileName();
        this.lineNumber = elem.getLineNumber();
        try {
            Method[] methods = Class.forName(elem.getClassName()).getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(elem.getMethodName())) {
                    Class<?>[] types = method.getParameterTypes();
                    if (types != null) {
                        this.argTypes = new String[types.length];
                        for (int i = 0; i < types.length; i++) {
                            this.argTypes[i] = types[i].getName();
                        }
                    }
                    if (method.getReturnType() != null) {
                        this.resultType = method.getReturnType().getName();
                    }
                    break;
                }
            }
        } catch (Throwable e) {
            argTypes = new String[0];
            resultType = "";
        }
        if ("void".equals(resultType)) {
            resultType = "";
        }
    }

    public TraceMethod(String declaringClass, String methodName, String fileName, int lineNumber, String[] argTypes, String resultType) {
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.argTypes = argTypes;
        this.resultType = resultType;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassMethodName() {
        return declaringClass + "__" + methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String[] getArgTypes() {
        return argTypes;
    }

    public String getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return getDeclaringClass() + ":" + getMethodName();
    }

    public String toLongString() {
        return "TraceMethod{" +
                "declaringClass='" + getDeclaringClass() + '\'' +
                ", methodName='" + getMethodName() + '\'' +
                ", fileName='" + getFileName() + '\'' +
                ", lineNumber=" + getLineNumber() +
                ", argTypes=" + Arrays.toString(getArgTypes()) +
                ", resultType='" + getResultType() + '\'' +
                '}';
    }
}