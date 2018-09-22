package ru.wikisearch.app;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class SplitterTest {
    @Test
    public void canSplitTest() {
        Splitter splitter = new Splitter(2, 1);
        String string = "a b c d e f g h";
        String[] arrayList = splitter.split(string, "d");
        assertEquals("b c d e f", arrayList[0]);
    }
    @Test
    public void canSplitTestNearBegin() {
        Splitter splitter = new Splitter(2, 1);
        String string = "a b c d e f g h";
        String[] arrayList = splitter.split(string, "b");
        assertEquals("a b c d", arrayList[0]);
    }
    @Test
    public void canSplitTestNearEnd() {
        Splitter splitter = new Splitter(2, 1);
        String string = "a b c d e f g h";
        String[] arrayList = splitter.split(string, "g");
        assertEquals("e f g h", arrayList[0]);
    }
    @Test
    public void canSplitTest2() {
        Splitter splitter = new Splitter(2, 2);
        String string = "a b X d e f X h";
        String[] arrayList = splitter.split(string, "X");
        assertEquals("a b X d e", arrayList[0]);
        assertEquals("e f X h", arrayList[1]);
    }
}
