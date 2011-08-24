
package alexoft.Minebackup;


import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author Alexandre
 */
public class MineBackup extends JavaPlugin {
    public Config							config;
    
    public boolean                          isBackupStarted;
    public int                              taskID;
    
    @Override
    public void onDisable() {
        try {
            this.getServer().getScheduler().cancelTasks(this);
            log("version " + this.getDescription().getVersion() + " disabled");
        } catch (Exception e) {
            this.logException(e);
        }
    }

    @Override
    public void onEnable() {
        try {
            log("ThisIsAreku present MINEBACKUP, v"
                            + this.getDescription().getVersion());
            config = new Config(this);
            isBackupStarted = false;
            
            resetSchedule();
            
            this.getServer().getPluginCommand("mbck").setExecutor(new MineBackupCommandListener(this));
           
            if (this.config.pauseWhenNoPlayers)
                this.getServer().getPluginManager().registerEvent(
                		Event.Type.PLAYER_JOIN,
                		new MineBackupPlayerListener(this),
                		Priority.Monitor,
                		this);
            
            //Remove save-off, save-on, .. messages from console
            this.getServer().getLogger().setFilter(new LogFilter());
            
            log("sucessfully loaded version " + this.getDescription().getVersion());
        } catch (Exception e) {
            this.logException(e);
        }
    }

    public void resetSchedule() {
        this.getServer().getScheduler().cancelTasks(this);
        if (this.config.daystokeep != 0) {
            this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                    new BackupsCleaner(this), 0, this.config.interval * 2);
        }
        this.taskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(
                this, new Backups(this), this.config.firstDelay, this.config.interval);
    }

    public void executeSchedule(String playerName) {
        Backups manualBackup;

        if ((playerName != null) && (!"".equals(playerName))) {
            manualBackup = new Backups(this, true, playerName);
        } else {
            manualBackup = new Backups(this);
        }

        this.getServer().getScheduler().scheduleSyncDelayedTask(this,
                manualBackup);
    }

    

    public void log(Level level, String l) {
        this.getServer().getLogger().log(level, "[MineBackup] " + l);
    }

    public void log(String l) {
        log(Level.INFO, l);
    }
    
    public void logException(Throwable e, String debugText) {
        log(Level.SEVERE, "---------------------------------------");
        log(Level.SEVERE, "--- an unexpected error has occured ---");
        log(Level.SEVERE, "-- please send line below to the dev --");
        log(Level.SEVERE, e.toString() + " : " + e.getLocalizedMessage());
        for (StackTraceElement t:e.getStackTrace()) { 
            log(Level.SEVERE, "\t" + t.toString());
        }
        if(this.config.debug && !debugText.equals("")) {
            log(Level.SEVERE, "--------- DEBUG ---------");
            log(Level.SEVERE, debugText);

        }
        log(Level.SEVERE, "---------------------------------------");
    }
    
    public void logException(Throwable e) {
    	logException(e,"");
    }

    public class LogFilter implements Filter {
        String LS1 = "Disabling level saving..";
        String LS2 = "ConsoleCommandSender: Disabling level saving..";
        String LS3 = "Enabling level saving..";
        String LS4 = "ConsoleCommandSender: Enabling level saving..";
        String LS5 = "Forcing save..";
        String LS6 = "ConsoleCommandSender: Forcing save..";
        String LS7 = "Save complete.";
        String LS8 = "ConsoleCommandSender: Save complete.";

        @Override
        public boolean isLoggable(LogRecord record) {
            if (isBackupStarted) {
                return !record.getMessage().equals(LS1)
                        & !record.getMessage().equals(LS2)
                        & !record.getMessage().equals(LS3)
                        & !record.getMessage().equals(LS4)
                        & !record.getMessage().equals(LS5)
                        & !record.getMessage().equals(LS6)
                        & !record.getMessage().equals(LS7)
                        & !record.getMessage().equals(LS8);
            } else {
                return true;
            }
        }
    }
}
