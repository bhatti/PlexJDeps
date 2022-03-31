package com.plexobject.db;

import com.sleepycat.je.DatabaseException;

import java.util.HashMap;
import java.util.Map;


public class RepositoryFactory {
    private static final String DEP_REPO = "dependencies";
    private final DatabaseStore databaseStore;
    private final boolean inMemory;
    private final Map<String, DependencyRepository> repositories = new HashMap<>();

    //
    public RepositoryFactory(final DatabaseStore databaseStore, boolean inMemory) {
        this.databaseStore = databaseStore;
        this.inMemory = inMemory;
    }

    public RepositoryFactory(final String dbName, boolean inMemory) throws DatabaseException {
        this(new DatabaseStore(dbName, false), inMemory);
    }

    public RepositoryFactory() throws DatabaseException {
        this(new DatabaseStore(), true);
    }

    public synchronized DependencyRepository getDependencyRepository() throws DatabaseException {
        DependencyRepository repository = repositories.get(DEP_REPO);
        if (repository == null) {
            if (inMemory) {
                repository = new DependencyRepositoryMem();
            } else {
                if (!databaseStore.getAllDatabases().contains(DEP_REPO)) {
                    databaseStore.createDatabase(DEP_REPO);
                }
                repository = new DependencyRepositoryBDB(databaseStore.getStore(DEP_REPO), databaseStore, this);
            }
            repositories.put(DEP_REPO, repository);
        }
        return repository;
    }

    public synchronized void closeDefault() throws DatabaseException {
        close(DEP_REPO);
    }

    public synchronized void close(final String domain) throws DatabaseException {
        repositories.remove(domain);
        databaseStore.close();
    }
}
