package com.plexobject.demo.ddb.controller;

import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.demo.ddb.service.CatalogService;

public class CatalogController {
    CatalogService svc = new CatalogService();

    public CatalogItem saveCatalog() {
        return svc.saveCatalog();
    }
}
