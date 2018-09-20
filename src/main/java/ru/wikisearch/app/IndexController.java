package ru.wikisearch.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {
    @GetMapping("/reindex")
    public String reindex(@RequestParam(name="dumpname", required=false, defaultValue="dump\\sample.xml.bz2") String dumpname,
                          @RequestParam(name="indexdir", required=false, defaultValue="dump\\index") String indexdir,
                          Model model) {
        model.addAttribute("dumpname", dumpname);
        model.addAttribute("indexdir", indexdir);
        WikiIndexer indexer = new WikiIndexer(dumpname, indexdir);
        try {
            indexer.DoIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "reindex";
    }
}

