package com.plexobject.demo.ddb.service;

import com.plexobject.demo.ddb.dao.DDBMapper;
import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.demo.ddb.model.MusicItems;

public class CatalogService {
    DDBMapper mapper = new DDBMapper();

    public CatalogItem saveCatalog() {
        return mapper.saveCatalog();
    }

}
