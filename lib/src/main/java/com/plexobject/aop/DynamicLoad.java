package com.plexobject.aop;

import com.sun.tools.attach.VirtualMachine;
import org.aspectj.weaver.loadtime.Agent;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DynamicLoad {
    public static void checkAspectJAgentLoaded() {
        try {
            Agent.getInstrumentation();
        } catch (UnsupportedOperationException e) {
            dynamicallyLoadAspectJAgent();
        }
    }

    public static void dynamicallyLoadAspectJAgent() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            if (vm == null) {
                throw new RuntimeException("Failed to find VM for " + pid + ", name " + nameOfRunningVM);
            }
            String jarFilePath = System.getProperty("AGENT_PATH");
            if (jarFilePath == null) {
                throw new RuntimeException("Failed to find Agent-Path for " + pid + ", name " + nameOfRunningVM);
            }
            vm.loadAgent(jarFilePath);
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkAdviceClassLoaded() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        m.setAccessible(true);
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        //System.err.println("Loaded : " + m.invoke(cl, TracerAspect.class.getName()));
    }
}
