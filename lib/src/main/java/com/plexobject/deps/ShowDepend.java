/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowDepend
 * Class Description:
 * ShowIDepend shows dependencies statements for a class
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

public class ShowDepend extends BaseDepHelper {
    public ShowDepend() {
    }

    public ShowDepend(boolean packageOnly, String[] pkgNames, boolean verbose) {
        this.packageOnly = packageOnly;
        this.pkgNames = pkgNames;
        this.verbose = verbose;
    }

    public String[] getDepends(final String name) {
        Vector importlist = new Vector();
        try {
            if (!acceptClass(name)) {
                if (verbose) System.err.println("# ** skipping " + name);
                return new String[0];
            }
            ///
            Class clazz = getClass().getClassLoader().loadClass(name);
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
                if (importlist.indexOf(ltypes[i]) == -1 &&
                        acceptClass(type, ltypes[i])) {
                    importlist.addElement(ltypes[i]);
                }
            }
        } catch (java.lang.ClassNotFoundException e) {
            if (verbose) System.err.println("Failed to add (" + name + ") " + e);
        } catch (java.lang.NoClassDefFoundError e) {
            if (verbose) System.err.println("Failed to add (" + name + ") " + e);
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

    public static void main(String[] args) {
        try {
            ShowDepend si = new ShowDepend();
            si.disallowedPackages = BaseDepHelper.getDisallowedPackages();


            String filename = null;
            for (int i = 0; i < args.length; i++) {
                File file = new File(args[i]);
                if (args[i].equals("-P")) si.packageOnly = true;
                else if (args[i].equals("-p")) {
                    String packages = args[++i];
                    if (packages == null || packages.length() == 0) {
                        si.pkgNames = new String[0];
                    } else {
                        StringTokenizer st = new StringTokenizer(packages, ",");
                        Vector list = new Vector();
                        while (st.hasMoreTokens()) {
                            String next = st.nextToken();
                            list.addElement(next.trim());
                        }
                        si.pkgNames = new String[list.size()];
                        list.copyInto(si.pkgNames);
                    }
                } else if (args[i].equals("-v")) {
                    si.verbose = true;
                } else if (args[i].equals("-o")) {
                    filename = args[++i];
                    if (si.verbose) System.err.println("# will save output to " + filename);
                } else if (args[i].equals("-k")) {
                    si.skipList.add(args[++i]);
                    if (si.verbose) System.err.println("# adding to skip list: " + args[i]);
                } else if (args[i].equals("-s")) {
                    si.sequenceMain = args[++i];
                    if (si.verbose) System.err.println("# adding to UML Sequence : " + args[i]);
                } else if (args[i].equals("-m")) {
                    si.mustList.add(args[++i]);
                    if (si.verbose) System.err.println("# adding to must list: " + args[i]);
                } else if (args[i].endsWith(".jar") || args[i].endsWith(".zip")) si.addZipDepend(args[i]);
                else if (file.exists() && file.isDirectory()) si.addDirDepend(args[i]);
                else if (file.exists() && file.isFile()) si.addClassDepend(si.file2class(args[i]));
                else si.addClassDepend(args[i]);
            }
            if (filename == null) si.printDotSyntax(System.out, "");
            else si.printDotSyntax(filename);
            if (si.sequenceMain != null) {
                sequenceDigrams(si.sequenceMain, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


