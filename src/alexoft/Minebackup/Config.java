package alexoft.Minebackup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;

public class Config {
	private MineBackup						plugin;
    private Configuration                   cfg;
    public Config(MineBackup plugin){
    	this.plugin = plugin;
        loadConfig();
    }

    public String                           bckDir;
    public String                           bckTempDir;
    public long                             interval;
    public long                             firstDelay;
    public long                             daystokeep;
    public List<String>                     worlds;
    public boolean                          pauseWhenNoPlayers;
    public boolean							msg_enable;
    public String							msg_BackupStarted;
    public String							msg_BackupEnded;
    public String							msg_BackupStartedUser;
    public boolean							isBackupDelayed;
    public boolean							compressionEnabled;
    public int								compressionMode;
    public int								compressionLevel;
    public boolean							backupPlugins;
    public boolean							debug;
    
    public void loadConfig() {
    	try{
        	this.plugin.log("Loading configuration...");
	        boolean rewrite = false;
	        String[] allowedKeys = new String[] {
	            "worlds", "backup-dir", "backup-temp-dir", "interval", "delay", "days-to-keep",
	            "pause-when-no-players", "messages.backup-started",
	            "messages.backup-started-user", "messages.backup-ended",
	            "messages.enabled", "compression.enabled", "compression.level",
	            "compression.mode", "backup-plugins", "debug"};
	
	        cfg = new Configuration(new File(this.plugin.getDataFolder() + "/config.yml"));
	        cfg.load();
	        worlds = cfg.getStringList("worlds", new ArrayList<String>());
	        bckDir = cfg.getString("backup-dir", null);
	        bckTempDir = cfg.getString("backup-temp-dir", null);
	        interval = cfg.getInt("interval", -1);
	        firstDelay = cfg.getInt("delay", -1);
	        daystokeep = cfg.getInt("days-to-keep", -1);
	        debug = cfg.getBoolean("debug", false);
	        pauseWhenNoPlayers = cfg.getBoolean("pause-when-no-players", true);

	        msg_enable = cfg.getBoolean("messages.enabled", true);
	        msg_BackupEnded = cfg.getString("messages.backup-ended", null);
	        msg_BackupStarted = cfg.getString("messages.backup-started", null);
	        msg_BackupStartedUser = cfg.getString("messages.backup-started-user",
	                null);
	        
	        compressionEnabled = cfg.getBoolean("compression.enabled", true);
	        String s_compressionMode = cfg.getString("compression.mode", null);
	        String s_compressionLevel = cfg.getString("compression.level", null);
	
	        compressionLevel = Deflater.BEST_COMPRESSION;
	        compressionMode = ZipOutputStream.DEFLATED;
	    	
	        backupPlugins = cfg.getBoolean("backup-plugins", true);
	    			
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
	            this.plugin.log("Removed " + i + " unknown key(s)");
	        }
	        
	        if (compressionEnabled) {
	            if (s_compressionLevel == null) {
	            	this.plugin.log(Level.WARNING, "Creating 'compression.level' config...");
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
	            	this.plugin.log(Level.WARNING, "Creating 'compression.mode' config...");
	                s_compressionMode = "DEFLATED";
	                cfg.setProperty("compression.mode", s_compressionMode);
	                rewrite = true;
	            } else {// TODO: compressionMethod
	            }
	        }
	        if (worlds.isEmpty()) {
	        	this.plugin.log(Level.WARNING, "Creating 'worlds' config...");
	            for (World w : this.plugin.getServer().getWorlds()) {
	                worlds.add(w.getName());
	            }
	            cfg.setProperty("worlds", worlds);
	            rewrite = true;
	        }
	
	        if (bckDir == null) {
	        	this.plugin.log(Level.WARNING, "Creating 'backup-dir' config...");
	            bckDir = "minebackup";
	            cfg.setProperty("backup-dir", bckDir);
	            rewrite = true;
	        }
	
	        if (bckTempDir == null) {
	        	this.plugin.log(Level.WARNING, "Creating 'backup-temp-dir' config...");
	            bckTempDir = "minebackup_temp";
	            cfg.setProperty("backup-temp-dir", bckTempDir);
	            rewrite = true;
	        }
	
	        if (interval <= 0) {
	        	this.plugin.log(Level.WARNING, "Creating 'interval' config...");
	            interval = 3600;
	            cfg.setProperty("interval", interval);
	            rewrite = true;
	        }
	
	        if (firstDelay < 0) {
	        	this.plugin.log(Level.WARNING, "Creating 'delay' config...");
	            firstDelay = 10;
	            cfg.setProperty("delay", firstDelay);
	            rewrite = true;
	        }
	
	        if (daystokeep < 0) {
	        	this.plugin.log(Level.WARNING, "Creating 'days-to-keep' config...");
	            daystokeep = 5;
	            cfg.setProperty("days-to-keep", firstDelay);
	            rewrite = true;
	        }
	        if (msg_BackupStarted == null) {
	        	this.plugin.log(Level.WARNING, "Creating 'messages.backup-started' config...");
	            msg_BackupStarted = ChatColor.GREEN + "[MineBackup] Backup started";
	            cfg.setProperty("messages.backup-started", msg_BackupStarted);
	            rewrite = true;
	        }
	
	        if (msg_BackupStartedUser == null) {
	        	this.plugin.log(Level.WARNING,
	                    "Creating 'messages.backup-started-user' config...");
	            msg_BackupStartedUser = ChatColor.GREEN
	                    + "[MineBackup] Backup started by %player%";
	            cfg.setProperty("messages.backup-started-user",
	                    msg_BackupStartedUser);
	            rewrite = true;
	        }
	
	        if (msg_BackupEnded == null) {
	        	this.plugin.log(Level.WARNING, "Creating 'messages.backup-ended' config...");
	            msg_BackupEnded = ChatColor.GREEN + "[MineBackup] Backup ended";
	            cfg.setProperty("messages.backup-ended", msg_BackupEnded);
	            rewrite = true;
	        }
	        
	        interval *= 20;
	        firstDelay *= 20;
	        if (rewrite) {            
	            String headerText = "# available worlds :\r\n";
	
	            for (World w : this.plugin.getServer().getWorlds()) {
	                headerText += "# - " + w.getName() + "\r\n";
	            }
	            cfg.setHeader(headerText);
	            cfg.save();
	        }
	        this.plugin.log(Level.INFO, worlds.size() + " worlds loaded.");
    	}catch(Exception e){
    		this.plugin.logException(e,"Error while loading config");
    	}
    }
}
