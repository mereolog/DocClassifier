/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package documentclassifier;


import java.util.HashSet;
import java.io.*;
import java.util.regex.*;

/**
 *
 * @author PG
 */
public class StopList {
    
    HashSet<String> stopList;
    
    public HashSet<String> loadStopList (String fileName)
    {
        try
        {
            stopList = new HashSet<String>();
            
            FileInputStream inStream = new FileInputStream(fileName);
            InputStreamReader read = new InputStreamReader(inStream, "UTF-8");
            
            BufferedReader buffReader = new BufferedReader(read);
        
            String content = "";
            String newLine = "";
            
            while (newLine != null)
            {
                try{newLine = buffReader.readLine();} catch (java.io.IOException e) {e.printStackTrace();}
                if (newLine != null) content = content+newLine;
            }
            
            
            Pattern stopPattern = Pattern.compile("(.+?)\\,");
            Matcher stopMatch = stopPattern.matcher(content);
            while (stopMatch.find())
            {
                stopList.add(stopMatch.group(1).trim());
            }
            
            //System.out.println(stopList);
            
            
        } catch (java.io.FileNotFoundException e){e.printStackTrace();} catch (java.io.UnsupportedEncodingException e) {e.printStackTrace();}
        
        return stopList;
    }
    
}
