package tdp.levelProgression;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import tdp.levelProgression.comandos.getdata;
import tdp.levelProgression.comandos.language;
import tdp.levelProgression.comandos.levelmax;
import tdp.levelProgression.comandos.levelxpmultiplier;
import tdp.levelProgression.comandos.menulvl;
import tdp.levelProgression.comandos.setlevel;
import tdp.levelProgression.comandos.startprogresion;
import tdp.levelProgression.listeners.AdventurerListener;
import tdp.levelProgression.listeners.ArcherListener;
import tdp.levelProgression.listeners.ArmorListener;
import tdp.levelProgression.listeners.BossesListener;
import tdp.levelProgression.listeners.ChunksListener;
import tdp.levelProgression.listeners.FoodRestrictionListener;
import tdp.levelProgression.listeners.HelloListener;
import tdp.levelProgression.listeners.LumberjackListener;
import tdp.levelProgression.listeners.MagicBooksListener;
import tdp.levelProgression.listeners.MinerListener;
import tdp.levelProgression.listeners.SoldierListener;
import tdp.levelProgression.listeners.TridentmanListener;


public class LevelProgression extends JavaPlugin implements Listener {
	private static LevelProgression customplugin1;
	public static LevelProgression getPlugin() {
		return customplugin1;
	}
	public static Inventory gui=Bukkit.createInventory(null, InventoryType.CHEST, "");
	public static FileConfiguration lang;
	PluginDescriptionFile pdffile = getDescription();
	public String version = pdffile.getVersion();
	public String nombre = ChatColor.AQUA+"["+pdffile.getName()+"]"; //xd
	public static int xPmultiplier;
	public static int maxLevel;
	public static int xpToLevel;
	public static int minLevelToEnd;
	public static boolean breakItemWhenDenied;
	public static List<WorldLevelConfig> worlds= new ArrayList<WorldLevelConfig>();
	public static List<String> worldsName = new ArrayList<String>();
	public int taskID;
	private int xpPerExpLevel=200;
	private int minExpLevel = 15;
	private static String[] boardData = {null,null,null,null,null,null,null,null,null,null,null,null,null};
		
	
	public static boolean testPluginActive(World w) {
		int i = worldsName.indexOf(w.getName());
		
		return worlds.get(i).getActive();
	}

