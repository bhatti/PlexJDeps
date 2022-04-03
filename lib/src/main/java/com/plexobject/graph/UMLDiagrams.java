package com.plexobject.graph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.Run;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UMLDiagrams {
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public Future<BufferedImage> asyncClassDep(String dot, int width, int height) throws IOException {
        return executor.submit(() -> {
            Graphviz.useEngine(new GraphvizCmdLineEngine());
            MutableGraph g = new Parser().read(dot);
            return Graphviz.fromGraph(g).width(width).height(height).render(Format.PNG).toImage();
        });
    }

    public ByteArrayOutputStream createClassDep(String dot, int width, int height) throws IOException, ExecutionException, InterruptedException {
        BufferedImage image = asyncClassDep(dot, width, height).get();
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ImageIO.write(image, "png", arr);
        return arr;
    }

    public ByteArrayOutputStream createSequence(String seq) throws IOException, InterruptedException {
        String[] args = new String[]{"-p"};
        final Option option = new Option(args);
        BufferedReader br = new BufferedReader(new StringReader(seq));
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(arr);
        Run.managePipe(option, br, ps);
        return arr;
    }
}
