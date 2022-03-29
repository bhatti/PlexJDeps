package com.plexobject.deps;

import com.plexobject.demo.service.GameServiceImpl;
import org.junit.jupiter.api.Test;

public class ShowImportsTest {
    @Test
    void testShowImports() throws Exception {
        ShowImports si = new ShowImports();
        String[] imports;
        imports = si.getImports(GameServiceImpl.class.getName());
        for (int j = 0; j < imports.length; j++) {
            System.out.println("import " + imports[j] + ";");
        }
    }
}
