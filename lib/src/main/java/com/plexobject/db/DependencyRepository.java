package com.plexobject.db;

import java.util.Set;

public interface DependencyRepository {
    Set<Dependency> getAll();

    Set<Dependency> getDependenciesFrom(String from);

    Set<Dependency> getDependenciesTo(String to);

    Dependency save(Dependency object);

    void clear();

    void close();
}
