package alexoft.Minebackup;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;

import org.bukkit.ChatColor;
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
            File tempDir = new File(String.valueOf(Math.random()));

            tempDir.mkdirs();
            for (String w:plugin.worlds) {
                plugin.log(Level.INFO, " * " + w);
                copyWorld(plugin.getServer().getWorld(w), tempDir);
            }
            ZipDir(tempDir);
        } catch (Exception ex) {
            this.plugin.log(Level.WARNING, "error; " + ex);            
        }
    }

    public void MakeBackup(World world) {
        try {
        	if(world == null) {
                this.plugin.log(
                        "world not found..");    
                return;
        	}
            this.plugin.log(Level.INFO,
                    "Backing up '" + world.getName() + "'...");
            File tempDir = new File(String.valueOf(Math.random()));

            tempDir.mkdirs();
            copyWorld(world, tempDir);
            ZipDir(tempDir);
        } catch (Exception ex) {
            plugin.log(Level.WARNING, "error; " + ex);            
        }
    }
    
    public void ZipDir(File tempDir) {
        this.plugin.log(Level.INFO, "Compressing...");
        Calendar today = Calendar.getInstance();
        String currentDirName = format(today.get(Calendar.DAY_OF_MONTH)) + "."
                + format(today.get(Calendar.MONTH) + 1) + "."
                + today.get(Calendar.YEAR);
        String currentFileName = format(today.get(Calendar.HOUR_OF_DAY)) + "_"
                + format(today.get(Calendar.MINUTE)) + "_"
                + format(today.get(Calendar.SECOND));

        new File(this.plugin.bckDir + "/" + currentDirName).mkdirs();
        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(
                this.plugin,
                new ZipDir(this.plugin, this, tempDir.getPath(),
                this.plugin.bckDir + "/" + currentDirName + "/"
                + currentFileName + ".zip"));
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

    public void afterZip() {
        plugin.log(Level.INFO, "Done !");
        this.plugin.getServer().dispatchCommand(
                new ConsoleCommandSender(this.plugin.getServer()), "save-on");
        this.plugin.getServer().broadcastMessage(
                ChatColor.GREEN + "[" + this.plugin.getDescription().getName()
                + "] Backup ended");
        this.plugin.isBackupStarted = false;
    }

    @Override
    public void run() {
        try {
            this.plugin.isBackupStarted = true;
            if (this.userStarted) {
                this.plugin.getServer().broadcastMessage(
                        ChatColor.GREEN + "["
                        + this.plugin.getDescription().getName()
                        + "] Backup started by " + this.userName);
            } else {
                this.plugin.getServer().broadcastMessage(
                        ChatColor.GREEN + "["
                        + this.plugin.getDescription().getName()
                        + "] Backup started");
            }
            this.plugin.getServer().dispatchCommand(
                    new ConsoleCommandSender(this.plugin.getServer()),
                    "save-off");
            this.plugin.getServer().dispatchCommand(
                    new ConsoleCommandSender(this.plugin.getServer()),
                    "save-all");
            this.MakeBackup();
        } catch (Exception ex) {
            this.plugin.log(Level.SEVERE, ex.getMessage());
        }
    }
}
