package tdp.levelProgression.comandos;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import tdp.levelProgression.LevelProgression;

public class language implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang; 
	public language(LevelProgression main) {
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED + lang.getString("CommandDenied"));
			return false;
		}
		
		arg0.sendMessage(ChatColor.RED + "Language changes are now done from the config.yml in the folder of this plugin. Change 'language: ' to the desired language");
		return true;
	}
}
