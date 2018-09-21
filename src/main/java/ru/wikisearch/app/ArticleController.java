package ru.wikisearch.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class ArticleController {
    @GetMapping("/article")
    public String query(@RequestParam(name="dumpname", required=false, defaultValue="sample.xml.bz2") String dumpname,
                        @RequestParam(name="indexdir", required=false, defaultValue="index") String indexdir,
                        @RequestParam(name="id") String id,
                        Model model) {
        WikiIndexer indexer = new WikiIndexer(dumpname, indexdir);
        String Article = null;
        try {
            Article = indexer.DisplayArticleById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("article", Article);
        return "article";
    }
}
