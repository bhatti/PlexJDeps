package com.plexobject.demo;

import com.plexobject.aop.Tracer;

public class AOPDemo {
    @Tracer
    public void runAopDemo() {
        System.out.println("inside runAopDemo");
    }
}
