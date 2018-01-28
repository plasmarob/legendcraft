package me.plasmarob.legendcraft;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.blocks.ChestBlock;
import me.plasmarob.legendcraft.blocks.Door;
import me.plasmarob.legendcraft.blocks.KeyholeRenderer;
import me.plasmarob.legendcraft.item.Bomb;
import me.plasmarob.legendcraft.item.Boomerang;
import me.plasmarob.legendcraft.item.FireRodBlast;
import me.plasmarob.legendcraft.item.GustJar;
import me.plasmarob.legendcraft.item.Hookshot;
import me.plasmarob.legendcraft.item.IceRodBlast;
import me.plasmarob.legendcraft.util.Tools;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedSoundEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * Player Listener class
 * * created and registered by Bending
 * 
 * @author      Robert Thorne <plasmarob@gmail.com>
 * @version     0.3                
 * @since       2014-08-01
 */
@SuppressWarnings("unused")
public class MainListener implements Listener {
	
	public LegendCraft plugin;
	public MainListener() {
		//this.plugin = plugin;		
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {

	}
	
	/*
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		LegendCraft.selectedDungeons.remove(player);
		LegendCraft.tuneStrings.remove(player);
	}
	*/
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event)
	{
		Player p = event.getPlayer();
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(p.getWorld().getName()))
			return;

		// Gust Jar
		if (p.getInventory().getItemInMainHand().getType() == Material.INK_SACK)
		{
			int dat = (int)p.getInventory().getItemInMainHand().getData().getData();
			if (event.isSneaking())
			{
				if (dat == 0) {
					new GustJar(p);
				}
			} else {
				if (dat > 0 && dat <= 3) {
					p.getInventory().setItemInMainHand(new ItemStack(Material.INK_SACK, 1, (short)0, (byte)0));
					GustJar.remove(p);
				}
			}	
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void playerTossEvent(PlayerDropItemEvent event)
	{
		Player p = event.getPlayer();
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(p.getWorld().getName()))
			return;
		
		ItemStack item = event.getItemDrop().getItemStack();
		if (item.getType() == Material.INK_SACK &&
				(int)item.getData().getData() <= 3) 
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void ItemEvent(ItemSpawnEvent event)
	{
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(event.getLocation().getWorld().getName()))
			return;
		if (event.getEntity().getItemStack().getType() == Material.GRAVEL)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		//TODO: check to see if it belongs to a spawner, and inform the spawner 
		// (track when spawner mobs die so they can trigger other events)
	}
	
	@EventHandler
	public void playerFish(PlayerFishEvent event)
	{
		//Tools.say(event.getState().toString());
		//FishHook fh = event.getHook();
		if (event.getState() == PlayerFishEvent.State.FISHING)
		{
			FishHook fh = event.getHook();
			new Hookshot(event.getPlayer(), fh);
			// event.getPlayer().getInventory().getItemInMainHand().getType();
		}
		
		
		
		/*
		Entity e = event.getEntity();
		if (event.getEntity() instanceof FishHook) {
			Player p = (Player)((Projectile)e).getShooter();
			p.sendMessage("Launch!");
		}
		*/
		
		//TODO: check to see if it belongs to a spawner, and inform the spawner 
		// (track when spawner mobs die so they can trigger other events)
	}
	
	@EventHandler
	public void onRedstoneChange(BlockRedstoneEvent event)
	{
		//redstone sense 	
		int prev = event.getOldCurrent();
		int next = event.getNewCurrent();
		if (prev == 0 && next > 0) {
			for (String dungeonStr : Dungeon.getDungeons().keySet()) {
				Dungeon.getDungeons().get(dungeonStr).testForRedstone(event.getBlock());
			}
		}
	}
	
	@EventHandler
	public void onFallingBlockChange(EntityChangeBlockEvent event)
	{
		//Tools.say(event.getEntityType());
		if (event.getEntity() instanceof FallingBlock)
		{
			FallingBlock fb = (FallingBlock)event.getEntity();
			//Tools.say("Land!");
			if (GustJar.fallers.contains(fb))
			{
				event.setCancelled(true);
				fb.remove();
				GustJar.fallers.remove(fb);
			}
			else if (ChestBlock.fallers.contains(fb) && fb.getMaterial() == Material.WOOD) {
				if (fb.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == 
						Material.AIR) {
					event.setCancelled(true);
					return;
				}
				
				
				Dungeon.checkChests(event.getBlock());
				ChestBlock.fallers.remove(fb);
				fb.remove();
				fb.eject();
				event.setCancelled(true);
			}
		
			
		}
	}
	
	
	// No messing with armor stands
	@EventHandler
	public void onArmorStandInteract(PlayerArmorStandManipulateEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}
	
