package alexoft.Minebackup;


import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;


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
    public static long daysBetween(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime() + ONE_HOUR) / (ONE_HOUR * 24));
    }
     
    @Override
    public void run() {
        try {
            if (this.plugin.daystokeep == 0) {
                return;
            }
            String[] children = new File(this.plugin.bckDir).list();
            Calendar today = Calendar.getInstance();
            Date currDate = this.dateFormat.parse(
                    format(today.get(Calendar.DAY_OF_MONTH)) + "."
                    + format(today.get(Calendar.MONTH) + 1) + "."
                    + today.get(Calendar.YEAR));
            Date dirDate;
            long diffDays;
            int bckDeleted = 0;

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    dirDate = this.dateFormat.parse(children[i]);
                    diffDays = daysBetween(currDate, dirDate);
                    if (diffDays > this.plugin.daystokeep) {
                        this.plugin.log(Level.INFO,
                                " + deleting " + children[i]
                                + " due to age limitation (" + diffDays
                                + " day(s))");
                        global.DirUtils.deleteDirectory(
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