	@Override
	public void onEnable() {
		//config start
		this.saveDefaultConfig();

		Bukkit.getConsoleSender().sendMessage(nombre + ChatColor.YELLOW + " Plugin current language: " + this.getConfig().getString("language"));
		
		//language start
		languageFile.setup(this.getConfig().getString("language"));
		lang = languageFile.get();
		
		//variables setup
		String[] data= {lang.getString("abilities"),
			lang.getString("xp"),
			lang.getString("level"),
			lang.getString("apoints"),
			lang.getString("adventurer"),
			lang.getString("wizard"),
			lang.getString("soldier"),
			lang.getString("arquer"),
			lang.getString("shieldman"),
			lang.getString("tridentman"),
			lang.getString("miner"),
			lang.getString("lumber"),
			lang.getString("foodlvl"),
			};
		
		for (int i=0;i<data.length;i++) {
			boardData[i]=data[i];
		}
		//xPmultiplier=this.getConfig().getInt("xPmultiplier"); //now depends on the world
		//disableDisplay = this.getConfig().getBoolean("disableScoreboard"); //now depends on the world
		minLevelToEnd=this.getConfig().getInt("minLevelToEnd");
		maxLevel=this.getConfig().getInt("maxLevel");
		xpToLevel = this.getConfig().getInt("xpToLevel");
		breakItemWhenDenied = this.getConfig().getBoolean("breakItemWhenDenied");
		
		//worlds start
		for (String worldName : this.getConfig().getConfigurationSection("worlds").getKeys(false)) {
			worldsName.add(worldName);
			
			int index = worldsName.indexOf(worldName);
			
			WorldLevelConfig worldConfig = new WorldLevelConfig();
			worldConfig.setConfig(this.getConfig().getConfigurationSection("worlds."+worldName));
			
			worlds.add(index, worldConfig);
		}
		
		//general setup
		setComandos(); 
		new HelloListener(this); 
		new ArmorListener(this); 
		new MagicBooksListener(this); 
		new FoodRestrictionListener(this);
		new AdventurerListener(this);
		new SoldierListener(this);
		new MinerListener(this);
		new ArcherListener(this);
		new LumberjackListener(this);
		new TridentmanListener(this);
		new ChunksListener(this);
		new BossesListener(this);
	
		this.getServer().getPluginManager().registerEvents(this, this); //includes main as listener
		
		if (!Bukkit.getOnlinePlayers().isEmpty()) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				createBoard(p,0);
				start(p);
			}
		}
		
		customCrafts();
		
		gui=createGui();
		
		Bukkit.getConsoleSender().sendMessage(nombre+ChatColor.YELLOW+" Plugin started successfully: "+ChatColor.BOLD+version);
		
        new UpdateChecker(this, 99104).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
            	getLogger().info("There is a new update available for LevelProgression.");
            } 
        });

		
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + lang.getString("Startmsg1") + ChatColor.DARK_PURPLE +""+ ChatColor.BOLD + "Level Progression "
		+ version +ChatColor.RESET +""+ChatColor.AQUA + lang.getString("Startmsg2") + ChatColor.YELLOW + "TextoDePrueba");
	}
		
	public void onDisable() {
		Bukkit.getConsoleSender().sendMessage(nombre+ChatColor.YELLOW+" Plugin disabled");
	}

	public void setComandos() { //implement all commands
		getServer().getPluginCommand("levelxpmultiplier").setExecutor(new levelxpmultiplier (this));
		getServer().getPluginCommand("levelmax").setExecutor(new levelmax (this));
		getServer().getPluginCommand("startprogresion").setExecutor(new startprogresion (this));
		getServer().getPluginCommand("setlevel").setExecutor(new setlevel (this));
		getServer().getPluginCommand("getdata").setExecutor(new getdata (this));
		getServer().getPluginCommand("language").setExecutor(new language (this));
		getServer().getPluginCommand("menulvl").setExecutor(new menulvl (this));
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		setAbilities(e.getPlayer());
		createBoard(e.getPlayer(),0);
		start(e.getPlayer());	
		if (getWorldConfig(e.getPlayer().getWorld()).getDisableScoreboard()) e.getPlayer().getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		GeneralBoard board = new GeneralBoard(e.getPlayer().getUniqueId());
		if (board.hasID()) board.stop();
	}
	
	@EventHandler
	public void killScores (PlayerChangedWorldEvent e) {
		if (getWorldConfig(e.getPlayer().getWorld()).getDisableScoreboard()) e.getPlayer().getScoreboard().clearSlot(DisplaySlot.SIDEBAR);;
	}
	
	@EventHandler
	public void endGameRestriction(PlayerPortalEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (e.getTo().getWorld().getEnvironment().equals(Environment.THE_END) && pgetData(e.getPlayer(), "LEVEL") < minLevelToEnd) {
			e.setCancelled(true);
			if (pgetData(e.getPlayer(),"CC1")==0) {
				e.getPlayer().sendMessage(ChatColor.RED + lang.getString("EndLimit1") + minLevelToEnd + lang.getString("EndLimit2"));
				psetData(e.getPlayer(),"CC1",4);
			}
		}
	}
	
	@EventHandler 
	public void xpResetOnDeath(PlayerDeathEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

		Player p = (Player) e.getEntity();
		
		if (p.getKiller() != null) { //rewards the killer with some XP
			Player killer = (Player) p.getKiller();
			
			if (p.getLevel() <= minExpLevel) {
				addXP(killer, pgetData(p,"XP"));
			}
			else {
				addXP(killer, pgetData(p,"XP") + (p.getLevel()-minExpLevel) * xpPerExpLevel);
			}
		}
		
		boolean invTest=true;
		
		for (ItemStack item : p.getInventory().getContents()) { //Keep inventory with totem feature
			if (item != null && invTest && item.getItemMeta().getLocalizedName().equals("ULTIMATE_TOTEM")) {
				p.setLevel(0);
				p.setExp(0);
				e.setKeepInventory(true);
				e.getDrops().clear();
				item.setAmount(0);
				item.setType(null);
				invTest=false;
			}
		}
		
		psetData(p, "XP", 0);
	}
	
	@EventHandler
	public void messageOfReward(PlayerLevelChangeEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (e.getPlayer().getLevel()>minExpLevel && e.getOldLevel()<e.getNewLevel() && (Math.random()*100+1)<=4) {
			Player p = e.getPlayer();
			Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + p.getName() 
			+ ChatColor.RESET + "" + ChatColor.AQUA + lang.getString("ExpMessage1") + (pgetData(p,"XP") + (p.getLevel()-minExpLevel) * xpPerExpLevel) + lang.getString("ExpMessage2"));
		}
	}
	
	//AXp reward when you obtain xp
	@EventHandler
	public void spLvl(PlayerExpChangeEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		int xpMult= worlds.get(worldsName.indexOf(e.getPlayer().getWorld().getName())).getXPmultiplier();
		
		int a = e.getAmount() * xpMult; //original * 5
		LevelProgression.addXP(e.getPlayer(),a);

	}
	
	@EventHandler
	public void breakBlockXP(BlockBreakEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		Block block = e.getBlock();
		Material b =block.getType();
		
		if (worlds.get(LevelProgression.worldsName.indexOf(e.getBlock().getWorld().getName())).getNoXpBlockReward()) {
		
			if (b == Material.DIRT || b==Material.GRASS_BLOCK) {
				if (pgetData(p, "MINER") >= 100) addXP(p,0);
				else addXP(p,4);
			} 
			else if (b == Material.ACACIA_LOG || b==Material.BIRCH_LOG || b==Material.DARK_OAK_LOG || b==Material.JUNGLE_LOG || b==Material.OAK_LOG || b==Material.SPRUCE_LOG) {
				addXP(p,7);
			} 
			else if (b == Material.STONE) {
				if (pgetData(p, "MINER") >= 100) addXP(p,2);
				else addXP(p,6);
			} 
			else if (b==Material.DEEPSLATE) {
				if (pgetData(p, "MINER") >= 100) addXP(p,4);
				else addXP(p,8);
			}
			else if (b==Material.NETHERRACK) {
				if (pgetData(p, "MINER") < 100) addXP(p,1);
			}
	
		}
		
	}
	
	public static void psetData(Player p, String nameString, int value) {
		p.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), nameString), PersistentDataType.INTEGER, value);
	} 
	
	public static int pgetData(Player p, String nameString) {
		return p.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), nameString), PersistentDataType.INTEGER);
	} 

	public static void addToScore(Player p, String nameString, int ammount) {
		int n=0;
		
		for (int i=0;i<ammount;i++) {
			
			if (nameString == "ABILITYPOINTS") {
			
				n= 1 + pgetData(p,"ABILITYPOINTS");
				psetData(p,"ABILITYPOINTS",n);
			
			} else if (nameString == "LEVEL") {
				if (pgetData(p,"LEVEL")<maxLevel) {
			
					n= 1 + pgetData(p,nameString);
					psetData(p,nameString,n);
	
					n= 1 + pgetData(p,"ABILITYPOINTS");
					psetData(p,"ABILITYPOINTS",n);
				} else {
					p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,20*3,2,false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,20*3,10,false));
				}
			} else if (nameString == "FOODLVL") {
				if (pgetData(p,"FOODLVL")<50 && pgetData(p,"ABILITYPOINTS") > 0) {
					n= 1 + pgetData(p,nameString);
					psetData(p,nameString,n);
				
					n=pgetData(p,"ABILITYPOINTS") - 1;
					psetData(p,"ABILITYPOINTS",n);
				
					p.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN+"UPGRADED");
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5.0F, 1.0F);
				}
			} else if (pgetData(p,nameString)<300 && pgetData(p,"ABILITYPOINTS") > 0) {
				
				n= 1 + pgetData(p,nameString);
				psetData(p,nameString,n);
				
				n= pgetData(p,"ABILITYPOINTS") - 1;
				psetData(p,"ABILITYPOINTS",n);
				
				p.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN+"UPGRADED");
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5.0F, 1.0F);
			} 
		}
	}
	
	public static void addXP(Player p,int ammount) {
		int newvalue=pgetData(p,"XP")+ammount;
		int finalvalue=0;
		
		if (newvalue >= xpToLevel) {
			LevelProgression.addToScore(p,"LEVEL",(newvalue/xpToLevel));
			finalvalue = newvalue%xpToLevel;
		} else {
			finalvalue=newvalue;
		}
		psetData(p,"XP", finalvalue);
	}
	
	public void start(Player player) {
		taskID= Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			GeneralBoard board = new GeneralBoard(player.getUniqueId());
			double maxCC=0;
			
			@Override 
			public void run() {
				double n = constantChecks(player);
				
				if (!getWorldConfig(player.getWorld()).getDisableScoreboard()) {					
					if (n>0) {
						if (maxCC<=0) maxCC=n;
						createBoard(player, (n/maxCC));
					}
					else {
						maxCC=0;
						createBoard(player, 0);
					}
				}
				
				if (!board.hasID()) board.setID(taskID);
			}
		}, 0, 10);
	}

	public int constantChecks(Player p){
		if (pgetData(p, "CC1")>0) {
			int cc = (pgetData(p, "CC1")-1);
			psetData(p, "CC1", cc);
			return cc;
		}	
		return 0;
	}
	
	public void createBoard(Player jugador, double ratio) { 
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective obj = board.registerNewObjective(boardData[0],"dummy",boardData[0]);

		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + boardData[0]);
		
		Score xp = obj.getScore(ChatColor.LIGHT_PURPLE + boardData[1] +": " + ChatColor.BOLD + "" + pgetData(jugador, "XP"));
		xp.setScore(12);
		
		Score level = obj.getScore(ChatColor.LIGHT_PURPLE + boardData[2] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "LEVEL"));
		level.setScore(11);
		
		if (pgetData(jugador, "ABILITYPOINTS")>0) {
			Score ab = obj.getScore(ChatColor.GOLD + boardData[3] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "ABILITYPOINTS"));
			ab.setScore(10);
		}

		Score cc = obj.getScore(ChatColor.BOLD + ccProgression(ratio));
		cc.setScore(9);

		if (pgetData(jugador, "ADVENTURER")>0) {
			Score adventurer = obj.getScore(ChatColor.BOLD + boardData[4] +": " + ChatColor.BOLD + "" + pgetData(jugador, "ADVENTURER"));
			adventurer.setScore(8);
		}
		
		if (pgetData(jugador, "WIZARD")>0) {
			Score wizard = obj.getScore(ChatColor.BOLD + boardData[5] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "WIZARD"));
			wizard.setScore(7);
		}
		
		if (pgetData(jugador, "SOLDIER")>0) {
			Score SOLDIER = obj.getScore(ChatColor.BOLD + boardData[6] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "SOLDIER"));
			SOLDIER.setScore(6);
		}
		
		if (pgetData(jugador, "ARCHER")>0) {
			Score ARCHER = obj.getScore(ChatColor.BOLD + boardData[7]  +": "+ ChatColor.BOLD + "" + pgetData(jugador, "ARCHER"));
			ARCHER.setScore(5);
		}
		
		if (pgetData(jugador, "SHIELDMAN")>0) {
			Score SHIELDMAN = obj.getScore(ChatColor.BOLD + boardData[8] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "SHIELDMAN"));
			SHIELDMAN.setScore(4);
		}
		
		if (pgetData(jugador, "TRIDENTMAN")>0) {
			Score TRIDENTMAN = obj.getScore(ChatColor.BOLD + boardData[9]  +": "+ ChatColor.BOLD + "" + pgetData(jugador, "TRIDENTMAN"));
			TRIDENTMAN.setScore(3);
		}
		
		if (pgetData(jugador, "MINER")>0) {
			Score MINER = obj.getScore(ChatColor.BOLD + boardData[10] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "MINER"));
			MINER.setScore(2);
		}
		
		if (pgetData(jugador, "LUMBERJACK")>0) {
			Score LUMBERJACK = obj.getScore(ChatColor.BOLD + boardData[11] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "LUMBERJACK"));
			LUMBERJACK.setScore(1);
		}
		
		if (pgetData(jugador, "FOODLVL")>0) {
			Score food = obj.getScore(ChatColor.BOLD + boardData[12] +": "+ ChatColor.BOLD + "" + pgetData(jugador, "FOODLVL"));
			food.setScore(0);
		}
		
		//crear la tabla en s�
		
		jugador.setScoreboard(board);
	}
	
	private String ccProgression (double ratio) {
		String bar=ChatColor.DARK_RED+"";
		double max=15;
		
		for (int i = 0; i<max;i++) {
			if (i==(int)(ratio*max)) bar = bar.concat(ChatColor.GRAY+"");
			bar = bar.concat("=");
		}
		
		return bar;
	}
	
	public static void enchVerifier(Player p , ItemStack item) {
		if (item.getItemMeta() != null) {
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <5) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL)>1 && LevelProgression.pgetData(p, "SHIELDMAN") <60) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL)>2 && LevelProgression.pgetData(p, "SHIELDMAN") <230) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL)>3 && LevelProgression.pgetData(p, "SHIELDMAN") <290) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.THORNS)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <15) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.THORNS)>1 && LevelProgression.pgetData(p, "SHIELDMAN") <170) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.THORNS)>2 && LevelProgression.pgetData(p, "SHIELDMAN") <270) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_FIRE)>2 && LevelProgression.pgetData(p, "SHIELDMAN") <20) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_FIRE)>3 && LevelProgression.pgetData(p, "SHIELDMAN") <110) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS)>2 && LevelProgression.pgetData(p, "SHIELDMAN") <22) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS)>3 && LevelProgression.pgetData(p, "SHIELDMAN") <130) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_PROJECTILE)>2 && LevelProgression.pgetData(p, "SHIELDMAN") <24) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_PROJECTILE)>3 && LevelProgression.pgetData(p, "SHIELDMAN") <150) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.FROST_WALKER)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <38 && LevelProgression.pgetData(p, "ADVENTURER") <30) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.FROST_WALKER)>1 && LevelProgression.pgetData(p, "ADVENTURER") <75) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SOUL_SPEED)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <43 && LevelProgression.pgetData(p, "ADVENTURER") <35) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SOUL_SPEED)>1 && LevelProgression.pgetData(p, "SHIELDMAN") <85 && LevelProgression.pgetData(p, "ADVENTURER") <80) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SOUL_SPEED)>2 && LevelProgression.pgetData(p, "ADVENTURER") <120) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_FALL)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <46 && LevelProgression.pgetData(p, "ADVENTURER") <25) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_FALL)>1 && LevelProgression.pgetData(p, "SHIELDMAN") <78 && LevelProgression.pgetData(p, "ADVENTURER") <70) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_FALL)>2 && LevelProgression.pgetData(p, "ADVENTURER") <110) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PROTECTION_FALL)>3 && LevelProgression.pgetData(p, "ADVENTURER") <160) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <75 && LevelProgression.pgetData(p, "ADVENTURER") <10 && LevelProgression.pgetData(p, "TRIDENTMAN")<200) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "SHIELDMAN") <180 && LevelProgression.pgetData(p, "ADVENTURER") <60) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "SHIELDMAN") <250 && LevelProgression.pgetData(p, "ADVENTURER") <125) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "SHIELDMAN") <240 && LevelProgression.pgetData(p, "ADVENTURER") <220) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DEPTH_STRIDER)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <40) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DEPTH_STRIDER)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <130) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DEPTH_STRIDER)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <220) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.WATER_WORKER)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <190) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.OXYGEN)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <140) enchDenied(p, item);
		} 
	}
	
	public static void enchDenied (Player p, ItemStack item) {
		ItemStack itemCloned = item.clone();
		item.setAmount(0);
		p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
		p.sendMessage(ChatColor.RED + lang.getString("enchDenied"));
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
	}
	
	public void setAbilities (Player p) {
		PersistentDataContainer data=p.getPersistentDataContainer();
		
		if (!data.has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "LEVEL"),PersistentDataType.INTEGER)) {
			LevelProgression.psetData(p,"ADVENTURER",0);
			LevelProgression.psetData(p,"WIZARD",0);
			LevelProgression.psetData(p,"SOLDIER",0);
			LevelProgression.psetData(p,"ARCHER",0);
			LevelProgression.psetData(p,"SHIELDMAN",0);
			LevelProgression.psetData(p,"TRIDENTMAN",0);
			LevelProgression.psetData(p,"MINER",0);
			LevelProgression.psetData(p,"LUMBERJACK",0);
			LevelProgression.psetData(p,"FOODLVL",0);
			LevelProgression.psetData(p,"XP",0);
			LevelProgression.psetData(p,"ABILITYPOINTS",0);
			LevelProgression.psetData(p,"LEVEL",0);
			LevelProgression.psetData(p,"CC1",0);
		}
	}
	
	public static Block getPosiblePlacedBlock(Player player, int range) {
	    List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, range);
	    if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isSolid()) return null;
	    
	    Block adjacentBlock = lastTwoTargetBlocks.get(0);
	    return adjacentBlock;
	}
	
	public static Entity getTargetEntity(final Entity entity) {
        return getTarget(entity, entity.getWorld().getEntities());
    }
	
	public static void substractAmount (ItemStack item, int amount) {
		if (item.getAmount() > amount) item.setAmount(item.getAmount() - amount);
		else item.setAmount(0);
	}
	
    public static <T extends Entity> T getTarget(final Entity entity,
            final Iterable<T> entities) {
        if (entity == null)
            return null;
        T target = null;
        final double threshold = 1;
        for (final T other : entities) {
            final Vector n = other.getLocation().toVector()
                    .subtract(entity.getLocation().toVector());
            if (entity.getLocation().getDirection().normalize().crossProduct(n)
                    .lengthSquared() < threshold
                    && n.normalize().dot(
                            entity.getLocation().getDirection().normalize()) >= 0) {
                if (target == null
                        || target.getLocation().distanceSquared(
                                entity.getLocation()) > other.getLocation()
                                .distanceSquared(entity.getLocation()))
                    target = other;
            }
        }
        return target;
    }
	
	public static void damageItem (Player p, ItemStack item, int damage) {
		if (item==null || !(item.getItemMeta() instanceof Damageable)) return;
		Damageable dam = (Damageable) item.getItemMeta();
		
		dam.setDamage(dam.getDamage()+damage);
		if (dam.getDamage()>item.getType().getMaxDurability()) {
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1.0F);
		} else item.setItemMeta((ItemMeta) dam);	
	}
	
	public static void damageItem (LivingEntity ent, ItemStack item, int damage) {
		if (item==null || !(item.getItemMeta() instanceof Damageable)) return;
		Damageable dam = (Damageable) item.getItemMeta();
		
		dam.setDamage(dam.getDamage()+damage);
		if (dam.getDamage()>item.getType().getMaxDurability()) {
			item.setAmount(0);
		} else item.setItemMeta((ItemMeta) dam);
		
	}
	
	public static void damageItem (ItemStack item, int damage) {
		if (item==null || !(item.getItemMeta() instanceof Damageable)) return;
		Damageable dam = (Damageable) item.getItemMeta();
		
		dam.setDamage(dam.getDamage()+damage);
		if (dam.getDamage()>item.getType().getMaxDurability()) {
			item.setAmount(0);
		} else item.setItemMeta((ItemMeta) dam);
		
	}

	public void customCrafts(){
		ItemStack item1 = new ItemStack(Material.EXPERIENCE_BOTTLE,7);
		ShapedRecipe xpBottle1 = new ShapedRecipe(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "CRAFT1") , item1);
		xpBottle1.shape(
				"%*",
				"B*");
		xpBottle1.setIngredient('%', Material.DIAMOND);
		xpBottle1.setIngredient('*', Material.LAPIS_LAZULI);
		xpBottle1.setIngredient('B', Material.GLASS_BOTTLE);
		getServer().addRecipe(xpBottle1);
		
		ItemStack item2 = new ItemStack(Material.EXPERIENCE_BOTTLE,22);
		ShapedRecipe xpBottle2 = new ShapedRecipe(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "CRAFT2") , item2);
		xpBottle2.shape(
				"%*",
				"B*");
		xpBottle2.setIngredient('%', Material.NETHERITE_INGOT);
		xpBottle2.setIngredient('*', Material.EMERALD);
		xpBottle2.setIngredient('B', Material.GLASS_BOTTLE);
		getServer().addRecipe(xpBottle2);		
		
		ItemStack item3 = new ItemStack(Material.POISONOUS_POTATO,1);
		ItemMeta meta = item3.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.setLocalizedName("levelResetItem");
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + lang.getString("poisonousPotato"));
		ArrayList<String> lore = new ArrayList<String>();
		for (String s : lang.getString("poisonousPotatoDescrp").split("//")) lore.add(ChatColor.DARK_PURPLE +s);
		meta.setLore(lore);
		
		item3.setItemMeta(meta);
		ShapedRecipe lvlReset = new ShapedRecipe(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "CRAFT3") , item3);
		lvlReset.shape(
				"**",
				"B*");
		lvlReset.setIngredient('*', Material.NETHER_STAR);
		lvlReset.setIngredient('B', Material.POISONOUS_POTATO);
		getServer().addRecipe(lvlReset);		
	
		//==============================
		
		ItemStack item4 = new ItemStack(Material.BEETROOT,1);
		meta = item4.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.setLocalizedName("WILD_FOOD");
		meta.setDisplayName(ChatColor.LIGHT_PURPLE +lang.getString("onion"));
		ArrayList<String> lore2 = new ArrayList<String>();
		for (String s : lang.getString("onionDescr").split("//")) lore2.add(ChatColor.DARK_PURPLE +s);
        meta.setLore(lore2);
		
		item4.setItemMeta(meta);
		ShapedRecipe wildChunk = new ShapedRecipe(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "CRAFT4") , item4);
		wildChunk.shape(
				"***",
				"*B*",
				"***");
		wildChunk.setIngredient('*', Material.DIRT);
		wildChunk.setIngredient('B', Material.WHEAT_SEEDS);
		getServer().addRecipe(wildChunk);		
		
		//==============================
		
		ItemStack item5 = new ItemStack(Material.TOTEM_OF_UNDYING,1);
		meta = item5.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.setLocalizedName("ULTIMATE_TOTEM");
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + lang.getString("megatotem"));
		ArrayList<String> lore3 = new ArrayList<String>();
		for (String s : lang.getString("megatotemdescr").split("//")) lore3.add(ChatColor.DARK_PURPLE +s);        
		
        meta.setLore(lore3);
		
		item5.setItemMeta(meta);
		ShapedRecipe UltimateTotem = new ShapedRecipe(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "CRAFT5") , item5);
		UltimateTotem.shape(
				"***",
				"*B*",
				"***");
		UltimateTotem.setIngredient('*', Material.DIAMOND);
		UltimateTotem.setIngredient('B', Material.TOTEM_OF_UNDYING);
		getServer().addRecipe(UltimateTotem);		
	}
	
	public static Inventory createGui() {

		ItemStack item = new ItemStack(Material.ACACIA_BOAT, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("adventurer"));
		meta.setLore(null);
		item.setItemMeta(meta);
		gui.setItem(9, item);
		
		item = new ItemStack(Material.ENCHANTED_BOOK, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("wizard"));
		item.setItemMeta(meta);
		gui.setItem(10, item);
		
		item = new ItemStack(Material.NETHERITE_SWORD, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("soldier"));
		item.setItemMeta(meta);
		gui.setItem(11, item);

		item = new ItemStack(Material.CROSSBOW, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("arquer"));
		item.setItemMeta(meta);
		gui.setItem(12, item);

		item = new ItemStack(Material.SHIELD, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("shieldman"));
		item.setItemMeta(meta);
		gui.setItem(13, item);

		item = new ItemStack(Material.TRIDENT, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("tridentman"));
		item.setItemMeta(meta);
		gui.setItem(14, item);

		item = new ItemStack(Material.GOLDEN_PICKAXE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("miner"));
		item.setItemMeta(meta);
		gui.setItem(15, item);
		
		item = new ItemStack(Material.IRON_AXE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("lumber"));
		item.setItemMeta(meta);
		gui.setItem(16, item);
		
		item = new ItemStack(Material.APPLE, 1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD  + "" + ChatColor.AQUA + lang.getString("foodlvl"));
		item.setItemMeta(meta);
		gui.setItem(17, item);
		
		for (int i=0;i<9;i++){
			ItemStack Bluepanel = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
			meta = Bluepanel.getItemMeta();
			meta.setDisplayName(".");
			Bluepanel.setItemMeta(meta);
			gui.setItem(i, Bluepanel);
		}
		for (int i=18;i<27;i++){
			ItemStack Bluepanel = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
			meta = Bluepanel.getItemMeta();
			meta.setDisplayName(".");
			Bluepanel.setItemMeta(meta);
			gui.setItem(i, Bluepanel);
		}
		
		
		
		return gui;
	}
	
	public WorldLevelConfig getWorldConfig (World w) {
		return worlds.get(worldsName.indexOf(w.getName()));
	}
	

} 
