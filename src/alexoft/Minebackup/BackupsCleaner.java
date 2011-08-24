package alexoft.Minebackup;


import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


/**
 *
 * @author Alexandre
 */
public class BackupsCleaner extends Thread {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-m-s");
    private MineBackup plugin;
     
    public BackupsCleaner(MineBackup plugin) {
        this.plugin = plugin;
    }
     
    public static long getDifference(Calendar a, Calendar b, TimeUnit units) {
        return
           units.convert(b.getTimeInMillis() - a.getTimeInMillis(), TimeUnit.MILLISECONDS);
      }
     
    @Override
    public void run() {
        try {
            if (this.plugin.config.daystokeep == 0) {
                return;
            }
            Calendar today = Calendar.getInstance();
            Calendar fday = Calendar.getInstance();
            today.setTime(this.dateFormat.parse(
            		format(today.get(Calendar.YEAR)) + "-" + 
                    		format(today.get(Calendar.MONTH) + 1) + "-" + 
                    		format(today.get(Calendar.DAY_OF_MONTH)) + "-" + 
                    		format(today.get(Calendar.HOUR_OF_DAY)) + "-" +
                            format(today.get(Calendar.MINUTE)) + "-" +
                            format(today.get(Calendar.SECOND))));
            long diffDays;
            int bckDeleted = 0;
String[] worlds  = new File(this.plugin.config.bckDir).list();
if (worlds != null) {	
    for (int w = 0; w < worlds.length; w++) {
            String[] children = new File(this.plugin.config.bckDir + "/" + worlds[w]).list();
            if (children != null) {            	
                for (int i = 0; i < children.length; i++) {
                	fday.setTime(this.dateFormat.parse(children[i]));
                    diffDays = getDifference(fday, today, TimeUnit.DAYS);
                    if(this.plugin.config.debug) this.plugin.log(worlds[w] + ":" + children[i] + " : " + diffDays + " days ?");
                    if (diffDays > this.plugin.config.daystokeep) {
                        this.plugin.log(Level.INFO,
                                " + deleting " + worlds[w] + ":" + children[i]
                                + " due to age limitation (" + diffDays
                                + " day(s))");
                        alexoft.Minebackup.DirUtils.delete(
                                new File(this.plugin.config.bckDir + "/" + worlds[w] + "/" + children[i]));
                        bckDeleted += 1;
                    }
                }
            }
        	
        }
}
            this.plugin.log(Level.INFO,
                    " + " + bckDeleted + " backup(s) deleted");
        } catch (ParseException ex) {
            this.plugin.log(Level.SEVERE, ex.getMessage());
        }
        
    }

    private String format(int i) {
        String r = String.valueOf(i);

        if (r.length() == 1) {
            r = "0" + r;
        }
        return r;
    }
     
}
