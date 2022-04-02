/**
 * <B>CLASS COMMENTS</B>
 * Class Name: TypesExtractor
 * Class Description:
 * TypesExtractor defines common functions for types extraction
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

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TypesExtractor {
    public static Class getComponentType(Class type) {
        if (type == null) return null;
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type;
    }

    public static String[] getComponentType(String name) {
        if (name == null || name.length() == 0) return null;
        if (name.length() > 1 && name.indexOf(';') == -1) {
            if (name.indexOf('/') != -1 || name.indexOf('.') != -1) {
                name = 'L' + name + ';';
            } else return null;
        }
        Vector list = new Vector();
        int endi = 0;
        int starti = 0;
        while (true) {
            if ((endi = name.indexOf(';')) == -1) break;
            starti = 0;
            while (name.charAt(starti) == 'B' ||
                    name.charAt(starti) == 'C' ||
                    name.charAt(starti) == 'D' ||
                    name.charAt(starti) == 'F' ||
                    name.charAt(starti) == 'I' ||
                    name.charAt(starti) == 'J' ||
                    name.charAt(starti) == 'S' ||
                    name.charAt(starti) == 'Z' ||
                    name.charAt(starti) == '[' ||
                    name.charAt(starti) == 'C') starti++;
            if (name.charAt(starti) != 'L') {
                throw new IllegalArgumentException("Type does not start with L " + name);
            } else starti++;
            if (endi <= starti) break;
            String type = name.substring(starti, endi).replace('/', '.');
            if (list.indexOf(type) == -1) list.addElement(type);
            if (endi + 1 >= name.length() - 1) break;
            name = name.substring(endi + 1);
        }
        String[] types = new String[list.size()];
        list.copyInto(types);
        return types;
    }

    public static String[] extractTypesUsingClassInfo(Class type) {
        Vector extracted = new Vector();
        extractTypesUsingClassInfo(type, extracted);
        String[] types = new String[extracted.size()];
        extracted.copyInto(types);
        return types;
    }

    public static InputStream getInputStreamUsingClassInfo(String type, StringBuffer sbPath) {
        String path = TypesExtractor.getPath(type);
        InputStream in = null;
        if (path != null) {
            //System.out.println("Loading " + type + " from " + path);
            if (sbPath != null) {
                sbPath.setLength(0);
                sbPath.append(path);
            }
            try {
                if (path.endsWith(".class")) {
                    in = TypesExtractor.getStream(path, type);
                } else {
                    byte[] data = TypesExtractor.loadClassFromJar(type, new File(path));
                    //System.out.println("Loading " + type + " from " + path + ", data " + data);
                    if (data == null) return null;
                    in = new DataInputStream(new ByteArrayInputStream(data));
                }
                return in;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return in;
    }

    public static void extractTypesUsingClassInfo(Class type, Vector extracted) {
        String path = getPath(type.getName());
        if (path != null) {
            try {
                DataInputStream in = null;
                if (path.endsWith(".class")) {
                    in = getStream(path, type.getName());
                } else {
                    byte[] data = loadClassFromJar(type.getName(), new File(path));
                    if (data == null) return;
                    in = new DataInputStream(new ByteArrayInputStream(data));
                }
                if (in == null) return;
                ClassParser parser = new ClassParser();
                ClassParser.ClassFile[] classInfo = parser.process(in, path);
                String[] types = ClassParser.getTypes(classInfo);
                for (int i = 0; i < types.length; i++) {
                    if (extracted.indexOf(types[i]) == -1) extracted.addElement(types[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static String[] extractTypesUsingReflection(Class type) {
        Vector extracted = new Vector();
        extractTypesUsingReflection(type, extracted);
        String[] types = new String[extracted.size()];
        extracted.copyInto(types);
        return types;
    }

    public static void extractTypesUsingReflection(Class type, Vector extracted) {
        if (!javap) {
            return;
        }
        // find super classes and interfaces
        Class decClazz = type.getDeclaringClass();
        if (decClazz != null) {
            String fname = TypesExtractor.getComponentType(decClazz).getName();
            if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
        }
        Class supClazz = type.getSuperclass();
        while (supClazz != null) {
            String fname = TypesExtractor.getComponentType(supClazz).getName();
            if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
            supClazz = supClazz.getSuperclass();
        }
        Class[] classes = type.getDeclaredClasses();
        for (int i = 0; i < classes.length; i++) {
            String fname = TypesExtractor.getComponentType(classes[i]).getName();
            if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
        }
        classes = type.getInterfaces();
        for (int i = 0; i < classes.length; i++) {
            String fname = TypesExtractor.getComponentType(classes[i]).getName();
            if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
        }

        // find attributes
        java.lang.reflect.Field[] fields = type.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String fname = TypesExtractor.getComponentType(fields[i].getType()).getName();
            if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
        }
        java.lang.reflect.Method[] methods = type.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String fname = TypesExtractor.getComponentType(methods[i].getReturnType()).getName();
            if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
            Class[] params = methods[i].getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                fname = TypesExtractor.getComponentType(params[j]).getName();
                if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
            }
            Class[] exceps = methods[i].getExceptionTypes();
            for (int j = 0; j < exceps.length; j++) {
                fname = TypesExtractor.getComponentType(exceps[j]).getName();
                if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
            }
        }
        java.lang.reflect.Constructor[] ctors = type.getDeclaredConstructors();
        for (int i = 0; i < ctors.length; i++) {
            Class[] params = ctors[i].getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String fname = TypesExtractor.getComponentType(params[j]).getName();
                if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
            }
            Class[] exceps = ctors[i].getExceptionTypes();
            for (int j = 0; j < exceps.length; j++) {
                String fname = TypesExtractor.getComponentType(exceps[j]).getName();
                if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
            }
        }
    }

    public static String[] extractTypesUsingListing(Class type) {
        Vector extracted = new Vector();
        extractTypesUsingListing(type, extracted);
        String[] types = new String[extracted.size()];
        extracted.copyInto(types);
        return types;
    }

    public static void extractTypesUsingListing(Class type, Vector extracted) {
        if (!javap) {
            return;
        }
        // find inner and anonymous classes
        final String name = type.getName();
        java.security.CodeSource cs = type.getProtectionDomain().getCodeSource();
        if (cs != null && cs.getLocation() != null) {
            String location = cs.getLocation().toExternalForm();
            System.err.println(type.getName() + " loaded from " + location);
            String typepkg = type.getPackage() != null ? type.getPackage().getName() :
                    "";
            java.io.File dir = new java.io.File(location + separator +
                    typepkg.replace('.', separator));
            if (dir.exists() && dir.canRead() && dir.isDirectory()) {
                String[] listing = dir.list(new java.io.FilenameFilter() {
                    public boolean accept(java.io.File dir, String basename) {
                        String className = null;
                        int ndx = name.lastIndexOf('.');
                        if (ndx == -1) className = name;
                        else className = name.substring(ndx + 1);
                        if (basename.startsWith(className) &&
                                basename.endsWith(".class")) return true;
                        return false;
                    }
                });
                for (int i = 0; i < listing.length; i++) {
                    String fname = null;
                    if (type.getPackage() != null)
                        fname = type.getPackage().getName() + "." +
                                listing[i].substring(0, listing[i].indexOf(".class"));
                    else
                        fname = listing[i].substring(0, listing[i].indexOf(".class"));

                    if (extracted.indexOf(fname) == -1) extracted.addElement(fname);
                }
            }
        }
    }

    public static String[] extractTypesUsingJavap(Class type) {
        Vector extracted = new Vector();
        extractTypesUsingJavap(type, extracted);
        String[] types = new String[extracted.size()];
        extracted.copyInto(types);
        return types;
    }

    public static void extractTypesUsingJavap(Class type, Vector extracted) {
        if (!javap) {
            extractTypesUsingClassInfo(type, extracted);
            return;
        }
        // Use javap disassemble to get all classes
        try {
            Process p = Runtime.getRuntime().exec("javap -b -c -private " +
                    type.getName());
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = null;
            int starti = 0;
            int endi = 0;
            while ((line = in.readLine()) != null) {
                String[] types = TypesExtractor.extractTypes(line);
                for (int k = 0; types != null && k < types.length; k++) {
                    if (types[k] != null && extracted.indexOf(types[k]) == -1) extracted.addElement(types[k]);
                }
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Unable to invoke (javap " + type.getName() +
                    ") :" + e.toString());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static String[] extractTypes(String line) {
        int n;
        int starti;
        int endi;
        Vector vecTypes = new Vector();
        try {
            if (line.indexOf("<String ") != -1) return new String[0];

            if ((starti = line.indexOf("<InterfaceMethod ")) != -1) {
                line = line.substring(0, starti) + "<Method " +
                        line.substring(starti + 17);
            }
            if (javap1_1 && (starti = line.indexOf("<Class ")) != -1) {
                if ((endi = line.indexOf(">", starti + 7)) != -1) {
                    starti += 7;
                    vecTypes.addElement('L' + line.substring(starti, endi) + ';');
                }
            } else if ((starti = line.indexOf("<Class ")) != -1) {
                if ((endi = line.indexOf(">", starti + 7)) != -1) {
                    starti += 7;
                    vecTypes.addElement(line.substring(starti, endi));
                }
            } else if (javap1_1 && (starti = line.indexOf("<Field ")) != -1) {
                if ((starti = line.lastIndexOf(" ")) != -1) {
                    endi = line.indexOf('>', starti);
                    vecTypes.addElement(line.substring(starti + 1, endi));
                }
            } else if ((starti = line.indexOf("<Field ")) != -1) {
                if ((endi = line.indexOf(" ", starti + 7)) != -1) {
                    starti += 7;
                    vecTypes.addElement(line.substring(starti, endi));
                }
            } else if (javap1_1 && (starti = line.indexOf("<Method ")) != -1) {
                starti += 8;
                boolean ctor = (endi = line.indexOf(".<init>()V>", starti)) != -1;
                if (ctor) {
                    vecTypes.addElement('L' + line.substring(starti, endi) + ';');
                } else {
                    endi = line.indexOf("(", starti);
                    String declType = line.substring(starti, endi);
                    starti = declType.lastIndexOf('.');
                    if (starti == -1) return new String[0];

                    declType = declType.substring(0, starti);
                    vecTypes.addElement('L' + declType + ';');

                    starti = endi + 1;
                    endi = line.indexOf(')', starti);
                    String parms = line.substring(starti, endi);
                    vecTypes.addElement(parms);
                    starti = endi + 1;
                    endi = line.indexOf('>', starti);
                    String rtype = line.substring(starti, endi);
                    vecTypes.addElement(rtype);
                }
            } else if ((starti = line.indexOf("<Method ")) != -1) {
                boolean ctor = false;
                String rtype = null;
                starti += 8;
                endi = line.indexOf(" ", starti);
                if (endi != -1 && line.charAt(endi - 1) == '.') {
                    endi = line.indexOf(" ", endi + 1);
                }
                if (endi == -1) {
                    // constructor
                    endi = line.indexOf("(", endi + 1);
                    ctor = true;
                }
                rtype = line.substring(starti, endi);
                if ((n = rtype.indexOf("[")) != -1) rtype = rtype.substring(0, n);

                if (ctor) starti = endi;
                else starti = line.indexOf("(", endi);

                if (starti != -1) starti++;
                while (starti != -1) {
                    endi = line.indexOf(",", starti);
                    if (endi == -1) endi = line.indexOf(")", starti);
                    if (endi == -1) break;
                    if (endi - starti <= 1) break;

                    String arg = line.substring(starti, endi);
                    if ((n = arg.indexOf("[")) != -1) arg = arg.substring(0, n);

                    vecTypes.addElement(arg);
                    starti = line.indexOf(",", endi);
                    if (starti == -1) break;
                    if (line.charAt(starti + 1) == ' ') starti += 2;
                    else starti++;
                }
                vecTypes.addElement(rtype);
            }
        } catch (Exception e) {
            System.err.println("Error parsing: " + line);
            e.printStackTrace();
            System.exit(3);
        }

        Vector finalTypes = new Vector();
        Enumeration it = vecTypes.elements();
        while (it.hasMoreElements()) {
            String type = (String) it.nextElement();
            starti = 0;
            while ((starti = type.indexOf(". ")) != -1) {
                type = type.substring(0, starti) + "$" + type.substring(starti + 2);
            }
            String[] types = getComponentType(type);
            if (types == null) {
                continue;
            }
            for (int i = 0; i < types.length; i++) finalTypes.addElement(types[i]);
        }
        String[] types = new String[finalTypes.size()];
        finalTypes.copyInto(types);
        return types;
    }


    public static String getPath(String typename) {
        //String resource = typename.replace('.', separator).concat(".class");
        String resource = typename.replace('.', '/').concat(".class");
        java.net.URL url = TypesExtractor.class.getClassLoader().getResource(resource);
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            System.err.println(typename + "[" + resource + "] not found");
            return null;
        }
        String location = url.toExternalForm();
        if (location.indexOf(':') == -1) {
            try {
                File file = new File(location);
                location = file.toURL().toString();
            } catch (Exception e) {
                location = url.toExternalForm();
            }
        }
        int ndx;
        if (location.startsWith("file:")) {
            location = location.substring(5);
        }
        File file = new File(location);
        if (!file.exists()) {
            if (location.startsWith("jar:file")) {
                int start = location.lastIndexOf(':');
                int end = location.indexOf('!');
                return location.substring(start + 1, end);
            }
            System.err.println("Failed to find resource '" + resource + "' at [" + location + "] url [" + url + "]");
            return null;
        }
        //System.out.println("******* location " + location);
        if ((ndx = location.indexOf(resource)) != -1)
            location = location.substring(0, ndx);
        if ((ndx = location.lastIndexOf(":")) != -1)
            location = location.substring(ndx + 1);
        if ((ndx = location.lastIndexOf("!")) != -1)
            return location.substring(0, ndx);
        if (location.endsWith(".class")) return location; // added 5/15/01
        String clsname = null;
        return location + typename.replace('.', separator) + ".class";
    }


    protected static DataInputStream getStream(String file, String type)
            throws java.io.IOException {
        //if (file == null || file.length() == 0 || file.indexOf("$") != -1) return null;
        if (file.endsWith(".class")) {  // ok
            return new DataInputStream(new BufferedInputStream(
                    new FileInputStream(file)));
        } else if (file.endsWith(".jar")) {
            JarResources jr = new JarResources(file);
            String entry = type.replace('.', '/') + ".class";
            byte[] data = jr.getResource(entry);
            if (data == null) {
                System.err.println("Resource [" + entry + "] not found in " + file);
                return null;
            }
            return new DataInputStream(new ByteArrayInputStream(data));
        } else {
            System.err.println("Unknown resource type " + file);
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            Class clazz = Class.forName(args[0]);

            String[] types = null;
            if (javap) types = extractTypesUsingJavap(clazz);
            else {
                String path = getPath(args[0]);
                if (path != null) {
                    ClassParser parser = new ClassParser();
                    DataInputStream in = getStream(path, args[0]);
                    if (in == null) return;
                    ClassParser.ClassFile[] classInfo = parser.process(in, path);
                    types = parser.getTypes(classInfo);
                } else types = new String[0];
            }
            for (int i = 0; i < types.length; i++) {
                System.out.println(types[i]);
            }
            //ReflectPrinter.printObject(file);
            //System.out.println(file.toString());
        } catch (ClassNotFoundException e) {
            System.err.println("Class [" + args[0] + "] not found");
            System.exit(4);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(4);
        }
        System.exit(0);
    }

    protected static byte[] loadClassFromJar(String name, File jarName) {
        try {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("No name specified");
            }
            if (jarName == null) {
                throw new IllegalArgumentException("No jar file specified");
            }
            if (!name.endsWith("class")) name = name.replace('.', '/').concat(".class");
            JarFile jarFile = null;
            jarFile = (JarFile) resources.get(jarName);
            if (jarFile == null) jarFile = new JarFile(jarName);
            resources.put(jarName, jarFile);
            ZipEntry entry = jarFile.getJarEntry(name);
            if (entry == null) {
                System.out.println("Entry name " + name + " not found in " + jarName);
                return null;
            }
            return loadClassFromZipEntry(jarFile, entry);
        } catch (Exception e) {
            return null;
        }
    }

    protected static byte[] loadClassFromZip(String name, File zipName) {
        try {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("No name specified");
            }
            if (zipName == null) {
                throw new IllegalArgumentException("No zip file specified");
            }
            ZipFile zipFile = null;
            zipFile = (ZipFile) resources.get(zipName);
            if (zipFile == null) zipFile = new ZipFile(zipName);
            resources.put(zipName, zipFile);
            ZipEntry entry = zipFile.getEntry(name);
            if (entry == null) return null;
            return loadClassFromZipEntry(zipFile, entry);
        } catch (Exception e) {
            return null;
        }
    }

    protected static byte[] loadClassFromZipEntry(ZipFile zipFile, ZipEntry entry) {
        try {
            if (entry == null) return null;
            BufferedInputStream in = new BufferedInputStream(
                    zipFile.getInputStream(entry));
            return loadClassFromInputStream(in, (int) entry.getSize());
        } catch (Exception e) {
            return null;
        }
    }

    protected static byte[] loadClassFromInputStream(InputStream in, int size) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(size);
            final int N = 1024;
            byte buf[] = new byte[N];
            int ln = 0;
            while (size > 0 &&  // workaround for bug
                    (ln = in.read(buf, 0, Math.min(N, size))) != -1) {
                out.write(buf, 0, ln);
                size -= ln;
            }
            in.close();
            out.close();
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static char separator =
            System.getProperty("file.separator").charAt(0);
    private static final boolean javap1_1 = true;
    private static boolean javap = "true".equals(System.getProperty("javap", "false"));
    private static Map resources = Collections.synchronizedMap(new WeakHashMap());
}
