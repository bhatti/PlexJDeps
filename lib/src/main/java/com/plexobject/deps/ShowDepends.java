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

import java.io.File;
import java.util.*;

public class ShowDepends extends BaseDepHelper {
    public ShowDepends() {
    }

    public ShowDepends(boolean packageOnly, String[] pkgNames, boolean dotSyntax) {
        this.packageOnly = packageOnly;
        this.pkgNames = pkgNames;
        this.springParser.pkgNames = pkgNames;
        this.dotSyntax = dotSyntax;
    }

    //
    public String[] getDepends(final String name) {
        if (!acceptClass(name)) {
            return new String[0];
        }
        Vector importlist = new Vector();
        try {
            if (!acceptClass(name)) {
                if (verbose) System.err.println("# ** skipping " + name);
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
            TypesExtractor.extractTypesUsingJavap(type, extracted, type1 -> acceptClass(type1));
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

    public void addClassDepend(String klass) {
        if (processed.contains(klass)) {
            return;
        }
        if (includes(skipList, klass)) {
            if (verbose) System.err.println("# found in skip list, skipping " + klass);
            return;
        }
        if (mustList.size() > 0 && !includes(mustList, klass)) {
            if (verbose) System.err.println("# not in must list, skipping " + klass);
            return;
        }
        if (verbose) System.err.println("** adding " + klass);
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

    public static void main(String[] args) throws Exception {
        ShowDepends si = new ShowDepends();
        si.disallowedPackages = getDisallowedPackages();
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
                si.verbose = true;
            } else if (args[i].equals("-m")) {
                si.inMemory = true;
            } else if (args[i].equals("-o")) {
                filename = args[++i];
                if (si.verbose) System.err.println("# will save output to " + filename);
            } else if (args[i].equals("-s")) {
                si.skipList.add(args[++i]);
                if (si.verbose) System.err.println("# adding to skip list: " + args[i]);
            } else if (args[i].equals("-m")) {
                si.mustList.add(args[++i]);
                if (si.verbose) System.err.println("# adding to must list: " + args[i]);
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
        if (!si.processed.isEmpty()) {
            sequenceDigrams(si.processed.iterator().next(), args);
        }
    }
}


