package alexoft.Minebackup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;

public class Config {
	private MineBackup						plugin;
    private Configuration                   cfg;
    public boolean							isBackupDelayed;
    
    public Config(MineBackup plugin){
    	this.plugin = plugin;
        loadConfig();
    }
    /* configuration fields */
    public List<String>                     worlds;

    public String                           bckDir;
    public String                           bckTempDir;
    public String                           bckFormat;
    
    public long                             interval;
    public long                             firstDelay;
    public long                             daystokeep;
    
    public boolean                          pauseWhenNoPlayers;
    public boolean							backupPlugins;
    public boolean							debug;
    
    public boolean							msg_enable;
    public String							msg_BackupStarted;
    public String							msg_BackupEnded;
    public String							msg_BackupStartedUser;
    
    public boolean							compressionEnabled;
    public int								compressionMode;
    public int								compressionLevel;
    /* end configuration fields */
    
    public void loadConfig() {
    	try{
        	this.plugin.log("Loading configuration...");
	        boolean rewrite = false;
	        String[] allowedKeys = new String[] {
	            "worlds",
	            
	            "backup.dir", "backup.temp-dir", "backup.format",
	            
	            "time.interval", "time.delay", "time.days-to-keep",
	            
	            "options.pause-when-no-players", "options.backup-plugins", "options.debug",
	            
	            "messages.backup-started", "messages.backup-started-user", "messages.backup-ended", "messages.enabled",
	            
	            "compression.enabled", "compression.level", "compression.mode"};
	
	        cfg = new Configuration(new File(this.plugin.getDataFolder() + "/config.yml"));
	        cfg.load();
	        
	        worlds = cfg.getStringList("worlds", new ArrayList<String>());
	        
	        bckDir = cfg.getString("backup.dir", "minebackup");
	        bckTempDir = cfg.getString("backup.temp-dir", "minebackup_temp");
	        bckFormat = cfg.getString("backup.format", "%W/%Y-%M-%D_%H-%m-%S");
	        
	        interval = cfg.getInt("time.interval", -1);
	        firstDelay = cfg.getInt("time.delay", -1);
	        daystokeep = cfg.getInt("time.days-to-keep", -1);
	        
	        debug = cfg.getBoolean("options.debug", false);	        
	        pauseWhenNoPlayers = cfg.getBoolean("options.pause-when-no-players", true);
	        backupPlugins = cfg.getBoolean("options.backup-plugins", true);

	        msg_enable = cfg.getBoolean("messages.enabled", true);
	        msg_BackupEnded = cfg.getString("messages.backup-ended", ChatColor.GREEN + "[MineBackup] Backup ended");
	        msg_BackupStarted = cfg.getString("messages.backup-started", ChatColor.GREEN + "[MineBackup] Backup started");
	        msg_BackupStartedUser = cfg.getString("messages.backup-started-user",
	        		ChatColor.GREEN + "[MineBackup] Backup started by %player%");
	        
	        compressionEnabled = cfg.getBoolean("compression.enabled", true);
	        String s_compressionMode = cfg.getString("compression.mode", null);
	        String s_compressionLevel = cfg.getString("compression.level", null);
	
	        compressionLevel = Deflater.BEST_COMPRESSION;
	        compressionMode = ZipOutputStream.DEFLATED;
	    	
	    			
	        int i = 0;
	        String key;
	    	for(Entry<String, Object> entry : cfg.getAll().entrySet()){
	    		key = entry.getKey();
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
	                s_compressionMode = "DEFLATED";
	                cfg.setProperty("compression.mode", s_compressionMode);
	                rewrite = true;
	            } else {// TODO: compressionMethod
	            }
	        }
	        if (worlds.isEmpty()) {
	            for (World w : this.plugin.getServer().getWorlds()) {
	                worlds.add(w.getName());
	            }
	            cfg.setProperty("worlds", worlds);
	            rewrite = true;
	        }
	
	        if (interval <= 0) {
	            interval = 3600;
	            cfg.setProperty("time.interval", interval);
	            rewrite = true;
	        }
	
	        if (firstDelay < 0) {
	            firstDelay = 10;
	            cfg.setProperty("time.delay", firstDelay);
	            rewrite = true;
	        }
	
	        if (daystokeep < 0) {
	            daystokeep = 5;
	            cfg.setProperty("time.days-to-keep", firstDelay);
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
