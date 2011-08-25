package alexoft.Minebackup;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


/**
 *
 * @author Alexandre
 */
public class BackupsCleaner extends Thread {
    private MineBackup plugin;
     
    public BackupsCleaner(MineBackup plugin) {
        this.plugin = plugin;
    }
     
    public static long getDifference(long a, long b, TimeUnit units) {
        return
           units.convert(b - a, TimeUnit.MILLISECONDS);
      }
     
    /* old func@Override
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
            for (int w = 0; w < this.plugin.config.worlds.size(); w++) {
            	World world = this.plugin.getServer().getWorld(this.plugin.config.worlds.get(w));
            	
				String BACKUP_NAME = new File(this.plugin.config.bckDir, this.plugin.getBackupName(world)).toString();
				int last = BACKUP_NAME.lastIndexOf(File.separator);
				String dir = BACKUP_NAME.substring(0, last);
				String file = BACKUP_NAME.substring(last+1);
				
	            String[] children = new File(dir).
            }

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
    }*/@Override
    public void run() {
    	long now = System.currentTimeMillis();
    	long diffDays;
    	int bckDeleted = 0;
    	for(File f:recursiveListFiles(new File(this.plugin.config.bckDir))){
    		if(f.toString() != this.plugin.config.bckDir){
    		diffDays = getDifference(f.lastModified(), now, TimeUnit.DAYS);
            if(this.plugin.config.debug) this.plugin.log(f + " : " + diffDays + " days");
            if (diffDays > this.plugin.config.daystokeep) {
                if(alexoft.Minebackup.DirUtils.delete(f)){
                    this.plugin.log(
                            " + deleted " + f
                            + " due to age limitation (" + diffDays
                            + " day(s))");
                }else{
                    this.plugin.log("Cannot delete " + f + " !");
                }
                bckDeleted += 1;
            }
    		}
    	}
    	for(int i = 0;i<3;i++) //remove empty directories
    	for(File f:recursiveListDir(new File(this.plugin.config.bckDir))){
    		if(f.list().length == 0)
    		{
    			f.delete();
    		}
    	}
        this.plugin.log(Level.INFO,
                " + " + bckDeleted + " backup(s) deleted");
    }
    private List<File> recursiveListFiles(File path) {
    	List<File> o = new ArrayList<File>();
    	if (path.isDirectory()) {
    	    File[] list = path.listFiles();
    	    if (list != null) {
	    		for (int i = 0; i < list.length; i++) {
	    			o.addAll(recursiveListFiles(list[i]));
	    		}
    	    } else {
    	    	this.plugin.log(Level.WARNING, "Cannot acces to " + path);
    	    }
    	} else {
    	    o.add(path);
    	}
    	return o;
     }
    private List<File> recursiveListDir(File path) {
    	List<File> o = new ArrayList<File>();
    	if (path.isDirectory()) {
    	    o.add(path);
    	    File[] list = path.listFiles();
    	    if (list != null) {
	    		for (int i = 0; i < list.length; i++) {
	    			o.addAll(recursiveListDir(list[i]));
	    		}
    	    } else {
    	    	this.plugin.log(Level.WARNING, "Cannot acces to " + path);
    	    }
    	}
    	return o;
     }
     
}
