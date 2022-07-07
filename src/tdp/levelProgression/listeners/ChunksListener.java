package tdp.levelProgression.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class ChunksListener implements Listener{
	private LevelProgression plugin;
//	private int ChunkTestDelay; //default 200
//	private int ChunkProbability; //default 15
	private List<Integer> ChunkTaskID= new ArrayList<Integer>();
	private FileConfiguration lang = LevelProgression.lang;

	public ChunksListener(LevelProgression plugin) {
		this.plugin = plugin; 
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
		
	@EventHandler
	public void onEnable(PluginEnableEvent e) {
		for (World w : Bukkit.getWorlds()) {
		
			int ChunkTestDelay = LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(w.getName())).getChunkTestDelay();
			int ChunkProbability = LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(w.getName())).getChunkProbability();	
			
			if (ChunkProbability>0) runChunkDetection(ChunkTestDelay,ChunkProbability,w); //If delay is -1, the wild chunks mechanic will not work
		}
	}
	
	@EventHandler
	public void chunkLoadWildness(ChunkLoadEvent e) {
		if (!LevelProgression.testPluginActive(e.getWorld())) return;
		
		int ChunkProbability = LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(e.getWorld().getName())).getChunkProbability();	

		if (ChunkProbability==0 && !e.getChunk().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"),PersistentDataType.INTEGER)) {
			e.getChunk().getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER,0);
		}
	}
	
	@EventHandler
	public void onDisable(PluginDisableEvent e) {
		for (int id : ChunkTaskID) Bukkit.getScheduler().cancelTask(id);
	}
			
	@EventHandler
	public void pullOutMobs(PlayerItemConsumeEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (!e.getItem().getItemMeta().getLocalizedName().equals("WILD_FOOD")) return;
		
		Player p = e.getPlayer();
		
		//============
		int ChunkProbability = LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(p.getWorld().getName())).getChunkProbability();	
		if (ChunkProbability==0 && !p.getLocation().getChunk().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"),PersistentDataType.INTEGER)) {
			p.getLocation().getChunk().getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER,0);
		}
		//============
		
		if (!p.getLocation().getChunk().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER)) {
			
			spawnWildEnemies(p);
			p.getLocation().getChunk().getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER,0);
			p.sendMessage(ChatColor.DARK_PURPLE + lang.getString("chunks1"));
			p.playSound(p.getLocation(), Sound.BLOCK_BELL_RESONATE, 5.0F, 1.0F);
		}
		else {
			p.sendMessage(ChatColor.GREEN + lang.getString("chunks2"));
		}
	}
		
	public void runChunkDetection(int delay,int probability,World w) {
		if (delay==(-1)) return;
		
		BukkitRunnable r = new BukkitRunnable(){
			   
		    @Override
		     public void run(){
		       
		        Collection<? extends Player> players = w.getPlayers();
		        
		    	if (!players.isEmpty()) for (Player p : players) {
		    		if ((Math.random() * 100 + 1) <= probability
		    		&& !p.getLocation().getChunk().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER)) { //El segundo n�mero es la probabilidad en porcentaje
		    			spawnWildEnemies(p);
		    			p.getLocation().getChunk().getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER,0);
		    			p.sendMessage(ChatColor.DARK_PURPLE + lang.getString("chunks1"));
		    			p.playSound(p.getLocation(), Sound.BLOCK_BELL_RESONATE, 5.0F, 1.0F);
		    		}
		    		else if (!p.getLocation().getChunk().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isNotWild"), PersistentDataType.INTEGER)) {
		    			p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 3.0F, 1.0F);
	    				p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + lang.getString("chunks3"));
	    			}
		    		
		    	}
		     }
		};
		
		r.runTaskTimer(plugin, 20 * delay, 20 * delay);
		
		ChunkTaskID.add(r.getTaskId());		
	}
	
	public void spawnWildEnemies(Player player) {
		Entity[] entities = player.getLocation().getChunk().getEntities();
		Location loc = player.getLocation();
		Biome bi = loc.getBlock().getBiome();
		double t = player.getWorld().getTemperature(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		
		for (Entity ent : entities) {
			if (ent instanceof Player) {
				Player p = (Player) ent;
				loc = p.getLocation();
				int lvl = LevelProgression.pgetData(p, "LEVEL");
				int r = (int) (Math.random()*100+1);
				
				if (bi == Biome.COLD_OCEAN || bi == Biome.DEEP_COLD_OCEAN || bi == Biome.DEEP_FROZEN_OCEAN || bi == Biome.LUKEWARM_OCEAN || bi == Biome.DEEP_LUKEWARM_OCEAN
				|| bi == Biome.DEEP_OCEAN || bi == Biome.OCEAN || bi == Biome.WARM_OCEAN) { //Ocean biome
					
					if (r<=50) {
						spawn(loc,lvl,EntityType.DROWNED,5,17,5);
					}
					else {
						spawn(loc,lvl,EntityType.GUARDIAN,5,17,6);
					}
					
				}
				else if (t >= 0.95) { //Dry biome
					
					if (r<=45) {
						spawn(loc,lvl,EntityType.HUSK,5,9,10);
						spawn(loc,lvl,EntityType.VEX,7,10,1);
					}
					else if (r <= 90) {
						spawn(loc,lvl,EntityType.HUSK,6,8,8);
						spawn(loc,lvl,EntityType.CAVE_SPIDER,6,8,1);
					}
					else {
						spawn(loc,lvl,EntityType.PIGLIN_BRUTE,6,8,1);
						spawn(loc,lvl,EntityType.ZOMBIFIED_PIGLIN,6,10,4);
						spawn(loc,lvl,EntityType.HOGLIN,8,12,1);
					}
				}
				else if (t <= 0.15) { //Snowy biome
					
					if (r<=33) {
						spawn(loc,lvl,EntityType.STRAY,5,12,4);
						spawn(loc,lvl,EntityType.POLAR_BEAR,0,4,2);
						spawn(loc,lvl,EntityType.WITCH,3,10,1);
					}
					else if (r<=66) {
						spawn(loc,lvl,EntityType.STRAY,5,12,5);
						spawn(loc,lvl,EntityType.POLAR_BEAR,0,4,2);
						spawn(loc,lvl,EntityType.CAVE_SPIDER,7,10,1);
						spawn(loc,lvl,EntityType.ENDERMAN,0,4,1);
					}
					else {
						spawn(loc,lvl,EntityType.SPIDER,3,7,8);
						spawn(loc,lvl,EntityType.CAVE_SPIDER,5,7,1);
					}

				}
				else { //Normal biome		
					
					if (r<=7) {
						spawn(loc,lvl,EntityType.SPIDER,4,7,2);
						spawn(loc,lvl,EntityType.CAVE_SPIDER,5,10,1);
						spawn(loc,lvl,EntityType.SILVERFISH,4,10,4);
						spawn(loc,lvl,EntityType.ENDERMITE,1,5,1);
					}
					else if (r<=95) {
						spawn(loc,lvl,EntityType.CREEPER,5,10,1);
						spawn(loc,lvl,EntityType.SKELETON,5,10,2);
						spawn(loc,lvl,EntityType.ZOMBIE,2,8,4);
					}
					else {
						spawn(loc,lvl,EntityType.PILLAGER,7,12,3);
						spawn(loc,lvl,EntityType.EVOKER,5,7,1);
						spawn(loc,lvl,EntityType.RAVAGER,5,7,1);
						spawn(loc,lvl,EntityType.VINDICATOR,5,10,2);
					}
				}
			}
		}
	}
	
	public void spawn(Location loc, int lvl, EntityType type, int rMin, int rMax, int ammount) {
		if (rMin > rMax) return;
		
		int count=1;
		
		for (int i=0; i<500; i++) {
			
			int x = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockX());
			int y = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockY());
			int z = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockZ());
			
			if (loc.distance(new Location(loc.getWorld(),x,y,z)) >= rMin) {
				Block b = loc.getWorld().getBlockAt(x,y,z);
				int divisor = 5;
				
				if (b.getType().isSolid() && !b.getWorld().getBlockAt(x, y+1, z).getType().isSolid() && !b.getWorld().getBlockAt(x, y+2, z).getType().isSolid()) {
					if (count<ammount) count++;
					else i=500; //para el for
					
					LivingEntity enemy = (LivingEntity) b.getWorld().spawnEntity(b.getLocation().add(0.5, 1, 0.5), type);
					double newHealth = enemy.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + (lvl / divisor); 
					
					if (newHealth>2000) {
						spawn(loc, (int)((newHealth-2000)*divisor),type,rMin,rMax,1);
						newHealth=2000;
					}
					
					enemy.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
					enemy.setHealth(newHealth);
					
					if (type!=EntityType.CREEPER) enemyEffects(enemy,lvl);
					
					if (type == EntityType.ZOMBIE || type == EntityType.SKELETON || type == EntityType.STRAY) {
						enemy.getEquipment().setHelmet(new ItemStack(Material.TURTLE_HELMET));
					}
				}
			}
		}
	}
	
	public void enemyEffects(LivingEntity ent, int lvl) {
		PotionEffectType eType=null;
		int maxAmp=0;
		int amp=0;
		
		for (int i = 0; i<(lvl/80); i++) {
			int r = (int) (Math.random()*100)+1;
			
			if (r<=10) {
				eType=PotionEffectType.DAMAGE_RESISTANCE;
				maxAmp=3;
			}
			else if (r<=20) {
				eType=PotionEffectType.INCREASE_DAMAGE;
				maxAmp=1;
			}
			else if (r<=30) {
				eType=PotionEffectType.SPEED;
				maxAmp=4;
			}
			else if (r<=40) {
				eType=PotionEffectType.JUMP;
				maxAmp=3;
			}
			else if (r<=50) {
				eType=PotionEffectType.REGENERATION;
				maxAmp=2;
			}
			else if (r<=60) {
				eType=PotionEffectType.SLOW_FALLING;
				maxAmp=0;
			}
			else if (r<=70) {
				eType=PotionEffectType.FIRE_RESISTANCE;
				maxAmp=0;
			}
			else if (r<=80) {
				eType=PotionEffectType.DOLPHINS_GRACE;
				maxAmp=0;
			}
			else if (r<=90) {
				eType=PotionEffectType.ABSORPTION;
				maxAmp=3;
			}
			else if (r<=95) {
				eType=PotionEffectType.INVISIBILITY;
				maxAmp=0;
			}
			else if (r<=100) {
				eType=PotionEffectType.BAD_OMEN;
				maxAmp=3;
			}
			
			if (ent.hasPotionEffect(eType)) {
				if (ent.getPotionEffect(eType).getAmplifier() < maxAmp){
					amp = ent.getPotionEffect(eType).getAmplifier() + 1;
					ent.addPotionEffect(new PotionEffect(eType, 20 * 999999, amp,false));
				}
			}
			else ent.addPotionEffect(new PotionEffect(eType, 20 * 999999, 0,false));

		}
	}
}
