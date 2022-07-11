package tdp.levelProgression.comandos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import tdp.levelProgression.LevelProgression;

public class startprogresion implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang;
	
	public startprogresion(LevelProgression main) {
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED + lang.getString("CommandDenied"));
			return false;
		}
		if (args.length!=1 || !(args[0] instanceof String)) return false;
		if (Bukkit.getPlayer(args[0]) ==null) {
			arg0.sendMessage(ChatColor.RED + "Couldn't find the player");
			return true;
		}
		
		Player p = Bukkit.getPlayer(args[0]);
		
		p.sendMessage(ChatColor.ITALIC +lang.getString("startProg1")+args[0]+lang.getString("startProg2"));
		LevelProgression.setAbilities(p);
		
		return true;
	}
}
