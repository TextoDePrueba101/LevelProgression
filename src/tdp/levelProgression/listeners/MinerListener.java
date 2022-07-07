package tdp.levelProgression.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class MinerListener implements Listener{
	private LevelProgression plugin;
	private boolean dobleClickProblem=false;
	private int miningDamage=0;
	private FileConfiguration lang = LevelProgression.lang;
	
	public MinerListener(LevelProgression plugin) {
		this.plugin = plugin; 
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void autoReplacePickaxe(PlayerItemBreakEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		ItemStack broke = e.getBrokenItem();
		
		if (p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getItemMeta().getLocalizedName().equals("MINER_HELMET") && LevelProgression.pgetData(p,"MINER")>=100 && isTool(broke)){
			boolean test=true;
			for (ItemStack i : p.getInventory().getStorageContents()) if (test && i!=null && isTool(i) && !i.equals(broke)) {
				test=false;
				broke.setAmount(0);
				p.getInventory().setItemInMainHand(i.clone());
				i.setAmount(0);
			}
		}

	}
	
	@EventHandler
	public void miningInteractions(BlockBreakEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		Block b = e.getBlock();
		
		if (p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getItemMeta().getLocalizedName().equals("MINER_HELMET") 
				&& LevelProgression.pgetData(p,"MINER")>=100 && e.getBlock().getType().isSolid()) {
			boolean test=true;
			ItemStack[] items = p.getInventory().getContents();
			
			for (int n=9;n<items.length;n++) {
				ItemStack i = items[n];
				if (test && n!=40 && i!=null && i.getType()==Material.TORCH && p.getLocation().getBlock().getType().isAir() && p.getLocation().add(0,-1,0).getBlock().getType().isSolid()) {
					test=false;
					i.setAmount(i.getAmount() - 1);
					p.getLocation().getBlock().setType(Material.TORCH);
				}
			}
		}

		if (LevelProgression.pgetData(p, "CC1")==0 && isTool(p.getInventory().getItemInOffHand())) {
			ItemStack offHand = p.getInventory().getItemInOffHand();
			LevelProgression.psetData(p, "CC1", 6);
						
			if (offHand.getType()==Material.WOODEN_PICKAXE && LevelProgression.pgetData(p,"MINER")>= 35) {
				breakHorizonally(e.getBlock(),p,4);
				LevelProgression.damageItem(p, offHand, 1);
			}
			else if (offHand.getType()==Material.STONE_PICKAXE && LevelProgression.pgetData(p,"MINER")>= 120) {
				Block topB = topestBlock(b);
				if (topB!=null) {
					p.teleport(topB.getLocation().add(0.5,1,0.5));
					p.playSound(p.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 5.0F, 1.0F);
					
					LevelProgression.damageItem(p, offHand, miningDamage * 3); 
					miningDamage=0;
				} else {
					p.sendMessage(ChatColor.RED + lang.getString("miner2"));
					LevelProgression.damageItem(p, offHand, 30);
				}
			}
			else if (offHand.getType()==Material.GOLDEN_PICKAXE && LevelProgression.pgetData(p,"MINER")>= 165 && b.getType()!=Material.CHEST && b.getType()!=Material.FURNACE
			&& b.getType()!=Material.HOPPER) {
				//playerJustBroke=true;
				
				for (ItemStack i : b.getDrops(p.getInventory().getItemInMainHand(), p)){ //Item siempre es uno, el for se ejecuta una sola vez
					
					if (i.getType()==Material.RAW_IRON) {
						for (int q=0;q<2;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.IRON_INGOT,60);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.IRON_BLOCK,5);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.RAW_COPPER) {
						for (int q=0;q<4;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.COPPER_INGOT,70);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.COPPER_BLOCK,10);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.RAW_GOLD) {
						for (int q=0;q<10;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.GOLD_INGOT,8);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.GOLD_BLOCK,5);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.DIAMOND) {
						for (int q=0;q<1;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.DIAMOND,90);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.DIAMOND_BLOCK,2);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.EMERALD) {
						for (int q=0;q<10;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.EMERALD,20);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.EMERALD_BLOCK,5);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.REDSTONE && e.getBlock().getType()!=Material.REDSTONE) {
						for (int q=0;q<4;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,3),Material.REDSTONE,90);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.REDSTONE_BLOCK,15);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.LAPIS_LAZULI) {
						for (int q=0;q<4;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,3),Material.LAPIS_LAZULI,80);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,2),Material.LAPIS_BLOCK,15);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.COAL) {
						for (int q=0;q<2;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,2),Material.COAL,95);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
							ItemStack it2= changeDrop(new ItemStack(Material.AIR,1),Material.COAL_BLOCK,50);
							if (it2.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it2);
						}
					}
					else if (i.getType()==Material.QUARTZ) {
						for (int q=0;q<5;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.QUARTZ_BLOCK,30);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
						}
					}
					else if (i.getType()==Material.COBBLESTONE) {
						for (int q=0;q<4;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,1),Material.OBSIDIAN,30);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
						}
					}
					else if (i.getType()==Material.GOLD_NUGGET) {
						for (int q=0;q<3;q++) {
							ItemStack it1= changeDrop(new ItemStack(Material.AIR,2),Material.GOLD_INGOT,30);
							if (it1.getType()!=Material.AIR) b.getWorld().dropItemNaturally(b.getLocation(), it1);
						}
					}

				}
				LevelProgression.damageItem(p, offHand, 17);
			}
			else if (offHand.getType()==Material.IRON_PICKAXE && LevelProgression.pgetData(p,"MINER")>= 125) {
				breakSquare(e.getBlock(),p,1);
				LevelProgression.damageItem(p, offHand, 2);
			}
			else if (offHand.getType()==Material.DIAMOND_PICKAXE && LevelProgression.pgetData(p,"MINER") >= 200) {
				Location loc = b.getLocation();
				for(int x = -4;x <= 4;x++){
				  for(int y = -4;y <= 4;y++){
				    for(int z = -4;z <= 4;z++){
				    	Block posOre = b.getWorld().getBlockAt(
				        (int) loc.getX() + x,
				        (int) loc.getY() + y,
				        (int) loc.getZ() + z);
				    	
				    	if (posOre.getType()==Material.IRON_ORE || posOre.getType()==Material.DEEPSLATE_IRON_ORE || posOre.getType()==Material.COPPER_ORE || posOre.getType()==Material.DEEPSLATE_COPPER_ORE
				    	|| posOre.getType()==Material.GOLD_ORE || posOre.getType()==Material.DEEPSLATE_GOLD_ORE || posOre.getType()==Material.GOLD_ORE || posOre.getType()==Material.LAPIS_ORE 
				    	|| posOre.getType()==Material.DEEPSLATE_LAPIS_ORE || posOre.getType()==Material.DIAMOND_ORE || posOre.getType()==Material.DEEPSLATE_DIAMOND_ORE || posOre.getType()==Material.EMERALD_ORE
				    	|| posOre.getType()==Material.DEEPSLATE_EMERALD_ORE || posOre.getType()==Material.ANCIENT_DEBRIS) {
				    		glowBlock(3,posOre);
				    	}
				    }
				  }
				}
				LevelProgression.damageItem(p, offHand, 2);
			}
			else if (offHand.getType()==Material.NETHERITE_PICKAXE && LevelProgression.pgetData(p,"MINER")>= 290) {
				breakSquare(e.getBlock(),p,2);
				LevelProgression.damageItem(p, offHand, 10);
			}
			else if (offHand.getType()==Material.WOODEN_SHOVEL && LevelProgression.pgetData(p,"MINER")>= 20) {
				e.setCancelled(true);
				Location loc = b.getLocation().add(0,1,0);
				Block aboveB = b.getWorld().getBlockAt(loc);
				
				while (aboveB.getType().hasGravity()) {
					loc.add(0,1,0);
					aboveB = b.getWorld().getBlockAt(loc);
				}

				loc.add(0,-1,0);
				aboveB = b.getWorld().getBlockAt(loc);
				
				while (aboveB.getType().hasGravity() && !aboveB.equals(b)) {
					p.breakBlock(aboveB);
					loc.add(0,-1,0);
					aboveB = b.getWorld().getBlockAt(loc);
				}
				e.setCancelled(false);
				
				LevelProgression.damageItem(p, offHand, 1);
			}
			else if (offHand.getType()==Material.STONE_SHOVEL && LevelProgression.pgetData(p,"MINER")>= 120) {
				Block deepB = deepestBlock(b);
				if (deepB!=null) {
					p.teleport(deepB.getLocation().add(0.5,1,0.5));
					p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 5.0F, 1.0F);
					
					LevelProgression.damageItem(p, offHand, miningDamage * 3); 
					miningDamage=0;
				} else {
					p.sendMessage(ChatColor.RED +lang.getString("miner1"));
					LevelProgression.damageItem(p, offHand, 30);
				}
			}
			else if (offHand.getType()==Material.GOLDEN_SHOVEL && LevelProgression.pgetData(p,"MINER")>= 60 && b.getType()!=Material.CHEST && b.getType()!=Material.FURNACE
					&& b.getType()!=Material.HOPPER) {
				for (ItemStack i : b.getDrops(p.getInventory().getItemInMainHand(), p)){ //Item siempre es uno, el for se ejecuta una sola vez
					
					if (i.getType()==Material.RAW_IRON) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=70)b.setType(changeMaterial(Material.AIR,Material.RAW_IRON_BLOCK,15));
						else b.setType(changeMaterial(Material.AIR,Material.GOLD_ORE,15));
					}
					else if (i.getType()==Material.RAW_COPPER) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=33)b.setType(changeMaterial(Material.AIR,Material.RAW_COPPER_BLOCK,15));
						else if (n1<=66) b.setType(changeMaterial(Material.AIR,Material.REDSTONE_ORE,15));
						else b.setType(changeMaterial(Material.AIR,Material.LAPIS_ORE,10));
					}
					else if (i.getType()==Material.RAW_GOLD) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=50)b.setType(changeMaterial(Material.AIR,Material.RAW_GOLD_BLOCK,3));
						else b.setType(changeMaterial(Material.AIR,Material.EMERALD_ORE,5));
					}
					else if (i.getType()==Material.DIAMOND) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=80)b.setType(changeMaterial(Material.AIR,Material.EMERALD_ORE,30));
						else b.setType(changeMaterial(Material.AIR,Material.ANCIENT_DEBRIS,5));
					}
					else if (i.getType()==Material.EMERALD) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						b.setType(changeMaterial(Material.AIR,Material.DIAMOND_ORE,5));
					}
					else if (i.getType()==Material.REDSTONE && e.getBlock().getType()!=Material.REDSTONE) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=50)b.setType(changeMaterial(Material.AIR,Material.GLOWSTONE,20));
						else b.setType(changeMaterial(Material.AIR,Material.LAPIS_ORE,10));
					}
					else if (i.getType()==Material.LAPIS_LAZULI) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=50)b.setType(changeMaterial(Material.AIR,Material.EMERALD_ORE,15));
						else b.setType(changeMaterial(Material.AIR,Material.REDSTONE_ORE,30));
					}
					else if (i.getType()==Material.COAL) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						double n1 = Math.random() * 100 +1;
						if (n1<=50)b.setType(changeMaterial(Material.AIR,Material.COPPER_ORE,15));
						else b.setType(changeMaterial(Material.AIR,Material.IRON_ORE,10));
					}
					else if (i.getType()==Material.QUARTZ) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						b.setType(changeMaterial(Material.AIR,Material.NETHER_GOLD_ORE,10));
					}
					else if (i.getType()==Material.COBBLESTONE || i.getType()==Material.COBBLED_DEEPSLATE) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						b.setType(changeMaterial(Material.AIR,Material.COAL_ORE,8));
					}
					else if (i.getType()==Material.GOLD_NUGGET) {
						e.setCancelled(true);
						b.breakNaturally(p.getInventory().getItemInMainHand());
						
						b.setType(changeMaterial(Material.AIR,Material.NETHER_QUARTZ_ORE,15));
					}
				}
				LevelProgression.damageItem(p, offHand, 1);
			}
			else if (offHand.getType()==Material.IRON_SHOVEL && LevelProgression.pgetData(p,"MINER")>= 75) {
				int r=1;
				
				for(int x = -r;x <= r;x++){
					for(int y = -r;y <= r;y++){
						for(int z = -r;z <= r;z++){
							Block B = p.getWorld().getBlockAt(
							(int) b.getX() + x,
							(int) b.getY() + y,
							(int) b.getZ() + z);
							if (!B.equals(b) && B.getType()!=Material.BEDROCK && !B.getType().isInteractable() && !B.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK)  && B.getType()!=Material.WATER && B.getType()!=Material.LAVA && B.getType()!=Material.AIR && !B.getType().isInteractable()) {
								FallingBlock fb = b.getWorld().spawnFallingBlock(B.getLocation().add(0.5,0,0.5), B.getBlockData());
								B.setType(Material.AIR);
								fb.setDropItem(false);
							}
						}
					}
				}
				
				LevelProgression.damageItem(p, offHand, 10);
			}
			else if (offHand.getType()==Material.DIAMOND_SHOVEL && LevelProgression.pgetData(p,"MINER")>= 155) {
				int r=1;
				
				for(int x = -r;x <= r;x++){
					for(int y = -r;y <= r;y++){
						for(int z = -r;z <= r;z++){
							Block B = p.getWorld().getBlockAt(
							(int) b.getX() + x,
							(int) b.getY() + y,
							(int) b.getZ() + z);
							
							if (!B.equals(b) && B.getType()!=Material.BEDROCK && !B.getType().isInteractable() && B.getType()!=Material.WATER && B.getType()!=Material.LAVA && B.getType()!=Material.AIR) p.breakBlock(B);
						}
					}
				}
				LevelProgression.damageItem(p, offHand, 8);
			}
			else if (offHand.getType()==Material.NETHERITE_SHOVEL && LevelProgression.pgetData(p,"MINER")>= 300) {
				int r=2;
				
				for(int x = -r;x <= r;x++){
					for(int y = -r;y <= r;y++){
						for(int z = -r;z <= r;z++){
							Block B = p.getWorld().getBlockAt(
							(int) b.getX() + x,
							(int) b.getY() + y,
							(int) b.getZ() + z);
							
							if (!B.equals(b) && B.getType()!=Material.BEDROCK && B.getType()!=Material.WATER && B.getType()!=Material.LAVA && B.getType()!=Material.AIR 
							&& !B.getType().isInteractable() && !B.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK)) {
								Vector vecBb = B.getLocation().add(b.getLocation().multiply(-1)).toVector().normalize(); //de b con sentido a B
								Vector vecBp = p.getLocation().add(B.getLocation().multiply(-1)).toVector().normalize(); //de B con sentido a p
								Vector vec = vecBb.add(vecBp).normalize().multiply(0.8);
								
								FallingBlock fB = b.getWorld().spawnFallingBlock(B.getLocation().add(0.5,0,0.5), B.getBlockData());
								fB.setVelocity(vec);
								fB.setDropItem(false);
								B.setType(Material.AIR);
							}
						}
					}
				}
				LevelProgression.damageItem(p, offHand, 20);	
			}
			LevelProgression.psetData(p, "CC1", 0);
		}	
	}
	
	@EventHandler
	public void interactLimitations(PlayerInteractEvent e) {
		if (dobleClickProblem) {
			dobleClickProblem=!dobleClickProblem;
			return;
		}
		
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		if (isTool(p.getInventory().getItemInOffHand())) toolVerifier(p, p.getInventory().getItemInOffHand());
		if (isTool(p.getInventory().getItemInMainHand())) toolVerifier(p, p.getInventory().getItemInMainHand());		
	}
		
	public Block deepestBlock(Block b) {
		Block deepB = b.getWorld().getBlockAt(b.getLocation().add(0,-1,0));
		Location loc = deepB.getLocation();
		
		while (deepB.getType().isSolid() && deepB.getType()!=Material.BEDROCK) {
			loc.add(0,-1,0);
			deepB = b.getWorld().getBlockAt(loc);
			miningDamage++;
			//p.sendMessage("While 1 y: " + loc.getY());
		}
		
		if (deepB.getType()==Material.BEDROCK) return null;
		else {
			while (!deepB.getType().isSolid() && deepB.getType()!=Material.LAVA && loc.getY()>=-64) {
				loc.add(0,-1,0);
				deepB = b.getWorld().getBlockAt(loc);
				miningDamage++;
				//p.sendMessage("While 2 y: " + loc.getY());
			}
			if (deepB.getType()==Material.LAVA) return null;
			else return deepB;
		}
		
	}
	
	public Block topestBlock(Block b) {
		Block topB = b.getWorld().getBlockAt(b.getLocation().add(0,1,0));
		Location loc = topB.getLocation();
		
		while (topB.getType().isSolid() && topB.getType()!=Material.BEDROCK) {
			loc.add(0,1,0);
			topB = b.getWorld().getBlockAt(loc);
			miningDamage++;
			//p.sendMessage("While 1 y: " + loc.getY());
		}
		if (topB.getType()==Material.BEDROCK) return null;
		else return topB;
	}
	
	public ItemStack changeDrop(ItemStack originalDrop, Material newDrop,int probability) { //probabiliy is in porcentage
			double n1 = Math.random() * 100 +1;
			if ((int) n1 <= probability) return new ItemStack(newDrop, originalDrop.getAmount());
			else return originalDrop;
	}
	
	public Material changeMaterial(Material originalBlock, Material newBlock,int probability) { //probabiliy is in porcentage
		double n1 = Math.random() * 100 +1;
		if ((int) n1 <= probability) return newBlock;
		else return originalBlock;
	}

	public boolean isTool(ItemStack item) {
		if (item==null) return false;
		else if (item.getType()==Material.DIAMOND_PICKAXE || item.getType()==Material.GOLDEN_PICKAXE || item.getType()==Material.IRON_PICKAXE 
		|| item.getType()==Material.NETHERITE_PICKAXE || item.getType()==Material.STONE_PICKAXE || item.getType()==Material.WOODEN_PICKAXE  
		|| item.getType()==Material.DIAMOND_SHOVEL || item.getType()==Material.GOLDEN_SHOVEL || item.getType()==Material.IRON_SHOVEL 
		|| item.getType()==Material.NETHERITE_SHOVEL || item.getType()==Material.STONE_SHOVEL || item.getType()==Material.WOODEN_SHOVEL) return true;
		else return false;
	}
	
	public void toolVerifier(Player p, ItemStack item) {
		if (item.getType()==Material.STONE_PICKAXE && LevelProgression.pgetData(p,"MINER") < 10) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} 
		else if (item.getType()==Material.GOLDEN_PICKAXE && LevelProgression.pgetData(p,"MINER") < 50) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		}
		else if (item.getType()==Material.IRON_PICKAXE && LevelProgression.pgetData(p,"MINER") < 100) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		}
		else if (item.getType()==Material.DIAMOND_PICKAXE && LevelProgression.pgetData(p,"MINER") < 190) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		}
		else if (item.getType()==Material.NETHERITE_PICKAXE && LevelProgression.pgetData(p,"MINER") < 250) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} 
		else if (item.getType()==Material.STONE_SHOVEL && LevelProgression.pgetData(p,"MINER") < 5) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} 
		else if (item.getType()==Material.GOLDEN_SHOVEL && LevelProgression.pgetData(p,"MINER") < 45) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		}
		else if (item.getType()==Material.IRON_SHOVEL && LevelProgression.pgetData(p,"MINER") < 70) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		}
		else if (item.getType()==Material.DIAMOND_SHOVEL && LevelProgression.pgetData(p,"MINER") < 150) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		}
		else if (item.getType()==Material.NETHERITE_SHOVEL && LevelProgression.pgetData(p,"MINER") < 220) {
			p.sendMessage(ChatColor.RED + lang.getString("miner3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} else {
			enchVerifier(p,item);
		}
	}

	public void breakSquare(Block block, Player p,int r) {
		Vector dir = p.getEyeLocation().getDirection();
		double X = Math.abs(dir.getX());
		double Y = Math.abs(dir.getY());
		double Z = Math.abs(dir.getZ());
		
		Location b = block.getLocation();
		
		if (Z >= X && Z >= Y) {
			
			for(int x = -r;x <= r;x++){
				for(int y = -r;y <= r;y++){
					Block B = p.getWorld().getBlockAt((int) b.getX() + x,(int) b.getY() + y,(int) b.getZ());
			        if (B.getType()!=Material.BEDROCK && B.getType()!=Material.END_GATEWAY && !B.getType().isInteractable() && B.getType()!=Material.WATER && B.getType()!=Material.LAVA) p.breakBlock(B);
				}
			}
		} else 	if (X >= Z && X >= Y) {
			
			for(int z = -r;z <= r;z++){
				for(int y = -r;y <= r;y++){
			        Block B=p.getWorld().getBlockAt((int) b.getX(),(int) b.getY() + y,(int) b.getZ() + z);
			        if (B.getType()!=Material.BEDROCK && B.getType()!=Material.END_GATEWAY && !B.getType().isInteractable() && B.getType()!=Material.WATER && B.getType()!=Material.LAVA) p.breakBlock(B);
				}
			}
		} else 	if (Y >= X && Y >= Z) {
			
			for(int x = -r;x <= r;x++){
				for(int z = -r;z <= r;z++){
					Block B = p.getWorld().getBlockAt((int) b.getX() + x,(int) b.getY(),(int) b.getZ() + z);
					if (B.getType()!=Material.BEDROCK && B.getType()!=Material.END_GATEWAY && !B.getType().isInteractable() && B.getType()!=Material.WATER && B.getType()!=Material.LAVA) p.breakBlock(B);
				}
			}
		}

		dobleClickProblem=true;
	}
	
	public void breakHorizonally(Block block, Player p, int r) {
		Vector dir = p.getEyeLocation().getDirection();
		double X = Math.abs(dir.getX());
		double Y = Math.abs(dir.getY());
		double Z = Math.abs(dir.getZ());
		
		Location breakLoc = block.getLocation().clone();
		
		Vector unitLoc = new Vector(0,0,0);
		
		if (X >= Z && X >= Y) {
			unitLoc.setX(dir.getX());
			unitLoc.normalize();
		}
		else if (Y >= X && Y >= Z) {
			unitLoc.setY(dir.getY());
			unitLoc.normalize();
		}
		else if (Z >= X && Z >= Y) {
			unitLoc.setZ(dir.getZ());
			unitLoc.normalize();	
		}

		for (int i=0;i<r;i++) {
			Block B = p.getWorld().getBlockAt(breakLoc);
			if (B.getType()!=Material.BEDROCK && B.getType()!=Material.END_GATEWAY && !B.getType().isInteractable() && B.getType()!=Material.WATER && B.getType()!=Material.LAVA) p.breakBlock(B);
			breakLoc.add(unitLoc.clone());
		}

		dobleClickProblem=true;
	}
	
	public void glowBlock(int duration, Block b) {
		Location loc = b.getLocation().add(0.5, 0, 0.5);
		Location spawn = loc.clone();
		spawn.setY(-65);
		
		MagmaCube glowing = (MagmaCube) b.getWorld().spawnEntity(loc, EntityType.MAGMA_CUBE);
		glowing.setSize(2);
		glowing.setInvisible(true);
		glowing.setInvulnerable(true);
		glowing.setAI(false);
		glowing.setCollidable(true);
		glowing.setSilent(true);
		glowing.setGlowing(true);
		glowing.setLootTable(null);
		
		BukkitRunnable r = new BukkitRunnable(){
			
		    @Override
		     public void run(){
		    	glowing.setGlowing(false);
		    	glowing.setSize(0);
		    	glowing.setInvulnerable(false);
		    	glowing.teleport(new Location(glowing.getWorld(),glowing.getLocation().getX(),-69,glowing.getLocation().getZ()));
		    	glowing.setHealth(0);
		        this.cancel(); //Cancels the timer
		     }
		};

		r.runTaskTimer(plugin, 20 * duration, 0);
		
		glowing.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,20*999,0,true));
    }

	public void enchVerifier(Player p , ItemStack item) {
		if (item.getItemMeta() != null) {
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>0 && LevelProgression.pgetData(p, "MINER") <15) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>1 && LevelProgression.pgetData(p, "MINER") <25) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>2 && LevelProgression.pgetData(p, "MINER") <80) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>3 && LevelProgression.pgetData(p, "MINER") <160) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>4 && LevelProgression.pgetData(p, "MINER") <210) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "MINER") <260) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SILK_TOUCH)>0 && LevelProgression.pgetData(p, "MINER") <180) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)>0 && LevelProgression.pgetData(p, "MINER") <90) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)>1 && LevelProgression.pgetData(p, "MINER") <170) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)>2 && LevelProgression.pgetData(p, "MINER") <240) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "MINER") <30) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "MINER") <110) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "MINER") <230) enchDenied(p, item);
		}
	}
	
	public void enchDenied(Player p , ItemStack item) {
		ItemStack itemCloned = item.clone();
		item.setAmount(0);
		p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
		p.sendMessage(ChatColor.RED + lang.getString("enchDenied"));
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
	}


}
