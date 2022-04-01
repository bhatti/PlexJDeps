/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowDepends
 * Class Description:
 * ShowDepend shows dependencies statements for a class
 *
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 * None
 * Concurrency Issues:
 * None
 * Invariants:
 * N/A
 * Modification History
 * Initial      Date            Changes
 * SAB          Apr 22, 1999    Created
 */

package com.plexobject.deps;

import com.plexobject.db.DatabaseStore;
import com.plexobject.db.Dependency;
import com.plexobject.db.DependencyRepository;
import com.plexobject.db.RepositoryFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class ShowDepends {
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

    private Map dependencies = new HashMap();
    private List skipList = new ArrayList();
    private List mustList = new ArrayList();
    private static boolean verbose = false;
    private boolean packageOnly;
    private boolean dotSyntax;
    private String filter;
    private boolean inMemory;
    private String[] pkgNames;
    private String[] disallowedPackages = SUN_PACKAGES;
    private SpringParser springParser = new SpringParser();
    private JaxParser jaxParser = new JaxParser();
    private List<String> processed = new ArrayList<>();

    //
    public ShowDepends() {
    }

    public ShowDepends(boolean packageOnly, String[] pkgNames, boolean dotSyntax) {
        this.packageOnly = packageOnly;
        this.pkgNames = pkgNames;
        this.springParser.pkgNames = pkgNames;
        this.dotSyntax = dotSyntax;
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

    //
    public String[] getDepends(final String name) {
        if (!acceptClass(name)) {
            return new String[0];
        }
        Vector importlist = new Vector();
        try {
            if (includes(skipList, name)) {
                if (verbose) System.err.println("# ** found in skip list, skipping " + name);
                return new String[0];
            }
            if (mustList.size() > 0 && includes(mustList, name)) {
                if (verbose) System.err.println("# ** not in must list, skipping " + name);
                return new String[0];
            }
            ///
            Class clazz = Class.forName(name);
            Class type = TypesExtractor.getComponentType(clazz);

            String[] reftypes = TypesExtractor.extractTypesUsingReflection(type);
            for (int i = 0; i < reftypes.length; i++) {
                if (acceptClass(type, reftypes[i])) importlist.addElement(reftypes[i]);
            }

            Vector extracted = new Vector();
            TypesExtractor.extractTypesUsingJavap(type, extracted);
            Enumeration it = extracted.elements();
            while (it.hasMoreElements()) {
                String extype = (String) it.nextElement();
                if (importlist.indexOf(extype) == -1 && acceptClass(type, extype)) {
                    importlist.addElement(extype);
                }
            }
            String[] ltypes = TypesExtractor.extractTypesUsingListing(type);
            for (int i = 0; i < ltypes.length; i++) {
                if (importlist.indexOf(ltypes[i]) == -1 && acceptClass(type, ltypes[i])) {
                    importlist.addElement(ltypes[i]);
                }
            }
        } catch (java.lang.ClassNotFoundException e) {
            System.err.println("Failed to add (" + name + ") " + e); // + ", pkgNames " + Arrays.toString(pkgNames) + ", accept " + acceptClass(name));
        } catch (java.lang.NoClassDefFoundError e) {
            System.err.println("Failed to add (" + name + ") " + e);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        String[] imports = new String[importlist.size()];
        Enumeration it = importlist.elements();
        int i = 0;
        while (it.hasMoreElements()) {
            String fname = (String) it.nextElement();
            imports[i++] = fname.replace('$', '.');
        }
        Arrays.sort(imports);
        return imports;
    }

    public void saveDependencies() throws Exception {
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
                    String line = "\"" + key + "\"" + " -> " + "\"" + depend[i] + "\"";
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
        System.out.println("Saved " + all.size() + " dependencies");
        repo.close();
        store.close();
    }

    private static String getLastPart(String name) {
        String[] tnames = name.split("\\.");
        return tnames[tnames.length - 1];
    }

    void search(String name) throws Exception {
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
                System.out.println(name + " depends on:");
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

    private void printDotSyntax(final String filename) throws IOException {
        File file = new File(filename);
        if (verbose) System.err.println("# Writing " + file.getAbsolutePath());
        PrintStream out = new PrintStream(new FileOutputStream(file));
        printDotSyntax(out);
        out.close();
    }

    private void printDotSyntax(PrintStream out) {
        Map duplicates = new HashMap();
        out.println("digraph G {");
        Iterator it = dependencies.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String[] depend = (String[]) dependencies.get(key);
            for (int i = 0; depend != null && i < depend.length; i++) {
                if (acceptClass(depend[i])) {
                    String line = "\"" + key + "\"" + " -> " + "\"" + depend[i] + "\"";
                    String oline = "\"" + depend[i] + "\"" + " -> " + "\"" + key + "\"";
                    if (duplicates.get(line) == null) {
                        duplicates.put(line, Boolean.TRUE);
                        out.println(line);
                        if (duplicates.get(oline) != null) {
                            out.println("#--duplicate " + line);
                        }
                    }
                }
            }
        }
        out.println("}");
    }

    private boolean includes(List list, String pattern) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            if (pattern.indexOf(name) != -1) return true;
        }
        return false;
    }

    void addClassDepend(String klass) {
        if (processed.contains(klass)) {
            return;
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
        String[] deps = getDepends(klass);
        if (packageOnly) {
            klass = klass.substring(0, klass.lastIndexOf('.'));
            for (int i = 0; i < deps.length; i++) {
                deps[i] = deps[i].substring(0, deps[i].lastIndexOf('.'));
            }
        }
        dependencies.put(klass, deps);
        processed.add(klass);
        for (String dep : deps) {
            if (acceptClass(dep) && !processed.contains(dep)) {
                addClassDepend(dep);
            }
        }
        BeanInfo info = springParser.classToInfos.get(klass);
        addClassDependFromBeanInfo(info);
    }

    private void addClassDependFromBeanInfo(BeanInfo info) {
        if (info == null) {
            return;
        }
        for (BeanInfo dep : info.children) {
            if (acceptClass(dep.className) && !processed.contains(dep.className)) {
                addClassDepend(dep.className);
                addClassDependFromBeanInfo(dep);
            }
        }
    }

    private void addZipDepend(String zip) {
        JarResources jr = new JarResources(zip);
        String[] names = jr.getResourceNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].endsWith(".class")) addClassDepend(file2class(names[i]));
        }
    }


    private void addDirDepend(String dirname) {
        File dir = new File(dirname);
        List files = new ArrayList();
        getFiles(dir.getAbsolutePath().length(), dir, files);
        Iterator it = files.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            addClassDepend(file2class(name));
        }
    }


    private void getFiles(int top, File dir, List files) {
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

    private String file2class(String name) {
        int n = name.indexOf(".class");
        if (n != -1) name = name.substring(0, n);
        name = name.replace('\\', '.');
        name = name.replace('/', '.');
        return name;
    }


    ////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private boolean acceptClass(String name) {
        //if (processed.indexOf(name) != -1) return false;
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


    private boolean acceptClass(Class originaltype, String name) {
        //System.out.println("acceptClass(" + originaltype + "," + name + ")");
        if (name.startsWith("java.lang.") && name.indexOf('.', 10) == -1)
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

    public static void main(String[] args) throws Exception {
        ShowDepends si = new ShowDepends();
        if (System.getProperty(DISALLOWED_PACKAGES) != null) {
            Vector list = new Vector();
            StringTokenizer st = new StringTokenizer(System.getProperty(DISALLOWED_PACKAGES), ",");
            while (st.hasMoreTokens()) {
                list.addElement(st.nextToken().trim());
            }
            si.disallowedPackages = new String[list.size()];
            list.copyInto(si.disallowedPackages);
        } else {
            si.disallowedPackages = SUN_PACKAGES;
        }
        boolean init = true;

        String filename = null;
        List<String> todo = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            File file = new File(args[i]);
            if (args[i].equals("-P")) {
                si.packageOnly = true;
            } else if (args[i].equals("-dot")) {
                si.dotSyntax = true;
            } else if (args[i].equals("-f")) {
                si.filter = args[++i];
            } else if (args[i].equals("-p")) {
                String packages = args[++i];
                if (packages == null || packages.length() == 0) {
                    si.pkgNames = new String[0];
                    si.springParser.pkgNames = new String[0];
                } else {
                    StringTokenizer st = new StringTokenizer(packages, ",");
                    Vector list = new Vector();
                    while (st.hasMoreTokens()) {
                        String next = st.nextToken();
                        list.addElement(next.trim());
                    }
                    si.pkgNames = new String[list.size()];
                    list.copyInto(si.pkgNames);
                    si.springParser.pkgNames = si.pkgNames;
                }
            } else if (args[i].equals("-search")) {
                init = false;
            } else if (args[i].equals("-v")) {
                SpringParser.verbose = true;
                verbose = true;
            } else if (args[i].equals("-m")) {
                si.inMemory = true;
            } else if (args[i].equals("-o")) {
                filename = args[++i];
                if (verbose) System.err.println("# will save output to " + filename);
            } else if (args[i].equals("-s")) {
                si.skipList.add(args[++i]);
                if (verbose) System.err.println("# adding to skip list: " + args[i]);
            } else if (args[i].equals("-m")) {
                si.mustList.add(args[++i]);
                if (verbose) System.err.println("# adding to must list: " + args[i]);
            } else if (args[i].endsWith(".jar") || args[i].endsWith(".zip")) si.addZipDepend(args[i]);
            else if (file.exists() && file.isDirectory()) {
                si.addDirDepend(args[i]);
            } else if (file.exists() && file.isFile()) {
                todo.add(si.file2class(args[i]));
            } else {
                todo.add(args[i]);
            }
        }

        if (init) {
            for (String d : todo) {
                si.addClassDepend(d);
            }
            si.addSpringClasses();
            si.saveDependencies();
        } else {
            si.addJaxClasses();
            for (String d : todo) {
                System.out.println("Searching " + d);
                si.search(d);
            }
        }
    }
}


