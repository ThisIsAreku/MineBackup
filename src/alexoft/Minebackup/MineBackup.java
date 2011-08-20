
package alexoft.Minebackup;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;


/**
 *
 * @author Alexandre
 */
public class MineBackup extends JavaPlugin {
    private MineBackupCommandListener commandListener;
    public String                           bckDir;
    public String                           bckTempDir;
    private Configuration                   cfg;
    public long                             interval;
    public long                             firstDelay;
    public long                             daystokeep;
    public int                              taskID;
    public List<String>                     worlds;
    public boolean                          isBackupStarted;
    public boolean                          pauseWhenNoPlayers;
    public String							msg_BackupStarted;
    public String							msg_BackupEnded;
    public String							msg_BackupStartedUser;
    public boolean							isBackupDelayed;
    public boolean							compressionEnabled;
    public int								compressionMode;
    public int								compressionLevel;
    public boolean							backupPlugins;
    public boolean							debug;
    
    @Override
    public void onDisable() {
        try {
            this.getServer().getScheduler().cancelTasks(this);
            worlds = null;
            cfg = null;
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
            isBackupStarted = false;
            loadConfig();
            resetSchedule();
            commandListener = new MineBackupCommandListener(this);
            this.getServer().getPluginCommand("mbck").setExecutor(
                    commandListener);
            if (this.pauseWhenNoPlayers) {
                this.getServer().getPluginManager().registerEvent(
                        Event.Type.PLAYER_JOIN,
                        new MineBackupPlayerListener(this), Priority.Monitor,
                        this);
            }
            this.getServer().getLogger().setFilter(new LogFilter());
            log(
                    "sucessfully loaded version "
                            + this.getDescription().getVersion());
        } catch (Exception e) {
            this.logException(e);
        }
    }

    public void resetSchedule() {
        this.getServer().getScheduler().cancelTasks(this);
        if (this.daystokeep != 0) {
            this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                    new BackupsCleaner(this), 0, interval * 2);
        }
        this.taskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(
                this, new Backups(this), firstDelay, interval);
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

