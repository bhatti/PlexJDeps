package com.plexobject.deps;
import java.util.*;
import java.io.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class SpringParser {
    static boolean verbose = false;
    Map<String, BeanInfo> idToInfos = new HashMap<>();
    Map<String, BeanInfo> classToInfos = new HashMap<>();
    Map<String, String> idToClass = new HashMap<>();
    Map<String, String> unresolvedDeps = new HashMap<>();
    String[] pkgNames;
    public void add(File inputFile) {
        if (!inputFile.exists()) {
            System.out.println("Spring file doesn't exist: " + inputFile);
            return;
        }
        try {
            if (verbose) System.out.println("Parsing " + inputFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList beans = doc.getElementsByTagName("bean");
            for (int i=0; i < beans.getLength(); i++) {
                Node node = beans.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element bean = (Element) node;
                    String id = bean.getAttribute("id");
                    if (id == null || id.length() == 0) {
                        id = bean.getAttribute("name");
                    }
                    String klass = bean.getAttribute("class");
                    String ref = null;
                    //
                    NodeList properties = bean.getElementsByTagName("property");
                    for (int j=0; j < properties.getLength(); j++) {
                        Node pnode = properties.item(j);
                        if (pnode.getNodeType() == Node.ELEMENT_NODE) {
                            Element property = (Element) pnode;
                            ref = property.getAttribute("ref");
                            if (ref == null || ref.length() == 0) {
                                NodeList refs = property.getElementsByTagName("ref");
                                for (int k=0; k < refs.getLength(); k++) {
                                    Node rnode = refs.item(k);
                                    if (rnode.getNodeType() == Node.ELEMENT_NODE) {
                                        Element refnode = (Element) rnode;
                                        ref = refnode.getAttribute("bean");
                                    }
                                }
                            }
                        }
                    }
                    if (isValidClass(klass)) {
                        if (id == null || id.length() == 0) {
                            id = klass;
                        }
                        BeanInfo info = new BeanInfo(id, klass);
                        idToClass.put(id, klass);
                        idToInfos.put(id, info);
                        classToInfos.put(klass, info);
                        if (ref != null) {
                            BeanInfo rinfo = idToInfos.get(ref);
                            if (rinfo != null) {
                                info.addChild(rinfo);
                            } else {
                                unresolvedDeps.put(id, ref);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    private boolean isValidClass(String klass) {
        if (klass == null || klass.length() == 0) {
            return false;
        }
        if (pkgNames == null || pkgNames.length == 0) {
            return true;
        }
        for (String pkg : pkgNames) {
            if (klass.startsWith(pkg)) {
                return true;
            }
        }
        return true;
    }

    ///
    public void aggregate() {
        for (String dep : new ArrayList<String>(unresolvedDeps.keySet())) {
            String rdep = unresolvedDeps.get(dep);
            BeanInfo info = idToInfos.get(dep);
            BeanInfo rinfo = idToInfos.get(rdep);
            if (info != null && rinfo != null) {
                info.addChild(rinfo);
            } else { 
                //System.out.println("Could not resolve " + dep + "->" + rdep);
            }
        }
        for (BeanInfo info : idToInfos.values()) {
            if (verbose) System.out.println(info);
        }
    }
    public void addAllSpringFiles() {
        add(new File("spring-app.xml"));
        Collection<File> files = getSpringFiles(new File("."));
        for (File file : files) {
            add(file);
        }
        aggregate();
    }
    //
    private static Collection<File> getSpringFiles(File dir) {
        Set<File> files = new HashSet<File>();
        for (File e : dir.listFiles()) {
            if (e.isFile() && e.getName().endsWith(".xml") && !e.getAbsolutePath().contains("target")) {
                try {
                    Path path = Paths.get(e.getAbsolutePath());
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    for (String line : lines) {
                        if (line.contains("<bean")) {
                            files.add(e);
                            break;
                        }
                    }
                } catch (Exception ex) {
                }
            } else if (e.isDirectory()) {
                files.addAll(getSpringFiles(e));
            }
        }
        return files;
    }

    public static void main(String[] args) throws Exception {
        SpringParser parser = new SpringParser();
        for (String arg : args) {
            parser.add(new File(arg));
        }
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-p")) {
                String packages = args[++i];
                if (packages == null || packages.length() == 0) {
                    parser.pkgNames = new String[0];
                } else {
                    parser.pkgNames = packages.split(",");
                }
            } else if (args[i].equals("-v")) {
                verbose = true;
            } else {
                File file = new File(args[i]);
                parser.add(file);
            }
        }
        if (args.length == 0) {
            parser.addAllSpringFiles();
        }
        parser.aggregate();
    }
}
