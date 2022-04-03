package com.plexobject.demo.ddb.controller;

import com.plexobject.demo.ddb.model.MusicItems;
import com.plexobject.demo.ddb.service.MusicService;

public class MusicController {
    MusicService svc = new MusicService();

    public MusicItems saveMusic(MusicItems items) {
        return svc.saveMusic(items);
    }
}
