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
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;

public class WikiIndexer {

    private String dumpname;
    private String indexdir;
    public WikiIndexer(String dumpName, String indexDir) {
        this.dumpname = dumpName;
        this.indexdir = indexDir;
    }

    public String DoIndex() throws Exception {
        File wikipediafile = new File(dumpname);
        if (!wikipediafile.exists()) {
            System.out.println("Can't find "
                    + wikipediafile.getAbsolutePath());
            return "Не найден дамп Википедии: " + wikipediafile.getAbsolutePath();
        }
        if (!wikipediafile.canRead()) {
            System.out.println("Can't read "
                    + wikipediafile.getAbsolutePath());
            return "Ошибка открытия дампа Википедии на чтение: " + wikipediafile.getAbsolutePath();
        }
        File outputDir = new File(this.indexdir);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.out.println("couldn't create "
                        + outputDir.getAbsolutePath());
                return "Ошибка создания каталога индекса: " + outputDir.getAbsolutePath();
            }
        }
        if (!outputDir.isDirectory()) {
            System.out.println(outputDir.getAbsolutePath()
                    + " is not a directory!");
            return "Путь, указанный как имя каталога индекса, не является каталогом: " + outputDir.getAbsolutePath();
        }
        if (!wikipediafile.canWrite()) {
            System.out.println("Can't write to "
                    + outputDir.getAbsolutePath());
            return "Ошибка записи в каталог индекса: " + outputDir.getAbsolutePath();
        }

        FSDirectory dir = FSDirectory.open(outputDir.toPath());

//        RussianAnalyzer analyzer = new RussianAnalyzer();
        EnglishAnalyzer analyzer = new EnglishAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter indexWriter = new IndexWriter(dir, config);

        DocMaker docMaker = new DocMaker();
        Properties properties = new Properties();
        properties.setProperty("content.source.forever", "false");

        properties.setProperty("docs.file",
                wikipediafile.getAbsolutePath());
        properties.setProperty("keep.image.only.docs", "false");
        Config c = new Config(properties);
        EnwikiContentSource source = new EnwikiContentSource();

        source.setConfig(c);
        source.resetInputs();
        docMaker.setConfig(c, source);

        int count = 0;
        int bodycount = 0;
        System.out.println("Начало индексирования дампа Википедии "
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
                    System.out.println("Тестовый прогон, 100 документов. Остановка.");
                    break;
                }
                if (count % 1000 == 0)
                    System.out
                            .println("Индексировано документов: "
                                    + count
                                    + ", полей: "+bodycount+", время: "
                                    + (System
                                    .currentTimeMillis() - start)
                                    + " миллисекунд");

            }
        } catch (org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException nmd) {
            nmd.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        String indexResult = "Индексировано документов: "
                + count + ". Время индексации (секунд): " + (finish - start)/1000;
        System.out.println(indexResult);
        System.out.println("Индекс расположен в "
                + dir.getDirectory().toAbsolutePath());
        indexWriter.close();

        return indexResult;
    }

    private IndexSearcher myIndexSearcher() {
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
        return searcher;
    }

    public String DisplayArticleById (String id) {
        String Article = null;

        IndexSearcher searcher = myIndexSearcher();

        Query query = new TermQuery(new Term("docid", id));
        TopDocs hits = null;
        try {
            hits = searcher.search(query, 1);
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
                System.out.println(iff.name()+ " : " + content);
                if (iff.name().equals("body") ) {
                    System.out.println("add to result:" + content);
                    Article = content;
                }
            }
        }

        return Article;

    }

    public ArrayList<HashMap> DoQuery(String searchStr, int nArticles) {
        ArrayList<HashMap> QueryResult = new ArrayList<HashMap>();
        HashMap Entry = null;

        System.out.println("Поиск слова '" + searchStr + "', максимальное количество: "
                + nArticles);

        IndexSearcher searcher = myIndexSearcher();

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
            Entry = new HashMap();
            for(IndexableField iff : document.getFields()) {
                String content = document.get(iff.name());
                if(content.length() > 400) content = content.substring(0,400)+"...";
                System.out.println(iff.name()+ " : " + content);
                if (iff.name().equals("docid") ) {
                    Entry.put("docid", content);
                }
                if (iff.name().equals("title") ) {
                    Entry.put("title", content);
                }
                if (iff.name().equals("body") ) {
                    Entry.put("body", content);
                }
            }
            QueryResult.add(Entry);
        }

        return QueryResult;
    }
}
