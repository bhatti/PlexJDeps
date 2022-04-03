package com.plexobject.db;


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

    public RepositoryFactory(final String dbName, boolean inMemory) {
        this(new DatabaseStore(dbName, false), inMemory);
    }

    public RepositoryFactory() {
        this(System.getProperty("user.home") + System.getProperty("file.separator") + ".deps", true);
    }

    public synchronized DependencyRepository getDependencyRepository() {
        DependencyRepository repository = repositories.get(DEP_REPO);
        if (repository == null) {
            if (inMemory) {
                repository = DependencyRepositoryMem.create("deps.ser");
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

    public synchronized void closeDefault() {
        close(DEP_REPO);
    }

    public synchronized void close(final String domain) {
        repositories.remove(domain);
        databaseStore.close();
    }
}
