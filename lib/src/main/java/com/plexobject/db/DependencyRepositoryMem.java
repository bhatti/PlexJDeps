package com.plexobject.db;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class DependencyRepositoryMem implements DependencyRepository, Serializable {
    private static final long serialVersionUID = 1L;
    private final ConcurrentHashMap<String, Dependency> lookupByID = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> depsFrom = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> depsTo = new ConcurrentHashMap<>();
    private String serializeFileName;

    public static DependencyRepository create(String name) {
        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(name)));
            DependencyRepositoryMem repo = (DependencyRepositoryMem) in.readObject();
            repo.serializeFileName = name;
            return repo;
        } catch (Throwable e) {
            System.err.println("Failed to load " + name + ": " + e);
            return new DependencyRepositoryMem();
        }
    }

    @Override
    public Set<Dependency> getAll() {
        Set<Dependency> all = new HashSet<>();
        all.addAll(lookupByID.values());
        return all;
    }

    @Override
    public Set<Dependency> getDependenciesFrom(String from) {
        Set<String> ids = depsFrom.get(from);
        return getDependencies(ids);
    }

    private Set<Dependency> getDependencies(Set<String> ids) {
        if (ids == null) {
            return Collections.emptySet();
        }
        Set<Dependency> result = new HashSet<>();
        for (String id : new HashSet<>(ids)) {
            Dependency d = lookupByID.get(id);
            if (d != null) {
                result.add(d);
            }
        }
        return result;
    }

    @Override
    public Set<Dependency> getDependenciesTo(String to) {
        Set<String> keys = depsTo.get(to);
        return getDependencies(keys);
    }

    @Override
    public Dependency save(Dependency d) {
        lookupByID.putIfAbsent(d.getId(), d);
        depsFrom.putIfAbsent(d.getFrom(), new HashSet<>());
        depsTo.putIfAbsent(d.getTo(), new HashSet<>());
        depsFrom.get(d.getFrom()).add(d.getId());
        depsTo.get(d.getFrom()).add(d.getId());
        return d;
    }

    @Override
    public void clear() {
        lookupByID.clear();
        depsFrom.clear();
        depsTo.clear();
    }

    @Override
    public void close() {
        if (serializeFileName != null) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(serializeFileName)));
                out.writeObject(this);
                out.close();
            } catch (Throwable e) {
                System.err.println("Failed to save " + serializeFileName + ": " + e);
            }
        }
        clear();
    }
}
