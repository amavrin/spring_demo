package ru.wikisearch.app;
import org.apache.commons.lang3.StringUtils;

public class RemoveWikiFormat {
    static public String removeFormat(String wiki) {
        String s = wiki;
        s = StringUtils.replacePattern(s, "\\[\\[[^\\]|]+\\|([^\\]|]+)\\]\\]", "$1");
        s = StringUtils.replacePattern(s, "\\{\\{[^\\]|]+\\|([^\\]|]+)\\}\\}", "$1");
        s = StringUtils.replacePattern(s,"\\[\\[", "");
        s = StringUtils.replacePattern(s,"\\]\\]", "");
        s = StringUtils.replacePattern(s,"\\{\\{", "");
        s = StringUtils.replacePattern(s,"\\}\\}", "");
        return s;
    }
}
