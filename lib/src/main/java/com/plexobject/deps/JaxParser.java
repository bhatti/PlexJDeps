package com.plexobject.deps;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class JaxParser {
    static boolean verbose = false;
    Map<String, String> classToUrl = new HashMap<>();
    String[] pkgNames;

    public void add(File inputFile) {
        try {
            if (verbose) System.err.println("Parsing " + inputFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList beans = doc.getElementsByTagName("jaxws:endpoint");
            for (int i = 0; i < beans.getLength(); i++) {
                Node node = beans.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element bean = (Element) node;
                    String klass = bean.getAttribute("implementorClass");
                    String url = bean.getAttribute("address");
                    if (klass != null && klass.length() > 0 && url != null && url.length() > 0) {
                        classToUrl.put(klass, url);
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

    public void addAllJaxFiles() {
        Collection<File> files = getJaxFiles(new File("."));
        for (File file : files) {
            add(file);
        }
    }

    //
    private static Collection<File> getJaxFiles(File dir) {
        Set<File> files = new HashSet<File>();
        for (File e : dir.listFiles()) {
            if (e.isFile() && e.getName().endsWith(".xml") && !e.getAbsolutePath().contains("target")) {
                try {
                    Path path = Paths.get(e.getAbsolutePath());
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    for (String line : lines) {
                        if (line.contains("<jaxws:")) {
                            files.add(e);
                            break;
                        }
                    }
                } catch (Exception ex) {
                }
            } else if (e.isDirectory()) {
                files.addAll(getJaxFiles(e));
            }
        }
        return files;
    }


    public static void main(String[] args) throws Exception {
        JaxParser parser = new JaxParser();
        for (String arg : args) {
            parser.add(new File(arg));
        }
        for (int i = 0; i < args.length; i++) {
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
        parser.addAllJaxFiles();
    }
}
