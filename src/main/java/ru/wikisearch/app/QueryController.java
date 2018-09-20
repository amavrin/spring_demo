package ru.wikisearch.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class QueryController {
    @GetMapping("/query")
    public String query(@RequestParam(name="dumpname", required=false, defaultValue="dump\\sample.xml.bz2") String dumpname,
                        @RequestParam(name="indexdir", required=false, defaultValue="dump\\index") String indexdir,
                        @RequestParam(name="query") String query,
                        @RequestParam(name="count", required=false, defaultValue="5") String count,
                        Model model) {
        int nArticles = Integer.parseInt(count);
        model.addAttribute("query", query);
        WikiIndexer indexer = new WikiIndexer(dumpname, indexdir);
        ArrayList QueryResult = new ArrayList();
        try {
            QueryResult = indexer.DoQuery(query, nArticles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("result", QueryResult);
        return "query";
    }
}
