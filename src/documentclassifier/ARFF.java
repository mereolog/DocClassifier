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
import org.apache.lucene.index.*;
import java.io.*;
import java.util.*;
import org.apache.lucene.document.*;
import java.util.regex.*;


/**
 *
 * @author PG
 */
public class ARFF {
    
        String initFolder;
        String initIndexFolder ;
        String arffFolder;
        int size;
    
    
        public ARFF (String folder)
        {
            initFolder = folder + "/src";
            initIndexFolder = folder + "/index";
            arffFolder=folder;
        }
        
        public void createARFFTFIDF(File folder) {
        
        String listOfCat = "";

        ArrayList<Term> termSelList = new ArrayList<Term>();

        

        HashMap<String, Integer> freqTermInCorpusMap = new HashMap<String, Integer>();
        HashMap<String, HashMap<Integer, Double>> tfidfMap = new HashMap<String, HashMap<Integer, Double>>();
        HashMap<String, HashMap<Integer, Double>> tfidfMapTrimmed = new HashMap<String, HashMap<Integer, Double>>();
        HashSet<String> selectedTermsSet = new HashSet<String>();
        TreeMap<Integer, HashSet<String>> inverseFreqMap = new TreeMap<Integer, HashSet<String>>();
        HashMap<String, Integer> noDocWithTerm = new HashMap<String, Integer>();

        try {
                //System.out.println("THERE");
                FileOutputStream outStream = new FileOutputStream(arffFolder+"LEARN"+".arff");
                OutputStreamWriter writerLearn = new OutputStreamWriter(outStream, "UTF-8");
                writerLearn.write("@relation learn \n");
                
                IndexReader reader = IndexReader.open(FSDirectory.open(folder), true);

                Integer noOfDocuments = reader.maxDoc();
                
                for (int i=0; i<noOfDocuments; i++)
                {
                    TermFreqVector termFreqVector = reader.getTermFreqVector(i, "contents");
                    
                    if (termFreqVector != null)
                    {
                        String[] termVector = termFreqVector.getTerms();
                    
                        for (int j=0; j<termVector.length; j++)
                        {
                            String word = termVector[j];
                        
                            if (!noDocWithTerm.containsKey(word)) noDocWithTerm.put(word, 1);
                            else noDocWithTerm.put(word, noDocWithTerm.get(word)+1);
                        }
                    }
                }
                
                
                
                for (int i=0; i<noOfDocuments; i++)
                {
                    TermFreqVector termFreqVector = reader.getTermFreqVector(i, "contents");
                    if (termFreqVector == null) continue;
                    
                    String[] termVector = termFreqVector.getTerms();
                    int[] freqVector = termFreqVector.getTermFrequencies();
                    
                    for (int j=0; j<termVector.length; j++)
                    {
                        String word = termVector[j];
                        
                        if (!freqTermInCorpusMap.containsKey(word))
                        {
                            freqTermInCorpusMap.put(word, 1);
                            
                            HashSet<String> newTermSet = new HashSet<String>();
                            newTermSet.add(word);
                            if (!inverseFreqMap.containsKey(1)) inverseFreqMap.put(1, newTermSet);
                            else {inverseFreqMap.get(1).add(word);}
                        }
                        else
                        {
                            int oldFreq = freqTermInCorpusMap.get(word);
                            
                            freqTermInCorpusMap.put(word, oldFreq+1);
                            
                            if (!inverseFreqMap.containsKey(oldFreq+1)) 
                            {
                                HashSet<String> newTermSet = new HashSet<String>();
                                newTermSet.add(word);
                                
                                inverseFreqMap.put(oldFreq+1, newTermSet);
                            }
                            else {inverseFreqMap.get(oldFreq+1).add(word);}
                            
                            //System.out.println(oldFreq);
                            if (inverseFreqMap.get(oldFreq).size()>1) inverseFreqMap.get(oldFreq).remove(word);
                            else inverseFreqMap.remove(oldFreq);
                            
                            
                        }
                        
                        double tf = (double) freqVector[j]/freqVector.length;
                        double idf = Math.log((double) noOfDocuments/noDocWithTerm.get(word));
                        double tfidf = (double) tf*idf;
                        
                        if (!tfidfMap.containsKey(word))
                        {
                            HashMap<Integer, Double> newMap = new HashMap<Integer, Double>();
                            newMap.put(i, tfidf);
                            tfidfMap.put(word, newMap);
                        }
                        else
                        {
                            HashMap<Integer, Double> oldMap = tfidfMap.get(word);
                            oldMap.put(i, tfidf);
                        }
                        
                    }
                }
                
                System.out.println("TFIDF is calcuated");
                
                Pattern numberPattern = Pattern.compile("\\d+?");
                
                for (Integer selectedFreq : inverseFreqMap.descendingKeySet())
                {
                    System.out.println(selectedFreq);
                    for (String word : inverseFreqMap.get(selectedFreq))
                    {
                        
                        Matcher matchDigits = numberPattern.matcher(word);
                        if (!selectedTermsSet.contains(word) && !matchDigits.find() && word.length()>1 && !word.contains(" "))
                        {
                            System.out.println(word);
                            writerLearn.write("@attribute "+word+" real \n" );
                        
                            //freqTermInDocMapTrimmed.put(word, freqTermInDocMap.get(word));
                            
                            selectedTermsSet.add(word);
                        }
                    }
                    
                    
                    if (selectedTermsSet.size()>size) break;
                }
                
                System.out.println("Terms are selected");
                
                for (int i=0; i<noOfDocuments; i++)
                {
                    String docFolder = reader.document(i).get("folder");
                    if (docFolder != null) if (!listOfCat.contains(docFolder)) listOfCat = listOfCat+docFolder+", ";
                }

                listOfCat = listOfCat.substring(0, listOfCat.lastIndexOf(", "));

                writerLearn.write("@attribute domain {"+listOfCat+"} \n");
                writerLearn.write("@data \n");
                
                writerLearn.flush();

                for (int i=0; i<noOfDocuments; i++)
                {
                    String stringToAdd = null;

                    String docFolder = reader.document(i).get("folder");
                    String docName = reader.document(i).get("name");
                    TermFreqVector vector = reader.getTermFreqVector(i, "contents");
                    
                    if (vector != null)
                    {
                        for (String selectedTerm :  selectedTermsSet)
                        {
                            if (vector.indexOf(selectedTerm) != -1)
                            {
                                double selectedFreq = 0;
                                
                                selectedFreq = tfidfMap.get(selectedTerm).get(i);
                                if (stringToAdd != null) stringToAdd = stringToAdd+", "+String.valueOf(selectedFreq); else stringToAdd = String.valueOf(selectedFreq);
                            }
                            
                            else if (stringToAdd != null) stringToAdd = stringToAdd+", "+Double.toString(0); else stringToAdd = Double.toString(0);
                        }
               
                    stringToAdd=stringToAdd+", "+docFolder+" \n";
                    writerLearn.write(stringToAdd);
                    } else {System.out.println("No vector for "+docName);}
                }

                writerLearn.close();
                //writerTest.close();
                reader.close();
          } catch (java.io.IOException ex) {ex.printStackTrace();}
             
    }