    public void loadConfig() {
        boolean rewrite = false;
        String[] allowedKeys = new String[] {
            "worlds", "backup-dir", "backup-temp-dir", "interval", "delay", "days-to-keep",
            "pause-when-no-players", "messages.backup-started",
            "messages.backup-started-user", "messages.backup-ended",
            "compression.enabled", "compression.level", "compression.mode",
            "backup-plugins", "debug"};

        cfg = new Configuration(new File(this.getDataFolder() + "/config.yml"));
        cfg.load();
        worlds = cfg.getStringList("worlds", new ArrayList<String>());
        bckDir = cfg.getString("backup-dir", null);
        bckTempDir = cfg.getString("backup-temp-dir", null);
        interval = cfg.getInt("interval", -1);
        firstDelay = cfg.getInt("delay", -1);
        daystokeep = cfg.getInt("days-to-keep", -1);
        debug = cfg.getBoolean("debug", false);
        String s_pauseWhenNoPlayers = cfg.getString("pause-when-no-players",
                null);

        msg_BackupStarted = cfg.getString("messages.backup-started", null);
        msg_BackupStartedUser = cfg.getString("messages.backup-started-user",
                null);
        msg_BackupEnded = cfg.getString("messages.backup-ended", null);
        
        String s_compressionEnabled = cfg.getString("compression.enabled", null);
        String s_compressionMode = cfg.getString("compression.mode", null);
        String s_compressionLevel = cfg.getString("compression.level", null);

        compressionLevel = Deflater.BEST_COMPRESSION;
        compressionMode = ZipOutputStream.DEFLATED;
    	
        String s_backupPlugins = cfg.getString("backup-plugins", null);
    			
        int i = 0;
        Set<String> cles = cfg.getAll().keySet();
        Iterator<String> it = cles.iterator();

        while (it.hasNext()) {
            String key = it.next();

            if (!Arrays.asList(allowedKeys).contains(key)) {
                cfg.removeProperty(key);
                i++;
            }
        }
        if (i > 0) {
            rewrite = true;
            log("Removed " + i + " unknown key(s)");
        }

        if (s_pauseWhenNoPlayers == "true") {
            pauseWhenNoPlayers = true;
        } else if (s_pauseWhenNoPlayers == "false") {
            pauseWhenNoPlayers = false;
        } else {
            log(Level.WARNING, "Creating 'pause-when-no-players' config...");
            pauseWhenNoPlayers = false;
            cfg.setProperty("pause-when-no-players", pauseWhenNoPlayers);
            rewrite = true;
        }

        if (s_backupPlugins == "true") {
            backupPlugins = true;
        } else if (s_backupPlugins == "false") {
            backupPlugins = false;
        } else {// TODO: backup-plugins
             log(Level.WARNING, "Creating 'backup-plugins' config...");
             backupPlugins = false;
             cfg.setProperty("backup-plugins", backupPlugins);
             rewrite = true;
       }

        if (s_compressionEnabled == "true") {
            compressionEnabled = true;
        } else if (s_compressionEnabled == "false") {
            compressionEnabled = false;
        } else {
            log(Level.WARNING, "Creating 'compression.enabled' config...");
            compressionEnabled = true;
            cfg.setProperty("compression.enabled", compressionEnabled);
            rewrite = true;
        }
        if (compressionEnabled) {
            if (s_compressionLevel == null) {
                log(Level.WARNING, "Creating 'compression.level' config...");
                s_compressionLevel = "BEST_COMPRESSION";
                cfg.setProperty("compression.level", s_compressionLevel);
                rewrite = true;
            } else {
                if (s_compressionLevel == "BEST_COMPRESSION") {
                    compressionLevel = Deflater.BEST_COMPRESSION;
                } else if (s_compressionLevel == "BEST_SPEED") {
                    compressionLevel = Deflater.BEST_SPEED;
                } else if (s_compressionLevel == "NO_COMPRESSION") {
                    compressionLevel = Deflater.NO_COMPRESSION;
                }
            }
        
            if (s_compressionMode == null) {
                log(Level.WARNING, "Creating 'compression.mode' config...");
                s_compressionMode = "DEFLATED";
                cfg.setProperty("compression.mode", s_compressionMode);
                rewrite = true;
            } else {// TODO: compressionMethod
            }
        }
        if (worlds.isEmpty()) {
            log(Level.WARNING, "Creating 'worlds' config...");
            for (World w : this.getServer().getWorlds()) {
                worlds.add(w.getName());
            }
            cfg.setProperty("worlds", worlds);
            rewrite = true;
        }

        if (bckDir == null) {
            log(Level.WARNING, "Creating 'backup-dir' config...");
            bckDir = "minebackup";
            cfg.setProperty("backup-dir", bckDir);
            rewrite = true;
        }

        if (bckTempDir == null) {
            log(Level.WARNING, "Creating 'backup-temp-dir' config...");
            bckTempDir = "minebackup_temp";
            cfg.setProperty("backup-temp-dir", bckTempDir);
            rewrite = true;
        }

        if (interval <= 0) {
            log(Level.WARNING, "Creating 'interval' config...");
            interval = 3600;
            cfg.setProperty("interval", interval);
            rewrite = true;
        }

        if (firstDelay < 0) {
            log(Level.WARNING, "Creating 'delay' config...");
            firstDelay = 10;
            cfg.setProperty("delay", firstDelay);
            rewrite = true;
        }

        if (daystokeep < 0) {
            log(Level.WARNING, "Creating 'days-to-keep' config...");
            daystokeep = 5;
            cfg.setProperty("days-to-keep", firstDelay);
            rewrite = true;
        }
        if (msg_BackupStarted == null) {
            log(Level.WARNING, "Creating 'messages.backup-started' config...");
            msg_BackupStarted = ChatColor.GREEN + "[MineBackup] Backup started";
            cfg.setProperty("messages.backup-started", msg_BackupStarted);
            rewrite = true;
        }

        if (msg_BackupStartedUser == null) {
            log(Level.WARNING,
                    "Creating 'messages.backup-started-user' config...");
            msg_BackupStartedUser = ChatColor.GREEN
                    + "[MineBackup] Backup started by %player%";
            cfg.setProperty("messages.backup-started-user",
                    msg_BackupStartedUser);
            rewrite = true;
        }

        if (msg_BackupEnded == null) {
            log(Level.WARNING, "Creating 'messages.backup-ended' config...");
            msg_BackupEnded = ChatColor.GREEN + "[MineBackup] Backup ended";
            cfg.setProperty("messages.backup-ended", msg_BackupEnded);
            rewrite = true;
        }

        /* if (this.getServer().getWorlds().size() != worlds.size()) {
         for (String n : worlds) {
         if (this.getServer().getWorld(n) == null) {
         this.log(
         "World '" + n
         + "' don't exist, removing from config...");
         worlds.remove(n);
         }
         }
         cfg.setProperty("worlds", worlds);
         rewrite = true;
         }*/
        
        interval *= 20;
        firstDelay *= 20;
        if (rewrite) {            
            String headerText = "# available worlds :\r\n";

            for (World w : this.getServer().getWorlds()) {
                headerText += "# - " + w.getName() + "\r\n";
            }
            cfg.setHeader(headerText);
            cfg.save();
        }
        log(Level.INFO, worlds.size() + " worlds loaded.");
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
        if(debug && !debugText.equals("")) {
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
