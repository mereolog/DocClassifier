/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package documentclassifier;

import java.io.*;
import java.util.HashSet;
import java.util.HashMap;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.bayes.*;
import weka.classifiers.Classifier;
import weka.core.converters.ConverterUtils.*;
import weka.core.converters.*;


/**
 *
 * @author PG
 */
public class Test {

    
    HashMap<Integer, String> internalMap;
    HashMap<String, String> classificationMap;
    /**
     * @param args the command line arguments
     */
    
    public Classifier createClassifier (Instances learnInst)
    {
        ComplementNaiveBayes classifier = new ComplementNaiveBayes();
        try{
        classifier.buildClassifier(learnInst);
        
        } catch (java.lang.Exception ex) {ex.printStackTrace();}
        
        return classifier;
        
    }
    
    public Instances createNewInstances (File folder, Instances oldInstances)
    {
        Instances newInstances = null;
        
        int attNo = oldInstances.numAttributes();
        HashSet<String> attSet = new HashSet<String>();
        
        for (int i=0; i<attNo-1; i++)
        {
            attSet.add(oldInstances.attribute(i).name());
        }
        
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(folder), true);
            Integer noOfDocuments = reader.maxDoc();
            //System.out.println(noOfDocuments);
            newInstances = new Instances(oldInstances, noOfDocuments);
            newInstances.setClassIndex(newInstances.numAttributes()-1);
            
            int noInst=0;
            
            //newInstances.deleteAttributeAt(oldInstances.numAttributes()-1);
            
            for (int i=0; i<noOfDocuments; i++)
            {
                
                String docFolder = reader.document(i).get("folder");
                String docName = reader.document(i).get("name");
                TermFreqVector testVector = reader.getTermFreqVector(i, "contents");
                //System.out.println(testVector);
                
                if (testVector != null)
                {
                    int[] freqVector = testVector.getTermFrequencies();
                    double[] tfVector = new double[attSet.size()];
                    int j=0;
                    
                    for (String selectedTerm :  attSet)
                    {
                        if (testVector.indexOf(selectedTerm) != -1)
                        {
                            double tf = (double) freqVector[testVector.indexOf(selectedTerm)]/freqVector.length;
                            tfVector[j]=tf;
                        }
                        else tfVector[j]=0;
                        
                        j++;
                    }
               
                    Instance newInstance = new Instance(1, tfVector);
                    newInstances.add(newInstance);
                    //System.out.println(newInstance+" <> "+docName);
                    internalMap.put(noInst, docName);
                    noInst++;
                    
                } else {System.out.println("No vector for "+docName);}
            }
            
        } catch (java.io.IOException e) {e.printStackTrace();}
        
        
        return newInstances;
    
    }
    
    public Instances loadInstances (String folder)
    {
        Instances instances = null;
        String learnARFF = folder+"LEARN.arff";
        
        try{
            
            //BufferedReader reader = new BufferedReader(new FileReader(learnARFF));
        
            FileInputStream fis = new FileInputStream(new File (learnARFF));
            InputStreamReader r = new InputStreamReader(fis, "UTF-8");
            //ArffLoader loader = new ArffLoader();
            //loader.setFile(new File (learnARFF));
            
            //instances = DataSource.read(loader);
            
            instances =  new Instances(r);
        
        if (instances.classIndex() == -1) {instances.setClassIndex(instances.numAttributes()-1);}
        
        } catch (java.lang.Exception ex) {ex.printStackTrace();}
        
        //System.out.print(instances);
        
        return instances;
    }
    
    public HashMap<String, String> classify(Classifier classifier, Instances instances, HashMap<Integer, String> map)
    {
        
        for (int i=0;i<instances.numInstances()-1;i++)
        {
            try{
                String docName = map.get(i);
                
                int classification = (int) classifier.classifyInstance(instances.instance(i));
                
                this.classificationMap.put(docName, instances.classAttribute().value(classification));
                
            } catch (java.lang.Exception e) {e.printStackTrace();}
        }
        
        return classificationMap;
    }
    
    public void runTest (String folder)
    {
        Index newIndex = new Index ();
        File folderFile = new File (folder);
        newIndex.createIndex(folderFile);
        System.out.println("Test index is created");
        
        this.classificationMap = new HashMap<String, String>();
        this.internalMap = new HashMap<Integer, String>();
        
        Instances oldInstances = this.loadInstances(folder);
        Classifier classifier = this.createClassifier(oldInstances);
        Instances newInstances = this.createNewInstances(folderFile, oldInstances);
        //System.out.println(internalMap);
        this.classify(classifier, newInstances, this.internalMap);
        
        try{DataSink.write(folder+"TEST.arff", newInstances);} catch (java.lang.Exception e) {e.printStackTrace();}
        
        System.out.println(this.classificationMap);
    }
}
