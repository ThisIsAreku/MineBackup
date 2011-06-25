/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alexoft.Minebackup;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
/**
 *
 * @author Alexandre
 */
public class MineBackup extends JavaPlugin {
    private Backups bck;
    private Configuration cfg;
    public List<String> worlds;
    public String bckDir;
    public long interval;
    public int taskID;

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        worlds = null;
        cfg = null;
        bck = null;
        log(Level.INFO,"version " + this.getDescription().getVersion() + " disabled");        
    }

    @Override
    public void onEnable() {
        bck = new Backups(this);
        loadConfig();
        resetSchedule();
        log(Level.INFO,"version " + this.getDescription().getVersion() + " ready");
            
    }
    public void resetSchedule() {
        this.getServer().getScheduler().cancelTasks(this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, bck, interval, interval);
    }
    public void loadConfig() {
        cfg = new Configuration(new File(this.getDataFolder() + "/config.yml"));
        cfg.load();
        worlds = cfg.getStringList("worlds", new ArrayList<String>());
        bckDir = cfg.getString("backup-dir", null);
        interval = cfg.getInt("tick", -1);
        if(worlds.isEmpty()){
            log(Level.INFO,"Creating 'worlds' config...");
            for(World w : this.getServer().getWorlds())
                    worlds.add(w.getName());
            cfg.setProperty("worlds", worlds);
        }
        if(bckDir == null){
            log(Level.INFO,"Creating 'backup-dir' config...");
            bckDir = "minebackup";
            cfg.setProperty("backup-dir", bckDir);
        }
        if(interval == -1){
            log(Level.INFO,"Creating 'tick' config...");
            interval = 3600;
            cfg.setProperty("tick", interval);
        }
        interval *= 1000;
        cfg.save();
        log(Level.INFO,worlds.size() +" worlds loaded.");
    }
    
    public void log(Level level, String msg) {
        this.getServer().getLogger().log(level, "[" + this.getDescription().getName() + "] " + msg);
    }
    
}
