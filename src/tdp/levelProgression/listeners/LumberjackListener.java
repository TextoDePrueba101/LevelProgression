package tdp.levelProgression.listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class LumberjackListener implements Listener {
	private boolean specialMine=false;
	private FileConfiguration lang= LevelProgression.lang;
	
	public LumberjackListener (LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void wolfsTame(EntityTameEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

		Player p = (Player) e.getOwner();
		
		if (e.getEntityType()==EntityType.WOLF) if (LevelProgression.pgetData(p, "LUMBERJACK")<50 || LevelProgression.pgetData(p, "ADVENTURER")<50) {
			p.sendMessage(ChatColor.RED + lang.getString("lumber1"));
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void armourExtraDamage(EntityDamageByEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		
		if (e.getDamager().getType()==EntityType.PLAYER && (e.getEntity() instanceof LivingEntity)) {
			Player p = (Player) e.getDamager();
			ItemStack mainHand = p.getInventory().getItemInMainHand();

			if (isAxe(mainHand) && LevelProgression.pgetData(p,"LUMBERJACK")>=100) {
				LivingEntity victim = (LivingEntity) e.getEntity();
				
				//armor extra damage based on actual victim life
				ItemStack[] equipment = victim.getEquipment().getArmorContents();
				for (int k=0;k<4;k++) {
					if (equipment[k] != null) {
						LevelProgression.damageItem(victim, equipment[k],(int) (victim.getHealth() * 0.15));
					}
				}
				
				//extra damage based on lumber's left health
				double porcentualHealth= p.getHealth()/p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				double dam=e.getDamage();
				
				dam = dam*(1.5-0.5*porcentualHealth);//dam + dam*0.5(1-porcentualHealth) = dam(1+0.5*(1-porcentualHealth)) = dam (1.5-0.5porcentualHealth)
				e.setDamage(dam);
			}
		}
	}

	
	@EventHandler
	public void interactLimitations(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
				
		Player p = e.getPlayer();
		if (isAxe(p.getInventory().getItemInOffHand())) axeVerifier(p, p.getInventory().getItemInOffHand());
		if (isAxe(p.getInventory().getItemInMainHand())) axeVerifier(p, p.getInventory().getItemInMainHand());	
	}
	
	@EventHandler
	public void playerHabilityActivate(PlayerSwapHandItemsEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();

		if (isAxe(p.getInventory().getItemInMainHand()) && !p.getPose().equals(Pose.SNEAKING)) {
				e.setCancelled(true);		
			
				if (LevelProgression.pgetData(p, "CC1")==0) {
					ItemStack mainHand = p.getInventory().getItemInMainHand();
					p.swingMainHand();
					if (LevelProgression.pgetData(p,"LUMBERJACK") >= 60 && mainHand.getType() == Material.WOODEN_AXE) {
						LevelProgression.psetData(p,"CC1", 8);
						Entity ent = LevelProgression.getTargetEntity(p);
						if (ent instanceof LivingEntity && p.getLocation().distance(ent.getLocation())<20) {
							LevelProgression.damageItem(p, mainHand, 1);
							LivingEntity ent1 = ((LivingEntity)ent);
							
							ent1.damage(4,p);
							ent1.setVelocity(ent1.getVelocity().multiply(-1.2));
							ent1.teleport(ent1.getLocation().setDirection(ent1.getEyeLocation().getDirection().multiply(-1)));
						}
					}
					else if (LevelProgression.pgetData(p,"LUMBERJACK") >= 70 && mainHand.getType() == Material.STONE_AXE) {
						LevelProgression.psetData(p,"CC1", 40);
						
						Collection<Entity> entities = p.getWorld().getNearbyEntities(p.getLocation(),4,2,4);
						entities.remove(p);
						
						for (Entity ent : entities) {
							if (ent instanceof LivingEntity) {
								LevelProgression.damageItem(p, mainHand, 5);
								
								LivingEntity ent1 = (LivingEntity) ent;
								
								p.attack(ent1);
								Block B = ent1.getWorld().getBlockAt(ent1.getLocation().add(0,-1,0));
								if (B.getType().isSolid() && B.getType()!=Material.BEDROCK && !B.getType().isInteractable() && !B.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK) ) p.breakBlock(B);
								B = ent1.getWorld().getBlockAt(ent1.getLocation().add(0,-2,0));
								if (B.getType().isSolid() && B.getType()!=Material.BEDROCK && !B.getType().isInteractable() && !B.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK) ) p.breakBlock(B);
								
								ent1.teleport(ent1.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
								ent1.setVelocity(new Vector(0,-3,0));
								
								ItemStack item = ent1.getEquipment().getBoots();
								LevelProgression.damageItem(ent1, item, 10);	
							}
						}
					}
					else if (LevelProgression.pgetData(p,"LUMBERJACK") >= 95 && mainHand.getType() == Material.GOLDEN_AXE) {
						LevelProgression.damageItem(p,mainHand,11);
						
						Entity ent = LevelProgression.getTargetEntity(p);
						
						if (ent instanceof LivingEntity) {
							LivingEntity LivEnt = (LivingEntity) ent;
							ItemStack[] items = LivEnt.getEquipment().getArmorContents();
												
							if (ent instanceof Player) {
								LevelProgression.psetData(p,"CC1", 200);
								((Player)ent).playSound(ent.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1.0F);
								ent.sendMessage(ChatColor.GOLD +lang.getString("lumber2"));
								
								int r = (int) (Math.random()*4);
								
								ent.getWorld().dropItem(ent.getLocation(),items[r].clone());
								items[r].setAmount(0);
								LevelProgression.damageItem(p,mainHand,11);
							}
							else {
								LevelProgression.psetData(p,"CC1", 10);
								for (int r=0;r<4;r++) {
									ent.getWorld().dropItem(ent.getLocation(),items[r].clone());
									items[r].setAmount(0);
								}
								LevelProgression.damageItem(p,mainHand,33);
							}
							
							LivEnt.getEquipment().setArmorContents(items);
							LivEnt.damage(1); 
						}
					}
					else if (LevelProgression.pgetData(p,"LUMBERJACK") >= 160 && mainHand.getType() == Material.IRON_AXE) {
						LevelProgression.psetData(p,"CC1", 100);
						p.playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 5.0F, 1.0F);
						
						Entity ent = LevelProgression.getTargetEntity(p);
						if (ent==null);
						else if (ent.getType() == EntityType.CREEPER) {
							LevelProgression.damageItem(p,mainHand,258);
							LivingEntity ent1 = (LivingEntity) ent;
							
							ent1.setHealth(0);
							ent1.getWorld().dropItem(ent1.getLocation(), new ItemStack(Material.CREEPER_HEAD));
						}
						else if (ent.getType() == EntityType.ZOMBIE) {
							LevelProgression.damageItem(p,mainHand,258);
							LivingEntity ent1 = (LivingEntity) ent;
							
							ent1.setHealth(0);
							ent1.getWorld().dropItem(ent1.getLocation(), new ItemStack(Material.ZOMBIE_HEAD));
						}
						else if (ent.getType() == EntityType.SKELETON) {
							LevelProgression.damageItem(p,mainHand,258);
							LivingEntity ent1 = (LivingEntity) ent;
							
							ent1.setHealth(0);
							ent1.getWorld().dropItem(ent1.getLocation(), new ItemStack(Material.SKELETON_SKULL));
						}
						else if (ent.getType() == EntityType.WITHER_SKELETON) {
							LevelProgression.damageItem(p,mainHand,258);
							LivingEntity ent1 = (LivingEntity) ent;
							
							ent1.setHealth(0);
							ent1.getWorld().dropItem(ent1.getLocation(), new ItemStack(Material.WITHER_SKELETON_SKULL));
						}
						else if (ent.getType() == EntityType.ENDER_DRAGON) {
							LevelProgression.damageItem(p,mainHand,258);
							
							p.damage(28);
							p.sendMessage(ChatColor.GOLD + lang.getString("lumber3"));
						}
						else if (ent.getType() == EntityType.PLAYER) {
							LevelProgression.damageItem(p,mainHand,258);
							LivingEntity ent1 = (LivingEntity) ent;
							
							LevelProgression.damageItem(ent1, ent1.getEquipment().getHelmet(), 220);
							((Player) ent1).playSound(ent1.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 5.0F, 1.0F);
						}
						else if (ent instanceof LivingEntity) {
							LevelProgression.damageItem(p,mainHand,258);
							LivingEntity ent1 = (LivingEntity) ent;
							
							LevelProgression.damageItem(ent1, ent1.getEquipment().getHelmet(), 600);
							ent1.damage(100);
						}

					}
					else if (LevelProgression.pgetData(p,"LUMBERJACK") >= 200 && mainHand.getType() == Material.DIAMOND_AXE) {
						LevelProgression.psetData(p,"CC1", 40); //40
						LevelProgression.damageItem(p,mainHand,20);
						
						Location loc = new Location(p.getWorld(), p.getLocation().getBlockX(),p.getLocation().getBlockY(),p.getLocation().getBlockZ());
						
						p.getWorld().playEffect(p.getLocation(), Effect.END_GATEWAY_SPAWN, 0);
						
						Location clone = loc.clone();
						clone.add(0,-1,0);
						
						int r=3;
						
						for (int y=-2;y<=0;y++) {
							for (int x=-r;x<=r;x++) {
								for (int z=-r;z<=r;z++) {
									Block B = p.getWorld().getBlockAt(
											(int) loc.getX() + x,
											(int) loc.getY() + y,
											(int) loc.getZ() + z);
									
									if (B.getType().isSolid() && !B.getType().isInteractable() && !B.getType().equals(Material.BEDROCK) 
									&& !B.getType().isInteractable() && !B.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK)  && !B.getLocation().equals(clone)) {
									
										BlockData b = B.getBlockData();
										B.setType(Material.AIR);
										FallingBlock fB = B.getWorld().spawnFallingBlock(B.getLocation().add(0.5,0,0.5), b);
										
										fB.setVelocity(new Vector(0,0.7,0));
										fB.setHurtEntities(true);
										fB.setDropItem(false);
									}
								}
							}
						}
						
					}
					else if (LevelProgression.pgetData(p,"LUMBERJACK") >= 300 && mainHand.getType() == Material.NETHERITE_AXE) {
						double finalDamage=0;
						LevelProgression.psetData(p,"CC1", 25); //20
						
						Collection<Entity> entities = p.getWorld().getNearbyEntities(p.getLocation(),8,3,8);
						entities.remove(p);
						
						for (Entity ent : entities) {
							if (ent instanceof LivingEntity) {
								LevelProgression.damageItem(p, mainHand, 10);
								LivingEntity ent1 = (LivingEntity) ent;
								
								//monster attacks player
								if (ent1.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null && ent1.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue()>0) {
									finalDamage+=ent1.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
								}
								
								//player attacks back
								if (!p.isDead() && ent1.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!=null) {
									ent1.damage(p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue(),p);
								}
								
								Vector vec = p.getLocation().toVector().add(ent1.getLocation().toVector().multiply(-1)).multiply(0.4);
								ent1.setVelocity(vec);
							}
						}
						
						p.damage(finalDamage);
					}	
					
					if (LevelProgression.pgetData(p, "LUMBERJACK")>=100 && p.getInventory().getItemInOffHand()!=null 
					&& p.getInventory().getItemInOffHand().getItemMeta() != null
					&& p.getInventory().getItemInOffHand().getItemMeta().getLocalizedName().equals("STRONG_BONEMEAL")) {
						
						LevelProgression.psetData(p, "CC1", (LevelProgression.pgetData(p, "CC1")/4));
						ItemStack boneMeal = p.getInventory().getItemInOffHand();
						boneMeal.setAmount(boneMeal.getAmount()-1);
					}
				}
		}
	}
	
	@EventHandler
	public void miningInteractions(BlockBreakEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		Block b = e.getBlock();
		
		if (p.getInventory().getItemInOffHand().getType()==Material.BONE_MEAL && LevelProgression.pgetData(p,"LUMBERJACK")>= 100 && p.getInventory().getItemInOffHand().getItemMeta().getLocalizedName().equals("STRONG_BONEMEAL")) {				
			Material m = b.getType();
			
			if (m==Material.ACACIA_LOG || m==Material.BIRCH_LOG || m==Material.DARK_OAK_LOG || m==Material.JUNGLE_LOG 
			|| m==Material.OAK_LOG || m==Material.SPRUCE_LOG) {
				b.getWorld().dropItem(b.getLocation(), new ItemStack(b.getType(),5));
				p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
			}
		}

		if (!specialMine && isAxe(p.getInventory().getItemInOffHand())) {
			ItemStack offHand = p.getInventory().getItemInOffHand();
			specialMine=true;
			
			if (offHand.getType()==Material.WOODEN_AXE && LevelProgression.pgetData(p,"LUMBERJACK")>= 3) {
				LevelProgression.damageItem(p, offHand, 1);
				
				if (b.getType()==Material.ACACIA_LEAVES) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.ACACIA_SAPLING));
				}
				else if (b.getType()==Material.BIRCH_LEAVES) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.BIRCH_SAPLING));
				}
				else if (b.getType()==Material.DARK_OAK_LEAVES) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.DARK_OAK_SAPLING));
				}
				else if (b.getType()==Material.JUNGLE_LEAVES) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.JUNGLE_SAPLING));
				}
				else if (b.getType()==Material.OAK_LEAVES) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.OAK_SAPLING));
				}
				else if (b.getType()==Material.SPRUCE_LEAVES) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.SPRUCE_SAPLING));
				}
			}
			else if (offHand.getType()==Material.STONE_AXE && LevelProgression.pgetData(p,"LUMBERJACK")>= 50) {
				LevelProgression.damageItem(p, offHand, 1);
				Location loc=b.getLocation();
				
				int r=3;
				
				for (int x=-r;x<=r;x++) {
					for (int y=-r;y<=r;y++) {
						for (int z=-r;z<=r;z++) {
							Block B = b.getWorld().getBlockAt(
									(int) loc.getX() + x,
									(int) loc.getY() + y,
									(int) loc.getZ() + z);
							
							if (!B.getType().isOccluding() && B.getType().isSolid()) p.breakBlock(B);
						}
	
					}					
				}
				
			}
			else if (offHand.getType()==Material.GOLDEN_AXE && LevelProgression.pgetData(p,"LUMBERJACK")>= 140) {
				LevelProgression.damageItem(p, offHand, 2);
				Material m = b.getType();
				
				if (m==Material.ACACIA_LEAVES || m==Material.BIRCH_LEAVES || m==Material.DARK_OAK_LEAVES || m==Material.JUNGLE_LEAVES 
				|| m==Material.OAK_LEAVES || m==Material.SPRUCE_LEAVES) {
					
					int n = (int) (Math.random() * 10000 + 1);
					if (n<=2) b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.ENCHANTED_GOLDEN_APPLE,1));
					else if (n<=6) b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.GOLDEN_APPLE,1));
					else if (n<=1000) b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.MELON_SEEDS,1));
					else if (n<=3000) b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.BEETROOT_SEEDS,1));
					else if (n<=4000) b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.PUMPKIN_SEEDS,1));
					else if (n<=10000) b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.WHEAT_SEEDS,1));
					else b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.APPLE,1));
				}
				
			}
			else if (offHand.getType()==Material.IRON_AXE && LevelProgression.pgetData(p,"LUMBERJACK")>= 130) {
				LevelProgression.damageItem(p, offHand, 4);
				
				for (int n=0;n<3;n++) {
					if (!p.getTargetBlock(null, 4).getPistonMoveReaction().equals(PistonMoveReaction.BLOCK)) p.breakBlock(p.getTargetBlock(null, 4));
				}
			}
			else if (offHand.getType()==Material.DIAMOND_AXE && LevelProgression.pgetData(p,"LUMBERJACK") >= 230) {
				LevelProgression.damageItem(p, offHand, 4);
				Material m = b.getType();
				
				if (m==Material.ACACIA_LOG || m==Material.BIRCH_LOG || m==Material.DARK_OAK_LOG || m==Material.JUNGLE_LOG 
				|| m==Material.OAK_LOG || m==Material.SPRUCE_LOG) {
					ExperienceOrb exp = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation(), EntityType.EXPERIENCE_ORB);
					
					exp.setExperience(2 +(int) (3 * Math.random()));
				}
			}
			else if (offHand.getType()==Material.NETHERITE_AXE && LevelProgression.pgetData(p,"LUMBERJACK")>= 300) {
				Material m = b.getType();
				Block B=b;
				
				while (m==Material.ACACIA_LOG || m==Material.BIRCH_LOG || m==Material.DARK_OAK_LOG || m==Material.JUNGLE_LOG 
				|| m==Material.OAK_LOG || m==Material.SPRUCE_LOG) {
					p.breakBlock(B);
					B = B.getWorld().getBlockAt(B.getLocation().add(0,1,0));
					m=B.getType();
					LevelProgression.damageItem(p, offHand, 1);
				}
			}

			specialMine=false;
		}	
	}

	
	//COMPROBATIONS==========================================
	public boolean isAxe(ItemStack item) {
		if (item==null) return false;
		else if (item.getType()==Material.WOODEN_AXE || item.getType()==Material.STONE_AXE || item.getType()==Material.GOLDEN_AXE || item.getType()==Material.IRON_AXE 
		|| item.getType()==Material.DIAMOND_AXE || item.getType()==Material.NETHERITE_AXE) return true;
		else return false;
	}
	
	public void axeVerifier(Player p, ItemStack item) {
		if (item.getType()==Material.STONE_AXE && LevelProgression.pgetData(p,"LUMBERJACK") < 5) {
			p.sendMessage(ChatColor.RED +  lang.getString("lumber4"));
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
		else if (item.getType()==Material.GOLDEN_AXE && LevelProgression.pgetData(p,"LUMBERJACK") < 40) {
			p.sendMessage(ChatColor.RED + lang.getString("lumber4"));
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
		else if (item.getType()==Material.IRON_AXE && LevelProgression.pgetData(p,"LUMBERJACK") < 90) {
			p.sendMessage(ChatColor.RED + lang.getString("lumber4"));
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
		else if (item.getType()==Material.DIAMOND_AXE && LevelProgression.pgetData(p,"LUMBERJACK") < 170) {
			p.sendMessage(ChatColor.RED + lang.getString("lumber4"));
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
		else if (item.getType()==Material.NETHERITE_AXE && LevelProgression.pgetData(p,"LUMBERJACK") < 260) {
			p.sendMessage(ChatColor.RED + lang.getString("lumber4"));
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
	
	public void enchVerifier(Player p , ItemStack item) {
		if (item.getItemMeta() != null) {
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <6) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>1 && LevelProgression.pgetData(p, "LUMBERJACK") <7) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>2 && LevelProgression.pgetData(p, "LUMBERJACK") <8) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>3 && LevelProgression.pgetData(p, "LUMBERJACK") <9) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>4 && LevelProgression.pgetData(p, "LUMBERJACK") <10) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <30) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>1 && LevelProgression.pgetData(p, "LUMBERJACK") <80) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>2 && LevelProgression.pgetData(p, "LUMBERJACK") <150) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>3 && LevelProgression.pgetData(p, "LUMBERJACK") <210) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED)>4 && LevelProgression.pgetData(p, "LUMBERJACK") <245) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <60) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)>1 && LevelProgression.pgetData(p, "LUMBERJACK") <120) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)>2 && LevelProgression.pgetData(p, "LUMBERJACK") <265) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <250) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SILK_TOUCH)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <190) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <20) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>1 && LevelProgression.pgetData(p, "LUMBERJACK") <100) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>2 && LevelProgression.pgetData(p, "LUMBERJACK") <145) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>3 && LevelProgression.pgetData(p, "LUMBERJACK") <210) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>4 && LevelProgression.pgetData(p, "LUMBERJACK") <280) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <15) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>1 && LevelProgression.pgetData(p, "LUMBERJACK") <90) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>2 && LevelProgression.pgetData(p, "LUMBERJACK") <180) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>3 && LevelProgression.pgetData(p, "LUMBERJACK") <270) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>4 && LevelProgression.pgetData(p, "LUMBERJACK") <290) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "LUMBERJACK") <50) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "LUMBERJACK") <110) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "LUMBERJACK") <240) enchDenied(p, item);
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
