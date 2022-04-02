package com.plexobject.demo.ddb.api;

import com.plexobject.demo.ddb.controller.MusicController;
import com.plexobject.demo.ddb.model.MusicItems;
import com.plexobject.demo.service.PlayerServiceImpl;

public class MusicAPI {
    MusicController ctr = new MusicController();
    PlayerServiceImpl playerService;

    public MusicItems saveMusic() {
        return ctr.saveMusic();
    }
}
