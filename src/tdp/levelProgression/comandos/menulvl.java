package tdp.levelProgression.comandos;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tdp.levelProgression.LevelProgression;

public class menulvl implements CommandExecutor{
	
	public menulvl(LevelProgression main) {
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!(arg0 instanceof Player)) {
			return false;
		}
		
		Player p = (Player) arg0;
		
		p.openInventory(LevelProgression.createGui());
		
		return true;
	}
}
