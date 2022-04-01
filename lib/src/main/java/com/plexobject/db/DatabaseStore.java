package com.plexobject.db;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DatabaseStore {
    private Environment dbEnvironment;
    private final boolean txn;
    private final StoreConfig storeConfig;
    private final Map<String, EntityStore> stores = new HashMap<>();
    private final Map<String, Database> databases = new HashMap<>();

    private static final ThreadLocal<Transaction> CURRENT_TXN = new ThreadLocal<>();

    public DatabaseStore() {
        this(System.getProperty("user.home") + System.getProperty("file.separator") + ".deps", false);
    }

    public DatabaseStore(final String databaseDir, boolean txn) {
        this.txn = txn;
        // Open the DB environment. Create if they do not already exist.
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(txn);
        Durability defaultDurability = new Durability(Durability.SyncPolicy.SYNC, null, null);
        final File dir = new File(databaseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //
        dbEnvironment = new Environment(dir, envConfig);
        storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        //storeConfig.setDeferredWrite(true);
    }

    //
    synchronized EntityStore getStore(final String domain) {
        EntityStore store = stores.get(domain);
        if (store == null) {
            store = new EntityStore(dbEnvironment, domain, storeConfig);
            stores.put(domain, store);
        }
        return store;
    }

    Collection<String> getAllDatabases() {
        return dbEnvironment.getDatabaseNames();
    }

    void removeDatabase(final String domain) {
        close(domain);
        Database db = databases.get(domain);
        if (db != null) {
            db.close();
        }
        dbEnvironment.removeDatabase(null, domain);
    }

    void createDatabase(final String domain) {
        DatabaseConfig dbconfig = new DatabaseConfig();
        dbconfig.setAllowCreate(true);
        dbconfig.setSortedDuplicates(false);
        dbconfig.setExclusiveCreate(false);
        dbconfig.setReadOnly(false);
        dbconfig.setTransactional(txn);
        Database db = dbEnvironment.openDatabase(null, domain, dbconfig);
        databases.put(domain, db);
        getStore(domain);
    }

    synchronized void close(final String domain) {
        EntityStore store = stores.remove(domain);
        if (store != null) {
            try {
                store.close();
            } catch (Exception e) {
            }
        }
    }

    public void close() {
        if (dbEnvironment == null) {
            return;
        }
        try {
            for (String domain : new ArrayList<>(stores.keySet())) {
                close(domain);
            }

            dbEnvironment.sync();
            dbEnvironment.close();
            dbEnvironment = null;
        } catch (Exception e) {
        }
    }

    void beginTransaction() {
        if (txn) {
            CURRENT_TXN.set(dbEnvironment.beginTransaction(null, null));
        }
    }

    void commitTransaction() {
        if (txn) {
            Transaction txn = CURRENT_TXN.get();
            if (txn != null) {
                txn.commit();
            }
        }
    }

    void abortTransaction() {
        if (txn) {
            Transaction txn = CURRENT_TXN.get();
            if (txn != null) {
                txn.abort();
            }
        }
    }
}
