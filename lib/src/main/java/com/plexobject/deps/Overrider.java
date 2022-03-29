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

public interface Overrider {
  public boolean override(File f);
}
