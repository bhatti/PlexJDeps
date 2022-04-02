package com.plexobject.demo.ddb.service;

import com.plexobject.demo.ddb.dao.DDBMapper;
import com.plexobject.demo.ddb.model.MusicItems;

public class MusicService {
    DDBMapper mapper = new DDBMapper();

    public MusicItems saveMusic() {
        return mapper.saveMusic();
    }

}
