package com.plexobject.aop;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TraceCollector {
    private static final TraceCollector INSTANCE = new TraceCollector();

    private final ConcurrentHashMap<String, List<Trace>> traces = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> locks = new ConcurrentHashMap<>();

    public static TraceCollector getInstance() {
        return INSTANCE;
    }

    public void add(Trace trace) {
        traces.putIfAbsent(trace.getSignature(), new ArrayList<>());
        locks.putIfAbsent(trace.getSignature(), true);
        synchronized (locks.get(trace.getSignature())) {
            traces.get(trace.getSignature()).add(trace);
        }
    }

    public List<Trace> get(String signature) {
        if (!locks.containsKey(signature)) {
            return Collections.emptyList();
        }
        if (!traces.containsKey(signature)) {
            return Collections.emptyList();
        }
        synchronized (locks.get(signature)) {
            return new ArrayList<>(traces.get(signature));
        }
    }

    public void dump() {
        for (List<Trace> traces : getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(trace.getSignature() + " -- " + Arrays.toString(trace.getArgs()));
            }
        }
    }

    public Set<String> getSignatures() {
        return new HashSet<>(traces.keySet());
    }

    public Map<String, List<Trace>> getAll() {
        Map<String, List<Trace>> result = new HashMap<>();
        for (String signature : new ArrayList<>(traces.keySet())) {
            result.put(signature, get(signature));
        }
        return result;
    }


    public int size() {
        return traces.size();
    }
}
