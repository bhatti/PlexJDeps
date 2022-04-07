package com.plexobject.deps;

import com.plexobject.demo.service.GameServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class ShowDependsTest {
    @Test
    void testSearch() throws Exception {
        ShowDepends si = new ShowDepends(false, new String[]{"com.plexobject.demo"}, Collections.emptyList(), true);
        si.addJaxClasses();
        si.search(GameServiceImpl.class.getName());
    }

    @Test
    void testShowDepends() throws Exception {
        ShowDepends si = new ShowDepends(false, new String[]{"com.plexobject.demo"}, Collections.emptyList(), true);
        si.addJaxClasses();
        si.addClassDepend(GameServiceImpl.class.getName());
        si.printDotSyntax(System.out, "");
    }

    @Test
    void testShowDepend() throws Exception {
        ShowDepend si = new ShowDepend(false, new String[]{"com.plexobject.demo"}, Collections.emptyList(), true);
        si.addClassDepend(GameServiceImpl.class.getName());
        si.printDotSyntax(System.out, "");
    }
}
