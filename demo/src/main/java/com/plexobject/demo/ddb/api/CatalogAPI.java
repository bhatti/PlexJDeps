package com.plexobject.demo.ddb.api;

import com.plexobject.demo.ddb.controller.CatalogController;
import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.demo.service.GameServiceImpl;

public class CatalogAPI {
    CatalogController ctr = new CatalogController();
    GameServiceImpl gameService;

    public CatalogItem saveCatalog() {
        CatalogItem item = new CatalogItem(String.valueOf(System.currentTimeMillis()), "title1", "isbn");
        return ctr.saveCatalog(item);
    }
}
