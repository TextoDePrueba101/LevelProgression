package tdp.levelProgression.listeners;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class ArmorListener implements Listener{
	private static FileConfiguration lang = LevelProgression.lang;
	
	public ArmorListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void ArmourHealth(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		Player p = e.getPlayer();
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) 
			IncreaseLife((Player) e.getPlayer(), e.getPlayer().getInventory().getArmorContents());
		
	
		if (e.getItem()!=null && e.getItem().getType() == Material.SHIELD) {
			if (LevelProgression.pgetData(e.getPlayer(), "SHIELDMAN") <10) {
				e.getPlayer().sendMessage(ChatColor.RED +lang.getString("shield1"));
				
				if (!LevelProgression.breakItemWhenDenied) {
					ItemStack itemCloned = e.getItem().clone();
					e.getItem().setAmount(0);
					p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
					p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				}
				else {
					e.getItem().setAmount(0);
					e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);								
				}
			} else {
				enchVerifier(e.getPlayer(),e.getItem());
			}
		}
		
		if (e.getItem() != null && e.getItem().getType() == Material.SCUTE && e.getAction() == Action.RIGHT_CLICK_BLOCK 
		&& LevelProgression.pgetData(p, "SHIELDMAN")>220 && p.getPose() == Pose.SNEAKING) { //Blindness to all 
			int minExp=6;	
			if (p.getLevel() >= minExp) {
					
					Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 7, 7, 7);
					
					for (Entity entities : ent) {
						if (entities instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) entities;
							entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 400, 0, false));
							entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 1, false));
						}
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 4, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 1, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 3, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 0, false));
					p.playSound(p.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5.0F, 1F);
					int n = p.getLevel() - minExp;
					p.setLevel(n);
					
				} else {
					p.sendMessage(ChatColor.RED +lang.getString("shield2") + minExp + lang.getString("shield22"));
				}
			}
			
				
	}
	
	@EventHandler
	public void IsProtected (EntityDamageEvent e) { //protection mechanic
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
		
		LivingEntity protectedEntity = (LivingEntity) e.getEntity();
		
		//===
		if (hasDef(protectedEntity,false)) {
			
			if (getDef(protectedEntity) != null) {
				Player defensor = (Player) getDef(protectedEntity);
				
				e.setCancelled(true);
				try {
					defensor.damage(e.getFinalDamage());
				}
				catch (Exception ex) {
					e.setCancelled(false);
				}

				if (protectedEntity instanceof Player && e.isCancelled()) {
					Player p = (Player) protectedEntity;
					p.playSound(protectedEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 5.0F, 1F);
					p.resetTitle();
					ChatColor color = ChatColor.RED;
					double lifePorcentage=(defensor.getHealth()/defensor.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					
					if (lifePorcentage >= 0.8) color=ChatColor.GREEN;
					else if (lifePorcentage >= 0.5) color=ChatColor.YELLOW;
					else if (lifePorcentage >= 0.2) color=ChatColor.GOLD;
					else if (lifePorcentage >= 0.1) color=ChatColor.RED;
					else color=ChatColor.DARK_RED;
					
					p.sendTitle(color + " ", color + "-" + (int) e.getDamage() + "-", 1, 14, 1);
				}
			} 
			else {
				removeDef(protectedEntity,true);
			}
		
		}
		//===

	}
	
	@EventHandler
	public void IsProtected (EntityDamageByEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
			
		LivingEntity shieldMan = (LivingEntity) e.getEntity();
		ItemStack shield = new ItemStack(Material.AIR);
		
		if (shieldMan.getEquipment().getItemInMainHand() != null && shieldMan.getEquipment().getItemInMainHand().getType()==Material.SHIELD) shield = shieldMan.getEquipment().getItemInMainHand();
		else if (shieldMan.getEquipment().getItemInOffHand() != null && shieldMan.getEquipment().getItemInOffHand().getType()==Material.SHIELD) shield = shieldMan.getEquipment().getItemInOffHand();
		
		if (shield.getType()==Material.SHIELD && shield.getItemMeta().getLocalizedName().equals("THRONS_SHIELD") && !e.getEntity().equals(e.getDamager()) 
		&& e.getEntity() instanceof Player && !((Player)e.getEntity()).isBlocking()){
	    	Player p = (Player) e.getEntity();
	    	
			if (e.getDamager() instanceof Player) {
				((Player)e.getDamager()).damage(e.getDamage()*0.51,p);
				LevelProgression.damageItem(shield,1);
			}
			else if (e.getDamager() instanceof LivingEntity) {
				((LivingEntity)e.getDamager()).damage(e.getDamage()*2.5,p);	
				LevelProgression.damageItem(shield,1);
			}		
		}
	}
	
	public void ConectionEndAfterRespawn (PlayerRespawnEvent e) { //protection mechanic
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		Player p = e.getPlayer();
		
		if (hasDef(p,false)) {
			removePrr(getDef(p),false);
			removeDef(p, true);
		}
		
		
		if (hasPrr(p,true)) {
			removeDef(getPrr(p),true);
			removePrr(p,true);
		}
		else if (hasPrr(p,false)) {
			removePrr(p,false);
		}

	}
	
	@EventHandler
	public void ProtectAbility (PlayerInteractEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (!(e.getRightClicked() instanceof LivingEntity)) return;
			
		Player p = e.getPlayer();
		LivingEntity ent = (LivingEntity) e.getRightClicked();
			
		if (p.getPose() == Pose.SNEAKING && p.getInventory().getItemInMainHand().getType() == Material.SHIELD 
			&& p.getCooldown(Material.SHIELD)==0
			&& LevelProgression.pgetData(p, "SHIELDMAN") >=140) {
				
			if (StartProtection(p,ent)) p.setCooldown(Material.SHIELD, 20*6);
			
		}
		
	}
	
	@EventHandler
	public void DeathInteractions(PlayerDeathEvent e) { //protection mechanic
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		
		Player p = e.getEntity();
		
		IncreaseLife(p,p.getInventory().getArmorContents());

		if (hasPrr(p,false)) {
			removeDef(getPrr(p),true);
			removePrr(p,true);
		}
	}
	
	//COMPROBATION FUNCTIONS:
	
	@EventHandler
	public void ArmourHealth(InventoryCloseEvent e) {
		
		IncreaseLife((Player) e.getPlayer(), e.getPlayer().getInventory().getArmorContents());
	}
	
	public void IncreaseLife(Player p, ItemStack[] itemarray){
		double baselife = 20;

		for (ItemStack item : itemarray) {
			if (item != null) {
				Material m = item.getType();

				if (m == Material.CHAINMAIL_BOOTS || m==Material.CHAINMAIL_CHESTPLATE ||m==Material.CHAINMAIL_HELMET || m==Material.CHAINMAIL_LEGGINGS ) {
						baselife = baselife + 5 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1166));
				}
				else if (m == Material.LEATHER_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 1 || LevelProgression.pgetData(p,"ADVENTURER") >= 1) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1233));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));
						
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
				}	
				else if (m == Material.LEATHER_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 2 || LevelProgression.pgetData(p,"ADVENTURER") >= 2) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1066));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.LEATHER_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 3 || LevelProgression.pgetData(p,"ADVENTURER") >= 3) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1066));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

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
				}
				else if (m == Material.LEATHER_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 4 || LevelProgression.pgetData(p,"ADVENTURER") >= 4) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0899));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.GOLDEN_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 6 || LevelProgression.pgetData(p,"ADVENTURER") >= 6) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1233));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.GOLDEN_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 7 || LevelProgression.pgetData(p,"ADVENTURER") >= 7) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1033));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				if (m == Material.GOLDEN_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 8 || LevelProgression.pgetData(p,"ADVENTURER") >= 8) {
						baselife = baselife + 5 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0833));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

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
				}
				else if (m == Material.GOLDEN_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 9 || LevelProgression.pgetData(p,"ADVENTURER") >= 9) {
						baselife = baselife + 5 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.066));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));
						
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
				}
				else if (m == Material.IRON_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 28) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));//
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.IRON_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 32) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.IRON_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 40) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

						if (!LevelProgression.breakItemWhenDenied) {
							ItemStack itemCloned = item.clone();
							item.setAmount(0);
							p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
							p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
							return;
						}

						p.getInventory().setLeggings(new ItemStack(Material.AIR,1));
						p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
					}
				}
				else if (m == Material.IRON_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 50) {
						baselife = baselife + 4  + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0533));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.DIAMOND_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 80) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.DIAMOND_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 70) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0533));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.DIAMOND_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 90) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.02));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

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
				}
				else if (m == Material.DIAMOND_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 100) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.02));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.NETHERITE_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 150) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0366));//
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.NETHERITE_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 125) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0366));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.NETHERITE_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 200) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0033));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.NETHERITE_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 175) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0033));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));
						
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
				} 
				else if (m == Material.ELYTRA) {
					if (LevelProgression.pgetData(p,"ADVENTURER") < 300) {
						p.sendMessage(ChatColor.RED +lang.getString("shield4"));
						
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
				} 
				else if (m == Material.TURTLE_HELMET) {
					if (LevelProgression.pgetData(p,"TRIDENTMAN") < 50) {
						p.sendMessage(ChatColor.RED +lang.getString("shield5"));
						
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
						baselife = baselife + 20;
					}
				} 

				//=====================================================================

				enchVerifier(p,item);
			}
		}
		
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baselife);
		if (p.getHealth() > p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		}
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

	//PROTECTION ABILITY:
	
	public static boolean StartProtection(Player defensor, LivingEntity protectedEntity) {
		boolean setShieldDelay=false;

		//Remove conection if protectedEntity is already a protectedEntity
		if (hasDef(protectedEntity,true)) {
			if (!getDef(protectedEntity).equals(defensor)) msg(1,defensor,protectedEntity.getName());
			
			removePrr(getDef(protectedEntity),true);	
			removeDef(protectedEntity,true);
			return true;
		}
		if (hasDef(protectedEntity,false)) {
			removeDef(protectedEntity,false);
		}
		
		//Deny conection if protectedEntity is already a defensor
		if (protectedEntity instanceof Player && hasPrr((Player)protectedEntity,true)) {
			msg(3,defensor,"");
			return true;
		}
		if (protectedEntity instanceof Player && hasPrr((Player)protectedEntity,false)) {
			removePrr((Player)protectedEntity,false);
		}

		//Deny conection if defensor is already a protectedEntity
		if (hasDef(defensor,true)) {
			msg(4,defensor,"");			
			return true;
		}
		if (hasDef(defensor,false)) {
			removeDef(defensor,false);
		}
		
		//Remove conection if defensor is already a defensor
		if (hasPrr(defensor,true)) {
			removeDef(getPrr(defensor),true);			
			removePrr(defensor,true);
			
			return true;
		}
		if (hasPrr(defensor,false)) {
			removePrr(defensor,false);
		}
		
		//Now both entities are clean, creating a conection
		if (!hasPrr(defensor,false) && !hasDef(protectedEntity,false)) {
			setPrr(defensor,protectedEntity);
			msg(5,defensor,protectedEntity.getName());
			setDef(protectedEntity,defensor);
			msg(6,protectedEntity,"");
			
			return true;
		}
		
		return setShieldDelay;
	}
	
	public static void msg(int type,LivingEntity target, String name) {
		//1 "this entity" is not protected any more
		//2 you are not protected any more
		//3 You can't defend someone who's already defending
		//4 You can't defend while you yourself are being protected
		//5 Protecting "this entity"
		//6 You are being protected
		switch(type) {
		case 1:
			if (target instanceof Player) ((Player)target).playSound(target, Sound.ENTITY_TURTLE_EGG_HATCH, 10.0F, 1.0F);
			target.sendMessage(ChatColor.GOLD +lang.getString("shield7")+name);
			break;
		case 2:
			if (target instanceof Player) ((Player)target).playSound(target, Sound.ENTITY_TURTLE_EGG_HATCH, 10.0F, 1.0F);
			target.sendMessage(ChatColor.GOLD +lang.getString("shield8"));
			break;
		case 3:
			if (target instanceof Player) ((Player)target).playSound(target, Sound.ENTITY_WANDERING_TRADER_NO, 5.0F, 1.0F);
			target.sendMessage(ChatColor.RED +lang.getString("shield6"));
			break;
		case 4:
			if (target instanceof Player) ((Player)target).playSound(target, Sound.ENTITY_WANDERING_TRADER_NO, 5.0F, 1.0F);
			target.sendMessage(ChatColor.RED +lang.getString("shield9"));
			break;
		case 5:
			if (target instanceof Player) ((Player)target).playSound(target, Sound.ITEM_GOAT_HORN_SOUND_1, 5.0F, 1.0F);
			target.sendMessage(ChatColor.GREEN +lang.getString("shield10")+name);
			break;
		case 6:
			if (target instanceof Player) ((Player)target).playSound(target, Sound.ITEM_GOAT_HORN_SOUND_1, 5.0F, 1.0F);
			target.sendMessage(ChatColor.GREEN +lang.getString("shield11"));
			break;
		default:
			break;
	
		}
	}
	
	public static void removeDef(LivingEntity protectedEntity,boolean msg){ //remove defensor player
		try {
			protectedEntity.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "defensor"));
			if (msg) msg(2,protectedEntity,"");
		}
		finally{}
	}
	public static void removePrr(Player defensorPlayer,boolean msg){ //remove protected entity
		try{
			LivingEntity pent = getPrr(defensorPlayer);
			defensorPlayer.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedEntity"));
			if (pent!=null && !pent.isDead() && msg) msg(1,defensorPlayer,pent.getName());
		}
		finally {}
	}
	
	public static void setDef(LivingEntity protectedEntity, Player defensor) { //set defensor player
		protectedEntity.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "defensor"), PersistentDataType.STRING, defensor.getUniqueId().toString());
	}
	public static void setPrr(Player defensorPlayer, LivingEntity protectedEntity) { //set protected entity
		defensorPlayer.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedEntity"), PersistentDataType.STRING, protectedEntity.getUniqueId().toString());
	}
	
	public static Player getDef(LivingEntity protectedEntity){ //get defensor player
		String uuid = protectedEntity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "defensor"), PersistentDataType.STRING);
		
		try {
			return (Player) Bukkit.getPlayer(UUID.fromString(uuid));
		}
		catch (Exception ex) {
			return null;
		}
	}
	public static LivingEntity getPrr(Player defensorPlayer){ //get Protected entity
		String uuid = defensorPlayer.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedEntity"), PersistentDataType.STRING);
		
		try {
			return (LivingEntity) Bukkit.getEntity(UUID.fromString(uuid));
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	public static boolean hasDef(LivingEntity protectedEntity, boolean hasToExist){ //has defensor player
		if (protectedEntity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "defensor"), PersistentDataType.STRING)) {
			if (hasToExist) {
				Player p = getDef(protectedEntity);
				if (p!=null && !p.isDead()) return true;
				else return false;
			}
			return true;
		}
		return false;
	}
	public static boolean hasPrr(Player defensorPlayer, boolean hasToExist){ //has protected entity
		if (defensorPlayer.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedEntity"), PersistentDataType.STRING)) {
			if (hasToExist) {
				LivingEntity l = getPrr(defensorPlayer);
				if (l!=null && !l.isDead()) return true;
				else return false;
			}
			return true;
		}
		return false;
	}

	
}
