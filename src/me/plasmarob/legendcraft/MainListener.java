package me.plasmarob.legendcraft;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.blocks.ChestBlock;
import me.plasmarob.legendcraft.blocks.Door;
import me.plasmarob.legendcraft.item.Bomb;
import me.plasmarob.legendcraft.item.Boomerang;
import me.plasmarob.legendcraft.item.GustJar;
import me.plasmarob.legendcraft.item.Hookshot;
import me.plasmarob.util.Tools;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
import org.bukkit.inventory.ItemStack;

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
	public MainListener(LegendCraft plugin) {
		this.plugin = plugin;		
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

		if (p.getItemInHand().getType() == Material.INK_SACK)
		{
			int dat = (int)p.getItemInHand().getData().getData();
			if (event.isSneaking())
			{
				if (dat == 0) {
					new GustJar(p);
				}
			} else {
				if (dat > 0 && dat <= 3) {
					p.setItemInHand(new ItemStack(Material.INK_SACK, 1, (short)0, (byte)0));
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
			// event.getPlayer().getItemInHand().getType();
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
			for (String dungeonStr : Dungeon.dungeons.keySet()) {
				Dungeon.dungeons.get(dungeonStr).testForRedstone(event.getBlock());
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
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		Player p = event.getPlayer();
		List<String> strings = LegendCraft.mainConfig.getStringList("worlds");
		if (!strings.contains(p.getWorld().getName()))
			return;
		
		if (p.getItemInHand().getType() == Material.CLAY_BRICK && (event.getAction() == Action.RIGHT_CLICK_AIR || 
				event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			new Boomerang(p);
			p.setItemInHand(null);
		} 
		
		if (p.getItemInHand().getType() == Material.INK_SACK && (event.getAction() == Action.RIGHT_CLICK_AIR || 
				event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			if (((int)p.getItemInHand().getData().getData()) == 5)
			{
				new Bomb(p);
				int amount = p.getItemInHand().getAmount();
				if (amount == 1)
					p.setItemInHand(null);
				else if (amount > 1)
					p.getItemInHand().setAmount(amount - 1);
					
			}
		} 
		
		if (p.getItemInHand().getType() == Material.INK_SACK && (event.getAction() == Action.RIGHT_CLICK_AIR || 
				event.getAction() == Action.RIGHT_CLICK_BLOCK))
		{
			if (((int)p.getItemInHand().getData().getData()) <= 3)
			{
				p.setItemInHand(new ItemStack(Material.INK_SACK, 1, (short)0, (byte)0));
				GustJar.remove(p);
			}
		}
		
		
		/*
		if (p.getItemInHand().getType() == Material.STICK && 
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
				p.setItemInHand(new ItemStack(Material.TORCH, 1, (short)0, (byte)0));
			}
		}
		*/
		
		
		//TODO: Unlock door with a key
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			//p.sendMessage("Right!");
			ItemStack itemHeld = p.getItemInHand();
			Material heldItem = p.getItemInHand().getType();
			
			byte heldData = p.getItemInHand().getData().getData();
			Block block = event.getClickedBlock();
			
			for (String s : Dungeon.dungeons.keySet())
			{
				if (Dungeon.dungeons.get(s).isEnabled())
				{
					ConcurrentHashMap<String, Door> doors = Dungeon.dungeons.get(s).getDoors();
					for (String dName : doors.keySet())
					{
						//p.sendMessage("is a door!");
						Door d = doors.get(dName);
						if (d.containsBlock(block) &&
								d.isEnabled() && d.isOn())
						{
							//p.sendMessage("has block!");
							if (d.getKeyMat() == heldItem &&
									d.getKeyDat() == heldData)
							{
								d.trigger();
								event.setCancelled(true);
								if (itemHeld.getAmount() > 1)
									itemHeld.setAmount(itemHeld.getAmount() - 1);
								else
									p.setItemInHand(null);
							}
							break;
						}
					}
				}
			}
		}
		
	}
	
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
			
			p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.7f);
			
			if (p.getMaxHealth() - p.getHealth() <= 2*size)
				p.setHealth(p.getMaxHealth());
			else
				p.setHealth(p.getHealth()+2*size);
		}
	}
	
	@EventHandler
	public void damageEvent(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			
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
