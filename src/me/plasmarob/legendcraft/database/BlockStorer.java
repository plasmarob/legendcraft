package me.plasmarob.legendcraft.database;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.TileEntity;

public class BlockStorer {
	
	public static void takeBlocks(Block[] blocks) {
		
		/* TODO:
		 * 
		 * Write a JSONifier for block metadata
		 * 
		 * - Banner
		 * - Beacon
		 * - Bed
		 * - BrewingStand
		 * - Chest
		 * - CommandBlock
		 * - Comparator
		 * -* Container
		 * 
		 */
		
		for (Block b : blocks) {
			
			BlockState state = b.getState();
			if (state instanceof Sign ) {
				((Sign)state).setLine(2,"d");
			}
			
			if(b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
			       final Sign s = (Sign) b.getState();
			       String[] lines = s.getLines();
			       //s.setLine(0,"Sign");
			       //s.update();
			}
			
			if(b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
			       final Sign s = (Sign) b.getState();
			       String[] lines = s.getLines();
			       //s.setLine(0,"Sign");
			       //s.update();
			}
			
			 BlockState[] bs = b.getChunk().getTileEntities();
			 //bs[0].
			 CraftBlock cb = (CraftBlock)b;
			 TileEntity te = null;
			 te.d();
			
			 ItemStack is = new ItemStack(Material.ACACIA_DOOR);
			 //is.
		}
	}
	
}
