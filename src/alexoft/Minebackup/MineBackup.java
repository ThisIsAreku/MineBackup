
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
    private final MineBackupCommandListener commandListener = new MineBackupCommandListener(this);
    private final Backups                   bck             = new Backups(this);
    public String                           bckDir;
    private Configuration                   cfg;
    public long                             interval;
    public int                              taskID;
    public List<String>                     worlds;

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        worlds = null;
        cfg    = null;
        log(Level.INFO, "version " + this.getDescription().getVersion() + " disabled");
    }

    @Override
    public void onEnable() {
        loadConfig();
        resetSchedule();
        this.getServer().getPluginCommand("mbck").setExecutor(commandListener);
        this.getServer().getLogger().setFilter(new LogFilter());
        log(Level.INFO, "version " + this.getDescription().getVersion() + " ready");
    }

    public void resetSchedule() {
        this.getServer().getScheduler().cancelTasks(this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, bck, 5000, interval);
    }

    public void executeSchedule(String playerName) {
        if ((playerName != null) && (!"".equals(playerName))) {
            bck.userName    = playerName;
            bck.userStarted = true;
        } else {
            bck.userName    = "";
            bck.userStarted = false;
        }

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, bck);
    }

    public void loadConfig() {
        cfg = new Configuration(new File(this.getDataFolder() + "/config.yml"));
        cfg.load();
        worlds   = cfg.getStringList("worlds", new ArrayList<String>());
        bckDir   = cfg.getString("backup-dir", null);
        interval = cfg.getInt("tick", -1);

        if (worlds.isEmpty()) {
            log(Level.INFO, "Creating 'worlds' config...");

            for (World w : this.getServer().getWorlds()) {
                worlds.add(w.getName());
            }

            cfg.setProperty("worlds", worlds);
        }

        if (bckDir == null) {
            log(Level.INFO, "Creating 'backup-dir' config...");
            bckDir = "minebackup";
            cfg.setProperty("backup-dir", bckDir);
        }

        if (interval == -1) {
            log(Level.INFO, "Creating 'tick' config...");
            interval = 3600;
            cfg.setProperty("tick", interval);
        }

        interval *= 1000;
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
            if (bck.isBackupStarted()) {
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
