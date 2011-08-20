package alexoft.Minebackup;


import java.io.File;


/**
 *
 * @author Alexandre
 */
public class ZipDir extends Thread {
    public Backups parent;
    public MineBackup plugin;
    public String srcDir;
    public String destDir;
    public int method;
    public int level;
    
    public ZipDir(MineBackup plugin, Backups parent, String source, String dest) {
        this.plugin = plugin;
        this.parent = parent;
        this.destDir = dest;
        this.srcDir = source;
    }
    
    @Override
    public void run() {

        try {
            new File(this.destDir).createNewFile();
            alexoft.Minebackup.ZipUtils.zipDir(this.srcDir, this.destDir,
                    this.plugin.compressionMode, this.plugin.compressionLevel);
            alexoft.Minebackup.DirUtils.deleteDirectory(new File(this.srcDir));
        } catch (Exception ex) {
            this.plugin.logException(ex,this.destDir);
            new File(this.destDir).delete();
        }
    }
    
}
