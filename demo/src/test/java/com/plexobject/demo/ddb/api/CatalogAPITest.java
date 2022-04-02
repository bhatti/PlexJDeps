package com.plexobject.demo.ddb.api;

import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.deps.ShowDepend;
import org.junit.jupiter.api.Test;

public class CatalogAPITest {
    CatalogAPI api = new CatalogAPI();

    @Test
    void testSaveCatalogTrace() {
        CatalogItem saved = api.saveCatalog();
    }

    @Test
    void testSaveCatalogDeps() throws Exception {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo"}, true);
        si.addClassDepend(CatalogItem.class.getName());
        si.printDotSyntax(System.out);
    }
}
