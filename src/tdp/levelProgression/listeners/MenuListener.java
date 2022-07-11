package tdp.levelProgression.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import tdp.levelProgression.LevelProgression;

public class MenuListener implements Listener {
	private Inventory gui=LevelProgression.gui;
	private Inventory gui2=Bukkit.createInventory(null, InventoryType.CHEST);
	private FileConfiguration lang = LevelProgression.lang;
	
	public MenuListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void stickEvent(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer(); 
		Material mat = p.getInventory().getItemInOffHand().getType();
		
		if (mat == Material.STICK) { 
			this.gui=LevelProgression.createGui();
			p.openInventory(this.gui);
		}
		
		
	}
	
	@EventHandler
	public void abilityMenuEvent(InventoryClickEvent e) { 
		if (!LevelProgression.testPluginActive(e.getWhoClicked().getWorld())) return;

		if (!(e.getInventory().getType()==InventoryType.CHEST)) return;
		
	
		if (e.getInventory().equals(this.gui)) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();

			gui2.setItem(22, LevelProgression.item(Material.ENDER_EYE, ChatColor.AQUA + "" + ChatColor.BOLD + lang.getString("guiList1"), null, false));
			
			ItemStack backgroundPane=LevelProgression.item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ".", null, false);
			for (int i=0;i<18;i++) if (i!=4 && i!=22) gui2.setItem(i, backgroundPane);
			
			String localName = e.getClickedInventory().getItem(e.getSlot()).getItemMeta().getLocalizedName();
			if (localName==null || localName.equals("")) return;		
			
