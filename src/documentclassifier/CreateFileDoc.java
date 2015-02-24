/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package documentclassifier;


import java.util.Date;
import java.io.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.benchmark.byTask.feeds.*;



/** A utility for making Lucene Documents from a File. */

public class CreateFileDoc {
  
    public static Document Document(File f)
       throws java.io.FileNotFoundException {

    // make a new, empty document
    Document doc = new Document();
    HTMLParser parser = new DemoHTMLParser();
    
    FileReader r = new FileReader (f);
    
    //System.out.println(f+"is parsed as "+r);
    
    DocData data = new DocData();
    
    try{parser.parse(data, null, new Date(), new String (), r, null);} catch (java.io.IOException e) {e.printStackTrace();} catch (java.lang.InterruptedException e) {e.printStackTrace();}
    //Date date = new Date();

    // Add the path of the file as a field named "path".  Use a field that is
    // indexed (i.e. searchable), but don't tokenize the field into words.
    String path = f.getAbsolutePath();
    String name = f.getName();
    String folder = f.getParentFile().getName();
    //System.out.println(folder);
    
    String parsedDoc = data.getBody();
    
    if (parsedDoc.length()>0)
    {
        doc.add(new Field("path", path, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc.add(new Field("name", name, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc.add(new Field("folder", folder, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));

        doc.add(new Field("contents", parsedDoc, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
    }
    
    
    return doc;
  }

  private CreateFileDoc() {}
}


