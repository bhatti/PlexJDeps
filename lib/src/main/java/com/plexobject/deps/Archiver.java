/**
 * <B>CLASS COMMENTS</B>
 * Class Name: Archiver
 * Class Description: 
 *   Archiver creates a zip for archiving
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 * Modification History
 * Initial      Date            Changes
 * SAB          Apr 22, 1999    Created
*/

package com.plexobject.deps;
import java.util.*;
import java.util.zip.*;
import java.io.*;

public class Archiver {
  public static void archive(File target, File dir) throws IOException {
    File[] files = listFiles(dir); 
    //System.out.println("archive(" + target + ", " + dir + ": " + files.length);
    File parent = dir.getParentFile();
    if (parent == null) {
      String[] x = StringHelper.split(dir.getAbsolutePath(), fsep);
      StringBuffer sb = new StringBuffer();
      int end = x.length-1;
      if (x[x.length-1].equals(fsep)) end--;
      for (int i=0; i<end; i++) {
        sb.append(x[i] + fsep);
      }
      parent = new File(sb.toString());
    }
    //if (debug) System.out.println("archive(" + target + "," + parent + "," + files.length + ")");
    archive(target, parent, files);
  }

  //
  public static void archive(File target, File parent, File[] files) 
        throws IOException {
    ZipOutputStream out = new ZipOutputStream(
                   new BufferedOutputStream(new FileOutputStream(target)));
    String common = null;
    if (parent == null) {
      String first = files[0].getAbsolutePath();
      for (int n=first.length(); n>0; n--) {
        String str = first.substring(0, n);
        boolean matched = true;
        for (int i=0; i<files.length; i++) {
          String next = files[i].getAbsolutePath();
          if (!next.startsWith(str)) {
            matched = false;
            break;
          }
        }
        if (matched) {
           File f = new File(str);
           if (f.isDirectory()) common = str;
           else {
             int x = str.lastIndexOf(fsep);
             if (x != -1) common = str.substring(0, x+1);
           }
           break;
        }
      }
    }


/*
    if (parent == null) {
      String[] min = null;
      for (int i=0; i<files.length; i++) {
        if (debug) System.out.println("StringHelper.split(" + files[i] + "," + fsep + ")");
        String[] x = StringHelper.split(files[i].getAbsolutePath(), fsep);
        if (min == null || x.length < min.length) min = x;
      }
      File minf = new File(StringHelper.join(min, fsep));
      if (minf.isDirectory()) parent = minf;
      else minf = parent.getParentFile();
    }
*/

    int parlen = parent != null ? parent.getAbsolutePath().length() : 0;
    for (int i=0; i<files.length; i++) {
      if (files[i] == null) continue;
      String resource = "";
      //if (debug) System.out.println("Archiver.archive processing " + files[i].getName() + ": " + files[i].toString());
      //if (parent != null) resource = parent.getName() + files[i].getAbsolutePath().substring(parlen).replace('\\', '/');
      if (parent != null) resource = files[i].getAbsolutePath().substring(parlen).replace('\\', '/');
      else resource = files[i].toString().replace('\\', '/');

      if (files[i].isDirectory() && !resource.endsWith("/")) resource += "/";

      if (common != null && resource.startsWith(common)) {
        resource = resource.substring(common.length());
        //if (debug) System.out.println("Archiver.archive(" + parent + ") adding " + resource + ", common " + common);
      }

      if (resource.length() > 2 && resource.charAt(1) == ':') {
         resource = resource.substring(2);
      }
      /////////////
      if (resource.startsWith("/") || resource.startsWith("\\")) {
	resource = resource.substring(1);
      }

      if (debug) System.out.println("Archiver.archive(" + target + ", " + parent + ") resource " + resource + ", common " + common);
      ZipEntry entry = new ZipEntry(resource);
      out.putNextEntry(entry);
      if (files[i].isDirectory()) continue;
      InputStream in = new BufferedInputStream(new FileInputStream(files[i]));
      byte[] buffer = new byte[8192];
      int len;
      while ((len=in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      in.close();
    }
    out.close();
  }

  //
  public static File[] listFiles(File dir) {
    ArrayList list = new ArrayList();
    listFiles(dir, list); 
    File[] f = new File[list.size()];
    for (int i=0; i<list.size(); i++) {
      f[i] = (File) list.get(i);
    }
    return f;
  }

  public static void archive(File target, File parent, ArrayList inFiles) 
        throws IOException {
    File[] f = new File[inFiles.size()];
    boolean dir = false;
    for (int i=0; i<inFiles.size(); i++) {
      f[i] = (File) inFiles.get(i);
      if (f[i].isDirectory()) dir = true;
    }

    if (dir) {
      //
      inFiles.clear();
      for (int i=0; i<f.length; i++) {
        inFiles.add(f[i]);
        if (f[i].isDirectory()) listFiles(f[i], inFiles);
      }
  
      //    
      f = new File[inFiles.size()];
      for (int i=0; i<inFiles.size(); i++) {
        f[i] = (File) inFiles.get(i);
      }
    }
    //
    Archiver.archive(target, parent, f);
  }



  public static void unzip(File zipFile, File parent, Overrider overrider)
        throws IOException {
    try {
      parent.mkdirs();
      Hashtable htSizes=new Hashtable();  
      // extracts just sizes only. 
      ZipFile zf=new ZipFile(zipFile);
      Enumeration e=zf.entries();
      while (e.hasMoreElements()) {
        ZipEntry ze=(ZipEntry)e.nextElement();
        htSizes.put(ze.getName(),new Integer((int)ze.getSize()));
      }
      zf.close();

      ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
      //
      ZipEntry ze = null;
      while ((ze=zis.getNextEntry())!=null) {
        File f = new File(parent, ze.getName());
        if (ze.isDirectory()) {
          f.mkdirs();
          if (debug) System.out.println("Creating dir " + f);
          continue;
        }


        int size=(int) ze.getSize();
        // -1 means unknown size.
        if (size==-1) {
          size=((Integer)htSizes.get(ze.getName())).intValue();
        }

        byte[] b=new byte[(int)size];
        int rb=0;
        int chunk=0;
        while (((int)size - rb) > 0) {
          chunk=zis.read(b,rb,(int)size - rb);
          if (chunk==-1) break;
          rb+=chunk;
        }
        String xs = f.getAbsolutePath();
        File dir = new File(xs.substring(0, xs.lastIndexOf(fsep)));
        dir.mkdirs();
        if (overrider != null && f.exists() && !overrider.override(f)) {
          continue;
        }
        //
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        out.write(b);
        out.close();
      } // while
      zis.close();
    } catch (NullPointerException e) { // done}
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      throw e;
    }
  }




  //
  public static void listFiles(File dir, ArrayList list) {
    File[] f = dir.listFiles();
    for (int i=0; i<f.length; i++) {
      if (f[i].isDirectory()) {
        listFiles(f[i], list);
        if (f[i].listFiles().length == 0) list.add(f[i]);
      } else {
        list.add(f[i]);
      }
    }
  }

 
  public static void main(String[] args) throws Exception {
/*
    File[] files = new File[args.length-1];
    for (int i=0; i<files.length; i++) files[i] = new File(args[i+1]);
    archive(new File(args[0]), null, files);
*/

    if (System.getProperty("zip") == null && args.length == 2) {
      // input, output, overrider
      unzip(new File(args[0]), new File(args[1]), null);
    } else if (args.length >= 2) {
      ArrayList list = new ArrayList();
      for (int i=1; i<args.length; i++) list.add(new File(args[i]));
      //archive(inFile, cwd, inList);
      archive(new File(args[0]), null, list);
      //archive(new File(args[0]), new File(args[1]));
    } else {
      System.out.println("Usage: java -Dzip=true Archiver");
    }
  }

  private static final boolean debug = System.getProperty("debug", "false").equals("true");
  private static final String fsep = System.getProperty("file.separator");
}
