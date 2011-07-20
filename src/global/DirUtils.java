/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package global;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 *
 * @author Alexandre
 */
public class DirUtils {
    static public boolean deleteDirectory(File path) { 
        boolean resultat = true; 
        
        if (path.exists()) { 
            File[] files = path.listFiles(); 

            for (int i = 0; i < files.length; i++) { 
                if (files[i].isDirectory()) { 
                    resultat &= deleteDirectory(files[i]); 
                } else { 
                    resultat &= files[i].delete(); 
                } 
            } 
        } 
        resultat &= path.delete(); 
        return(resultat); 
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws FileNotFoundException, IOException {
        
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();

            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
