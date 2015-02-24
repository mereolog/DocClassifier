/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package documentclassifier;

/**
 *
 * @author PG
 */
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.*;
import java.io.*;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.document.*;
import java.lang.Math.*;
import morfologik.stemming.PolishStemmer.DICTIONARY;


/**
 *
 * @author PG
 */
public class Index {
    
        
    public void createIndex (File folder)
    {
        
        try {
                //IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, new MorfologikAnalyzer(Version.LUCENE_35, DICTIONARY.MORFOLOGIK));
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, new PolishAnalyzer(Version.LUCENE_35));
                
                IndexWriter writer = new IndexWriter(FSDirectory.open(folder), config);
                 
                indexDocs(writer, folder);
                
                writer.close();
             } catch (IOException e)
                {
                    System.out.println(" caught a " + e.getClass() +"\n with message: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
    
    }
    static void indexDocs(IndexWriter writer, File file)
    throws IOException {
    // do not try to index files that cannot be read
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {
        
        if (file.getName().contains("txt"))
        {
        try {
            //System.out.println("adding " + file);
            
          Document doc = CreateFileDoc.Document(file);
          if (doc.getField("contents") != null) writer.addDocument(doc);
        }
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        catch (FileNotFoundException fnfe) {
          ;
        }
        } //else System.out.println("skipping " + file);
      }
    }
  }



}
