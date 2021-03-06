/**
 * 
 */
package alexoft.Minebackup;


import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;


/**
 * @author Alexandre
 *
 */
public class MineBackupPlayerListener extends PlayerListener {
    private MineBackup plugin;

    public MineBackupPlayerListener(MineBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.plugin.config.pauseWhenNoPlayers) {
            if (this.plugin.config.isBackupDelayed) {
                if (!this.plugin.isBackupStarted) {
                    this.plugin.log("Performing delayed backup");
                    this.plugin.executeSchedule(null);
                    this.plugin.config.isBackupDelayed = false;
                }
            }
        }

    }

}
