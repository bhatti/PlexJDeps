package com.plexobject.demo.dao;

import com.plexobject.demo.model.Player;

public class PlayerDaoImpl implements PlayerDao {
    public Player getPlayer() {
        return new Player();
    }
}
