package tdp.levelProgression.comandos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import tdp.levelProgression.LevelProgression;

public class getlevel implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang; 
	
	public getlevel(LevelProgression main) {
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED + lang.getString("CommandDenied"));
			return false;
		}
		
		if (args.length!=1 || args[0] == null || !(args[0] instanceof String)) return false;
		if (Bukkit.getPlayer(args[0]) ==null) {
			arg0.sendMessage(ChatColor.RED + "Couldn't find the player");
			return true;
		}

			Player p = Bukkit.getServer().getPlayer(args[0]);
			
			arg0.sendMessage(""+ChatColor.BOLD + ChatColor.YELLOW + "Levels of "+ args[0] + ":");
			
			arg0.sendMessage("ADVENTURER " + ChatColor.BOLD + LevelProgression.pgetData(p,"ADVENTURER"));
			arg0.sendMessage("WIZARD " + ChatColor.BOLD + LevelProgression.pgetData(p,"WIZARD"));
			arg0.sendMessage("SOLDIER " + ChatColor.BOLD + LevelProgression.pgetData(p,"SOLDIER"));
			arg0.sendMessage("ARCHER " + ChatColor.BOLD + LevelProgression.pgetData(p,"ARCHER"));
			arg0.sendMessage("SHIELDMAN " + ChatColor.BOLD + LevelProgression.pgetData(p,"SHIELDMAN"));
			arg0.sendMessage("TRIDENTMAN " + ChatColor.BOLD + LevelProgression.pgetData(p,"TRIDENTMAN"));
			arg0.sendMessage("MINER " + ChatColor.BOLD + LevelProgression.pgetData(p,"MINER"));
			arg0.sendMessage("LUMBERJACK " + ChatColor.BOLD + LevelProgression.pgetData(p,"LUMBERJACK"));
			arg0.sendMessage("FOODLVL " + ChatColor.BOLD + LevelProgression.pgetData(p,"FOODLVL"));
			arg0.sendMessage("XP " + ChatColor.BOLD + LevelProgression.pgetData(p,"XP"));
			arg0.sendMessage("ABILITYPOINTS " + ChatColor.BOLD + LevelProgression.pgetData(p,"ABILITYPOINTS"));
			arg0.sendMessage("LEVEL " + ChatColor.BOLD + LevelProgression.pgetData(p,"LEVEL"));
			arg0.sendMessage("CC1 " + ChatColor.BOLD + LevelProgression.pgetData(p,"CC1"));

		
		
		return true;
	}
}
