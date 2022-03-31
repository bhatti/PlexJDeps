package com.plexobject.db;

import com.sleepycat.je.DatabaseException;

import java.util.Set;

public interface DependencyRepository {
    Set<Dependency> getAll() throws DatabaseException;

    Set<Dependency> getDependenciesFrom(String from) throws DatabaseException;

    Set<Dependency> getDependenciesTo(String to) throws DatabaseException;

    Dependency save(Dependency object) throws DatabaseException;

    void clear() throws DatabaseException;

    void close();
}