	//TODO: rewrite all of these so that they are only one pair of action tests.
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		//unless we use left hand, do nothing. that code would go here
		if (event.getHand().equals(EquipmentSlot.OFF_HAND))
			return;
		
		
		Player p = event.getPlayer();
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(p.getWorld().getName()))
			return;
		
		// Boomerang
		if (p.getInventory().getItemInMainHand().getType() == Material.CLAY_BRICK && (event.getAction() == Action.RIGHT_CLICK_AIR || 
				event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			new Boomerang(p);
			p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		} 
		
		// Bomb
		if (p.getInventory().getItemInMainHand().getType() == Material.INK_SACK && (event.getAction() == Action.RIGHT_CLICK_AIR || 
				event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			if (((int)p.getInventory().getItemInMainHand().getData().getData()) == 5)
			{
				new Bomb(p);
				int amount = p.getInventory().getItemInMainHand().getAmount();
				if (amount == 1)
					p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
				
				else if (amount > 1)
					p.getInventory().getItemInMainHand().setAmount(amount - 1);
					
			}
		} 
		
		// Gust Jar
		if (p.getInventory().getItemInMainHand().getType() == Material.INK_SACK && (event.getAction() == Action.RIGHT_CLICK_AIR || 
				event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			if (((int)p.getInventory().getItemInMainHand().getData().getData()) <= 3)
			{
				p.getInventory().setItemInMainHand(new ItemStack(Material.INK_SACK, 1, (short)0, (byte)0));
				GustJar.remove(p);
			}
		}
		
		// Ice Rod
		if (p.getInventory().getItemInMainHand().getType() == Material.INK_SACK && (event.getAction() == Action.LEFT_CLICK_AIR || 
				event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			if (((int)p.getInventory().getItemInMainHand().getData().getData()) == 4)
			{
				if (p.getFoodLevel() > 1 || p.getGameMode() == GameMode.CREATIVE) {
					new IceRodBlast(p);
					if (p.getGameMode() != GameMode.CREATIVE) {
						p.setFoodLevel(p.getFoodLevel()-1);
					}
				}
				
			}
		}
		
		// Fire Rod
		if (p.getInventory().getItemInMainHand().getType() == Material.INK_SACK && (event.getAction() == Action.LEFT_CLICK_AIR || 
				event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			if (((int)p.getInventory().getItemInMainHand().getData().getData()) == 13)
			{
				if (p.getFoodLevel() > 1 || p.getGameMode() == GameMode.CREATIVE) {
					new FireRodBlast(p);
					if (p.getGameMode() != GameMode.CREATIVE) {
						p.setFoodLevel(p.getFoodLevel()-1);
					}
				}
				
			}
		}
		
		/*
		if (p.getInventory().getItemInMainHand().getType() == Material.STICK && 
				(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR))
		{
			//Block b = event.getClickedBlock();
			List<Block> blocks = p.getLastTwoTargetBlocks(new HashSet<Material>(), 5);
			Block b = blocks.get(blocks.size()-1);
			for (int i = 0; i < blocks.size(); i++)
				p.sendMessage(blocks.get(i).toString());
			if (b.getType() == Material.TORCH && 
					b.getRelative(BlockFace.DOWN).getType() == Material.COBBLE_WALL)
			{
				p.getInventory().setItemInMainHand(new ItemStack(Material.TORCH, 1, (short)0, (byte)0));
			}
		}
		*/
		
		/*
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && p.getGameMode() == GameMode.SURVIVAL) {
			
			//TODO: make a keyhole map view
			ItemStack mapStack = new ItemStack(Material.MAP);
			mapStack.setDurability((short)16);
			
			MapView mapView = Bukkit.getMap(mapStack.getDurability());
			
			Tools.say(mapView);
			
			//mapStack.getData().get
			
			//MapView mapView = new MapView();
					
			//MapRenderer renderer = mapView.getRenderers().get(0);
			//remove any renderers
			//for (MapRenderer mapRenderer : mapView.getRenderers()) {
			//	mapView.removeRenderer(mapRenderer);
	       // }
			
			
			//mapView.addRenderer(new KeyholeRenderer());
			
			
			//mapView.addRenderer(new MapRenderer(
					//TODO make a custom map renderer
			//		));
			
			
			try {
				ItemFrame frame = (ItemFrame) p.getWorld().spawn(
						event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),
						ItemFrame.class);
				frame.setItem(mapStack);
				frame.setFacingDirection(event.getBlockFace());
			} catch (IllegalArgumentException iae) {
				// do nothing, you just can't place an item frame here.
				//(top or bottom)
			}
		}
		*/
		
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!p.isSneaking() && (p.getInventory().getItemInMainHand().getType() == Material.AIR || p.getInventory().getItemInMainHand().getType() == null) ) {
				if (Dungeon.selectedDungeons.containsKey(p)) {
					String dungeonStr = Dungeon.selectedDungeons.get(p);
					Dungeon d = Dungeon.getDungeons().get(dungeonStr);
					if (!d.isEnabled() && d.getBlock(event.getClickedBlock()) != null) {
						String block = d.getBlock(event.getClickedBlock());
						Dungeon.getDungeons().get(dungeonStr).show(p, block);
						event.setCancelled(true);
					}
				}
			}
		}
		
		
		
		//TODO: Unlock door with a key
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			for (String s : Dungeon.getDungeons().keySet()) {
				if (Dungeon.getDungeons().get(s).isEnabled()) {
					ConcurrentHashMap<String, Door> doors = Dungeon.getDungeons().get(s).getDoors();
					Door d;
					for (String dName : doors.keySet()) {
						d = doors.get(dName);
						if (d.hasBlock(block)) {
							d.tryOpen(p, event.getBlockFace());
							break;
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
			//((ItemFrame)event.getRightClicked()).
			ItemStack is = ((ItemFrame)event.getRightClicked()).getItem();
			if (is.getType().equals(Material.INK_SACK) && is.getData().getData() == (byte)6) {
				Material handItemType = event.getPlayer().getInventory().getItemInMainHand().getType();
				if (handItemType.equals(Material.GOLD_NUGGET)) {
					for (String s : Dungeon.getDungeons().keySet()) {
						if (Dungeon.getDungeons().get(s).isEnabled()) {
							ConcurrentHashMap<String, Door> doors = Dungeon.getDungeons().get(s).getDoors();
							Door d;
							for (String dName : doors.keySet()) {
								d = doors.get(dName);
								if (d.hasFrame(is)) {
									d.tryOpen(event.getPlayer(), ((ItemFrame)event.getRightClicked()).getFacing() );
									return;
								}
							}
						}
					}
				}
				event.setCancelled(true);
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerItemPicked(PlayerPickupItemEvent event)
	{
		
		Player p = event.getPlayer();
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(p.getWorld().getName()))
			return;
		
		if (event.getItem().getItemStack().getType() == Material.SEEDS)
		{
			int size = event.getItem().getItemStack().getAmount();
			event.setCancelled(true);
			event.getItem().remove();
			
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.7f);
			
			if (p.getMaxHealth() - p.getHealth() <= 2*size)
				p.setHealth(p.getMaxHealth());
			else
				p.setHealth(p.getHealth()+2*size);
		}
	}
	
	@EventHandler
	public void damageEvent(EntityDamageEvent event)
	{
		// No ice suffocate
		if (event.getCause() == DamageCause.SUFFOCATION) {
			if (event.getEntity() instanceof LivingEntity){
				LivingEntity le = (LivingEntity)event.getEntity();
				if (le.getEyeLocation().getBlock().getType() == Material.ICE)
					event.setCancelled(true);
			}
		} else if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			
			Player p = (Player)event.getEntity();
			List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
			if (!strings.contains(p.getWorld().getName()))	//Plugin world check
				return;
			
			int fall = (int)Math.floor(p.getFallDistance() / 11.0f);
			if (fall > 3)
				fall = 3;
			if (fall == 0)
				event.setCancelled(true);
			else
				event.setDamage((double)fall);
			
		}
	}
	
	@EventHandler
	public void playerSwitchesWorlds (PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		//TODO: see if this returns both worlds
		//TODO: we nee the player to be able to get and lose their Zelda health
		player.sendMessage(event.getFrom().getName());
		player.sendMessage(player.getWorld().getName());
	}
	
	@EventHandler
	public void entityTeleportThroughPortal(EntityPortalEvent event)
	{
		//TODO: i need to cancel sending boomerang through a portal later.
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerItemChange(PlayerItemHeldEvent event)
	{
		Player p = event.getPlayer();
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(p.getWorld().getName()))
			return;
		
		//TODO: i need to stop bugs that happen via changing what is in your hand
		
		ItemStack is = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
		
		if (is != null && is.getType() == Material.INK_SACK)
		{
			int dat = (int)is.getData().getData();
			if (dat > 0 && dat <= 3) {
				p.getInventory().setItem(event.getPreviousSlot(),
						new ItemStack(Material.INK_SACK, 1, (short)0, (byte)0));
				GustJar.remove(p);
			}
		}
	}
	
	
	
}
