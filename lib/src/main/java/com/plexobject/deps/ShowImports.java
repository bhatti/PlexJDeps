/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ShowImports
 * Class Description:
 * ShowImports shows import statements for a class
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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

public class ShowImports extends BaseDepHelper {
    public static void main(String[] args) throws Exception {
        ShowImports si = new ShowImports();
        String[] imports;
        for (int i = 0; i < args.length; i++) {
            System.out.println("// Import statements for " + args[i]);
            imports = si.getImports(args[i]);
            for (int j = 0; j < imports.length; j++) {
                System.out.println("import " + imports[j] + ";");
            }
            System.out.println();
        }
    }

    public String[] getImports(final String name) {
        Vector importlist = new Vector();
        try {
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
                if (importlist.indexOf(ltypes[i]) == -1 &&
                        acceptClass(type, ltypes[i])) {
                    importlist.addElement(ltypes[i]);
                }
            }
        } catch (Exception e) {
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
}
