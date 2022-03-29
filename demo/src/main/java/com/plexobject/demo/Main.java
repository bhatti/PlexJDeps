package com.plexobject.demo;

import com.plexobject.aop.DynamicLoad;

public class Main {
    public static void main(String[] args) throws Exception {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        new AOPDemo().runAopDemo();
    }
}
