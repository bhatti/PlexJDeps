package com.plexobject.demo.ddb.api;

import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.demo.service.GameServiceImpl;
import com.plexobject.deps.ShowDepend;
import com.plexobject.graph.UMLDiagrams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MusicAPITest {
    MusicAPI api = new MusicAPI();

    @AfterEach
    void teardown() {
        System.out.println("*** Traces ****");
        TraceCollector.getInstance().dump();
    }

    @Test
    void tesSaveMusicTrace() throws IOException, InterruptedException {
        api.saveMusic();
        for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
            for (Trace trace : traces) {
                System.out.println(trace.buildSequenceConfig());
                ByteArrayOutputStream out = new UMLDiagrams().createSequence(trace.buildSequenceConfig());
                FileOutputStream png = new FileOutputStream(trace.getMethods().get(0) + ".png");
                png.write(out.toByteArray());
                png.close();
            }
        }
    }

    @Test
    void testShowDependPackages() throws Exception {
        ShowDepend si = new ShowDepend(true, new String[]{"com.plexobject.demo"}, false);
        si.addClassDepend(MusicAPI.class.getName());
        si.printDotSyntax(System.out, "");
    }

    @Test
    void testShowDepend() throws Exception {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo"}, false);
        si.addClassDepend(MusicAPI.class.getName());
        si.printDotSyntax(System.out, "");
    }

    @Test
    void tesSaveMusicDeps() throws IOException, ExecutionException, InterruptedException {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo", "com.amazonaws.services.dynamodbv3"}, false);
        si.addClassDepend(MusicAPI.class.getName());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);
        si.printDotSyntax(out, "");
        String dot = new String(bout.toByteArray());
        System.out.println(dot);
        //bout = new UMLDiagrams().createClassDep(dot, 800, 1200);
        //FileOutputStream png = new FileOutputStream("musc_deps.png");
        //png.write(bout.toByteArray());
        //png.close();
    }
}
