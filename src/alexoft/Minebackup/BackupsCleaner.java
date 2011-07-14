/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alexoft.Minebackup;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexandre
 */
public class BackupsCleaner extends Thread {
     private SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy");
     private MineBackup plugin;
     
     public BackupsCleaner(MineBackup plugin) {
         this.plugin = plugin;
     }
     
     static final long ONE_HOUR = 60 * 60 * 1000L;
     public static long daysBetween(Date d1, Date d2){
       return ( (d2.getTime() - d1.getTime() + ONE_HOUR) /
                     (ONE_HOUR * 24));
     }
     
    @Override
    public void run() {
        if (this.plugin.daystokeep == 0) return;
        String[] children = new File(this.plugin.bckDir).list();
        Date currDate = new Date();
        this.plugin.log(Level.WARNING, currDate.toString());
        Date dirDate;
        long diffDays;
        int bckDeleted = 0;
        if (children != null) {
            for (int i=0; i<children.length; i++) {
                try {
                    dirDate = this.dateFormat.parse(children[i]);
                    diffDays = daysBetween(currDate, dirDate);
                    if (diffDays > this.plugin.daystokeep) {
                        this.plugin.log(Level.INFO, " + deleting " + children[i] + " due to age limitation");
                        global.DirUtils.deleteDirectory(new File(this.plugin.bckDir + "/" + children[i]));
                        bckDeleted += 1;
                    }
                } catch (ParseException ex) {
                    this.plugin.log(Level.SEVERE, ex.getMessage());
                }
            }
        }
        this.plugin.log(Level.INFO, " + " + bckDeleted + " backup(s) deleted");
        
    }
     
}
