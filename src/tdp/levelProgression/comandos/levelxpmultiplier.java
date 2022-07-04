package tdp.levelProgression.comandos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import tdp.levelProgression.LevelProgression;

public class levelxpmultiplier implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang;
	
	public levelxpmultiplier(LevelProgression main) {
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED + lang.getString("CommandDenied"));
			return false;
		}
		
		if (args.length>0 && args[0] != null) {
			Bukkit.getServer().getPluginManager().getPlugin("LevelProgression").getConfig().set("xPmultiplier", Integer.parseInt(args[0]));
			LevelProgression.xPmultiplier=Integer.parseInt(args[0]);
		}
		
		arg0.sendMessage(ChatColor.ITALIC + lang.getString("xpMultiplierCommand") + LevelProgression.xPmultiplier);
		return true;
	}
}
