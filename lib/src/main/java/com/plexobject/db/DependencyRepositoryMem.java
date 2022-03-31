package com.plexobject.db;

import com.sleepycat.je.DatabaseException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class DependencyRepositoryMem implements DependencyRepository {
    private final ConcurrentHashMap<String, Dependency> lookupByID = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> depsFrom = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> depsTo = new ConcurrentHashMap<>();

    @Override
    public Set<Dependency> getAll() throws DatabaseException {
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
    public Dependency save(Dependency d) throws DatabaseException {
        lookupByID.putIfAbsent(d.getId(), d);
        depsFrom.putIfAbsent(d.getFrom(), new HashSet<>());
        depsTo.putIfAbsent(d.getTo(), new HashSet<>());
        depsFrom.get(d.getFrom()).add(d.getId());
        depsTo.get(d.getFrom()).add(d.getId());
        return d;
    }

    @Override
    public void clear() throws DatabaseException {
        lookupByID.clear();
        depsFrom.clear();
        depsTo.clear();
    }

    @Override
    public void close() {
        clear();
    }
}
