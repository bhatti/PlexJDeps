package com.plexobject.deps;

import com.plexobject.demo.service.GameServiceImpl;
import org.junit.jupiter.api.Test;

class ShowDependsTest {
    @Test
    void testShowDepends() throws Exception {
        ShowDepends si = new ShowDepends(false, new String[]{"com.plexobject.demo"}, true);
        si.addJaxClasses();
        si.search(GameServiceImpl.class.getName());
    }

    @Test
    void testShowDepend() throws Exception {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo"}, true);
        si.addClassDepend(GameServiceImpl.class.getName());
        si.printDotSyntax(System.out, "");
    }
}
