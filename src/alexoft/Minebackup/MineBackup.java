
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package alexoft.Minebackup;

//~--- non-JDK imports --------------------------------------------------------

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Alexandre
 */
public class MineBackup extends JavaPlugin {
    private MineBackupCommandListener commandListener;
    public String                           bckDir;
    private Configuration                   cfg;
    public long                             interval;
    public long                             firstDelay;
    public long                             daystokeep;
    public int                              taskID;
    public List<String>                     worlds;
    public boolean                          isBackupStarted;

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        worlds = null;
        cfg    = null;
        log(Level.INFO, "version " + this.getDescription().getVersion() + " disabled");
    }

    @Override
    public void onEnable() {
        isBackupStarted = false;
        loadConfig();
        resetSchedule();
        commandListener  = new MineBackupCommandListener(this);
        this.getServer().getPluginCommand("mbck").setExecutor(commandListener);
        this.getServer().getLogger().setFilter(new LogFilter());
        log(Level.INFO, "version " + this.getDescription().getVersion() + " ready");
    }

    public void resetSchedule() {
        this.getServer().getScheduler().cancelTasks(this);
        if (this.daystokeep != 0) this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new BackupsCleaner(this),0,interval*2);
        this.taskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Backups(this), firstDelay, interval);
    }

    public void executeSchedule(String playerName) {
        Backups manualBackup;
        if ((playerName != null) && (!"".equals(playerName))) {
            manualBackup  = new Backups(this,true,playerName);
        } else {
            manualBackup  = new Backups(this);
        }

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, manualBackup);
    }

    public void loadConfig() {
        cfg = new Configuration(new File(this.getDataFolder() + "/config.yml"));
        cfg.load();
        worlds   = cfg.getStringList("worlds", new ArrayList<String>());
        bckDir   = cfg.getString("backup-dir", null);
        interval = cfg.getInt("tick", -1);
        firstDelay = cfg.getInt("delay", -1);
        daystokeep = cfg.getInt("days-to-keep", -1);

        if (worlds.isEmpty()) {
            log(Level.WARNING, "Creating 'worlds' config...");
            for (World w : this.getServer().getWorlds()) {
                worlds.add(w.getName());
            }
            cfg.setProperty("worlds", worlds);
        }

        if (bckDir == null) {
            log(Level.WARNING, "Creating 'backup-dir' config...");
            bckDir = "minebackup";
            cfg.setProperty("backup-dir", bckDir);
        }

        if (interval <= 0) {
            log(Level.WARNING, "Creating 'tick' config...");
            interval = 3600;
            cfg.setProperty("tick", interval);
        }

        if (firstDelay < 0) {
            log(Level.WARNING, "Creating 'delay' config...");
            firstDelay = 10;
            cfg.setProperty("delay", firstDelay);
        }

        if (daystokeep < 0) {
            log(Level.WARNING, "Creating 'days-to-keep' config...");
            daystokeep = 5;
            cfg.setProperty("days-to-keep", firstDelay);
        }

        interval *= 20;
        firstDelay *= 20;
        cfg.save();
        log(Level.INFO, worlds.size() + " worlds loaded.");
    }

    public void log(Level level, String msg) {
        this.getServer().getLogger().log(level, "[" + this.getDescription().getName() + "] " + msg);
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
                return !record.getMessage().equals(LS1) & !record.getMessage().equals(LS2)
                       & !record.getMessage().equals(LS3) & !record.getMessage().equals(LS4)
                       & !record.getMessage().equals(LS5) & !record.getMessage().equals(LS6)
                       & !record.getMessage().equals(LS7) & !record.getMessage().equals(LS8);
            } else {
                return true;
            }
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
