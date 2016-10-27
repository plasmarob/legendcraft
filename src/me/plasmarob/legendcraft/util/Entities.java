package me.plasmarob.legendcraft.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public enum Entities {
	INSTANCE;
	
	
	
	/** 
	 *  Get all entities in a location's chunk and surrounding chunks (depending on the chunk radius):
     * chunkRadius of 0: 1 chunk only
     * chunkRadius of 1: 9 chunks centered on the location's chunk
     * chunkRadius of 2: 25 chunks centered on the location's chunk
     * etc...
	 */
	public static Set<Entity> getEntitiesInChunks(Block b, int chunkRadius) {
	    Set<Entity> entities = new HashSet<Entity>();
	    for (int x = -16 * chunkRadius; x <= 16 * chunkRadius; x += 16) {
	        for (int z = -16 * chunkRadius; z <= 16 * chunkRadius; z += 16) {
	            for (Entity e : b.getRelative(x, 0, z).getChunk().getEntities()) {
	                entities.add(e);
	            }
	        }
	    }
	    return entities;
	}
	
	
    public void inChunks (String arg) {
        // Perform operation here 
    }
    public void more (String arg) {
        // Perform operation here 
    }
}