        public void createARFFTF(File folder, StopList stopList) {
        
        String listOfCat = "";

        ArrayList<Term> termSelList = new ArrayList<Term>();

        //Instances instances = new Instances(new InputStreamReader());

        HashMap<String, Integer> freqTermInCorpusMap = new HashMap<String, Integer>();
        HashMap<String, HashMap<Integer, Double>> tfidfMap = new HashMap<String, HashMap<Integer, Double>>();
        HashMap<String, HashMap<Integer, Double>> tfMap = new HashMap<String, HashMap<Integer, Double>>();
        HashSet<String> selectedTermsSet = new HashSet<String>();
        TreeMap<Integer, HashSet<String>> inverseFreqMap = new TreeMap<Integer, HashSet<String>>();
        HashMap<String, Integer> noDocWithTerm = new HashMap<String, Integer>();

        try {
                //System.out.println("THERE");
                FileOutputStream outStream = new FileOutputStream(arffFolder+"LEARN"+".arff");
                OutputStreamWriter writerLearn = new OutputStreamWriter(outStream, "UTF-8");
                writerLearn.write("@relation learn \n");
                
                IndexReader reader = IndexReader.open(FSDirectory.open(folder), true);

                Integer noOfDocuments = reader.maxDoc();
                
                for (int i=0; i<noOfDocuments; i++)
                {
                    TermFreqVector termFreqVector = reader.getTermFreqVector(i, "contents");
                    if (termFreqVector == null) continue;
                    String[] termVector = termFreqVector.getTerms();
                    
                    for (int j=0; j<termVector.length; j++)
                    {
                        String word = termVector[j];
                        
                        if (!noDocWithTerm.containsKey(word))
                        {
                            noDocWithTerm.put(word, 1);
                        }
                        else
                        {
                            noDocWithTerm.put(word, noDocWithTerm.get(word)+1);
                        }
                        
                    }
                }
                
                
                
                for (int i=0; i<noOfDocuments; i++)
                {
                    TermFreqVector termFreqVector = reader.getTermFreqVector(i, "contents");
                    if (termFreqVector == null) continue;
                    String[] termVector = termFreqVector.getTerms();
                    int[] freqVector = termFreqVector.getTermFrequencies();
                    
                    for (int j=0; j<termVector.length; j++)
                    {
                        String word = termVector[j];
                        
                        if (!freqTermInCorpusMap.containsKey(word))
                        {
                            freqTermInCorpusMap.put(word, 1);
                            
                            HashSet<String> newTermSet = new HashSet<String>();
                            newTermSet.add(word);
                            if (!inverseFreqMap.containsKey(1)) inverseFreqMap.put(1, newTermSet);
                            else {inverseFreqMap.get(1).add(word);}
                        }
                        else
                        {
                            int oldFreq = freqTermInCorpusMap.get(word);
                            
                            freqTermInCorpusMap.put(word, oldFreq+1);
                            
                            if (!inverseFreqMap.containsKey(oldFreq+1)) 
                            {
                                HashSet<String> newTermSet = new HashSet<String>();
                                newTermSet.add(word);
                                
                                inverseFreqMap.put(oldFreq+1, newTermSet);
                            }
                            else {inverseFreqMap.get(oldFreq+1).add(word);}
                            
                            //System.out.println(oldFreq);
                            if (inverseFreqMap.get(oldFreq).size()>1) inverseFreqMap.get(oldFreq).remove(word);
                            else inverseFreqMap.remove(oldFreq);
                            
                            
                        }
                        
                        double tf = (double) freqVector[j]/freqVector.length;
                        double idf = Math.log((double) noOfDocuments/noDocWithTerm.get(word));
                        double tfidf = (double) tf*idf;
                        
                        if (!tfidfMap.containsKey(word))
                        {
                            HashMap<Integer, Double> newTFIDFMap = new HashMap<Integer, Double>();
                            newTFIDFMap.put(i, tfidf);
                            tfidfMap.put(word, newTFIDFMap);
                            
                        }
                        else
                        {
                            HashMap<Integer, Double> oldMap = tfidfMap.get(word);
                            oldMap.put(i, tfidf);
                        }
                        
                        if (!tfMap.containsKey(word))
                        {
                            HashMap<Integer, Double> newTFMap = new HashMap<Integer, Double>();
                            newTFMap.put(i, tf);
                            tfMap.put(word, newTFMap);
                        }
                        else
                        {
                            HashMap<Integer, Double> oldMap = tfMap.get(word);
                            oldMap.put(i, tf);
                        }
                        
                    }
                }
                
                System.out.println("TFIDF is calcuated");
                
                Pattern numberPattern = Pattern.compile("\\d+?");
                
                for (Integer selectedFreq : inverseFreqMap.descendingKeySet())
                {
                    //System.out.println(selectedFreq);
                    for (String word : inverseFreqMap.get(selectedFreq))
                    {
                        
                        Matcher matchDigits = numberPattern.matcher(word);
                        if (!selectedTermsSet.contains(word) && !stopList.stopList.contains(word) && !word.contains(" ") && word.length()>1 && !matchDigits.find())
                        {
                            //System.out.println(word);
                            writerLearn.write("@attribute "+word+" real \n" );
                        
                            //freqTermInDocMapTrimmed.put(word, freqTermInDocMap.get(word));
                            
                            selectedTermsSet.add(word);
                        }
                    }
                    
                    
                    if (selectedTermsSet.size()>size) break;
                }
                
                System.out.println("Terms are selected");
                
                for (int i=0; i<noOfDocuments; i++)
                {
                    String docFolder = reader.document(i).get("folder");
                    if (docFolder != null) if (!listOfCat.contains(docFolder)) listOfCat = listOfCat+docFolder+", ";
                }

                listOfCat = listOfCat.substring(0, listOfCat.lastIndexOf(", "));

                writerLearn.write("@attribute domain {"+listOfCat+"} \n");
                writerLearn.write("@data \n");
                //writerTest.write("@attribute domain {"+listOfCat+"} \n");
                //writerTest.write("@data \n");
                
                writerLearn.flush();

                for (int i=0; i<noOfDocuments; i++)
                {
                    //System.out.println("Document "+i);
                    
                    String stringToAdd = null;

                    String docFolder = reader.document(i).get("folder");
                    String docName = reader.document(i).get("name");
                    TermFreqVector vector = reader.getTermFreqVector(i, "contents");
                    
                    if (vector != null)
                    {
                        for (String selectedTerm :  selectedTermsSet)
                        {
                            String addValue = "0";
                            if (vector.indexOf(selectedTerm) != -1)
                            {
                                double selectedFreq = 0;
                                
                                //if (freqTermInDocMap.get(selectedTerm).containsKey(i)) 
                                selectedFreq = tfMap.get(selectedTerm).get(i);
                                if (selectedFreq > 0) addValue=String.valueOf(selectedFreq);
                                //if (stringToAdd != null) stringToAdd = stringToAdd+", "+String.valueOf(selectedFreq); else stringToAdd = String.valueOf(selectedFreq);
                            
                                if (stringToAdd != null) stringToAdd = stringToAdd+", "+addValue; else stringToAdd = addValue;
                            
                            }
                            
                            else if (stringToAdd != null) stringToAdd = stringToAdd+", "+Double.toString(0); else stringToAdd = Double.toString(0);
                                //if (stringToAdd != null) stringToAdd = stringToAdd+", "+addValue; else stringToAdd = addValue;
                                }
               
                    stringToAdd=stringToAdd+", "+docFolder+" \n";
                    //ratio = (float) i/noOfDocuments;
                    writerLearn.write(stringToAdd);
                    //if (ratio < 0.71) writerLearn.write(stringToAdd); else writerTest.write(stringToAdd);
                    } else {System.out.println("No vector for "+docName);}
                }

                writerLearn.close();
                //writerTest.close();
                reader.close();
          } catch (java.io.IOException ex) {ex.printStackTrace();}
             
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
