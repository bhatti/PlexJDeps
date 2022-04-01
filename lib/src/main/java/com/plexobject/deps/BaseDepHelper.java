package com.plexobject.deps;

import com.plexobject.db.Dependency;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

public abstract class BaseDepHelper {
    public static final String DISALLOWED_PACKAGES = "DISALLOWED_PACKAGES";
    public static final String[] SUN_PACKAGES = {
            "java.",
            "javax.",
            "sun.",
            "com.sun.",
            "org.omg."
    };
    public static final String[] SUN_CLASSES = {
            "boolean",
            "byte",
            "char",
            "float",
            "double",
            "int",
            "long",
            "short",
            "I",
            "B",
            "C",
            "F",
            "D",
            "J",
            "S",
            "V",
            "Z",
            "void"
    };
    boolean packageOnly;
    String[] pkgNames;
    boolean checkSM = true;
    boolean verbose = false;

    String[] disallowedPackages = BaseDepHelper.SUN_PACKAGES;
    Map dependencies = new HashMap();
    List skipList = new ArrayList();
    List mustList = new ArrayList();

    public void printDotSyntax(PrintStream out) {
        Map duplicates = new HashMap();
        out.println("digraph G {");
        Iterator it = dependencies.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            printDotSyntax(out, duplicates, key);
        }
        out.println("}");
    }

    public void printDotSyntax(PrintStream out, Map duplicates, String key) {
        String[] depend = (String[]) dependencies.get(key);
        if (depend == null) {
            return;
        }
        for (int i = 0; i < depend.length; i++) {
            String to = depend[i];
            printDotSyntax(out, duplicates, key, to);
            printDotSyntax(out, duplicates, to);
        }
    }

    void printDotSyntax(PrintStream out, Map duplicates, String key, String to) {
        if (acceptClass(to)) {
            String line = "\"" + Dependency.getClassName(key) + "\"" + " -> " + "\"" + Dependency.getClassName(to) + "\"";
            String oline = "\"" + Dependency.getClassName(to) + "\"" + " -> " + "\"" + Dependency.getClassName(key) + "\"";
            if (duplicates.get(line) == null) {
                duplicates.put(line, Boolean.TRUE);
                out.println(line);
                if (duplicates.get(oline) != null) {
                    out.println("#--duplicate " + line);
                }
            }
        }
    }

    boolean acceptClass(String name) {
        if (name.contains("Exception") || name.startsWith("java.lang.") && name.indexOf('.', 10) == -1)
            return false;
        for (int i = 0; i < disallowedPackages.length; i++) {
            if (name.startsWith(disallowedPackages[i])) return false;
        }
        for (int i = 0; i < SUN_CLASSES.length; i++) {
            if (name.equals(SUN_CLASSES[i])) return false;
        }
        if (pkgNames == null || pkgNames.length == 0) return true;
        for (int j = 0; j < pkgNames.length; j++) {
            if (name.startsWith(pkgNames[j])) return true;
        }
        return false;
    }


    boolean acceptClass(Class originaltype, String name) {
        if (name.contains("Exception") || name.startsWith("java.lang.") && name.indexOf('.', 10) == -1)
            return false;
        // inner or nested class
        if (name.startsWith(originaltype.getName())) return false;
        // same package
        if (originaltype.getPackage() != null &&
                name.startsWith(originaltype.getPackage().getName()) &&
                name.indexOf('.', originaltype.getPackage().getName().length() + 2) == -1)
            return false;

        // skip anonymous classes
        int n = name.lastIndexOf('$');
        if (n != -1 && Character.isDigit(name.charAt(n + 1))) {
            return false;
        }
        for (int i = 0; i < SUN_CLASSES.length; i++) {
            if (name.equals(SUN_CLASSES[i])) return false;
        }
        return true;
    }

    public void addClassDepend(String klass) {
        Set visited = new HashSet();
        addClassDepend(klass, visited);
    }

    public void addClassDepend(String klass, Set visited) {
        if (visited.contains(klass)) {
            return;
        }
        if (checkSM && System.getSecurityManager() != null) {
            if (verbose) System.err.println("# set security manager");
            checkSM = false;
        }
        if (includes(skipList, klass)) {
            if (verbose) System.err.println("# found in skip list, skipping " + klass);
            return;
        }
        if (mustList.size() > 0 && includes(mustList, klass)) {
            if (verbose) System.err.println("# not in must list, skipping " + klass);
            return;
        }
        if (verbose) System.err.println("# adding " + klass);
        visited.add(klass);
        String[] deps = getDepends(klass);
        for (String d : deps) {
            addClassDepend(d, visited);
        }
        if (packageOnly) {
            klass = klass.substring(0, klass.lastIndexOf('.'));
            for (int i = 0; i < deps.length; i++) {
                deps[i] = deps[i].substring(0, deps[i].lastIndexOf('.'));
            }
        }
        dependencies.put(klass, deps);
    }


    public void addZipDepend(String zip) throws Exception {
        JarResources jr = new JarResources(zip);
        String[] names = jr.getResourceNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].endsWith(".class")) addClassDepend(file2class(names[i]));
        }
    }


    public void addDirDepend(String dirname) throws Exception {
        File dir = new File(dirname);
        List files = new ArrayList();
        getFiles(dir.getAbsolutePath().length(), dir, files);
        Iterator it = files.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            addClassDepend(file2class(name));
        }
    }


    private void getFiles(int top, File dir, List files) throws Exception {
        File[] list = dir.listFiles();
        for (int i = 0; list != null && i < list.length; i++) {
            if (list[i].isDirectory()) getFiles(top, list[i], files);
            else {
                String name = list[i].getAbsolutePath();
                if (name.endsWith(".class")) files.add(name.substring(top + 1));
                else if (name.endsWith(".jar")) addZipDepend(name);
            }
        }
    }

    String file2class(String name) {
        int n = name.indexOf(".class");
        if (n != -1) name = name.substring(0, n);
        name = name.replace('\\', '.');
        name = name.replace('/', '.');
        return name;
    }

    public static String[] getDisallowedPackages() {
        if (System.getProperty(BaseDepHelper.DISALLOWED_PACKAGES) != null) {
            Vector list = new Vector();
            StringTokenizer st = new StringTokenizer(
                    System.getProperty(BaseDepHelper.DISALLOWED_PACKAGES), ",");
            while (st.hasMoreTokens()) {
                list.addElement(st.nextToken().trim());
            }
            String[] disallowedPackages = new String[list.size()];
            list.copyInto(disallowedPackages);
            return disallowedPackages;
        } else {
            return BaseDepHelper.SUN_PACKAGES;
        }
    }

    public String[] getDepends(final String name) {
        return new String[0];
    }

    boolean includes(List list, String pattern) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            if (pattern.indexOf(name) != -1) return true;
        }
        return false;
    }

}
