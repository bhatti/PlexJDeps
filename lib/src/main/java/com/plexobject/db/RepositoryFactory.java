package com.plexobject.db;

import java.util.HashMap;
import java.util.Map;
import com.sleepycat.je.DatabaseException;


public class RepositoryFactory {
    private static final String DEP_REPO = "dependencies";
    private final DatabaseStore databaseStore;
    private final Map<String, DependencyRepository> applicationRepositories = new HashMap<>();
    //
    public RepositoryFactory(final DatabaseStore databaseStore) {
        this.databaseStore = databaseStore;
    }

    public RepositoryFactory(final String dbName) throws DatabaseException {
        this(new DatabaseStore(dbName, false));
    }

    public RepositoryFactory() throws DatabaseException {
        this(new DatabaseStore());
    }

    public synchronized DependencyRepository getDependencyRepository() throws DatabaseException {
        DependencyRepository repository = applicationRepositories.get(DEP_REPO);
        if (repository == null) {
            if (!databaseStore.getAllDatabases().contains(DEP_REPO)) {
                databaseStore.createDatabase(DEP_REPO);
            }
            repository = new DependencyRepository(databaseStore.getStore(DEP_REPO), databaseStore, this);
            applicationRepositories.put(DEP_REPO, repository);
        }
        return repository;
    }

    public synchronized void closeDefault() throws DatabaseException {
        close(DEP_REPO);
    }

    public synchronized void close(final String domain) throws DatabaseException {
        applicationRepositories.remove(domain);
        databaseStore.close();
    }
}
