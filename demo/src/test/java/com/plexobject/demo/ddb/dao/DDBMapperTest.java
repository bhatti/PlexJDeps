package com.plexobject.demo.ddb.dao;

import com.plexobject.aop.DynamicLoad;
import com.plexobject.aop.TraceCollector;
import com.plexobject.demo.AOPDemo;
import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.demo.ddb.model.MusicItems;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DDBMapperTest {
    private final DDBMapper mapper = new DDBMapper();

    @BeforeEach
    void setup() throws Exception {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        DynamicLoad.checkAdviceClassLoaded();
    }

    @AfterEach
    void teardown() {
        System.out.println("*** Traces ****");
        TraceCollector.getInstance().dump();
    }

    @Test
    void testAop() {
        new AOPDemo().runAopDemo();
    }

    @Test
    void testListTables() {
        for (String table : mapper.getTables()) {
            System.out.println("======" + table);
            mapper.describeTable(table);
        }
    }

    @Test
    void tesUpdateMusic() {
        MusicItems saved = mapper.saveMusic();
        MusicItems loaded = mapper.loadMusic(saved.getArtist(), saved.getSongTitle());
        System.out.println("Item retrieved:" + loaded);
        loaded.setAwards(2);
        System.out.println("Item updated:" + mapper.updateMusic(loaded) + "::::" + mapper.countMusic());
    }

    @Test
    void testUpdateCatalog() {
        CatalogItem item = new CatalogItem(String.valueOf(System.currentTimeMillis()), "title1", "isbn");
        CatalogItem saved = mapper.saveCatalog(item);
        CatalogItem loaded = mapper.loadCatalog(saved.getId());
        System.out.println(loaded + "::::: " + mapper.countCatalog());
    }
}
