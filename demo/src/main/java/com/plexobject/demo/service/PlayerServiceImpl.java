package com.plexobject.demo.service;
import com.plexobject.demo.dao.*;
import com.plexobject.demo.model.*;

public class PlayerServiceImpl implements PlayerService {
    PlayerDao dao;
    public Player getPlayer() {
        return dao.getPlayer();
    }
}
