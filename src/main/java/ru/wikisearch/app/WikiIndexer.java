package ru.wikisearch.app;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.*;
import org.apache.lucene.benchmark.byTask.feeds.EnwikiContentSource;
import org.apache.lucene.analysis.ru.RussianAnalyzer;

public class WikiIndexer {

    private String dumpname;
    private String indexdir;
    public WikiIndexer(String dumpName, String indexDir) {
        this.dumpname = dumpName;
        this.indexdir = indexDir;
    }

    public void DoIndex() throws Exception {
        File wikipediafile = new File(dumpname);
        if (!wikipediafile.exists()) {
            System.out.println("Can't find "
                    + wikipediafile.getAbsolutePath());
            return;
        }
        if (!wikipediafile.canRead()) {
            System.out.println("Can't read "
                    + wikipediafile.getAbsolutePath());
            return;
        }
        File outputDir = new File(this.indexdir);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.out.println("couldn't create "
                        + outputDir.getAbsolutePath());
                return;
            }
        }
        if (!outputDir.isDirectory()) {
            System.out.println(outputDir.getAbsolutePath()
                    + " is not a directory!");
            return;
        }
        if (!wikipediafile.canWrite()) {
            System.out.println("Can't write to "
                    + outputDir.getAbsolutePath());
            return;
        }

        // we should be "ok" now

        FSDirectory dir = FSDirectory.open(outputDir.toPath());

        RussianAnalyzer analyzer = new RussianAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);// overwrites
        // if
        // needed
        IndexWriter indexWriter = new IndexWriter(dir, config);

        DocMaker docMaker = new DocMaker();
        Properties properties = new Properties();
        properties.setProperty("content.source.forever", "false"); // will
        // parse
        // each
        // document
        // only
        // once
        properties.setProperty("docs.file",
                wikipediafile.getAbsolutePath());
        properties.setProperty("keep.image.only.docs", "false");
        Config c = new Config(properties);
        EnwikiContentSource source = new EnwikiContentSource();
        source.setConfig(c);
        source.resetInputs();// though this does not seem needed, it is
        // (gets the file opened?)
        docMaker.setConfig(c, source);
        int count = 0;
        int bodycount = 0;
        System.out.println("Starting Indexing of Wikipedia dump "
                + wikipediafile.getAbsolutePath());
        long start = System.currentTimeMillis();
        Document doc;
        try {
            while ((doc = docMaker.makeDocument()) != null) {
                Document mydoc = new Document();
                if((doc.getField("docid"))!=null) {
                    mydoc.add(new TextField("docid",
                            doc.get("docid"),
                            Field.Store.YES));
                    bodycount++;
                }
                if((doc.getField("docname"))!=null) {
                    mydoc.add(new TextField("name",
                            doc.get("docname"),
                            Field.Store.YES));
                    bodycount++;
                }
                if(doc.getField("doctitle")!=null) {
                    mydoc.add(new TextField("title",
                            doc.get("doctitle"),
                            Field.Store.YES));
                    bodycount++;
                }
                if(doc.getField("body")!=null) {
                    if(doc.get("body") != null) {
                        mydoc.add(new TextField("body",
                                doc.get("body"),
                                Field.Store.YES));
                        bodycount++;
                    }
                }
                indexWriter.addDocument(mydoc);

                ++count;

                if(false && count == 100) {
                    System.out.println("test run. 100 document indexed, break");
                    break;
                }
                if (count % 1000 == 0)
                    System.out
                            .println("Indexed "
                                    + count
                                    + " documents ("+bodycount+" bodies) in "
                                    + (System
                                    .currentTimeMillis() - start)
                                    + " ms");

            }
        } catch (org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException nmd) {
            nmd.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        System.out.println("Indexing " + count + " documents took "
                + (finish - start) + " ms");
        System.out.println("Index should be located at "
                + dir.getDirectory().toAbsolutePath());
        indexWriter.close();

    }
    public ArrayList DoQuery(String searchStr, int nArticles) {
        ArrayList QueryResult = new ArrayList();

        System.out.println("We are going to test the index by querying the word '" + searchStr + "' and getting the top 3 documents:");

        File idir = new File(indexdir);
        FSDirectory dir = null;
        try {
            dir = FSDirectory.open(idir.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        IndexSearcher searcher = new IndexSearcher(reader);

        Query query = new TermQuery(new Term("body", searchStr));
        TopDocs hits = null;
        try {
            hits = searcher.search(query, nArticles);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(ScoreDoc hit: hits.scoreDocs) {
            Document document = null;

            try {
                document = searcher.doc(hit.doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Hit: ");
            for(IndexableField iff : document.getFields()) {
                String content = document.get(iff.name());
                if(content.length() > 400) content = content.substring(0,400)+"...";
                System.out.println(iff.name()+ " : " + content);
                if (iff.name().equals("body") ) {
                    System.out.println("add to result:" + content);
                    QueryResult.add(content);
                }
            }
        }

        return QueryResult;
    }
}