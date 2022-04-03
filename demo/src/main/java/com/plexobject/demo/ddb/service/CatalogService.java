package com.plexobject.demo.ddb.service;

import com.plexobject.demo.ddb.dao.DDBMapper;
import com.plexobject.demo.ddb.model.CatalogItem;

public class CatalogService {
    DDBMapper mapper = new DDBMapper();

    public CatalogItem saveCatalog(CatalogItem item) {
        return mapper.saveCatalog(item);
    }

}
