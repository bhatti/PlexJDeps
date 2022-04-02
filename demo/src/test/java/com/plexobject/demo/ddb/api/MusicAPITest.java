package com.plexobject.demo.ddb.api;

import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.demo.ddb.model.MusicItems;
import com.plexobject.deps.ShowDepend;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MusicAPITest {
    MusicAPI api = new MusicAPI();

    @Test
    void tesSaveMusicTrace() {
        api.saveMusic();
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(">>>>>>>>" + trace);
            }
        }
    }

    @Test
    void tesSaveMusicDeps() {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo", "com.amazonaws.services.dynamodbv2"}, false);
        si.addClassDepend(MusicAPI.class.getName());
        si.printDotSyntax(System.out);
    }
}
