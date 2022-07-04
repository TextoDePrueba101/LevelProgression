package tdp.levelProgression.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Stray;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class BossesListener implements Listener{
	private LevelProgression plugin;
	private boolean BossExist = false;
	private FileConfiguration lang = LevelProgression.lang;
	
	public BossesListener(LevelProgression plugin) {
		this.plugin = plugin; 
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void CreateSummoner(CreatureSpawnEvent e) {
		if (!LevelProgression.testPluginActive(e.getLocation().getWorld())) return;
		
		if (isException(e, e.getEntityType())) return;
		
		int evilSpiritProbability = LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(e.getLocation().getWorld().getName())).getEvilSpiritProbability();
		boolean evilSpiritSpawnAtEgg = LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(e.getLocation().getWorld().getName())).getEvilSpiritSpawnAtEgg();
		
		if (e.getSpawnReason()==SpawnReason.NATURAL || e.getSpawnReason()==SpawnReason.DEFAULT) if (((Math.random())*100+1)<=evilSpiritProbability) { //1
			LivingEntity ent = (LivingEntity) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.MAGMA_CUBE);
			((Slime)ent).setSize(0);
			ent.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "SpawnBoss"), PersistentDataType.INTEGER,0);
			ent.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + lang.getString("bosses1"));
		}
		if (e.getSpawnReason()==SpawnReason.CUSTOM) if (((Math.random())*100+1)<=(evilSpiritProbability-1)) { //1
			LivingEntity ent = (LivingEntity) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.MAGMA_CUBE);
			((Slime)ent).setSize(0);
			ent.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "SpawnBoss"), PersistentDataType.INTEGER,0);
			ent.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + lang.getString("bosses1"));
		}
		if (e.getSpawnReason()==SpawnReason.SPAWNER_EGG) if (evilSpiritSpawnAtEgg) { //1
			LivingEntity ent = (LivingEntity) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.MAGMA_CUBE);
			((Slime)ent).setSize(0);
			ent.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "SpawnBoss"), PersistentDataType.INTEGER,0);
			ent.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + lang.getString("bosses1"));
		}
	}
	
	public boolean isException(CreatureSpawnEvent e, EntityType t) {
		if (t.equals(EntityType.MAGMA_CUBE) || t.equals(EntityType.ARMOR_STAND)) return true;
		else return false;
	}
	
	@EventHandler
	public void OnDespawnDeath(EntitiesUnloadEvent e) {
		if (BossExist) for (Entity ent : e.getEntities()) {
			if (ent.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
				LivingEntity boss = (LivingEntity) ent;
				
				boss.setHealth(0);
			}
		};
	}
	
	@EventHandler
	public void SummonBoss(EntityDeathEvent e) {

		if (e.getEntity().getType()==EntityType.MAGMA_CUBE && e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "SpawnBoss"), PersistentDataType.INTEGER) 
		&& e.getEntity().getKiller() != null && e.getEntity().getLastDamageCause().getCause()==DamageCause.ENTITY_ATTACK) {
			if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

			for (ItemStack i : e.getDrops()) {
				i.setAmount(0);
			}
			
			int LVL = LevelProgression.pgetData(e.getEntity().getKiller(), "LEVEL");
			CreateBoss(e.getEntity().getLocation(),LVL * 2);
			Bukkit.broadcastMessage(ChatColor.GOLD +lang.getString("bosses2"));
		}
		else if (BossExist) {
			if (e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
				LivingEntity boss = e.getEntity();
				
				int taskid = boss.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER);
				Bukkit.getScheduler().cancelTask(taskid);
				BossExist=false;
				
				Bukkit.getBossBar(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), boss.getUniqueId().toString())).removeAll();
				Bukkit.removeBossBar(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), boss.getUniqueId().toString()));
				
				if (e.getEntity().getKiller()!= null) {
					ItemStack dropXP = new ItemStack(Material.POTION);
					ItemMeta m = dropXP.getItemMeta();
					m.setLocalizedName("XPbottle");
					m.setDisplayName(ChatColor.DARK_PURPLE +lang.getString("bosses3"));
					ArrayList<String> lore = new ArrayList<String>();
			        for (String s : lang.getString("bosses4").split("//")) lore.add(ChatColor.DARK_PURPLE +s);
			        m.setLore(lore);
			        dropXP.setItemMeta(m);
					
					int ammount = (int) (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(e.getEntity().getWorld().getName())).getHealthPerReward());
					
					for (int i=0;i<ammount;i++) {
						e.getDrops().add(dropXP.clone());
						e.getDrops().add(dropXP.clone());
					}
					
					if (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() >= 200) {
						if ((Math.random()*100+1)<=50) e.getDrops().add(specialItem(0,boss));
						else e.getDrops().add(specialItem(1,boss));
					}
				}
			}
		}
	}
	
	public ItemStack specialItem(int n,LivingEntity boss) {
		ItemStack item = new ItemStack(Material.STICK);
		ItemMeta m = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();

		if (n == 0) {
			
			if (boss.getType()==EntityType.SLIME) {
				m.setLocalizedName("TP_GOLDEN_CARROT");
				item.setType(Material.GOLDEN_CARROT);
				m.setDisplayName(ChatColor.YELLOW + lang.getString("bosses5"));
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				m.addEnchant(Enchantment.DURABILITY, 1, true);
		        for (String s : lang.getString("bosses6").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(8);
			}
			else if (boss.getType()==EntityType.STRAY) {
				m.setLocalizedName("INFINITE_CROSSBOW");
				item.setType(Material.CROSSBOW);
				m.setDisplayName(ChatColor.YELLOW + lang.getString("bosses7"));
		        for (String s : lang.getString("bosses8").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
			}
			else if (boss.getType()==EntityType.HUSK) {
				m.setLocalizedName("DURABILITY_BRICK");
				item.setType(Material.BRICK);
				m.setDisplayName(ChatColor.YELLOW + lang.getString("bosses9"));
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				m.addEnchant(Enchantment.DURABILITY, 1, true);
		        for (String s : lang.getString("bosses10").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(64);
			}
			else if (boss.getType()==EntityType.ELDER_GUARDIAN) {
				m.setLocalizedName("RAIN_KELP");
				item.setType(Material.DRIED_KELP);
				m.setDisplayName(ChatColor.YELLOW +lang.getString("bosses11"));
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				m.addEnchant(Enchantment.DURABILITY, 1, true);
		        for (String s : lang.getString("bosses12").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(5);
			}

		}
		else {
		
			if (boss.getType()==EntityType.SLIME) {
				item = new ItemStack(Material.POTION);
				m = item.getItemMeta();
				((PotionMeta)m).setColor(Color.GREEN);
				m.setDisplayName(ChatColor.YELLOW +lang.getString("bosses13"));
				m.setLocalizedName("MAGIC_XP_BOTTLE");
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				m.addEnchant(Enchantment.DURABILITY, 1, true);
		        for (String s : lang.getString("bosses14").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(3);
			}
			else if (boss.getType()==EntityType.STRAY) {
				m.setLocalizedName("STRONG_BONEMEAL");
				item.setType(Material.BONE_MEAL);
				m.setDisplayName(ChatColor.YELLOW +lang.getString("bosses15"));
				m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				m.addEnchant(Enchantment.DURABILITY, 1, true);
		        for (String s : lang.getString("bosses16").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(64);
			}
			else if (boss.getType()==EntityType.HUSK) {
				m.setLocalizedName("MINER_HELMET");
				item.setType(Material.CHAINMAIL_HELMET);
				m.setDisplayName(ChatColor.YELLOW +lang.getString("bosses17"));
		        for (String s : lang.getString("bosses18").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(1);
				m.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
			}
			else if (boss.getType()==EntityType.ELDER_GUARDIAN) {
				m.setLocalizedName("THRONS_SHIELD");
				item.setType(Material.SHIELD);
				m.setDisplayName(ChatColor.YELLOW +lang.getString("bosses19"));
		        for (String s : lang.getString("bosses20").split("//")) lore.add(ChatColor.LIGHT_PURPLE +s);
				item.setAmount(1);
				m.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
			}

		}
		
		m.setLore(lore);
		item.setItemMeta(m);
		return item;
	}
	
	@EventHandler
	public void BossesAbilitiesAtDamage(EntityDamageEvent e) {
		if (!BossExist) return;
		
		if (e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& e.getCause()==DamageCause.SUFFOCATION) {
			LivingEntity boss = (LivingEntity) e.getEntity();
			boolean tp=true;
			
			e.setDamage(0);
			
			List<Entity> near = boss.getNearbyEntities(40, 40, 40);
			for (Entity ent : near) {
				if (tp && ent instanceof Player) {
    			   
					Location loc = ent.getLocation();
					loc.setDirection(((Player)ent).getEyeLocation().getDirection());
					loc.add(((Player)ent).getEyeLocation().getDirection().normalize().multiply(-1.1));
   				
					boss.teleport(loc);
					tp=false;
				}
			}
		}

		if (e.getEntityType()==EntityType.SLIME 
		&& e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
			LivingEntity boss = (LivingEntity) e.getEntity();
			
			if ((Math.random()*100+1) <= 25) {
				
				e.setCancelled(true);
				Slime sl = (Slime) boss.getWorld().spawnEntity(boss.getLocation(), EntityType.SLIME);
				sl.setSize(2);
				sl.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8);
				sl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,20*99999,2,true));
			}
		}
		
		if (e.getEntityType()==EntityType.STRAY 
		&& e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& e.getCause()==DamageCause.PROJECTILE) {
			LivingEntity boss = (LivingEntity) e.getEntity();
			
			if ((Math.random()*100+1)<=30) {
				e.setCancelled(true);
				if ((boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()-boss.getHealth())>=15) boss.setHealth(boss.getHealth()+15);
			}
		}	
		
		if (e.getEntityType()==EntityType.ELDER_GUARDIAN 
		&& e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& e.getCause()==DamageCause.DRYOUT) {
			LivingEntity boss = (LivingEntity) e.getEntity();
			List<Entity> entities = boss.getNearbyEntities(40, 40, 40);
			
			for (Entity ent : entities) if (ent instanceof Player) {
				Player p = (Player) ent;
				p.damage(e.getDamage());
			}
		}			
	}
	
	@EventHandler
	public void BossesAbilitiesAtDamageOfEntity(EntityDamageByEntityEvent e) {
		if (!BossExist) return;
		
		if (e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& !(e.getDamager() instanceof Player)) {
			if (e.getDamager() instanceof LivingEntity) ((LivingEntity)e.getDamager()).setHealth(0);
		}
		
		if (e.getEntity().getType()==EntityType.HUSK
		&& (e.getDamager() instanceof Player) 
		&& e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
			Player p = (Player) e.getDamager();
			
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,20*5,0,true));
			p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,20*10,0,true));
		}
		
		if (e.getDamager().getType()==EntityType.HUSK
		&& (e.getEntity() instanceof Player) 
		&& e.getDamager().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
			Player p = (Player) e.getEntity();
			p.addPotionEffect(new PotionEffect(PotionEffectType.POISON,20* 3,1,true));
		}
		
		if (e.getEntityType()==EntityType.ELDER_GUARDIAN 
		&& e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& e.getCause()==DamageCause.ENTITY_ATTACK
		&& !(e.getDamager().isDead())) {

			((LivingEntity)e.getDamager()).damage(e.getDamage() * 1);
		}
		
		if (e.getDamager().getType()==EntityType.ELDER_GUARDIAN
		&& (e.getEntity() instanceof Player) 
		&& e.getDamager().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& e.getCause()!=DamageCause.THORNS) {
			Player p = (Player) e.getEntity();
			LivingEntity boss = (LivingEntity) e.getDamager();
			
			Location loc = boss.getLocation();
			loc.setDirection(boss.getEyeLocation().getDirection().multiply(-1));
			loc.add(boss.getEyeLocation().getDirection().normalize().multiply(3));
			
			p.teleport(loc);
		}
	}

	@EventHandler 
	public void KingSlimeDeath(SlimeSplitEvent e) {
		if (!e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) return;
		
		List<Entity> entities = e.getEntity().getNearbyEntities(20, 20, 20);
		
		for (Entity ent : entities) {
			if (!(ent instanceof Player)) ent.setVelocity(new Vector(0,1.5,0));
		}
		
		BukkitRunnable r = new BukkitRunnable(){
			
		    @Override
		     public void run(){
				e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 7, false, true);
				
		    	this.cancel();
		    }
		};
		
		r.runTaskTimer(plugin, 20, 0);
	}
	
	@EventHandler
	public void ColdSkeletonHit(ProjectileHitEvent e) {
		if (!BossExist) return;
		
		if (e.getEntity().getCustomName()!=null
		&& e.getEntity().getShooter() instanceof Stray
		&& ((Stray)e.getEntity().getShooter()).getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)
		&& e.getHitEntity() != null) {
			Entity arrow = e.getEntity();
			Stray boss = (Stray) e.getEntity().getShooter();
			
			if (arrow.getCustomName().equals("ENEMIES_BOSS_ARROW")) {
				spawn(arrow.getLocation(),EntityType.POLAR_BEAR,0,6,1);
				spawn(arrow.getLocation(),EntityType.SPIDER,0,6,1);
				spawn(arrow.getLocation(),EntityType.HUSK,5,10,1);
			}
			
			if ((Math.random()*100+1)<=30) {
				if ((boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()-boss.getHealth())>=10) boss.setHealth(boss.getHealth()+10);
			}
		}
		
		if (e.getHitEntity() != null 
		&& e.getHitEntity() instanceof LivingEntity
		&& ((LivingEntity) e.getHitEntity()).getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
			Projectile arrow = e.getEntity();
			LivingEntity boss = (LivingEntity) e.getHitEntity();
			
			if (arrow.getShooter() instanceof Player) {
				Player p = (Player) arrow.getShooter();
				if  ((Math.random()*100+1) <= 35) {
					if (boss.getType()==EntityType.STRAY) {
						spawn(p.getLocation(),EntityType.STRAY,0,4,1);
					}
					else if (boss.getType()==EntityType.HUSK) {
						spawn(p.getLocation(),EntityType.HUSK,0,4,1);
					}
					else if (boss.getType()==EntityType.ELDER_GUARDIAN) {
						spawn(p.getLocation(),EntityType.DROWNED,0,4,2);
					}
					else if (boss.getType()==EntityType.SLIME) {
						spawn(p.getLocation(),EntityType.SPIDER,0,4,1);
					}
				}
				
				if(boss.getType()==EntityType.HUSK) p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,20*3,0,true));
			}
			else if (arrow.getShooter() instanceof LivingEntity) {
				LivingEntity badEnemy = (LivingEntity) arrow.getShooter();
				badEnemy.setHealth(0);
			}
		}
	}
	
	@EventHandler
	public void ColdSkeletonShoot(EntityShootBowEvent e) {
		if (!BossExist) return;
		
		if (e.getEntityType()==EntityType.STRAY 
		&& e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
			Stray boss = (Stray) e.getEntity();
			double r = Math.random();
			
			if ((r*100+1) <= 10) {
				e.getProjectile().setCustomName("EMERALD");
			}
			else if ((r*100+1) <= 11) {
				e.getProjectile().setCustomName("NETHERITE_INGOT CROSSBOW");
			}
			else if ((r*100+1) <= 20) {
				e.getProjectile().setCustomName("ENEMIES_BOSS_ARROW");
				spawn(boss.getLocation(),EntityType.SPIDER,0,6,1);
				spawn(boss.getLocation(),EntityType.HUSK,5,10,1);
			}
			else if ((r*100+1) <= 25) {
				e.getProjectile().setCustomName("GOLD_INGOT CROSSBOW");
			}
			else if ((r*100+1) <= 35) {
				e.getProjectile().setCustomName("IRON_INGOT BOW");
			}
			else if ((r*100+1) <= 100) {
				e.getProjectile().setCustomName("REDSTONE");
			}

		}
	}
	
	public void CreateBoss(Location loc, int lvl) {
		Biome bi = loc.getBlock().getBiome();
		double t = loc.getWorld().getTemperature(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		
		
		if (bi == Biome.COLD_OCEAN || bi == Biome.DEEP_COLD_OCEAN || bi == Biome.DEEP_FROZEN_OCEAN || bi == Biome.LUKEWARM_OCEAN || bi == Biome.DEEP_LUKEWARM_OCEAN
		|| bi == Biome.DEEP_OCEAN || bi == Biome.OCEAN || bi == Biome.WARM_OCEAN) { //Ocean biome
			OceanBoss(loc,lvl);
		}
		else if (t >= 0.95) { //Dry biome
			DryBoss(loc,lvl);
		}
		else if (t <= 0.15) { //Snowy biome
			SnowyBoss(loc,lvl);
		}
		else { //Normal biome		
			NormalBoss(loc,lvl);
		}
	}
	
	public void OceanBoss(Location loc, int lvl) {
		LivingEntity boss = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.ELDER_GUARDIAN);
		boss.setCustomName(ChatColor.BLUE + "" + ChatColor.BOLD + lang.getString("bossMar"));
		
		KeyedBossBar bossBar = Bukkit.createBossBar(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), boss.getUniqueId().toString()),
		ChatColor.BLUE +lang.getString("bossMar"), BarColor.BLUE, BarStyle.SEGMENTED_20, BarFlag.CREATE_FOG,BarFlag.DARKEN_SKY, BarFlag.PLAY_BOSS_MUSIC);
		
		//set health
		double mult=LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(loc.getWorld().getName())).getBossHealtMultiplier();
		double testHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + lvl * mult;
		if (testHealth>2000) {
			double remainingHealth = testHealth-2000;
			double additionalArmour = 0;
			double additionalToughness = 0;
			for (int i=0; i<5;i++) if (remainingHealth>0) {
				additionalArmour +=4;
				additionalToughness ++;
				remainingHealth -= 500;
			}
			
			boss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(additionalArmour);
			boss.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(additionalToughness);
		
			if (remainingHealth>20) OceanBoss(loc, (int) (remainingHealth/mult));
			testHealth=2000;
		}
		final double newHealth = testHealth;
		boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
		boss.setHealth(newHealth);

		
		boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(2);

		boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20* 99999, 0, true));
		
		BukkitRunnable r = new BukkitRunnable(){
			
		    @Override
		     public void run(){
		    	beginRun(bossBar,boss,newHealth);
			    if (boss.isDead()) this.cancel();
		    	//Actual code: 
		       
			    if (true) {
			    	List<Entity> entities = boss.getNearbyEntities(40, 40, 40);
			    	   
			    	for (Entity ent : entities) if (ent instanceof Player) {
			    		Player p = (Player) ent;
			    		p.setFireTicks(p.getMaxFireTicks());
			    	}
			    }
			    
			    if ((Math.random()*1000+1)<=5) {
			    	   List<Entity> entities = boss.getNearbyEntities(50, 50, 50);
			    	   
			    	   int playerCount=0;
			    	   for (Entity ent : entities) if (ent instanceof Player) playerCount++;
			    	   spawnWater(boss.getLocation(),EntityType.DROWNED,0,15,playerCount*2);
			    	   spawnWater(boss.getLocation(),EntityType.GUARDIAN,0,20, (playerCount*2));
			    }
			       
			    if ((Math.random()*1000+1)<=3) {
			    	   List<Entity> entities = boss.getNearbyEntities(50, 50, 50);
			    	   
			    	   for (Entity ent : entities) if (ent instanceof Player) {
			    		   Player p = (Player) ent;
			    		   
			    		   for (ItemStack i : p.getInventory().getContents()) if (i!=null) p.getWorld().dropItem(p.getLocation(), i.clone());
			    		   
			    		   p.getInventory().clear();
			    		   p.setVelocity(new Vector(0,-2,0));
			    	   }
			   }
		     }
		};
		
		r.runTaskTimer(plugin, 0, 10);
		
		boss.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER, r.getTaskId());
	}
	
	public void DryBoss(Location loc, int lvl) {
		LivingEntity boss = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.HUSK);
		boss.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + lang.getString("bossSand"));
		
		KeyedBossBar bossBar = Bukkit.createBossBar(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), boss.getUniqueId().toString()),
		ChatColor.YELLOW + lang.getString("bossSand"), BarColor.YELLOW, BarStyle.SEGMENTED_20, BarFlag.CREATE_FOG, BarFlag.PLAY_BOSS_MUSIC);
		
		//set health
		double mult=LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(loc.getWorld().getName())).getBossHealtMultiplier();
		double testHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + lvl * mult;
		if (testHealth>2000) {
			double remainingHealth = testHealth-2000;
			double additionalArmour = 0;
			double additionalToughness = 0;
			for (int i=0; i<5;i++) if (remainingHealth>0) {
				additionalArmour +=4;
				additionalToughness ++;
				remainingHealth -= 500;
			}
			
			boss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(additionalArmour);
			boss.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(additionalToughness);
		
			if (remainingHealth>20) DryBoss(loc, (int) (remainingHealth/mult));
			testHealth=2000;
		}
		final double newHealth = testHealth;
		boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
		boss.setHealth(newHealth);
		
		ItemStack hand = new ItemStack(Material.WOODEN_AXE);
		hand.addEnchantment(Enchantment.DAMAGE_ALL, 2);
		boss.getEquipment().setItemInMainHand(hand);

		boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20* 99999, 0, true));
		boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20* 99999, 1, true));

		BukkitRunnable r = new BukkitRunnable(){
			
		    @Override
		     public void run(){
		    	beginRun(bossBar,boss,newHealth);
			    if (boss.isDead()) this.cancel();
		    	//Actual code: 
		       
			    if ((Math.random()*1000+1)<=5) {
			    	List<Entity> entities = boss.getNearbyEntities(40, 40, 40);
			    	   
			    	int playerCount=0;
			    	for (Entity ent : entities) if (ent instanceof Player) playerCount++;
			    	spawn(boss.getLocation(),EntityType.CAVE_SPIDER,0,20,(playerCount+1) * 2);
			    	spawn(boss.getLocation(),EntityType.SILVERFISH,0,20, (playerCount+1) * 2);
			    }
			    
			    if ((Math.random()*1000+1)<=4) {
					boolean tp=true;
					List<Entity> near = boss.getNearbyEntities(40, 40, 40);
					
					for (Entity ent : near) {
						if (ent instanceof Player) {
							Location loc = ent.getLocation();
							loc.setDirection(((Player)ent).getEyeLocation().getDirection());
							loc.add(((Player)ent).getEyeLocation().getDirection().normalize().multiply(-2.1));
							((Player) ent).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,20*3,0,true));
							
							if (tp) {
								boss.teleport(loc);
								tp=false;
							}
							else {
								LivingEntity clone = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.HUSK);
								clone.setCustomName(boss.getCustomName());
								clone.getEquipment().setItemInMainHand(boss.getEquipment().getItemInMainHand().clone());
								clone.getEquipment().setArmorContents(boss.getEquipment().getArmorContents().clone());
							}
						}
					}
			    }
			    
			    if ((Math.random()*1000+1)<=4) {
			    	List<Entity> entities = boss.getNearbyEntities(40, 40, 40);
			    	
			    	for (Entity ent : entities) if (ent instanceof LivingEntity) {
			    		
						Vector vec = boss.getLocation().toVector().add(ent.getLocation().toVector().multiply(-1)).multiply(0.6);
						ent.setVelocity(vec);
						
			    		((LivingEntity)ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20*30,2));
			    	}
			    	
			    }
		     }
		};
		
		r.runTaskTimer(plugin, 0, 10);
		
		boss.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER, r.getTaskId());
	}

	public void SnowyBoss(Location loc, int lvl) {
		LivingEntity boss = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.STRAY);
		boss.setCustomName(ChatColor.BOLD +lang.getString("bossIce"));
			
		KeyedBossBar bossBar = Bukkit.createBossBar(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), boss.getUniqueId().toString()),
		lang.getString("bossIce"), BarColor.WHITE, BarStyle.SEGMENTED_20, BarFlag.CREATE_FOG, BarFlag.PLAY_BOSS_MUSIC);
		
		//set health
		double mult=LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(loc.getWorld().getName())).getBossHealtMultiplier();
		double testHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + lvl * mult;
		if (testHealth>2000) {
			double remainingHealth = testHealth-2000;
			double additionalArmour = 0;
			double additionalToughness = 0;
			for (int i=0; i<5;i++) if (remainingHealth>0) {
				additionalArmour +=4;
				additionalToughness ++;
				remainingHealth -= 500;
			}
			
			boss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(additionalArmour);
			boss.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(additionalToughness);
		
			if (remainingHealth>20) SnowyBoss(loc, (int) (remainingHealth/mult));
			testHealth=2000;
		}
		final double newHealth = testHealth;
		boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
		boss.setHealth(newHealth);
		
		ItemStack bow= new ItemStack(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_DAMAGE, 4);
		bow.addEnchantment(Enchantment.DURABILITY, 3);
		bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		boss.getEquipment().setItemInMainHand(bow);
		
		boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20* 99999, 0, true));

		BukkitRunnable r = new BukkitRunnable(){
			
		    @Override
		     public void run(){
			    if (boss.isDead()) this.cancel();
		    	beginRun(bossBar,boss,newHealth);
		     }
		};
		
		r.runTaskTimer(plugin, 0, 10);
		
		boss.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER, r.getTaskId());
	}
	
	public void NormalBoss(Location loc, int lvl) {
		LivingEntity boss = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
		boss.setCustomName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD +lang.getString("bossNature"));
		
		Slime sl = (Slime) boss;
		sl.setSize(8);
		
		KeyedBossBar bossBar = Bukkit.createBossBar(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), boss.getUniqueId().toString()),
		ChatColor.GREEN + lang.getString("bossNature"), BarColor.GREEN, BarStyle.SEGMENTED_20, BarFlag.CREATE_FOG, BarFlag.PLAY_BOSS_MUSIC);
		
		//set health
		double mult=LevelProgression.worlds.get(LevelProgression.worldsName.indexOf(loc.getWorld().getName())).getBossHealtMultiplier();
		double testHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + lvl * mult;
		if (testHealth>2000) {
			double remainingHealth = testHealth-2000;
			double additionalArmour = 0;
			double additionalToughness = 0;
			for (int i=0; i<5;i++) if (remainingHealth>0) {
				additionalArmour +=4;
				additionalToughness ++;
				remainingHealth -= 500;
			}
			
			boss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(additionalArmour);
			boss.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(additionalToughness);
		
			if (remainingHealth>20) NormalBoss(loc, (int) (remainingHealth/mult));
			testHealth=2000;
		}
		final double newHealth = testHealth;
		boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
		boss.setHealth(newHealth);
		

		boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
		
		boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20* 99999, 0, true));
		
		BukkitRunnable r = new BukkitRunnable(){
			
		    @Override
		     public void run(){
		       beginRun(bossBar,boss,newHealth);
		       if (boss.isDead()) this.cancel();
		    	//Actual code: 
		       
		       if ((Math.random()*1000+1) <=5) {
		    	   List<Entity> entities = boss.getNearbyEntities(40, 40, 40);
		    	   for (Entity ent : entities) if (!(ent instanceof Item)) ent.setVelocity(new Vector(0,1.5,0));
		       }

		       if ((Math.random()*1000+1)<=6) {
		    	   List<Entity> entities = boss.getNearbyEntities(30, 30, 30);
		    	   
		    	   int playerCount=0;
		    	   for (Entity ent : entities) if (ent instanceof Player) playerCount++;
		    	   spawn(boss.getLocation(),EntityType.WITCH,0,20,1+playerCount);
		    	   spawn(boss.getLocation(),EntityType.PILLAGER,0,20, 2+playerCount);
		       }
		       
		       if ((Math.random()*1000+1)<=5) {
		    	   List<Entity> entities = boss.getNearbyEntities(30, 30, 30);
		    	   
		    	   for (Entity ent : entities) {
		    		   if (ent instanceof LivingEntity) ent.teleport(ent.getLocation().add(0,-2,0));
		    	   }
		       }
		       
		     }
		};
		
		r.runTaskTimer(plugin, 0, 10);
		
		boss.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER, r.getTaskId());
	}
	
	public void beginRun(KeyedBossBar bossBar, LivingEntity boss,double newHealth) {
			if (!boss.isDead()) BossExist = true;
			else BossExist=false;
			
			bossBar.setProgress(boss.getHealth()/newHealth);
	       
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (boss.isDead()) {
					bossBar.removeAll();
					Bukkit.removeBossBar(bossBar.getKey());
				}
				else if (p.getWorld().equals(boss.getWorld()) && p.getLocation().distance(boss.getLocation()) <= 40) bossBar.addPlayer(p);
				else if (bossBar.getPlayers().contains(p)) bossBar.removePlayer(p);
			}
	       
			if ((int)(Math.random()*1000+1) <= 5) {
				boolean tp=true;
				List<Entity> near = boss.getNearbyEntities(40, 40, 40);
				for (Entity ent : near) {
					if (tp && ent instanceof Player) {
	    			   
						Location loc = ent.getLocation();
						loc.setDirection(((Player)ent).getEyeLocation().getDirection());
						loc.add(((Player)ent).getEyeLocation().getDirection().normalize().multiply(-1.1));
	   				
						boss.teleport(loc);
						tp=false;
					}
				}
			}
	}
	
	public void spawn(Location loc, EntityType type, int rMin, int rMax, int ammount) {
		if (rMin > rMax) return;
		
		int count=1;
		
		for (int i=0; i<500; i++) {
			
			int x = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockX());
			int y = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockY());
			int z = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockZ());
			
			if (loc.distance(new Location(loc.getWorld(),x,y,z)) >= rMin) {
				Block b = loc.getWorld().getBlockAt(x,y,z);
				
				if (b.getType().isSolid() && !b.getWorld().getBlockAt(x, y+1, z).getType().isSolid() && !b.getWorld().getBlockAt(x, y+2, z).getType().isSolid()) {
					if (count<ammount) count++;
					else i=500; //para el for
					
					b.getWorld().spawnEntity(b.getLocation().add(0.5, 1, 0.5), type);
				}
			}
		}
	}
	
	public void spawnWater(Location loc, EntityType type, int rMin, int rMax, int ammount) {
		if (rMin > rMax) return;
		
		int count=1;
		
		for (int i=0; i<500; i++) {
			
			int x = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockX());
			int y = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockY());
			int z = (int) (Math.random() * 2 * rMax - rMax + loc.getBlockZ());
			
			if (loc.distance(new Location(loc.getWorld(),x,y,z)) >= rMin) {
				Block b = loc.getWorld().getBlockAt(x,y,z);
				
				if (b.getType()==Material.WATER && !b.getWorld().getBlockAt(x, y+1, z).getType().isSolid() && !b.getWorld().getBlockAt(x, y+2, z).getType().isSolid()) {
					if (count<ammount) count++;
					else i=500; //para el for
					
					b.getWorld().spawnEntity(b.getLocation().add(0.5, 1, 0.5), type);
				}
			}
		}
	}

}