package tdp.levelProgression;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

public class GeneralBoard {

		private static Map<UUID, Integer> TASKS = new HashMap<UUID, Integer>();
		private final UUID uuid;
		
		public GeneralBoard(UUID uuid) {
			this.uuid=uuid;
		}
	
		public void setID (int id) {
			TASKS.put(uuid, id);
		}
		
		public int getID() {
			return TASKS.get(uuid);
		}
		
		public boolean hasID() {
			if (TASKS.containsKey(uuid)) return true;
			else return false;
		}
		
		public void stop() {
			Bukkit.getScheduler().cancelTask(TASKS.get(uuid));
			TASKS.remove(uuid);
		}
		
}
