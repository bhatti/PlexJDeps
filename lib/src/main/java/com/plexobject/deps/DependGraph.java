/**
 * <B>CLASS COMMENTS</B>
 * Class Name: DependGraph
 * Class Description:
 * DependGraph shows dependency graph
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

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

public class DependGraph {
    private class DependencyLocator extends ClassLoader {
        private DependencyLocator() {
            cache = new java.util.Hashtable();
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, true);
        }

        public synchronized Class loadClass(String name,
                                            boolean resolveIt) throws ClassNotFoundException {
            Class result = null;
            result = (Class) cache.get(name);
            if (result != null) return result;
            result = findClass(name);
            if (result != null && resolveIt) resolveClass(result);
            cache.put(name, result);
            return result;
        }

        public Class findClass(String name) throws ClassNotFoundException {
            if (!acceptClass(name)) {
                return super.findSystemClass(name);
            }
            byte[] buffer = loadClassData(name);
            if (buffer == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, buffer, 0, buffer.length);
        }

        private byte[] loadClassData(String name) {
            //if (name == null || name.length() == 0 || name.indexOf("$") != -1) return null;
            java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
            try {
                String resource = name.replace('.', separator).concat(".class");
                java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(
                        resource);
                if (in == null) {
                    System.err.println("Resource " + resource + " not found");
                    return null;
                }
                String path = getPath(name);
                if (path != null && printed.indexOf(path) == -1) {
                    printed.addElement(path);
                }
                location = getClass().getClassLoader().getResource(resource).toExternalForm();
                int ndx = location.indexOf(resource);
                if (ndx != -1) location = location.substring(0, ndx);
                if (location.startsWith("file:")) location = location.substring(5);

                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    result.write(buffer, 0, len);
                }
                return result.toByteArray();
            } catch (java.io.IOException e) {
                return null;
            }
        }

        private java.util.Hashtable cache;
        private String location;
    }


    public DependGraph(String[] pkgNames, boolean deep)
            throws java.io.IOException {
        this.pkgNames = pkgNames;
        this.deep = deep;
        locator = this.new DependencyLocator();
        processed = new Vector();
        printed = new Vector();
        disallowedPackages = BaseDepHelper.getDisallowedPackages();
        if ("true".equals(System.getProperty("debug")) ||
                "true".equals(System.getProperty("verbose"))) debug = true;
    }

    public void add(final String name)
            throws ClassNotFoundException, java.io.IOException {
        String path = getPath(name);
        if (path != null && printed.indexOf(path) == -1) {
            if (path.indexOf('$') == -1) {
                //System.out.println(path + ":");
            }
            printed.addElement(path);
        }
        _add(name);
        //System.out.println();
    }

    private void _add(final String name)
            throws ClassNotFoundException, java.io.IOException {
        Class clazz = locator.loadClass(name);
        Class type = TypesExtractor.getComponentType(clazz);

        if (processed.indexOf(type.getName()) != -1) return;
        if (clazz.isArray()) {
            locator.loadClass(type.getName());
        }
        processed.addElement(type.getName());
        if (debug) System.err.println("adding " + type.getName());

        String[] reftypes = TypesExtractor.extractTypesUsingReflection(type);
        for (int i = 0; i < reftypes.length; i++) {
            if (debug) System.err.println("reflect type " + reftypes[i]);
            //if (deep && acceptClass(reftypes[i])) _add(reftypes[i]);
            if (acceptClass(reftypes[i])) _add(reftypes[i]);
        }


        if (deep) {
            Vector extracted = new Vector();
            TypesExtractor.extractTypesUsingJavap(type, extracted, type1 -> acceptClass(type1));
            Enumeration it = extracted.elements();
            while (it.hasMoreElements()) {
                String extype = (String) it.nextElement();
                if (acceptClass(extype)) {
                    try {
                        _add(extype);
                    } catch (java.lang.ClassNotFoundException e) {
                        System.err.println("Failed to add (" + extype + ") " + e);
                    } catch (java.lang.NoClassDefFoundError e) {
                        System.err.println("Failed to add (" + extype + ") " + e);
                    }
                }
            }
            String[] ltypes = TypesExtractor.extractTypesUsingListing(type);
            for (int i = 0; i < ltypes.length; i++) {
                if (acceptClass(ltypes[i])) _add(ltypes[i]);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    private boolean acceptClass(String name) {
        if (processed.indexOf(name) != -1) return false;
        for (int i = 0; i < disallowedPackages.length; i++) {
            if (name.startsWith(disallowedPackages[i])) return false;
        }
        for (int i = 0; i < BaseDepHelper.SUN_CLASSES.length; i++) {
            if (name.equals(BaseDepHelper.SUN_CLASSES[i])) return false;
        }
        if (pkgNames.length == 0) return true;
        for (int j = 0; j < pkgNames.length; j++) {
            if (name.startsWith(pkgNames[j])) return true;
        }
        return false;
    }

    private static void usage() {
        System.err.println("Usage: java " + DependGraph.class.getName() +
                " -d -p package1,package2,.. classes");
        System.err.println("If -d is specified, for each dependent classes, its dependenet classes are also searched");
        System.exit(1);
    }

    private String getPath(String typename) {
        String resource = typename.replace('.', separator).concat(".class");
        java.net.URL url = getClass().getClassLoader().getResource(resource);
        if (url == null) {
            System.err.println(typename + "[" + resource + "] not found");
            return null;
        }
        String location = url.toExternalForm();
        int ndx;
        if ((ndx = location.indexOf(resource)) != -1)
            location = location.substring(0, ndx);
        if ((ndx = location.lastIndexOf(":")) != -1)
            location = location.substring(ndx + 1);
        if ((ndx = location.lastIndexOf("!")) != -1)
            return location.substring(0, ndx);
        String clsname = null;
        //if ((ndx=typename.lastIndexOf('.')) == -1) clsname = typename;
        //else clsname = typename.substring(ndx+1);
        return location + typename.replace('.', separator) + ".class";
    }


    public static void main(String[] args) {
        try {
            int optind;
            boolean deep = false;
            String packages = null;
            String clazz = null;
            for (optind = 0; optind < args.length; optind++) {
                if (args[optind].equals("-p")) {
                    packages = args[++optind];
                } else if (args[optind].equals("-d")) {
                    deep = true;
                } else if (args[optind].equals("--")) {
                    optind++;
                    break;
                } else if (args[optind].equals("-h")) {
                    usage();
                } else if (args[optind].equals("help")) {
                    usage();
                } else if (args[optind].startsWith("-")) {
                    usage();
                } else {
                    break;
                }
            }
            String[] pkgNames = null;
            if (packages == null || packages.length() == 0) {
                pkgNames = new String[0];
            } else {
                StringTokenizer st = new StringTokenizer(packages, ",");
                Vector list = new Vector();
                while (st.hasMoreTokens()) {
                    String next = st.nextToken();
                    list.addElement(next.trim());
                }
                pkgNames = new String[list.size()];
                list.copyInto(pkgNames);
            }

            System.out.println("digraph G {");
            DependGraph depgraph = new DependGraph(pkgNames, deep);
            String location = null;
            for (; optind < args.length; optind++) {
                depgraph.add(args[optind]);
            }
            System.out.println("}");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Attributes
    private DependencyLocator locator;
    private String[] pkgNames;
    private String[] disallowedPackages = BaseDepHelper.SUN_PACKAGES;
    private static final char separator = System.getProperty("file.separator").charAt(0);
    private Vector processed;
    private Vector printed;
    private boolean deep;
    private boolean debug;
}
