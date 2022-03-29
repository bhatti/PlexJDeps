package com.plexobject.demo.dao;

import com.plexobject.demo.model.Game;

public class GameDaoImpl implements GameDao {
    public Game getGame() {
        return new Game();
    }
}
