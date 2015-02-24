/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package documentclassifier;

import java.io.*;


/**
 *
 * @author PG
 */
public class DocumentClassifier {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Download newDown = new Download();
        
        //newDown.model = newDown.loadOntology(null);
        
        //newDown.classToFileMap = newDown.createFolders(newDown.rootClass);
        
        //newDown.classToSearchStringMap = newDown.createSearchStrings();
        
        //newDown.downloadDocuments();
        
        File indexedFolder = new File(newDown.initFolder);
        //Index newIndex = new Index ();
        //newIndex.createIndex(indexedFolder);
        //System.out.println("Index is created");
        
        //StopList newList = new StopList();
        //newList.loadStopList("Stoplista.txt");
        
        //ARFF newARFF = new ARFF(
        //        args[0]);
        //newARFF.size=7*newDown.sampleSize;
        //newARFF.createARFFTF(indexedFolder, newList);
        
        Test newTest = new Test();
        
        newTest.runTest(
                args[0]);
    }
}
