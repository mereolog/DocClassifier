/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package documentclassifier;

import java.net.*;
import java.io.*;
import java.util.regex.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import java.util.HashMap;

/**
 *
 * @author PG
 */
public class Download {

    OntModel model;
    
    String classifOntLocation = "http://www.l3g.pl/ontologies/Classification.owl";
    
    HashMap<OntClass, File> classToFileMap;
    HashMap<OntClass, String> classToSearchStringMap;
    
    String initFolder = "/Users/PG/Documents/Private/Professional/Cognitum/Classifier/src";
    
    OntClass rootClass;
    
    AnnotationProperty isDescribedBy;
    
    int sampleSize=100;
    
    public OntModel loadOntology (String location)
    {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.setDynamicImports(true);
        model.read(classifOntLocation, null, null);
        
        rootClass = model.getOntClass("http://www.l3g.pl/ontologies/Classification.owl#Domain");
        
        isDescribedBy = model.getAnnotationProperty(classifOntLocation+"#isDescribedBy");
        
        return model;
    }
    
    public HashMap<OntClass, File> createFolders (OntClass ontClass)
    {
        if (ontClass.equals(rootClass))
        {
            classToFileMap = new HashMap<OntClass, File>();
            
            File rootFolder = new File(initFolder+"/"+rootClass.getLocalName());
            System.out.println(rootFolder);
            rootFolder.mkdir();
            try {rootFolder.createNewFile();} catch (java.io.IOException ex) {ex.printStackTrace();}
            
            
            classToFileMap.put(ontClass, rootFolder);
        }
        
        if (ontClass.hasSubClass())
        {
            for (OntClass subClass : ontClass.listSubClasses().toSet())
            {
                
                File newFolder = new File(classToFileMap.get(ontClass).getAbsolutePath()+"/"+subClass.getLocalName());
                newFolder.mkdir();
                try {newFolder.createNewFile();} catch (java.io.IOException ex) {ex.printStackTrace();}
                
                
                classToFileMap.put(subClass, newFolder);
                
                classToFileMap=createFolders(subClass);
            }
        }
        
        return classToFileMap;
    }
    
    public HashMap<OntClass, String> createSearchStrings ()
    {
        classToSearchStringMap = new HashMap<OntClass, String>();
        
        for (OntClass ontClass : model.listNamedClasses().toSet())
        {
            String searchString = "";
            
            if (ontClass.getPropertyValue(isDescribedBy) != null)
            {
                for (RDFNode description : ontClass.listPropertyValues(isDescribedBy).toSet())
                {
                    searchString = searchString+description.asLiteral().getString()+"+";
                }
                
                searchString=searchString.substring(0, searchString.length()-1);
                classToSearchStringMap.put(ontClass, searchString);
            }
        }
        
        return classToSearchStringMap;
    }
    
    public void downloadDocuments ()
    {
        String googleSearchPattern = "http://www.google.com/search?";
        
        int count=1;

       //String googleSearchPattern = "http://www.google.com/search?num=100&lr=en&filter=1&q=";
        //String searchTermPattern = "religion+religious+open+belief+faith+life";
        //String initDir = "/Users/PG/Desktop/DocClass/src/religion";

        //http://www.google.com/search?client=safari&rls=en&q=lekcja+szkoła+podstawowa&ie=UTF-8&oe=UTF-8
        
        for (OntClass ontClass : classToSearchStringMap.keySet())
        {
            
            
            try
            {
                URL urlGoogle = new URL(googleSearchPattern+"&lr=lang_pl&"+"num="+sampleSize+"&"+"q="+classToSearchStringMap.get(ontClass));
                //System.out.println(urlGoogle);
                HttpURLConnection connGoogle = (HttpURLConnection)urlGoogle.openConnection();
                connGoogle.setRequestProperty("User-agent", "Mozilla/4.0");

                connGoogle.connect();

                InputStream in = connGoogle.getInputStream();

                String cs = connGoogle.getContentEncoding();
                
                if (cs == null) cs="UTF-8";

                int expectedLength = connGoogle.getContentLength();

                if (expectedLength == -1) expectedLength=20240;

                byte[] buf = new byte[expectedLength];
                int n;
                int total = 0;

                while ((n = in.read (buf, total, buf.length - total)) != -1)
                {
                    total += n;
                    if (total == buf.length)
                    {
                    // try to read one more character
                        int c = in.read ();

                        if (c == -1)
                            break; // EOF, we're done
                        else
                        {
                            // need more space in array.  Double the array, but don't make
                            // it bigger than maxBytes.
                            byte[] newbuf = new byte[Math.min (buf.length * 2, Integer.MAX_VALUE)];
                            System.arraycopy (buf, 0, newbuf, 0, buf.length);
                            buf = newbuf;
                            buf[total++] = (byte) c;
                        }
                    }
                }

                String result = new String(buf, cs);
                in.close ();

                Pattern hyperLink = Pattern.compile("<p><a href=..url.q=(.*?).amp");
                Matcher matchLink = hyperLink.matcher(result);
                
                Pattern encPattern = Pattern.compile("charset\\=(.+?)\\z");
                

                String csPage = null;
                
                //(new File(initDir+"/"+search.categoryName)).mkdir();

                while (matchLink.find())
                {
                    String matchString = matchLink.group(1);
                    System.out.println(matchString);
                    if (!matchString.startsWith("https") && !matchString.contains(".pdf"))
                    {
                        URL url = new URL(matchString);
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setRequestProperty("User-agent", "safari");
                        conn.connect();
                        if (conn.getResponseCode() <= 300)
                        {
                            String type = conn.getContentType();
                            System.out.println(type);
                            if (type.startsWith("text/html"))
                            {
                                BufferedReader inReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                String resultPage="";
                                String inputLine;
                                while ((inputLine = inReader.readLine()) != null) 
                                resultPage=resultPage+inputLine;
                                inReader.close();
                                
                                Matcher encMatch = encPattern.matcher(type);
                                
                                while (encMatch.find())
                                {
                                    csPage = encMatch.group(1).toUpperCase();
                                }
                                
                                if (csPage == null) csPage="UTF-8";
                                
                                if (csPage.length() == 0) csPage="UTF-8";
                                
                                csPage=csPage.replace("\"", "");

                                String namePage = "File_";
                                //namePage = namePage.replaceAll("\\.", "_");
                                //namePage = namePage.replaceAll("http://", "");
                                //namePage = namePage.replaceAll("/", "_");
                                //System.out.println(namePage);

                                File newDocument = new File(classToFileMap.get(ontClass)+"/"+namePage+count+".txt");
                                newDocument.createNewFile();
                            
                                count++;
                                
                                
                            
                                FileOutputStream outStream = new FileOutputStream(newDocument);
                                OutputStreamWriter writer = new OutputStreamWriter(outStream, csPage);
                                writer.write(resultPage);
                                writer.close();   
                            }

                            
                        }
                    }
            }
            } catch (java.net.MalformedURLException ex) {ex.printStackTrace();} catch (java.io.IOException ex) {ex.printStackTrace();}
        }
    }
}
