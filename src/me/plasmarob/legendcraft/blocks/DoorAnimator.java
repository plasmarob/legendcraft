package me.plasmarob.legendcraft.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

public class DoorAnimator implements Runnable {
	
	Door door;
	String type;
	Vector max, min;
	int dx,dy,dz;
	int mx,my,mz,Mx,My,Mz;
	int frame;
	Player player;
	public Material[][][] matList;
	public Byte[][][] datList;
	
	boolean openingComplete = false;
	boolean closing = false;
	int maxDist;
	Location bottomCenter;
	
	//TODO: down distance, max open range, overlap delay?
	
	public DoorAnimator(Door door, Player player) {
		this.door = door;
		this.player = player;
		
		min = door.getMin();
		max = door.getMax();
		mx = min.getBlockX();
		my = min.getBlockY();
		mz = min.getBlockZ();
		Mx = max.getBlockX();
		My = max.getBlockY();
		Mz = max.getBlockZ();
		dx = Mx-mx+1;
		dy = My-my+1;
		dz = Mz-mz+1;
		
		matList = door.getMatList();
		datList = door.getDatList();
		
		//get player distance info (door bottom)
		maxDist = door.doorThickness()+1;
		bottomCenter = door.getBottomCenter();
		
		//Tools.say(maxDist);
		
		frame = 0;
		
		Block b;
		Block front;
		Block back;
		for (int y = my; y <= My; y++) {
			for (int x = mx; x <= Mx; x++) {
				for (int z = mz; z <= Mz; z++) {
					b = door.getWorld().getBlockAt(x, y, z);
					front = b.getRelative(door.getFacing());
					back = b.getRelative(door.getOppositeFacing());
					if (front.getType().equals(Material.IRON_FENCE))
						front.setType(Material.AIR);
					if (back.getType().equals(Material.IRON_FENCE))
						back.setType(Material.AIR);
				}
			}
		}
		
	}

	//TODO: make the current functionality be the "up" "open" type
	@SuppressWarnings("deprecation")
	public void update()
	{
		frame++;
		Block b;
		Block front;
		Block back;
			
		if (!openingComplete) {
			// from top to bottom, set the block to be the block underneath it
			for (int y = My; y >= my+frame; y--) {
				for (int x = mx; x <= Mx; x++) {
					for (int z = mz; z <= Mz; z++) {
						b = door.getWorld().getBlockAt(x, y, z);
						b.setType(matList[x-mx][y-my-frame][z-mz]);
						b.setData(datList[x-mx][y-my-frame][z-mz]);
					}
				}
			}
			
			// set lowest row to air
			int y=my+frame-1;
			for (int x = mx; x <= Mx; x++) {
				for (int z = mz; z <= Mz; z++) {
					b = door.getWorld().getBlockAt(x, y, z);
					b.setType(Material.AIR);
					b.setData((byte)0);
					
					/*
					front = b.getRelative(door.getFacing());
					back = b.getRelative(door.getOppositeFacing());
					if (front.getType().equals(Material.IRON_FENCE))
						front.setType(Material.AIR);
					if (back.getType().equals(Material.IRON_FENCE))
						back.setType(Material.AIR);
					 */
				}
			}
			
			if (my+frame > My) {
				openingComplete = true;
				frame = 0;
			}
		} else {
			
			if (player.getLocation().distance(bottomCenter) > maxDist)
				closing = true;
			
			if (closing) {
				boolean frontBarred = door.isFrontBarred();
				boolean backBarred = door.isBackBarred();
				// from top to bottom, bring the blocks down
				for (int y = My; y >= My-frame+1; y--) {
					for (int x = mx; x <= Mx; x++) {
						for (int z = mz; z <= Mz; z++) {
							b = door.getWorld().getBlockAt(x, y, z);
							b.setType(matList[x-mx][y-my-(dy-frame)][z-mz]);
							b.setData(datList[x-mx][y-my-(dy-frame)][z-mz]);
							
							front = b.getRelative(door.getFacing());
							back = b.getRelative(door.getOppositeFacing());
							if (door.isBarred() && frontBarred && front.getType().equals(Material.AIR))
								front.setType(Material.IRON_FENCE);
							if (door.isBarred() && backBarred && back.getType().equals(Material.AIR))
								back.setType(Material.IRON_FENCE);
						}
					}
				}
				
				if (my+frame > My) {
					door.stopAnimating();
				}
			}
			else
				frame = 0;
		}
		
		
	
	}
	
	@Override
	public void run()
	{
	    try {
	        update();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}

}
