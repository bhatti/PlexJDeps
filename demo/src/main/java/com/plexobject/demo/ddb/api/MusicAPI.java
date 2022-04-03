package com.plexobject.demo.ddb.api;

import com.plexobject.demo.ddb.controller.MusicController;
import com.plexobject.demo.ddb.model.MusicItems;
import com.plexobject.demo.service.PlayerServiceImpl;

public class MusicAPI {
    MusicController ctr = new MusicController();
    PlayerServiceImpl playerService;

    public MusicItems saveMusic() {
        MusicItems items = new MusicItems();
        items.setArtist("artist1" + System.currentTimeMillis());
        items.setSongTitle("songTitle1");
        items.setAlbumTitle("albumTitle1");
        items.setAwards(11);
        return ctr.saveMusic(items);
    }
}
