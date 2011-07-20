package alexoft.Minebackup;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 *
 * @author Alexandre
 */
public class MineBackupCommandListener implements CommandExecutor {
    private MineBackup plugin;
    public MineBackupCommandListener(MineBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (!(cs instanceof Player)) {
            return false;
        }
        Player player = (Player) cs;

        if (!player.isOp()) {
            return false;
        }
        this.plugin.executeSchedule(player.getName());
        return true;
    }
    
}
