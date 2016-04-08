package me.plasmarob.legendcraft.item;

import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.util.Tools;
import net.minecraft.server.v1_9_R1.SoundEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Hookshot {

	public static ConcurrentHashMap<Player, Hookshot> instances = new ConcurrentHashMap<Player, Hookshot>();
	FishHook fh;
	Vector direction;
	Player player;
	Location previousLoc;
	double previousDist;
	int stage = 0;
	int delay = 0;
	
	public Hookshot(Player player, FishHook fh) {
		this.fh = fh;
		previousLoc = fh.getLocation().clone();
		direction = player.getEyeLocation().getDirection().clone();
		direction.normalize();
		instances.put(player, this);
		this.player = player;
	}

	
	@SuppressWarnings("deprecation")
	private boolean progress() {
		if (stage != 2 && (fh.isDead() || player.isDead())) {
			instances.remove(player);
			return false;
		}
		
		sound(fh.getLocation());
		
		if (stage == 0) {
			delay++;
			fh.setVelocity(direction);
			if (delay > 10 && previousLoc.distance(fh.getLocation()) < 0.5) {
				Location loc = fh.getLocation().clone();
				loc.setDirection(direction);
				BlockIterator bit = new BlockIterator(loc,0,3);
				Block next = fh.getLocation().getBlock();
				while (bit.hasNext()) {
					if ((next.getType() == Material.WOOL && (int)next.getData() == 12) ||
						(next.getType() == Material.SMOOTH_BRICK && (int)next.getData() == 3) ||
						 next.getType() == Material.CHEST) {
						stage = 1;
						player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 2.0f, 0.5f);
						break;
					} else if (next.getType() == Material.COBBLE_WALL) {
						if (next.getRelative(BlockFace.NORTH).getType().isSolid() ||
								next.getRelative(BlockFace.EAST).getType().isSolid() ||
								next.getRelative(BlockFace.WEST).getType().isSolid() ||
								next.getRelative(BlockFace.SOUTH).getType().isSolid())
						{
							player.setItemInHand(null);
							stage = 2;
							player.playSound(fh.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1.1f);
							break;
						} else {
							stage = 1;
							player.playSound(fh.getLocation(), Sound.BLOCK_WOOD_BREAK, 2.0f, 0.5f);
							break;
						}
					} else if (!Tools.canSeeThrough(next.getType())) {
						player.setItemInHand(null);
						stage = 2;
						player.playSound(fh.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1.1f);
						break;
					}
					next = bit.next();
				}
				
			}
			previousDist = player.getEyeLocation().distance(fh.getLocation());
		} else if (stage == 1) {
			
			direction = Tools.getDirection(player.getEyeLocation(), fh.getLocation()).normalize();
			player.setVelocity(direction);
			fh.setVelocity(new Vector(0,0.01,0));
			if (player.getEyeLocation().distance(fh.getLocation()) < 1 ||
				player.getEyeLocation().distance(fh.getLocation()) > previousDist+0.5) {
				if (player.getItemInHand().getType() == Material.FISHING_ROD) {
					player.setItemInHand(null);
					stage = 2;
					player.setFallDistance(0);
					player.setVelocity(new Vector());
				}
			}
			previousDist = player.getEyeLocation().distance(fh.getLocation());
		} else if (stage == 2) {
			player.setItemInHand(new ItemStack(Material.FISHING_ROD, 1, (short)0, (byte)0));
			instances.remove(player);
		}
		
		//player.sendMessage("" + previousLoc.distance(fh.getLocation()));
		previousLoc = fh.getLocation().clone();
		return false;
	}

	SoundEffect e = new SoundEffect(null);
	
	private void sound(Location loc) {
		/*
		PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(
				// TODO: needs SoundEffect and SoundCategory
				new SoundEffect(), 
				"note.snare", 
				loc.getX(),loc.getY(),loc.getZ(), 
				0.5f, 2f);
		new PacketPlayOutNamedSoundEffect(null, null, 1, previousDist, previousDist, delay, delay);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(sound);
		*/
		player.playSound(loc, Sound.BLOCK_NOTE_SNARE, 0.5f, 2f);
	}
	
	public static void progressAll() {
		if (instances.size() > 0)
			for (Player p : instances.keySet())
				instances.get(p).progress();
	}
	


	public static boolean progress(Player p) {
		if (instances.get(p) != null)
			return instances.get(p).progress();
		return false;
	}
}
