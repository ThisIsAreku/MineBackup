package alexoft.Minebackup;


import java.io.File;
import java.util.logging.Level;


/**
 *
 * @author Alexandre
 */
public class CopyDir extends Thread {
    public Backups parent;
    public MineBackup plugin;
    public String srcDir;
    public String destDir;
    public int method;
    public int level;
    
    public CopyDir(MineBackup plugin, Backups parent, String source, String dest) {
        this.plugin = plugin;
        this.parent = parent;
        this.destDir = dest;
        this.srcDir = source;
    }
    
    @Override
    public void run() {

        try {
            new File(this.destDir).mkdirs();
            alexoft.Minebackup.DirUtils.copyDirectory(this.srcDir, this.destDir);
            alexoft.Minebackup.DirUtils.deleteDirectory(new File(this.srcDir));
           } catch (Exception ex) {
            this.plugin.log(Level.WARNING, "error; " + ex);
            new File(this.destDir).delete();
        }
    }
    
}
