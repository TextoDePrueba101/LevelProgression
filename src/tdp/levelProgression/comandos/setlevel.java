package tdp.levelProgression.comandos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import tdp.levelProgression.LevelProgression;

public class setlevel implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang;
	
	public setlevel(LevelProgression main) {
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED +lang.getString("CommandDenied"));
			return false;
		}
		if (args.length<3) return false;
		
		if (args[1].equals("ALL")) {
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"ADVENTURER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"WIZARD",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"SOLDIER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"ARCHER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"SHIELDMAN",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"TRIDENTMAN",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"MINER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"LUMBERJACK",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"FOODLVL",Integer.parseInt(args[2]));
			
			arg0.sendMessage(ChatColor.ITALIC + lang.getString("setlvl1") + args[1]+lang.getString("setlvl2")+ args[0] +lang.getString("setlvl3")+ args[2]);
		}
		else {
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),args[1],Integer.parseInt(args[2]));
			arg0.sendMessage(ChatColor.ITALIC + lang.getString("setlvl1") + args[1]+lang.getString("setlvl2")+ args[0] +lang.getString("setlvl3")+ LevelProgression.pgetData(Bukkit.getPlayer(args[0]), args[1]));
		}

		return true;
	}
}
