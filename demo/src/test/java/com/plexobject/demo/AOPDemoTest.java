package com.plexobject.demo;

import com.plexobject.aop.DynamicLoad;
import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.demo.dao.DDBMapper;
import com.plexobject.demo.ddb.CatalogItem;
import com.plexobject.demo.ddb.MusicItems;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AOPDemoTest {
    private final DDBMapper mapper = new DDBMapper();

    @BeforeEach
    void setup() throws Exception {
        DynamicLoad.checkAdviceClassLoaded();
        DynamicLoad.checkAspectJAgentLoaded();
        DynamicLoad.checkAdviceClassLoaded();
    }

    @AfterEach
    void teardown() {
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(">>>>>>>>" + trace);
            }
        }
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
    void tesSaveMusic() {
        MusicItems saved = mapper.saveMusic();
        MusicItems loaded = mapper.loadMusic(saved.getArtist(), saved.getSongTitle());
        System.out.println("Item retrieved:" + loaded);
        loaded.setAwards(2);
        System.out.println("Item updated:" + mapper.updateMusic(loaded) + "::::" + mapper.countMusic());
    }

    @Test
    void testSaveCatalog() {
        CatalogItem saved = mapper.saveCatalog();
        CatalogItem loaded = mapper.loadCatalog(saved.getId());
        System.out.println(loaded + "::::: " + mapper.countCatalog());
    }
}
