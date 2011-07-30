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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
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
            if (this.plugin.daystokeep == 0) {
                return;
            }
            String[] children = new File(this.plugin.bckDir).list();
            Calendar today = Calendar.getInstance();
            Calendar fday = Calendar.getInstance();
            today.setTime(this.dateFormat.parse(
                    format(today.get(Calendar.YEAR)) + "."
                    + format(today.get(Calendar.MONTH) + 1) + "."
                    + today.get(Calendar.DAY_OF_MONTH)));
            long diffDays;
            int bckDeleted = 0;

            if (children != null) {
            	
                for (int i = 0; i < children.length; i++) {
                	fday.setTime(this.dateFormat.parse(children[i]));
                    diffDays = getDifference(fday, today, TimeUnit.DAYS);
                    this.plugin.log(children[i] + " : " + diffDays + " days ?");
                    if (diffDays > this.plugin.daystokeep) {
                        this.plugin.log(Level.INFO,
                                " + deleting " + children[i]
                                + " due to age limitation (" + diffDays
                                + " day(s))");
                        alexoft.Minebackup.DirUtils.deleteDirectory(
                                new File(this.plugin.bckDir + "/" + children[i]));
                        bckDeleted += 1;
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
