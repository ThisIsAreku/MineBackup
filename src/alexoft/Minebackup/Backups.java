/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alexoft.Minebackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.logging.Level;
import org.bukkit.World;

/**
 *
 * @author Alexandre
 */
public class Backups {
    private MineBackup plugin;
    
    public Backups(MineBackup plugin) {
        this.plugin = plugin;
    }
    
    public void MakeBackup() {
        try {
            plugin.log(Level.INFO, "Starting backup...");
            File tempDir = new File(String.valueOf(Math.random()));
            tempDir.mkdirs();
            for(String w:plugin.worlds) {
                plugin.log(Level.INFO, " * " + w);
                copyWorld(plugin.getServer().getWorld(w),tempDir);
            }
            BackupWorld(tempDir);
            deleteDirectory(tempDir);
            plugin.log(Level.INFO, "Done !");
        }catch(Exception ex){
            plugin.log(Level.WARNING, "error; " + ex);            
        }
    }
    public void MakeBackup(World world) {
        try {
            plugin.log(Level.INFO, "Backing up '" + world.getName() + "'...");
            File tempDir = new File(String.valueOf(Math.random()));
            tempDir.mkdirs();
            copyWorld(world,tempDir);
            BackupWorld(tempDir);
            deleteDirectory(tempDir);
            plugin.log(Level.INFO, "Done !");
        }catch(Exception ex){
            plugin.log(Level.WARNING, "error; " + ex);            
        }
    }
    
    public void BackupWorld(File tempDir) {
       plugin.log(Level.INFO, "Compressing...");
        Calendar today = Calendar.getInstance();
        String currentDirName = format(today.get(Calendar.DAY_OF_MONTH)) + "." + format(today.get(Calendar.MONTH))+ "." + today.get(Calendar.YEAR);
        String currentFileName = format(today.get(Calendar.HOUR_OF_DAY)) + "_" + format(today.get(Calendar.MINUTE))+ "_" + format(today.get(Calendar.SECOND));
        new File(plugin.bckDir + "/" + currentDirName).mkdirs();
        try {
            new File(plugin.bckDir + "/" + currentDirName + "/" + currentFileName + ".zip").createNewFile();
            global.OutilsZip.zipDir(tempDir.getPath(), plugin.bckDir + "/" + currentDirName + "/" + currentFileName + ".zip");
        }catch(Exception ex) {
            plugin.log(Level.WARNING, "error; " + ex);
            new File(plugin.bckDir + "/" + currentDirName + "/" + currentFileName + ".zip").delete();
        }
    }
    
    private void copyWorld(World world,File tempDir) throws IOException {
        world.save();
        try {
        copyDirectory(new File(world.getName()), new File(tempDir.getPath() + "/" + world.getName()));
        }catch(Exception ex) {
            deleteDirectory(tempDir);
            throw new IOException();
        }
    }
    private String format(int i) {
        String r = String.valueOf(i);
        if(r.length() == 1) r = "0" + r;
        return r;
    }
    static public boolean deleteDirectory(File path) { 
        boolean resultat = true; 
        
        if( path.exists() ) { 
                File[] files = path.listFiles(); 
                for(int i=0; i<files.length; i++) { 
                        if(files[i].isDirectory()) { 
                                resultat &= deleteDirectory(files[i]); 
                        } 
                        else { 
                        resultat &= files[i].delete(); 
                        } 
                } 
        } 
        resultat &= path.delete(); 
        return( resultat ); 
}
     public void copyDirectory(File sourceLocation , File targetLocation) throws FileNotFoundException, IOException{
        
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
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
