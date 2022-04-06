package com.plexobject.deps;

import com.plexobject.aop.Trace;
import com.plexobject.aop.TraceCollector;
import com.plexobject.db.DatabaseStore;
import com.plexobject.db.Dependency;
import com.plexobject.db.DependencyRepository;
import com.plexobject.db.RepositoryFactory;
import com.plexobject.graph.UMLDiagrams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
    boolean inMemory;
    boolean dotSyntax;
    String filter;

    String[] disallowedPackages = BaseDepHelper.SUN_PACKAGES;
    final Map dependencies = new HashMap();
    final List skipList = new ArrayList();
    final List mustList = new ArrayList();
    final List<String> processed = new ArrayList<>();
    final SpringParser springParser = new SpringParser();
    final JaxParser jaxParser = new JaxParser();


    public void printDotSyntax(final String filename) {
        PrivilegedAction action = new PrivilegedAction() {
            public Object run() {
                try {
                    File file = new File(filename);
                    if (verbose) System.err.println("# Writing " + file.getAbsolutePath());
                    PrintStream out = new PrintStream(new FileOutputStream(file));
                    printDotSyntax(out, file.getName());
                    out.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        AccessController.doPrivileged(action);
    }

    public void printDotSyntax(PrintStream out, String title) {
        Map duplicates = new HashMap();
        out.println("digraph G {");
        out.println("  fontname=\"Helvetica,Arial,sans-serif\"");
        out.println("  node [fontname=\"Helvetica,Arial,sans-serif\"]");
        out.println("  edge [fontname=\"Helvetica,Arial,sans-serif\"]");
        out.println("  labelloc=\"t\"");
        out.println("  label=\"" + title + "\"");
        out.println("  graph [splines=false]");
        out.println("  node [shape=record style=filled fillcolor=gray95]");
        out.println("  edge [arrowhead=vee style=dashed]");
        Iterator it = dependencies.keySet().iterator();
        Set visited = new HashSet();
        while (it.hasNext()) {
            String key = (String) it.next();
            printDotSyntax(out, duplicates, key, visited);
        }
        out.println("}");
    }

    public void printDotSyntax(PrintStream out, Map duplicates, String key, Set visited) {
        if (visited.contains(key)) {
            return;
        }
        visited.add(key);
        if (!acceptClass(key)) {
            return;
        }
        String[] depend = (String[]) dependencies.get(key);
        if (depend == null) {
            return;
        }
        for (int i = 0; i < depend.length; i++) {
            String to = depend[i];
            printDotSyntax(out, duplicates, key, to);
            printDotSyntax(out, duplicates, to, visited);
        }
    }

    void printDotSyntax(PrintStream out, Map duplicates, String key, String to) {
        if (acceptClass(to)) {
            String dotKey = Dependency.getClassName(key);
            String dotTo = Dependency.getClassName(to);
            if (duplicates.get(key + "_node") == null) {
                duplicates.put(key + "_node", Boolean.TRUE);
                out.println("  " + dotKey + "[label = <{<b>«" + key + "»</b> | " + getDotClassFields(key) + "}>]");
            }
            if (duplicates.get(to + "_node") == null) {
                duplicates.put(to + "_node", Boolean.TRUE);
                out.println("  " + dotTo + "[label = <{<b>«" + to + "»</b> | " + getDotClassFields(to) + "}>]");
            }
            String line = "  \"" + dotKey + "\"" + " -> " + "\"" + dotTo + "\"";
            String oline = "  \"" + dotTo + "\"" + " -> " + "\"" + dotKey + "\"";
            if (duplicates.get(line) == null) {
                duplicates.put(line, Boolean.TRUE);
                out.println(line);
                if (duplicates.get(oline) != null) {
                    //out.println("#--duplicate " + line);
                }
            }
        }
    }

    public void saveDependencies() {
        Map duplicates = new HashMap();
        Iterator it = dependencies.keySet().iterator();
        DatabaseStore store = new DatabaseStore();
        RepositoryFactory factory = new RepositoryFactory(store, inMemory);
        DependencyRepository repo = factory.getDependencyRepository();
        repo.clear();
        while (it.hasNext()) {
            String key = (String) it.next();
            String[] depend = (String[]) dependencies.get(key);
            for (int i = 0; depend != null && i < depend.length; i++) {
                if (acceptClass(depend[i])) {
                    String line = "  \"" + key + "\"" + " -> " + "\"" + depend[i] + "\"";
                    if (duplicates.get(line) == null) {
                        duplicates.put(line, Boolean.TRUE);
                        Dependency dep = new Dependency(key, depend[i]);
                        BeanInfo info = springParser.classToInfos.get(key);
                        if (info != null) {
                            for (BeanInfo child : info.children) {
                                if (child.className.equals(depend[i])) {
                                    dep.setSpringDI(true);
                                    break;
                                }
                            }
                        }
                        //System.out.println("Saving " + line);System.out.flush();
                        repo.save(dep);
                    }
                }
            }
        }
        Set<Dependency> all = repo.getAll();
        System.err.println("Saved " + all.size() + " dependencies");
        repo.close();
        store.close();
    }

    static String getLastPart(String name) {
        String[] tnames = name.split("\\.");
        return tnames[tnames.length - 1];
    }

    boolean acceptClass(String name) {
        if (name.contains("Exception") || name.contains("$") || name.startsWith("java")) {
            return false;
        }
        if (includes(skipList, name)) {
            if (verbose) System.err.println("# found in skip list, skipping " + name);
            return false;
        }
        if (mustList.size() > 0 && !includes(mustList, name)) {
            if (verbose) System.err.println("# not in must list, skipping " + name);
            return false;
        }
        for (int i = 0; i < disallowedPackages.length; i++) {
            if (name.startsWith(disallowedPackages[i])) return false;
        }
        for (int i = 0; i < SUN_CLASSES.length; i++) {
            if (name.equals(SUN_CLASSES[i])) return false;
        }
        if (pkgNames == null || pkgNames.length == 0) {
            return true;
        }
        for (int j = 0; j < pkgNames.length; j++) {
            if (name.startsWith(pkgNames[j])) {
                //if (verbose) System.err.println("# accepting " + name + "--- " + pkgNames[j]);
                return true;
            }
        }
        //if (verbose) System.err.println("# rejecting " + name);
        return false;
    }


    boolean acceptClass(Class originaltype, String name) {
        if (name.contains("Exception") || name.contains("$") || name.startsWith("java")) {
            return false;
        }
        // inner or nested class
        if (name.startsWith(originaltype.getName())) return false;
        if (includes(skipList, name)) {
            if (verbose) System.err.println("# found in skip list, skipping " + name);
            return false;
        }
        if (mustList.size() > 0 && !includes(mustList, name)) {
            if (verbose) System.err.println("# not in must list, skipping " + name);
            return false;
        }
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

    public void addSpringClasses() {
        springParser.addAllSpringFiles();
        for (String klass : springParser.classToInfos.keySet()) {
            addClassDepend(klass);
        }
    }

    public void addJaxClasses() {
        jaxParser.addAllJaxFiles();
    }

    public void addClassDepend(String klass) {
        if (processed.contains(klass)) {
            return;
        }
        processed.add(klass);
        if (!acceptClass(klass)) {
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
        if (mustList.size() > 0 && !includes(mustList, klass)) {
            if (verbose) System.err.println("# not in must list, skipping " + klass);
            return;
        }
        if (verbose) System.err.println("# adding " + klass);
        String[] deps = getDepends(klass);
        for (String d : deps) {
            addClassDepend(d);
        }
        if (packageOnly) {
            klass = klass.substring(0, klass.lastIndexOf('.'));
            for (int i = 0; i < deps.length; i++) {
                deps[i] = deps[i].substring(0, deps[i].lastIndexOf('.'));
            }
        }
        dependencies.put(klass, deps);
    }

    public void search(String name) throws Exception {
        Set<String> duplicates = new HashSet<>();
        RepositoryFactory factory = new RepositoryFactory();
        DependencyRepository repo = factory.getDependencyRepository();
        Set<Dependency> from = repo.getDependenciesFrom(name);
        Set<Dependency> to = repo.getDependenciesTo(name);
        //
        if (dotSyntax) {
            System.out.println("digraph G {");
            System.out.println("\"" + getLastPart(name) + "\" [shape=polygon, sides=5, peripheries=3, color=purple, style=filled];");
        }
        if (from.size() > 0) {
            if (!dotSyntax) {
                System.err.println(name + " depends on:");
            }
            for (Dependency d : from) {
                if (filter == null || d.to.contains(filter)) {
                    String url = jaxParser.classToUrl.get(d.to);
                    String suffix = url != null ? ", service endpoint " + url : "";
                    if (dotSyntax) {
                        System.out.println("\"" + getLastPart(name) + "\" -> \"" + getLastPart(d.to) + "\" [shape = box];");
                    } else {
                        System.out.println("\t" + d.to + suffix);
                    }
                }
            }
        }
        if (to.size() > 0) {
            if (!dotSyntax) {
                System.out.println(name + " depended by:");
            }
            for (Dependency d : to) {
                if (filter == null || d.from.contains(filter)) {
                    String url = jaxParser.classToUrl.get(d.from);
                    if (dotSyntax) {
                        String suffix = url != null ? ", label=\"URI:" + url + "\"" : "";
                        System.out.println("\"" + getLastPart(d.from) + "\" -> \"" + getLastPart(name) + "\" [shape = circle" + suffix + "];");
                    } else {
                        String suffix = url != null ? ", URI=" + url : "";
                        System.out.println("\t" + d.from + suffix);
                    }
                }
            }

            //
            Set<Dependency> indirect = new HashSet<>();
            for (Dependency d : to) {
                if (!duplicates.contains(d.from)) {
                    indirectDeps(d.from, indirect, repo, duplicates);
                }
            }
            if (indirect.size() > 0) {
                if (!dotSyntax) {
                    System.out.println(name + " indirectly depended by:");
                }
                for (Dependency d : indirect) {
                    if (filter == null || d.from.contains(filter)) {
                        String url = jaxParser.classToUrl.get(d.from);
                        if (dotSyntax) {
                            String suffix = url != null ? ", label=\"URI:" + url + "\"" : "";
                            if (name.equals(d.from) || name.equals(d.to)) {
                                System.out.println("\"" + getLastPart(d.from) + "\" -> \"" + getLastPart(d.to) + "\" [shape = circle" + suffix + "];");
                            } else {
                                System.out.println("\"" + getLastPart(d.from) + "\" -> \"" + getLastPart(d.to) + "\" [shape = invtriangle, style=dotted" + suffix + "];");
                            }
                        } else {
                            String suffix = url != null ? ", URI=" + url : "";
                            System.out.println("\t" + d.from + suffix);
                        }
                    }
                }
            }
        }
        if (dotSyntax) {
            System.out.println("}");
        }
        repo.close();
    }

    private void indirectDeps(String name, Set<Dependency> indirect, DependencyRepository repo, Set<String> duplicates) throws Exception {
        if (duplicates.contains(name)) {
            return;
        }
        duplicates.add(name);
        Set<Dependency> to = repo.getDependenciesTo(name);
        for (Dependency d : to) {
            if (!duplicates.contains(d.from)) {
                indirect.add(d);
                indirectDeps(d.from, indirect, repo, duplicates);
            }
        }
    }


    String getDotClassFields(String t) {
        try {
            Class clazz = Class.forName(t);
            Field[] fields = clazz.getDeclaredFields();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 2 && i < fields.length; i++) {
                sb.append(" +" + fields[i].getName() + " <br align=\"left\"/>");
            }
            sb.append("...<br align=\"left\"/>");
            return sb.toString();
        } catch (Throwable e) {
            return " ...<br align=\"left\"/>";
        }
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

    static String[] getDisallowedPackages() {
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

    static void sequenceDigrams(String main, String[] args) {
        try {
            Class clazz = Class.forName(main);
            Method m = clazz.getDeclaredMethod("main", String[].class);
            m.invoke(null, (Object[]) args);
            for (List<Trace> traces : TraceCollector.getInstance().getAll().values()) {
                for (Trace trace : traces) {
                    System.out.println(trace.buildSequenceConfig());
                    ByteArrayOutputStream out = new UMLDiagrams().createSequence(trace.buildSequenceConfig());
                    FileOutputStream png = new FileOutputStream(trace.getMethods().get(0) + ".png");
                    png.write(out.toByteArray());
                    png.close();
                }
            }
        } catch (ClassNotFoundException e) {
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
}
