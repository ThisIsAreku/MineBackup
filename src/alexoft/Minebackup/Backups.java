package alexoft.Minebackup;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;


/**
 *
 * @author Alexandre
 */
public class Backups extends Thread {
    private MineBackup plugin;
    public boolean userStarted;
    public String userName;
    
    public Backups(MineBackup plugin) {
        this.plugin = plugin;
        this.userStarted = false;
        this.userName = "";
    }

    public Backups(MineBackup plugin, boolean userStarted, String userName) {
        this.plugin = plugin;
        this.userStarted = userStarted;
        this.userName = userName;
    }
    
    public void MakeBackup() {
        try {
            this.plugin.log(Level.INFO, "Starting backup...");
            for (String w:plugin.config.worlds) {
                World world = plugin.getServer().getWorld(w);

                if (world == null) {
                    this.plugin.log(
                            "world '" + w
                            + "' not found.. check your config file");    
                } else {
                    plugin.log(Level.INFO, " * " + w);
                    MakeBackup(world);
                }
            }
        } catch (Exception ex) {
            this.plugin.logException(ex);          
        }
    }

    public void MakeBackup(World world) {
        try {
            if (world == null) {
                this.plugin.log("world not found.. check your config file");    
            } else {
                /*this.plugin.log(Level.INFO,
                        "Backing up '" + world.getName() + "'...");*/
                File tempDir = new File(this.plugin.config.bckTempDir, String.valueOf(Math.random()));
	
                tempDir.mkdirs();
                copyWorld(world, tempDir);
                compressDir(tempDir, world);
            }
        } catch (Exception ex) {
            this.plugin.logException(ex);              
        }
    }
    
    public void compressDir(File tempDir,World world) {
    	String BACKUP_NAME = new File(this.plugin.config.bckDir, getBackupName(world)).toString();
    	int last = BACKUP_NAME.lastIndexOf(File.separator);
    	String dir = BACKUP_NAME.substring(0, last);
    	String file = BACKUP_NAME.substring(last+1);
    	if(!new File(dir).exists()) new File(dir).mkdirs();
        if (this.plugin.config.compressionEnabled) {
            this.plugin.log(Level.INFO, "\tCompressing...");
            this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(
                    this.plugin,
                    new ZipDir(this.plugin, this, tempDir.getPath(),
                    		dir + "/" + file + ".zip"));
        } else {
            this.plugin.log(Level.INFO, "\tCopying...");
            this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(
                    this.plugin,
                    new CopyDir(this.plugin, this, tempDir.getPath(),
                    		dir + "/" + file));
    		
        }
    }
    
    private void copyWorld(World world, File tempDir) throws IOException {
        world.save();
        this.plugin.getServer().savePlayers();
        try {
            alexoft.Minebackup.DirUtils.copyDirectory(new File(world.getName()),
                    new File(tempDir.getPath() + "/" + world.getName()));
        } catch (Exception ex) {
            alexoft.Minebackup.DirUtils.deleteDirectory(tempDir);
            throw new IOException();
        }
    }

    private String format(int i) {
        String r = String.valueOf(i);

        if (r.length() == 1) {
            r = "0" + r;
        }
        return r;
    }
    private String getBackupName(World world) {
        Calendar today = Calendar.getInstance();
    	Map<String, String> formats = new HashMap<String, String>();
    	formats.put("%Y", format(today.get(Calendar.YEAR)));
    	formats.put("%M", format(today.get(Calendar.MONTH)));
    	formats.put("%D", format(today.get(Calendar.DAY_OF_MONTH)));
    	formats.put("%H", format(today.get(Calendar.HOUR_OF_DAY)));
    	formats.put("%m", format(today.get(Calendar.MINUTE)));
    	formats.put("%S", format(today.get(Calendar.SECOND)));
    	formats.put("%W", world.getName());
    	formats.put("%U", world.getUID().toString());
    	formats.put("%s", String.valueOf(world.getSeed()));
    	
    	String fname = this.plugin.config.bckFormat;
    	for(Entry<String, String> entry : formats.entrySet())
    	    fname = fname.replaceAll(entry.getKey(), entry.getValue());
    	
    	return fname;
    }

    public void afterRun() {
        plugin.log(Level.INFO, "Done !");
        this.plugin.getServer().dispatchCommand(
                new ConsoleCommandSender(this.plugin.getServer()), "save-on");
    	SendMessage(this.plugin.config.msg_BackupEnded); 
        this.plugin.isBackupStarted = false;
    }
    
    private void backupRun() {
        this.plugin.isBackupStarted = true;
        if (this.userStarted) {
        	SendMessage(this.plugin.config.msg_BackupStarted.replaceAll("%player%", this.userName));                  
        } else {
        	SendMessage(this.plugin.config.msg_BackupStarted); 
        }
        this.plugin.getServer().dispatchCommand(
                new ConsoleCommandSender(this.plugin.getServer()), "save-off");
        this.plugin.getServer().dispatchCommand(
                new ConsoleCommandSender(this.plugin.getServer()), "save-all");
        this.MakeBackup();
        afterRun();
    }
    
    private void SendMessage(String m){
    	if(this.plugin.config.msg_enable) this.plugin.getServer().broadcastMessage(m);
    }

    @Override
    public void run() {
        try {
            if (this.plugin.config.pauseWhenNoPlayers) {
                if (this.plugin.getServer().getOnlinePlayers().length > 0) {
                    this.plugin.config.isBackupDelayed = false;
                    backupRun();
                } else {
                    this.plugin.config.isBackupDelayed = true;
                    this.plugin.log("No players online, backup delayed");
                }
            } else {
                this.plugin.config.isBackupDelayed = false;
                backupRun();
            }
        } catch (Exception ex) {
            this.plugin.logException(ex);
        }
    }
}
