package com.plexobject.db;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

import java.util.HashSet;
import java.util.Set;


public class DependencyRepositoryBDB implements DependencyRepository {
    private final static ThreadLocal<Transaction> CURRENT_TXN = new ThreadLocal<>();
    private final DatabaseStore databaseStore;
    private final RepositoryFactory repositoryFactory;
    private final EntityStore store;
    private PrimaryIndex<String, Dependency> primaryIndex;
    private SecondaryIndex<String, String, Dependency> fromIndex;
    private SecondaryIndex<String, String, Dependency> toIndex;
    private SecondaryIndex<String, String, Dependency> shortFromIndex;
    private SecondaryIndex<String, String, Dependency> shortToIndex;

    public DependencyRepositoryBDB(EntityStore entityStore, final DatabaseStore databaseStore, final RepositoryFactory repositoryFactory) throws DatabaseException {
        this.store = entityStore;
        this.databaseStore = databaseStore;
        this.repositoryFactory = repositoryFactory;
        //
        primaryIndex = store.getPrimaryIndex(String.class, Dependency.class);
        fromIndex = store.getSecondaryIndex(primaryIndex, String.class, "from");
        toIndex = store.getSecondaryIndex(primaryIndex, String.class, "to");
        shortFromIndex = store.getSecondaryIndex(primaryIndex, String.class, "shortFrom");
        shortToIndex = store.getSecondaryIndex(primaryIndex, String.class, "shortTo");
    }

    @Override
    public Set<Dependency> getAll() throws DatabaseException {
        Set<Dependency> all = new HashSet<>();
        databaseStore.beginTransaction();
        EntityCursor<Dependency> cursor = primaryIndex.entities(null, false, null, true);
        for (Dependency next : cursor) {
            all.add(next);
        }
        cursor.close();
        databaseStore.commitTransaction();
        return all;
    }


    @Override
    public Set<Dependency> getDependenciesFrom(String from) throws DatabaseException {
        Set<Dependency> all = new HashSet<>();
        databaseStore.beginTransaction();
        boolean isShort = from.split("\\.").length == 1;
        EntityCursor<Dependency> cursor = isShort ? shortFromIndex.subIndex(from).entities() : fromIndex.subIndex(from).entities();
        for (Dependency next : cursor) {
            all.add(next);
        }
        cursor.close();
        databaseStore.commitTransaction();
        return all;
    }

    @Override
    public Set<Dependency> getDependenciesTo(String to) throws DatabaseException {
        Set<Dependency> all = new HashSet<>();
        databaseStore.beginTransaction();
        boolean isShort = to.split("\\.").length == 1;
        EntityCursor<Dependency> cursor = isShort ? shortToIndex.subIndex(to).entities() : toIndex.subIndex(to).entities();
        for (Dependency next : cursor) {
            all.add(next);
        }
        cursor.close();
        databaseStore.commitTransaction();
        return all;
    }

    public Dependency findById(String id) throws DatabaseException {
        databaseStore.beginTransaction();
        Dependency d = primaryIndex.get(id);
        databaseStore.commitTransaction();
        return d;
    }

    public boolean remove(String id) throws DatabaseException {
        databaseStore.beginTransaction();
        boolean d = primaryIndex.delete(id);
        databaseStore.commitTransaction();
        return d;
    }

    @Override
    public Dependency save(Dependency object) throws DatabaseException {
        databaseStore.beginTransaction();
        Dependency d = primaryIndex.put(object);
        databaseStore.commitTransaction();
        return d;
    }

    @Override
    public void clear() throws DatabaseException {
        databaseStore.beginTransaction();
        store.truncateClass(Dependency.class);
        databaseStore.commitTransaction();
        //
        primaryIndex = store.getPrimaryIndex(String.class, Dependency.class);
        fromIndex = store.getSecondaryIndex(primaryIndex, String.class, "from");
        toIndex = store.getSecondaryIndex(primaryIndex, String.class, "to");
        shortFromIndex = store.getSecondaryIndex(primaryIndex, String.class, "shortFrom");
        shortToIndex = store.getSecondaryIndex(primaryIndex, String.class, "shortTo");
    }

    @Override
    public void close() {
        try {
            store.close();
        } catch (Exception e) {
        }
    }
}
