package ru.wikisearch.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Splitter {
    private int dist = 0;
    private int max = 1;
    public Splitter(int dist, int max) {
        this.dist = dist;
        this.max = max;
    }
    public String[] split(String string, String word) {
        String[] list = string.split("\\s+");
        String[] result = new String[max];
        int index = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].matches(word)) {
                String a = list[i];
                for (int j = i - 1; j>=0 && j>=i-dist; j--) {
                    a = list[j] + " " + a;
                }
                for (int j = i + 1; j<list.length && j<=i+dist; j++) {
                    a = a + " " + list[j];
                }
                result[index++] = a;
                if (index == max) break;
            }
        }
        return result;
    }
}