			if (localName.equals("lvlShow")) {
				LevelProgression.psetDatab(p,"displayLevels", true);
				return;
			}
			if (localName.equals("lvlHide")) {
				LevelProgression.psetDatab(p,"displayLevels", false);
				return;
			}
			newLvlGui(p,localName);
		} 
		else if (e.getInventory().getItem(4) != null && e.getInventory().getItem(4).getItemMeta().getLocalizedName().compareTo("Ability Upgrade item") == 0) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			
			String menuT = e.getInventory().getItem(0).getItemMeta().getLocalizedName();
			
			switch (e.getSlot()) {
			case 4:
				LevelProgression.addToScore(p, menuT,1);
				
				newLvlGui(p,menuT);
				break;
			case 22:
				Features(p,menuT,LevelProgression.pgetData(p, menuT),0);
				break;
			}
		}
		else if (e.getInventory().getItem(0) != null && e.getInventory().getItem(0).getItemMeta().getLocalizedName().equals("BACKPANE")) {//luego para comprobar si se puede ir atras o no, debe comprobar si hay un panel verde en el lado opuesto, en �l va a estar guardada la info del typeMenu
			e.setCancelled(true);
			Inventory inv = e.getInventory();
			Player p = (Player) e.getWhoClicked();
			
			String menuType = inv.getItem(0).getItemMeta().getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "MENU"), PersistentDataType.STRING);
			int page = inv.getItem(0).getItemMeta().getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "PAGE"), PersistentDataType.INTEGER);
			
			if (e.getSlot()==0) {
				if (page <= 0) p.openInventory(gui);
				else Features(p,menuType,LevelProgression.pgetData(p, menuType),(page-1));
			}
			else if (e.getSlot()==8 && inv.getItem(8)!= null && inv.getItem(8).getItemMeta().getLocalizedName().equals("next")) {
				Features(p,menuType,LevelProgression.pgetData(p, menuType),(page+1));
			}
		}
	}
	
	public void newLvlGui(Player p, String menuType) {
		Inventory gui2 = Bukkit.createInventory(p, InventoryType.CHEST, lang.getString("guiList2"));
		
		gui2.setItem(22, LevelProgression.item(Material.ENDER_EYE, ChatColor.AQUA + "" + ChatColor.BOLD + lang.getString("guiList1"), null, false));
		
		ItemStack backgroundPanel = LevelProgression.item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ".",null,false);
		for (int i=0;i<27;i++) if (i!=4 && i!=22) gui2.setItem(i, backgroundPanel);
		
		
		gui2.setItem(4, LevelProgression.item(Material.EMERALD_BLOCK,
				ChatColor.DARK_GREEN+""+ChatColor.BOLD+""+ LevelProgression.pgetData(p, menuType), 
				null,"Ability Upgrade item", false));
		
		ItemStack item = gui2.getItem(0);
		ItemMeta meta = item.getItemMeta();
		meta.setLocalizedName(menuType);
		item.setItemMeta(meta);
		gui2.setItem(0, item);
		
		
		p.openInventory(gui2);
	}
	
	public void Features(Player p, String menuType, int lvl, int page) {
		Inventory features = Bukkit.createInventory(p, 54, lang.getString("abilities"));
		ItemStack pane = item(lvl,0,Material.RED_STAINED_GLASS_PANE, ChatColor.RED + lang.getString("guiList3"), null, "BACKPANE", false, true);
		ItemMeta meta= pane.getItemMeta();
		meta.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "MENU"), PersistentDataType.STRING, menuType);
		meta.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "PAGE"), PersistentDataType.INTEGER, page);
		pane.setItemMeta(meta);
		
		features.setItem(0, pane);
		
		if (menuType.equals("ADVENTURER")) {
			features.setItem(53, item(lvl,30,Material.APPLE,lang.getString("info1"),lang.getString("info2"), false));
			features.setItem(44, item(lvl,90,Material.BREAD,lang.getString("info3"), lang.getString("info4"), false));
			features.setItem(35, item(lvl,170,Material.COOKED_BEEF,lang.getString("info5"),lang.getString("info6"), false));
			
			features.setItem(1, item(lvl,150,Material.EMERALD,lang.getString("info7"),lang.getString("info8"), false));

			features.setItem(2, item(lvl,25,Material.BIRCH_BOAT,lang.getString("info9"),lang.getString("info10"), false));
			
			features.setItem(3, item(lvl,50,Material.SADDLE,lang.getString("info11"),lang.getString("info12"), false));

			features.setItem(4, item(lvl,75,Material.WARPED_FUNGUS_ON_A_STICK,lang.getString("info13"),lang.getString("info14"), false));

			features.setItem(5, item(lvl,50,Material.BONE,lang.getString("info15"),lang.getString("info16"), false));

			features.setItem(6, item(lvl,300,Material.ELYTRA,lang.getString("info17"),lang.getString("info18"), false));
			
			features.setItem(7, item(lvl,100,Material.FILLED_MAP,lang.getString("info19"),lang.getString("info20"), false));

			features.setItem(8, item(lvl,95,Material.CAMPFIRE, lang.getString("info21"),lang.getString("info22"), false));

			features.setItem(17, item(lvl,150,Material.ENDER_CHEST,lang.getString("info23"),lang.getString("info24"), false));

			features.setItem(16, item(lvl,250,Material.SHULKER_BOX,lang.getString("info25"),lang.getString("info26"), false));

			features.setItem(15, item(lvl,130,Material.SPYGLASS,lang.getString("info27"), lang.getString("info28"), false));

			features.setItem(14, item(lvl,300,Material.ENDER_EYE,lang.getString("info29"),lang.getString("info30"), false));

			features.setItem(13, item(lvl,230,Material.COMPASS,lang.getString("info31"),lang.getString("info32"), false));

			features.setItem(12, item(lvl,270,Material.COMPASS,lang.getString("info33"),lang.getString("info34"), true));

			features.setItem(11, item(lvl,200,Material.RABBIT_FOOT,lang.getString("info35"),lang.getString("info36"), false));

			features.setItem(47, item(lvl,25,Material.ENCHANTED_BOOK,lang.getString("id1") + " I", lang.getString("ench"), false));
			features.setItem(38, item(lvl,70,Material.ENCHANTED_BOOK,lang.getString("id1") + " II", lang.getString("ench"), false));
			features.setItem(29, item(lvl,110,Material.ENCHANTED_BOOK,lang.getString("id1") + " III", lang.getString("ench"), false));
			features.setItem(20, item(lvl,160,Material.ENCHANTED_BOOK,lang.getString("id1") + " IV", lang.getString("ench"), false));

			features.setItem(48, item(lvl,35,Material.ENCHANTED_BOOK,lang.getString("id2") + " I", lang.getString("ench"), false));
			features.setItem(39, item(lvl,80,Material.ENCHANTED_BOOK,lang.getString("id2") + " II", lang.getString("ench"), false));
			features.setItem(30, item(lvl,120,Material.ENCHANTED_BOOK,lang.getString("id2") + " III", lang.getString("ench"), false));

			features.setItem(49, item(lvl,30,Material.ENCHANTED_BOOK,lang.getString("id3") + " I", lang.getString("ench"), false));
			features.setItem(40, item(lvl,75,Material.ENCHANTED_BOOK,lang.getString("id3") + " II", lang.getString("ench"), false));

			features.setItem(50, item(lvl,10,Material.ENCHANTED_BOOK,lang.getString("id4") + " I", lang.getString("ench"), false));
			features.setItem(41, item(lvl,60,Material.ENCHANTED_BOOK,lang.getString("id4") + " II", lang.getString("ench"), false));
			features.setItem(32, item(lvl,125,Material.ENCHANTED_BOOK,lang.getString("id4") + " III", lang.getString("ench"), false));

			features.setItem(51, item(lvl,220,Material.ENCHANTED_BOOK,lang.getString("id5") + " I", lang.getString("ench"), false));

			features.setItem(18, item(lvl,1,Material.LEATHER_HELMET, lang.getString("arm1"), lang.getString("equi"), false));
			features.setItem(45, item(lvl,2,Material.LEATHER_BOOTS, lang.getString("arm2"), lang.getString("equi"), false));
			features.setItem(36, item(lvl,3,Material.LEATHER_LEGGINGS, lang.getString("arm3"), lang.getString("equi"), false));
			features.setItem(27, item(lvl,4,Material.LEATHER_CHESTPLATE, lang.getString("arm4"), lang.getString("equi"), false));

			features.setItem(19, item(lvl,6,Material.GOLDEN_HELMET, lang.getString("arm5"), lang.getString("equi"), false));
			features.setItem(46, item(lvl,7,Material.GOLDEN_BOOTS, lang.getString("arm6"), lang.getString("equi"), false));
			features.setItem(37, item(lvl,8,Material.GOLDEN_LEGGINGS, lang.getString("arm7"), lang.getString("equi"), false));
			features.setItem(28, item(lvl,9,Material.GOLDEN_CHESTPLATE, lang.getString("arm8"), lang.getString("equi"), false));
			
		}
		else if (menuType.equals("WIZARD")) {
			if(page==0) {
				features.setItem(8, item(lvl,0,Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + lang.getString("guiList4"), null, "next", false, true));

				features.setItem(45, item(lvl,1,Material.ENCHANTED_BOOK, lang.getString("id6") + " I " + lang.getString("spell"), lang.getString("info47"), false));
				features.setItem(36, item(lvl,10,Material.ENCHANTED_BOOK,lang.getString("id6") + " II " + lang.getString("spell"), lang.getString("info48"), false));
				features.setItem(27, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id6") + " III " + lang.getString("spell"), lang.getString("info49"), false));
				features.setItem(18, item(lvl,50,Material.ENCHANTED_BOOK, lang.getString("id6") + " IV " + lang.getString("spell"), lang.getString("info50"), false));
				features.setItem(9, item(lvl,75,Material.ENCHANTED_BOOK, lang.getString("id6") + " V " + lang.getString("spell"), lang.getString("info51"), false));
				
				features.setItem(46, item(lvl,15,Material.ENCHANTED_BOOK, lang.getString("id7") + " I " + lang.getString("spell"), lang.getString("info52"), false));
				features.setItem(37, item(lvl,55,Material.ENCHANTED_BOOK, lang.getString("id7") + " II " + lang.getString("spell"), lang.getString("info53"), false));
				features.setItem(28, item(lvl,175,Material.ENCHANTED_BOOK, lang.getString("id7") + " III " + lang.getString("spell"), lang.getString("info54"), false));
				features.setItem(19, item(lvl,295,Material.ENCHANTED_BOOK, lang.getString("id7") + " IV " + lang.getString("spell"), lang.getString("info55"), false));

				features.setItem(47, item(lvl,67,Material.ENCHANTED_BOOK, lang.getString("id8") + " I " + lang.getString("spell"), lang.getString("info56"), false));
				features.setItem(38, item(lvl,87,Material.ENCHANTED_BOOK, lang.getString("id8") + " II " + lang.getString("spell"), lang.getString("info57"), false));
				features.setItem(29, item(lvl,165,Material.ENCHANTED_BOOK, lang.getString("id8") + " III " + lang.getString("spell"), lang.getString("info58"), false));
				features.setItem(20, item(lvl,235,Material.ENCHANTED_BOOK, lang.getString("id8") + " IV " + lang.getString("spell"), lang.getString("info59"), false));
				
				features.setItem(48, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id1") + " I " + lang.getString("spell"), lang.getString("info60"), false));
				features.setItem(39, item(lvl,52,Material.ENCHANTED_BOOK, lang.getString("id1") + " II " + lang.getString("spell"), lang.getString("info61"), false));
				features.setItem(30, item(lvl,214,Material.ENCHANTED_BOOK, lang.getString("id1") + " III " + lang.getString("spell"), lang.getString("info62"), false));
				features.setItem(21, item(lvl,268,Material.ENCHANTED_BOOK, lang.getString("id1") + " IV " + lang.getString("spell"), lang.getString("info63"), false));

				features.setItem(49, item(lvl,67,Material.ENCHANTED_BOOK, lang.getString("id9") + " I " + lang.getString("spell"), lang.getString("info64"), false));
				features.setItem(40, item(lvl,128,Material.ENCHANTED_BOOK, lang.getString("id9") + " II " + lang.getString("spell"), lang.getString("info65"), false));
				features.setItem(31, item(lvl,187,Material.ENCHANTED_BOOK, lang.getString("id9") + " III " + lang.getString("spell"), lang.getString("info66"), false));
				features.setItem(22, item(lvl,278,Material.ENCHANTED_BOOK, lang.getString("id9") + " IV " + lang.getString("spell"), lang.getString("info67"), false));

				features.setItem(50, item(lvl,109,Material.ENCHANTED_BOOK, lang.getString("id10") + " I " + lang.getString("spell"), lang.getString("info68"), false));
				features.setItem(41, item(lvl,126,Material.ENCHANTED_BOOK, lang.getString("id10") + " II " + lang.getString("spell"), lang.getString("info69"), false));
				features.setItem(32, item(lvl,148,Material.ENCHANTED_BOOK, lang.getString("id10") + " III " + lang.getString("spell"), lang.getString("info70"), false));
				features.setItem(23, item(lvl,166,Material.ENCHANTED_BOOK, lang.getString("id10") + " IV " + lang.getString("spell"), lang.getString("info71"), false));

				features.setItem(51, item(lvl,35,Material.ENCHANTED_BOOK, lang.getString("id4") + " I " + lang.getString("spell"), lang.getString("info72"), false));
				features.setItem(42, item(lvl,90,Material.ENCHANTED_BOOK, lang.getString("id4") + " II " + lang.getString("spell"), lang.getString("info73"), false));
				features.setItem(33, item(lvl,120,Material.ENCHANTED_BOOK,lang.getString("id4") + " III " + lang.getString("spell"), lang.getString("info74"), false));

				features.setItem(52, item(lvl,22,Material.ENCHANTED_BOOK, lang.getString("id11") + " I " + lang.getString("spell"), lang.getString("info75"), false));
				features.setItem(43, item(lvl,37,Material.ENCHANTED_BOOK, lang.getString("id11") + " II " + lang.getString("spell"), lang.getString("info76"), false));
				features.setItem(34, item(lvl,53,Material.ENCHANTED_BOOK, lang.getString("id11") + " III " + lang.getString("spell"), lang.getString("info77"), false));
				
				features.setItem(53, item(lvl,101,Material.ENCHANTED_BOOK, lang.getString("id12") + " I " + lang.getString("spell"), lang.getString("info78"), false));
				features.setItem(44, item(lvl,210,Material.ENCHANTED_BOOK, lang.getString("id12") + " II " + lang.getString("spell"), lang.getString("info79"), false));
				features.setItem(35, item(lvl,279,Material.ENCHANTED_BOOK, lang.getString("id12") + " III " + lang.getString("spell"), lang.getString("info80"), false));
			
				features.setItem(1, item(lvl,50,Material.BOOK,lang.getString("info37"),lang.getString("info38"), false));
				features.setItem(2, item(lvl,100,Material.BREWING_STAND,lang.getString("info39"),lang.getString("info40"), false));
				features.setItem(3, item(lvl,20,Material.EXPERIENCE_BOTTLE,lang.getString("info41"),lang.getString("info42"), false));
				features.setItem(5, item(lvl,90,Material.DRAGON_BREATH,lang.getString("info43"),lang.getString("info44"), false));
				features.setItem(6, item(lvl,5,Material.APPLE,lang.getString("infoApple"),lang.getString("infoApple"), false));
				features.setItem(7, item(lvl,10,Material.SUSPICIOUS_STEW,lang.getString("info45"),lang.getString("info46"), false));
			}
			else if (page==1) {
				features.setItem(45, item(lvl,72,Material.ENCHANTED_BOOK, lang.getString("id2") + " I " + lang.getString("spell"), lang.getString("info81"), false));
				features.setItem(36, item(lvl,107,Material.ENCHANTED_BOOK, lang.getString("id2") + " II " + lang.getString("spell"), lang.getString("info82"), false));
				features.setItem(27, item(lvl,207,Material.ENCHANTED_BOOK, lang.getString("id2") + " III " + lang.getString("spell"), lang.getString("info83"), false));

				features.setItem(46, item(lvl,189,Material.ENCHANTED_BOOK, lang.getString("id13") + " I " + lang.getString("spell"), lang.getString("info84"), false));
				features.setItem(37, item(lvl,263,Material.ENCHANTED_BOOK, lang.getString("id13") + " II " + lang.getString("spell"), lang.getString("info85"), false));
				features.setItem(28, item(lvl,175,Material.ENCHANTED_BOOK, lang.getString("id13") + " III " + lang.getString("spell"), lang.getString("info86"), false));
				
				features.setItem(47, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id14") + " I " + lang.getString("spell"), lang.getString("info87"), false));
				features.setItem(38, item(lvl,54,Material.ENCHANTED_BOOK, lang.getString("id14") + " II " + lang.getString("spell"), lang.getString("info88"), false));
				features.setItem(29, item(lvl,100,Material.ENCHANTED_BOOK, lang.getString("id14") + " III " + lang.getString("spell"), lang.getString("info89"), false));
				
				features.setItem(48, item(lvl,60,Material.ENCHANTED_BOOK, lang.getString("id3") + " I " + lang.getString("spell"), lang.getString("info90"), false));
				features.setItem(39, item(lvl,170,Material.ENCHANTED_BOOK, lang.getString("id3") + " II " + lang.getString("spell"), lang.getString("info91"), false));
				
				features.setItem(49, item(lvl,230,Material.ENCHANTED_BOOK, lang.getString("id15") + " I " + lang.getString("spell"), lang.getString("info92"), false));

				features.setItem(50, item(lvl,230,Material.ENCHANTED_BOOK, lang.getString("id16") + " I " + lang.getString("spell"), lang.getString("info93"), false));

				features.setItem(51, item(lvl,100,Material.ENCHANTED_BOOK, lang.getString("id17") + " I " + lang.getString("spell"), lang.getString("info94"), false));
			}	
		}
		else if (menuType.equals("SOLDIER")) {
			features.setItem(45, item(lvl,10,Material.ENCHANTED_BOOK, lang.getString("id18") + " I", lang.getString("ench"), false));
			features.setItem(36, item(lvl,90,Material.ENCHANTED_BOOK, lang.getString("id18") + " II", lang.getString("ench"), false));
			features.setItem(27, item(lvl,125,Material.ENCHANTED_BOOK, lang.getString("id18") + " III", lang.getString("ench"), false));
			features.setItem(18, item(lvl,285,Material.ENCHANTED_BOOK, lang.getString("id18") + " IV", lang.getString("ench"), false));
			features.setItem(9, item(lvl,300,Material.ENCHANTED_BOOK, lang.getString("id18") + " V", lang.getString("ench"), false));

			features.setItem(46, item(lvl,20,Material.ENCHANTED_BOOK, lang.getString("id19") + " I", lang.getString("ench"), false));
			features.setItem(37, item(lvl,85,Material.ENCHANTED_BOOK, lang.getString("id19") + " II", lang.getString("ench"), false));
			features.setItem(28, item(lvl,190,Material.ENCHANTED_BOOK, lang.getString("id19") + " III", lang.getString("ench"), false));
			features.setItem(19, item(lvl,290,Material.ENCHANTED_BOOK, lang.getString("id19") + " IV", lang.getString("ench"), false));
			features.setItem(10, item(lvl,300,Material.ENCHANTED_BOOK, lang.getString("id19") + " V", lang.getString("ench"), false));

			features.setItem(47, item(lvl,1,Material.ENCHANTED_BOOK, lang.getString("id20") + " I", lang.getString("ench"), false));
			features.setItem(38, item(lvl,5,Material.ENCHANTED_BOOK, lang.getString("id20") + " II", lang.getString("ench"), false));
			features.setItem(29, item(lvl,15,Material.ENCHANTED_BOOK, lang.getString("id20") + " III", lang.getString("ench"), false));
			features.setItem(20, item(lvl,20,Material.ENCHANTED_BOOK, lang.getString("id20") + " IV", lang.getString("ench"), false));
			features.setItem(11, item(lvl,25,Material.ENCHANTED_BOOK, lang.getString("id20") + " V", lang.getString("ench"), false));

			features.setItem(48, item(lvl,35,Material.ENCHANTED_BOOK, lang.getString("id21") + " I", lang.getString("ench"), false));
			features.setItem(39, item(lvl,140,Material.ENCHANTED_BOOK, lang.getString("id21") + " II", lang.getString("ench"), false));
			features.setItem(30, item(lvl,205,Material.ENCHANTED_BOOK, lang.getString("id21") + " III", lang.getString("ench"), false));

			features.setItem(49, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id22") + " I", lang.getString("ench"), false));
			features.setItem(40, item(lvl,165,Material.ENCHANTED_BOOK, lang.getString("id22") + " II", lang.getString("ench"), false));
			features.setItem(31, item(lvl,275,Material.ENCHANTED_BOOK, lang.getString("id22") + " III", lang.getString("ench"), false));

			features.setItem(50, item(lvl,40,Material.ENCHANTED_BOOK, lang.getString("id4") + " I", lang.getString("ench"), false));
			features.setItem(41, item(lvl,130,Material.ENCHANTED_BOOK, lang.getString("id4") + " II", lang.getString("ench"), false));
			features.setItem(32, item(lvl,240,Material.ENCHANTED_BOOK, lang.getString("id4") + " III", lang.getString("ench"), false));

			features.setItem(51, item(lvl,105,Material.ENCHANTED_BOOK, lang.getString("id23") + " I", lang.getString("ench"), false));
			features.setItem(42, item(lvl,215,Material.ENCHANTED_BOOK, lang.getString("id23") + " II", lang.getString("ench"), false));

			features.setItem(52, item(lvl,110,Material.ENCHANTED_BOOK, lang.getString("id25") + " I", lang.getString("ench"), false));
			features.setItem(43, item(lvl,220,Material.ENCHANTED_BOOK, lang.getString("id25") + " II", lang.getString("ench"), false));

			features.setItem(53, item(lvl,225,Material.ENCHANTED_BOOK, lang.getString("id5") + " I", lang.getString("ench"), false));

			features.setItem(8, item(lvl,0,Material.WOODEN_SWORD, lang.getString("info95"), lang.getString("info96"), false));
			features.setItem(17, item(lvl,50,Material.WOODEN_SWORD, lang.getString("info97"), lang.getString("info98"), true));
			features.setItem(7, item(lvl,50,Material.STONE_SWORD, lang.getString("info99"), lang.getString("info96"), false));
			features.setItem(16, item(lvl,65,Material.STONE_SWORD, lang.getString("info100"), lang.getString("info101"), true));
			features.setItem(6, item(lvl,0,Material.GOLDEN_SWORD, lang.getString("info102"), lang.getString("info96"), false));
			features.setItem(15, item(lvl,95,Material.GOLDEN_SWORD, lang.getString("info103"), lang.getString("info104"), true));
			features.setItem(5, item(lvl,100,Material.IRON_SWORD, lang.getString("info105"), lang.getString("info96"), false));
			features.setItem(14, item(lvl,150,Material.IRON_SWORD, lang.getString("info106"), lang.getString("info107"), true));
			features.setItem(4, item(lvl,200,Material.DIAMOND_SWORD, lang.getString("info108"), lang.getString("info96"), false));
			features.setItem(13, item(lvl,230,Material.DIAMOND_SWORD, lang.getString("info109"), lang.getString("info110"), true));
			features.setItem(3, item(lvl,250,Material.NETHERITE_SWORD, lang.getString("info111"), lang.getString("info96"), false));
			features.setItem(12, item(lvl,270,Material.NETHERITE_SWORD, lang.getString("info112"), lang.getString("info113"), true));

			features.setItem(26, item(lvl,0,Material.STICK, lang.getString("info114"), lang.getString("info115"), true));
			features.setItem(25, item(lvl,65,Material.REDSTONE, lang.getString("info116"), lang.getString("info117"), true));
		}
		else if (menuType.equals("ARCHER")) {
			features.setItem(45, item(lvl,60,Material.ENCHANTED_BOOK, lang.getString("id26") + " I", lang.getString("ench"), false));
			features.setItem(36, item(lvl,100,Material.ENCHANTED_BOOK, lang.getString("id26") + " II", lang.getString("ench"), false));
			features.setItem(27, item(lvl,150,Material.ENCHANTED_BOOK, lang.getString("id26") + " III", lang.getString("ench"), false));
			features.setItem(18, item(lvl,230,Material.ENCHANTED_BOOK, lang.getString("id26") + " IV", lang.getString("ench"), false));
			features.setItem(9, item(lvl,300,Material.ENCHANTED_BOOK, lang.getString("id26") + " V", lang.getString("ench"), false));

			features.setItem(46, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id27") + " I", lang.getString("ench"), false));
			features.setItem(37, item(lvl,120,Material.ENCHANTED_BOOK, lang.getString("id27") + " II", lang.getString("ench"), false));
			features.setItem(28, item(lvl,190,Material.ENCHANTED_BOOK, lang.getString("id27") + " III", lang.getString("ench"), false));
			features.setItem(19, item(lvl,290,Material.ENCHANTED_BOOK, lang.getString("id27") + " IV", lang.getString("ench"), false));
		
			features.setItem(47, item(lvl,90,Material.ENCHANTED_BOOK, lang.getString("id4") + " I", lang.getString("ench"), false));
			features.setItem(38, item(lvl,200,Material.ENCHANTED_BOOK, lang.getString("id4") + " II", lang.getString("ench"), false));
			features.setItem(29, item(lvl,250,Material.ENCHANTED_BOOK, lang.getString("id4") + " III", lang.getString("ench"), false));

			features.setItem(48, item(lvl,20,Material.ENCHANTED_BOOK, lang.getString("id28") + " I", lang.getString("ench"), false));
			features.setItem(39, item(lvl,130,Material.ENCHANTED_BOOK, lang.getString("id28") + " II", lang.getString("ench"), false));
			features.setItem(30, item(lvl,220,Material.ENCHANTED_BOOK, lang.getString("id28") + " III", lang.getString("ench"), false));

			features.setItem(49, item(lvl,70,Material.ENCHANTED_BOOK, lang.getString("id24") + " I", lang.getString("ench"), false));
			features.setItem(40, item(lvl,170,Material.ENCHANTED_BOOK, lang.getString("id24") + " II", lang.getString("ench"), false));

			features.setItem(50, item(lvl,260,Material.ENCHANTED_BOOK, lang.getString("id5") + " I", lang.getString("ench"), false));

			features.setItem(51, item(lvl,180,Material.ENCHANTED_BOOK, lang.getString("id29") + " I", lang.getString("ench"), false));

			features.setItem(52, item(lvl,100,Material.ENCHANTED_BOOK, lang.getString("id30") + " I", lang.getString("ench"), false));

			features.setItem(53, item(lvl,160,Material.ENCHANTED_BOOK, lang.getString("id31") + " I", lang.getString("ench"), false));

			features.setItem(1, item(lvl,0,Material.ANVIL, lang.getString("info118"), lang.getString("info119"), true));
			features.setItem(2, item(lvl,50,Material.BOW, lang.getString("info120"), lang.getString("info121"), false));
			features.setItem(3, item(lvl,10,Material.CROSSBOW, lang.getString("info122"), lang.getString("info123"), false));

			features.setItem(5, item(lvl,280,Material.NETHERITE_INGOT, lang.getString("info124"), lang.getString("info125"), true));
			features.setItem(6, item(lvl,270,Material.NETHERITE_INGOT, lang.getString("info126"), lang.getString("info127"), false));
			features.setItem(7, item(lvl,80,Material.DIAMOND, lang.getString("info128"), lang.getString("info129"), true));
			features.setItem(8, item(lvl,140,Material.DIAMOND, lang.getString("info130"), lang.getString("info131"), false));

			features.setItem(14, item(lvl,40,Material.IRON_INGOT, lang.getString("info132"), lang.getString("info133"), true));
			features.setItem(15, item(lvl,40,Material.IRON_INGOT, lang.getString("info134"), lang.getString("info135"), false));
			features.setItem(16, item(lvl,110,Material.GOLD_INGOT, lang.getString("info136"), lang.getString("info137"), false));
			features.setItem(17, item(lvl,170,Material.GOLD_BLOCK, lang.getString("info138"), lang.getString("info139"), false));

			features.setItem(23, item(lvl,240,Material.COPPER_BLOCK, lang.getString("info140"), lang.getString("info141"), false));
			features.setItem(24, item(lvl,0,Material.LAPIS_LAZULI, lang.getString("info142"), lang.getString("info143"), false));
			features.setItem(25, item(lvl,0,Material.EMERALD, lang.getString("info144"), lang.getString("info145"), false));
			features.setItem(26, item(lvl,0,Material.REDSTONE, lang.getString("info146"), lang.getString("info147"), false));
		
			features.setItem(35, item(lvl,20,Material.COAL_BLOCK, lang.getString("info14701"), lang.getString("info14702"), false));
			
			features.setItem(4, item(lvl,150,Material.SPYGLASS, lang.getString("info14703"), lang.getString("info14704"), true));
	
		}
		else if (menuType.equals("SHIELDMAN")) {
			if (page==0) {
				features.setItem(8, item(lvl,0,Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + lang.getString("guiList4"), null, "next", false, true));
				
				features.setItem(45, item(lvl,5,Material.ENCHANTED_BOOK, lang.getString("id7") + " I", lang.getString("ench"), false));
				features.setItem(36, item(lvl,60,Material.ENCHANTED_BOOK, lang.getString("id7") + " II", lang.getString("ench"), false));
				features.setItem(27, item(lvl,230,Material.ENCHANTED_BOOK, lang.getString("id7") + " III", lang.getString("ench"), false));
				features.setItem(18, item(lvl,290,Material.ENCHANTED_BOOK, lang.getString("id7") + " IV", lang.getString("ench"), false));
				
				features.setItem(46, item(lvl,46,Material.ENCHANTED_BOOK, lang.getString("id1") + " I", lang.getString("ench"), false));
				features.setItem(37, item(lvl,78,Material.ENCHANTED_BOOK, lang.getString("id1") + " II", lang.getString("ench"), false));
	
				features.setItem(47, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id8") + " I", lang.getString("ench"), false));
				features.setItem(38, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id8") + " II", lang.getString("ench"), false));
				features.setItem(29, item(lvl,20,Material.ENCHANTED_BOOK, lang.getString("id8") + " III", lang.getString("ench"), false));
				features.setItem(20, item(lvl,110,Material.ENCHANTED_BOOK, lang.getString("id8") + " IV", lang.getString("ench"), false));

				features.setItem(48, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id9") + " I", lang.getString("ench"), false));
				features.setItem(39, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id9") + " II", lang.getString("ench"), false));
				features.setItem(30, item(lvl,22,Material.ENCHANTED_BOOK, lang.getString("id9") + " III", lang.getString("ench"), false));
				features.setItem(21, item(lvl,130,Material.ENCHANTED_BOOK, lang.getString("id9") + " IV", lang.getString("ench"), false));

				features.setItem(49, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id10") + " I", lang.getString("ench"), false));
				features.setItem(40, item(lvl,0,Material.ENCHANTED_BOOK, lang.getString("id10") + " II", lang.getString("ench"), false));
				features.setItem(31, item(lvl,24,Material.ENCHANTED_BOOK, lang.getString("id10") + " III", lang.getString("ench"), false));
				features.setItem(22, item(lvl,150,Material.ENCHANTED_BOOK, lang.getString("id10") + " IV", lang.getString("ench"), false));

				features.setItem(50, item(lvl,15,Material.ENCHANTED_BOOK, lang.getString("id14") + " I", lang.getString("ench"), false));
				features.setItem(41, item(lvl,170,Material.ENCHANTED_BOOK, lang.getString("id14") + " II", lang.getString("ench"), false));
				features.setItem(32, item(lvl,270,Material.ENCHANTED_BOOK, lang.getString("id14") + " III", lang.getString("ench"), false));
				
				features.setItem(51, item(lvl,75,Material.ENCHANTED_BOOK, lang.getString("id4") + " I", lang.getString("ench"), false));
				features.setItem(42, item(lvl,180,Material.ENCHANTED_BOOK, lang.getString("id4") + " II", lang.getString("ench"), false));
				features.setItem(33, item(lvl,250,Material.ENCHANTED_BOOK, lang.getString("id4") + " III", lang.getString("ench"), false));

				features.setItem(52, item(lvl,43,Material.ENCHANTED_BOOK, lang.getString("id2") + " I", lang.getString("ench"), false));
				features.setItem(43, item(lvl,85,Material.ENCHANTED_BOOK, lang.getString("id2") + " II", lang.getString("ench"), false));
				
				features.setItem(53, item(lvl,38,Material.ENCHANTED_BOOK, lang.getString("id3") + " I", lang.getString("ench"), false));
				
				features.setItem(26, item(lvl,240,Material.ENCHANTED_BOOK, lang.getString("id5") + " I", lang.getString("ench"), false));

				features.setItem(3, item(lvl,10,Material.SHIELD, lang.getString("info148"), lang.getString("info149"), false));
				features.setItem(4, item(lvl,220,Material.SCUTE, lang.getString("info150"), lang.getString("info151"), false));
				features.setItem(5, item(lvl,140,Material.SHIELD, lang.getString("info152"), lang.getString("info153"), true));
				features.setItem(13, item(lvl,0,Material.IRON_CHESTPLATE, lang.getString("info154"), lang.getString("info155"), true));
			}
			else if (page==1) {
				features.setItem(10, item(lvl,0,Material.CHAINMAIL_HELMET, lang.getString("arm9"), lang.getString("equi"), false));
				features.setItem(37, item(lvl,0,Material.CHAINMAIL_BOOTS, lang.getString("arm10"), lang.getString("equi"), false));
				features.setItem(28, item(lvl,0,Material.CHAINMAIL_LEGGINGS, lang.getString("arm11"), lang.getString("equi"), false));
				features.setItem(19, item(lvl,0,Material.CHAINMAIL_CHESTPLATE, lang.getString("arm12"), lang.getString("equi"), false));

				features.setItem(11, item(lvl,1,Material.LEATHER_HELMET, lang.getString("arm1"), lang.getString("equi"), false));
				features.setItem(38, item(lvl,2,Material.LEATHER_BOOTS, lang.getString("arm2"), lang.getString("equi"), false));
				features.setItem(29, item(lvl,3,Material.LEATHER_LEGGINGS, lang.getString("arm3"), lang.getString("equi"), false));
				features.setItem(20, item(lvl,4,Material.LEATHER_CHESTPLATE, lang.getString("arm4"), lang.getString("equi"), false));
	
				features.setItem(12, item(lvl,6,Material.GOLDEN_HELMET, lang.getString("arm5"), lang.getString("equi"), false));
				features.setItem(39, item(lvl,7,Material.GOLDEN_BOOTS, lang.getString("arm6"), lang.getString("equi"), false));
				features.setItem(30, item(lvl,8,Material.GOLDEN_LEGGINGS, lang.getString("arm7"), lang.getString("equi"), false));
				features.setItem(21, item(lvl,9,Material.GOLDEN_CHESTPLATE, lang.getString("arm8"), lang.getString("equi"), false));
				
				features.setItem(14, item(lvl,28,Material.IRON_HELMET, lang.getString("arm13"), lang.getString("equi"), false));
				features.setItem(41, item(lvl,32,Material.IRON_BOOTS, lang.getString("arm14"), lang.getString("equi"), false));
				features.setItem(32, item(lvl,40,Material.IRON_LEGGINGS, lang.getString("arm15"), lang.getString("equi"), false));
				features.setItem(23, item(lvl,50,Material.IRON_CHESTPLATE, lang.getString("arm16"), lang.getString("equi"), false));

				features.setItem(15, item(lvl,70,Material.DIAMOND_HELMET, lang.getString("arm17"), lang.getString("equi"), false));
				features.setItem(42, item(lvl,80,Material.DIAMOND_BOOTS, lang.getString("arm18"), lang.getString("equi"), false));
				features.setItem(33, item(lvl,90,Material.DIAMOND_LEGGINGS, lang.getString("arm19"), lang.getString("equi"), false));
				features.setItem(24, item(lvl,100,Material.DIAMOND_CHESTPLATE, lang.getString("arm20"), lang.getString("equi"), false));

				features.setItem(16, item(lvl,125,Material.NETHERITE_HELMET, lang.getString("arm21"), lang.getString("equi"), false));
				features.setItem(43, item(lvl,150,Material.NETHERITE_BOOTS, lang.getString("arm22"), lang.getString("equi"), false));
				features.setItem(34, item(lvl,175,Material.NETHERITE_LEGGINGS, lang.getString("arm23"), lang.getString("equi"), false));
				features.setItem(25, item(lvl,200,Material.NETHERITE_CHESTPLATE, lang.getString("arm24"), lang.getString("equi"), false));
			}
		}
		else if (menuType.equals("TRIDENTMAN")) {
			if (page==0) {
				features.setItem(8, item(lvl,0,Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + lang.getString("guiList4"), null, "next", false, true));

				features.setItem(45, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id32") + " I", lang.getString("ench"), false));
				features.setItem(36, item(lvl,90,Material.ENCHANTED_BOOK, lang.getString("id32") + " II", lang.getString("ench"), false));
				features.setItem(27, item(lvl,160,Material.ENCHANTED_BOOK, lang.getString("id32") + " III", lang.getString("ench"), false));
				features.setItem(18, item(lvl,210,Material.ENCHANTED_BOOK, lang.getString("id32") + " IV", lang.getString("ench"), false));
				features.setItem(9, item(lvl,290,Material.ENCHANTED_BOOK, lang.getString("id32") + " V", lang.getString("ench"), false));
				
				features.setItem(46, item(lvl,80,Material.ENCHANTED_BOOK, lang.getString("id33") + " I", lang.getString("ench"), false));
				features.setItem(37, item(lvl,140,Material.ENCHANTED_BOOK, lang.getString("id33") + " II", lang.getString("ench"), false));
				features.setItem(28, item(lvl,220,Material.ENCHANTED_BOOK, lang.getString("id33") + " III", lang.getString("ench"), false));
	
				features.setItem(47, item(lvl,110,Material.ENCHANTED_BOOK, lang.getString("id34") + " I", lang.getString("ench"), false));
				features.setItem(38, item(lvl,180,Material.ENCHANTED_BOOK, lang.getString("id34") + " II", lang.getString("ench"), false));
				features.setItem(29, item(lvl,260,Material.ENCHANTED_BOOK, lang.getString("id34") + " III", lang.getString("ench"), false));
				
				features.setItem(48, item(lvl,70,Material.ENCHANTED_BOOK, lang.getString("id4") + " I", lang.getString("ench"), false));
				features.setItem(39, item(lvl,170,Material.ENCHANTED_BOOK, lang.getString("id4") + " II", lang.getString("ench"), false));
				features.setItem(30, item(lvl,270,Material.ENCHANTED_BOOK, lang.getString("id4") + " III", lang.getString("ench"), false));
				features.setItem(21, item(lvl,200,Material.ENCHANTED_BOOK, lang.getString("id4") + " I" + lang.getString("inarmour"), lang.getString("ench"), false));
	
				features.setItem(49, item(lvl,40,Material.ENCHANTED_BOOK, lang.getString("id13") + " I", lang.getString("ench"), false));
				features.setItem(40, item(lvl,130,Material.ENCHANTED_BOOK, lang.getString("id13") + " II", lang.getString("ench"), false));
				features.setItem(31, item(lvl,220,Material.ENCHANTED_BOOK, lang.getString("id13") + " III", lang.getString("ench"), false));
	
				features.setItem(50, item(lvl,250,Material.ENCHANTED_BOOK, lang.getString("id5") + " I", lang.getString("ench"), false));
	
				features.setItem(51, item(lvl,100,Material.ENCHANTED_BOOK, lang.getString("id15") + " I", lang.getString("ench"), false));
	
				features.setItem(52, item(lvl,140,Material.ENCHANTED_BOOK, lang.getString("id35") + " I", lang.getString("ench"), false));
	
				features.setItem(53, item(lvl,190,Material.ENCHANTED_BOOK, lang.getString("id16") + " I", lang.getString("ench"), false));
	
				features.setItem(7, item(lvl,280,Material.NETHERITE_INGOT, lang.getString("info156"), lang.getString("info157"), false));
				features.setItem(6, item(lvl,240,Material.DIAMOND, lang.getString("info158"), lang.getString("info159"), false));
				features.setItem(5, item(lvl,225,Material.EMERALD, lang.getString("info160"), lang.getString("info161"), false));
				features.setItem(4, item(lvl,230,Material.GOLD_BLOCK, lang.getString("info162"), lang.getString("info163"), false));
				features.setItem(3, item(lvl,90,Material.LAPIS_LAZULI, lang.getString("info164"), lang.getString("info165"), false));
				features.setItem(2, item(lvl,120,Material.IRON_INGOT, lang.getString("info166"), lang.getString("info167"), false));
				features.setItem(1, item(lvl,195,Material.COPPER_BLOCK, lang.getString("info168"), lang.getString("info169"), false));
	
				features.setItem(17, item(lvl,25,Material.TRIDENT, lang.getString("info170"), lang.getString("info171"), false));
				features.setItem(16, item(lvl,50,Material.TURTLE_HELMET, lang.getString("info172"), lang.getString("info173"), false));
				features.setItem(26, item(lvl,25,Material.LIGHTNING_ROD, lang.getString("info174"), lang.getString("info175"), false));
				features.setItem(25, item(lvl,150,Material.TRIDENT, lang.getString("info176"), lang.getString("info177"), true));
				features.setItem(35, item(lvl,60,Material.DIAMOND_CHESTPLATE, lang.getString("info178"), lang.getString("info179"), true));
				features.setItem(34, item(lvl,300,Material.CONDUIT, lang.getString("info180"), lang.getString("info181"), false));
				features.setItem(44, item(lvl,0,Material.CREEPER_HEAD, lang.getString("info182"), lang.getString("info183"), false));
				features.setItem(43, item(lvl,0,Material.PUFFERFISH, lang.getString("info184"), lang.getString("info185"), false));
			}
			else if (page==1) {
				features = Bukkit.createInventory(p, 36, lang.getString("abilities"));
				features.setItem(0, pane);
				
				features.setItem(27, item(lvl,4,Material.ENCHANTED_BOOK, lang.getString("id4") + " I" + lang.getString("inrod"), lang.getString("ench"), false));
				features.setItem(18, item(lvl,8,Material.ENCHANTED_BOOK, lang.getString("id4") + " II" + lang.getString("inrod"), lang.getString("ench"), false));
				features.setItem(9, item(lvl,10,Material.ENCHANTED_BOOK, lang.getString("id4") + " III" + lang.getString("inrod"), lang.getString("ench"), false));

				features.setItem(28, item(lvl,3,Material.ENCHANTED_BOOK, lang.getString("id11") + " I", lang.getString("ench"), false));
				features.setItem(19, item(lvl,7,Material.ENCHANTED_BOOK, lang.getString("id11") + " II", lang.getString("ench"), false));
				features.setItem(10, item(lvl,10,Material.ENCHANTED_BOOK, lang.getString("id11") + " III", lang.getString("ench"), false));

				features.setItem(29, item(lvl,2,Material.ENCHANTED_BOOK, lang.getString("id12") + " I", lang.getString("ench"), false));
				features.setItem(20, item(lvl,6,Material.ENCHANTED_BOOK, lang.getString("id12") + " II", lang.getString("ench"), false));
				features.setItem(11, item(lvl,9,Material.ENCHANTED_BOOK, lang.getString("id12") + " III", lang.getString("ench"), false));

				features.setItem(30, item(lvl,5,Material.ENCHANTED_BOOK, lang.getString("id5") + " I" + lang.getString("inrod"), lang.getString("ench"), false));

				features.setItem(26, item(lvl,1,Material.FISHING_ROD, lang.getString("info259"), lang.getString("info260"), true));
			}
		}
		else if (menuType.equals("MINER")) {
			features.setItem(45, item(lvl,15,Material.ENCHANTED_BOOK, lang.getString("id6") + " I", lang.getString("ench"), false));
			features.setItem(36, item(lvl,25,Material.ENCHANTED_BOOK, lang.getString("id6") + " II", lang.getString("ench"), false));
			features.setItem(27, item(lvl,80,Material.ENCHANTED_BOOK, lang.getString("id6") + " III", lang.getString("ench"), false));
			features.setItem(18, item(lvl,160,Material.ENCHANTED_BOOK, lang.getString("id6") + " IV", lang.getString("ench"), false));
			features.setItem(9, item(lvl,210,Material.ENCHANTED_BOOK, lang.getString("id6") + " V", lang.getString("ench"), false));
			
			features.setItem(46, item(lvl,90,Material.ENCHANTED_BOOK, lang.getString("id36") + " I", lang.getString("ench"), false));
			features.setItem(37, item(lvl,170,Material.ENCHANTED_BOOK, lang.getString("id36") + " II", lang.getString("ench"), false));
			features.setItem(28, item(lvl,240,Material.ENCHANTED_BOOK, lang.getString("id36") + " III", lang.getString("ench"), false));

			features.setItem(47, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id4") + " I", lang.getString("ench"), false));
			features.setItem(38, item(lvl,110,Material.ENCHANTED_BOOK, lang.getString("id4") + " II", lang.getString("ench"), false));
			features.setItem(29, item(lvl,230,Material.ENCHANTED_BOOK, lang.getString("id4") + " III", lang.getString("ench"), false));

			features.setItem(48, item(lvl,260,Material.ENCHANTED_BOOK, lang.getString("id5") + " I", lang.getString("ench"), false));

			features.setItem(49, item(lvl,210,Material.ENCHANTED_BOOK, lang.getString("id17") + " I", lang.getString("ench"), false));

			features.setItem(8, item(lvl,0,Material.WOODEN_PICKAXE, lang.getString("info186"), lang.getString("info187"), false));
			features.setItem(7, item(lvl,0,Material.WOODEN_SHOVEL, lang.getString("info188"), lang.getString("info189"), false));
			features.setItem(6, item(lvl,35,Material.WOODEN_PICKAXE, lang.getString("info190"), lang.getString("info191"), true));
			features.setItem(5, item(lvl,20,Material.WOODEN_SHOVEL, lang.getString("info192"), lang.getString("info193"), true));

			features.setItem(17, item(lvl,10,Material.STONE_PICKAXE, lang.getString("info194"), lang.getString("info187"), false));
			features.setItem(16, item(lvl,5,Material.STONE_SHOVEL, lang.getString("info195"), lang.getString("info189"), false));
			features.setItem(15, item(lvl,120,Material.STONE_PICKAXE, lang.getString("info196"), lang.getString("info197"), true));
			features.setItem(14, item(lvl,120,Material.STONE_SHOVEL, lang.getString("info198"), lang.getString("info199"), true));

			features.setItem(26, item(lvl,50,Material.GOLDEN_PICKAXE, lang.getString("info200"), lang.getString("info187"), false));
			features.setItem(25, item(lvl,45,Material.GOLDEN_SHOVEL, lang.getString("info201"), lang.getString("info189"), false));
			features.setItem(24, item(lvl,165,Material.GOLDEN_PICKAXE, lang.getString("info202"), lang.getString("info203"), true));
			features.setItem(23, item(lvl,60,Material.GOLDEN_SHOVEL, lang.getString("info204"), lang.getString("info205"), true));

			features.setItem(35, item(lvl,100,Material.IRON_PICKAXE, lang.getString("info206"), lang.getString("info187"), false));
			features.setItem(34, item(lvl,70,Material.IRON_SHOVEL, lang.getString("info207"), lang.getString("info189"), false));
			features.setItem(33, item(lvl,125,Material.IRON_PICKAXE, lang.getString("info208"), lang.getString("info209"), true));
			features.setItem(32, item(lvl,75,Material.IRON_SHOVEL, lang.getString("info210"), lang.getString("info211"), true));

			features.setItem(44, item(lvl,190,Material.DIAMOND_PICKAXE, lang.getString("info212"), lang.getString("info187"), false));
			features.setItem(43, item(lvl,150,Material.DIAMOND_SHOVEL, lang.getString("info213"), lang.getString("info189"), false));
			features.setItem(42, item(lvl,200,Material.DIAMOND_PICKAXE, lang.getString("info214"), lang.getString("info215"), true));
			features.setItem(41, item(lvl,155,Material.DIAMOND_SHOVEL, lang.getString("info216"), lang.getString("info217"), true));

			features.setItem(53, item(lvl,250,Material.NETHERITE_PICKAXE, lang.getString("info218"), lang.getString("info187"), false));
			features.setItem(52, item(lvl,220,Material.NETHERITE_SHOVEL, lang.getString("info219"), lang.getString("info189"), false));
			features.setItem(51, item(lvl,290,Material.NETHERITE_PICKAXE, lang.getString("info220"), lang.getString("info221"), true));
			features.setItem(50, item(lvl,300,Material.NETHERITE_SHOVEL, lang.getString("info222"), lang.getString("info223"), true));
		}
		else if (menuType.equals("LUMBERJACK")) {
			if (page==0) {
				features.setItem(8, item(lvl,0,Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + lang.getString("guiList4"), null, "next", false, true));

				features.setItem(45, item(lvl,6,Material.ENCHANTED_BOOK, lang.getString("id20") + " I", lang.getString("ench"), false));
				features.setItem(36, item(lvl,7,Material.ENCHANTED_BOOK, lang.getString("id20") + " II", lang.getString("ench"), false));
				features.setItem(27, item(lvl,8,Material.ENCHANTED_BOOK, lang.getString("id20") + " III", lang.getString("ench"), false));
				features.setItem(18, item(lvl,9,Material.ENCHANTED_BOOK, lang.getString("id20") + " IV", lang.getString("ench"), false));
				features.setItem(9, item(lvl,10,Material.ENCHANTED_BOOK, lang.getString("id20") + " V", lang.getString("ench"), false));
	
				features.setItem(46, item(lvl,30,Material.ENCHANTED_BOOK, lang.getString("id6") + " I", lang.getString("ench"), false));
				features.setItem(37, item(lvl,80,Material.ENCHANTED_BOOK, lang.getString("id6") + " II", lang.getString("ench"), false));
				features.setItem(28, item(lvl,150,Material.ENCHANTED_BOOK, lang.getString("id6") + " III", lang.getString("ench"), false));
				features.setItem(19, item(lvl,210,Material.ENCHANTED_BOOK, lang.getString("id6") + " IV", lang.getString("ench"), false));
				features.setItem(10, item(lvl,245,Material.ENCHANTED_BOOK, lang.getString("id6") + " V", lang.getString("ench"), false));
	
				features.setItem(47, item(lvl,60,Material.ENCHANTED_BOOK, lang.getString("id36") + " I", lang.getString("ench"), false));
				features.setItem(38, item(lvl,120,Material.ENCHANTED_BOOK, lang.getString("id36") + " II", lang.getString("ench"), false));
				features.setItem(29, item(lvl,265,Material.ENCHANTED_BOOK, lang.getString("id36") + " III", lang.getString("ench"), false));
	
				features.setItem(48, item(lvl,20,Material.ENCHANTED_BOOK, lang.getString("id18") + " I", lang.getString("ench"), false));
				features.setItem(39, item(lvl,100,Material.ENCHANTED_BOOK, lang.getString("id18") + " II", lang.getString("ench"), false));
				features.setItem(30, item(lvl,145,Material.ENCHANTED_BOOK, lang.getString("id18") + " III", lang.getString("ench"), false));
				features.setItem(21, item(lvl,210,Material.ENCHANTED_BOOK, lang.getString("id18") + " IV", lang.getString("ench"), false));
				features.setItem(12, item(lvl,280,Material.ENCHANTED_BOOK, lang.getString("id18") + " V", lang.getString("ench"), false));
				
				features.setItem(49, item(lvl,15,Material.ENCHANTED_BOOK, lang.getString("id19") + " I", lang.getString("ench"), false));
				features.setItem(40, item(lvl,90,Material.ENCHANTED_BOOK, lang.getString("id19") + " II", lang.getString("ench"), false));
				features.setItem(31, item(lvl,180,Material.ENCHANTED_BOOK, lang.getString("id19") + " III", lang.getString("ench"), false));
				features.setItem(22, item(lvl,270,Material.ENCHANTED_BOOK, lang.getString("id19") + " IV", lang.getString("ench"), false));
				features.setItem(13, item(lvl,290,Material.ENCHANTED_BOOK, lang.getString("id19") + " V", lang.getString("ench"), false));
				
				features.setItem(50, item(lvl,50,Material.ENCHANTED_BOOK, lang.getString("id4") + " I", lang.getString("ench"), false));
				features.setItem(41, item(lvl,110,Material.ENCHANTED_BOOK, lang.getString("id4") + " II", lang.getString("ench"), false));
				features.setItem(32, item(lvl,240,Material.ENCHANTED_BOOK, lang.getString("id4") + " III", lang.getString("ench"), false));
	
				features.setItem(51, item(lvl,250,Material.ENCHANTED_BOOK, lang.getString("id5") + " I", lang.getString("ench"), false));
				
				features.setItem(52, item(lvl,190,Material.ENCHANTED_BOOK, lang.getString("id17") + " I", lang.getString("ench"), false));
				
				features.setItem(17, item(lvl,50,Material.BONE, lang.getString("info224"), lang.getString("info225"), false));

				features.setItem(26, item(lvl,100,Material.CHAINMAIL_CHESTPLATE, lang.getString("info226"), lang.getString("info227"), true));
			}
			else if (page==1) {
				
				features.setItem(5, item(lvl,0,Material.OAK_LOG, lang.getString("info228"), lang.getString("info229"), false));
				features.setItem(4, item(lvl,3,Material.WOODEN_AXE, lang.getString("info230"), lang.getString("info231"), false));
				features.setItem(3, item(lvl,60,Material.WOODEN_AXE, lang.getString("info232"), lang.getString("info233"), true));

				features.setItem(14, item(lvl,5,Material.STONE, lang.getString("info234"), lang.getString("info229"), false));
				features.setItem(13, item(lvl,50,Material.STONE_AXE, lang.getString("info235"), lang.getString("info236"), false));
				features.setItem(12, item(lvl,70,Material.STONE_AXE, lang.getString("info237"), lang.getString("info238"), true));
				
				features.setItem(23, item(lvl,40,Material.GOLD_INGOT, lang.getString("info239"), lang.getString("info229"), false));
				features.setItem(22, item(lvl,140,Material.GOLDEN_AXE, lang.getString("info240"), lang.getString("info241"), false));
				features.setItem(21, item(lvl,95,Material.GOLDEN_AXE, lang.getString("info242"), lang.getString("info243"), true));
	
				features.setItem(32, item(lvl,90,Material.IRON_INGOT, lang.getString("info244"), lang.getString("info229"), false));
				features.setItem(31, item(lvl,130,Material.IRON_AXE, lang.getString("info245"), lang.getString("info246"), false));
				features.setItem(30, item(lvl,160,Material.IRON_AXE, lang.getString("info247"), lang.getString("info248"), true));

				features.setItem(41, item(lvl,170,Material.DIAMOND, lang.getString("info249"), lang.getString("info229"), false));
				features.setItem(40, item(lvl,230,Material.DIAMOND_AXE, lang.getString("info250"), lang.getString("info251"), false));
				features.setItem(39, item(lvl,200,Material.DIAMOND_AXE, lang.getString("info252"), lang.getString("info253"), true));

				features.setItem(50, item(lvl,260,Material.NETHERITE_INGOT, lang.getString("info254"), lang.getString("info229"), false));
				features.setItem(49, item(lvl,300,Material.NETHERITE_AXE, lang.getString("info255"), lang.getString("info256"), false));
				features.setItem(48, item(lvl,300,Material.NETHERITE_AXE, lang.getString("info257"), lang.getString("info258"), true));
			}			
		}
		else if (menuType.equals("FOODLVL")) {
			features = Bukkit.createInventory(p, 9, lang.getString("abilities"));
			features.setItem(0, pane);

			features.setItem(3, item(lvl,10,Material.APPLE, lang.getString("info1"), lang.getString("info2"), false));
			features.setItem(4, item(lvl,25,Material.BREAD, lang.getString("info3"), lang.getString("info4"), false));
			features.setItem(5, item(lvl,50,Material.COOKED_BEEF, lang.getString("info5"), lang.getString("info6"), false));		
		}
		
		p.openInventory(features);
	}
	
	public ItemStack item(int lvl, int lvlMin, Material m, String name, String loreText, boolean enchanted) {
		ItemStack item = new ItemStack(Material.STICK);
		
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		
		if(lvl>=lvlMin) {
			item.setType(m);
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + name);
			lore.add(ChatColor.DARK_GREEN + lang.getString("guiList6") + lvlMin);
			if (loreText!=null) for (String s : loreText.split("//")) lore.add(ChatColor.YELLOW + s);

			if (enchanted) {
				meta.addEnchant(Enchantment.DURABILITY, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
		}
		else {
			item.setType(Material.RED_CONCRETE);
			meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + name);
			lore.add(ChatColor.RED + lang.getString("guiList5") + lvlMin);
		}
		
        meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public ItemStack item(int lvl, int lvlMin, Material m, String name, String loreText, String localName, boolean enchanted,boolean showLvl) {
		ItemStack item = item(lvl,lvlMin,m,name,loreText,enchanted);
		ItemMeta meta = item.getItemMeta();
        meta.setLocalizedName(localName);
        
        if (showLvl) {
	        List<String> lore = meta.getLore();
	        lore.remove(0);
	        meta.setLore(lore);
	    }
        
        item.setItemMeta(meta);
		return item;
	}

}
