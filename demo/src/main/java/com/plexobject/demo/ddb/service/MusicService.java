package com.plexobject.demo.ddb.service;

import com.plexobject.demo.ddb.api.MusicAPI;
import com.plexobject.demo.ddb.dao.DDBMapper;
import com.plexobject.demo.ddb.model.MusicItems;

public class MusicService {
    DDBMapper mapper = new DDBMapper();
    MusicAPI api;
    public MusicItems saveMusic(MusicItems items) {
        return mapper.updateMusic(items);
    }

}
