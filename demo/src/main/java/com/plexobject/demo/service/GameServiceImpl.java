package com.plexobject.demo.service;
import com.plexobject.demo.dao.*;
import com.plexobject.demo.model.*;

public class GameServiceImpl implements GameService {
    GameDao dao;
    public Game getGame() {
        return dao.getGame();
    }
}
