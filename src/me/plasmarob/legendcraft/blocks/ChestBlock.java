package me.plasmarob.legendcraft.blocks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.util.Vector;

import me.plasmarob.legendcraft.Dungeon;
import me.plasmarob.legendcraft.database.DatabaseInserter;
import me.plasmarob.legendcraft.database.DatabaseMethods;
import me.plasmarob.legendcraft.database.InventoryToBase64;
import me.plasmarob.legendcraft.util.Tools;

public class ChestBlock implements Receiver {
	private Dungeon dungeon;
	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	private Block block;
	Inventory inv;
	HashMap<Integer,ItemStack> items = new HashMap<Integer,ItemStack>();
	public static List<FallingBlock> fallers = new ArrayList<FallingBlock>();
	BlockFace face;
	
	public ChestBlock(Player player, Block block, String name, Dungeon dungeon) {
		this.dungeon = dungeon;
		this.block = block;
		this.name = name;
		Chest chest = (Chest) block.getState();
		inv = chest.getInventory();
		
		for (int i = 0; i < 27; i++) {
			if (inv.getItem(i) != null)
				items.put(i, inv.getItem(i).clone());
		}
		face = ((DirectionalContainer)chest.getData()).getFacing();
		
		dbInsert();
	}
	
	public ChestBlock(Map<String,Object> data, Dungeon dungeon) {
		this.name = (String) data.get("name");
		this.dungeon = dungeon;
		block = Tools.blockFromXYZ((String) data.get("location"), dungeon.getWorld());
		defaultOnOff = Boolean.parseBoolean((String) data.get("default"));
		inverted = Boolean.parseBoolean((String) data.get("inverted"));
		face = BlockFace.valueOf((String) data.get("facing"));
		byte[] invBytes = (byte[]) data.get("inventory");
		String invString = new String(invBytes, StandardCharsets.UTF_8);
		Inventory inventory = null;
		try {
			inventory = InventoryToBase64.fromBase64(invString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		block.setType(Material.CHEST);
		Chest chest = (Chest) block.getState();
		((DirectionalContainer)chest.getData()).setFacingDirection(face);
		
		Inventory chestInventory = chest.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null) {
				chestInventory.setItem(i, inventory.getItem(i).clone());
				items.put(i, inventory.getItem(i).clone());
			}
		}
		
		
	}
	
	@Deprecated
	public ChestBlock(List<Map<?, ?>> map, List<Integer> locs, Block mainBlock, String name, Dungeon dungeon) {
		this.dungeon = dungeon;
		this.block = mainBlock;
		this.name = name;
		for (int i = 0; i < map.size(); i++)
		{
			@SuppressWarnings("unchecked")
			ItemStack is = ItemStack.deserialize((Map<String, Object>) map.get(i));
			int n = locs.get(i);
			items.put(n, is);
		}
		
		//Base64Coder
	}
	
	public void dbInsert() {
		// Insert into DB
		new DatabaseInserter("block")
			.dungeon_id(dungeon.getDungeonId())
			.type_id("CHEST")
			.name(name)
			.location(block.getX(), block.getY(), block.getZ())
			.add("default", defaultOnOff)
			.add("inverted", inverted)
			.execute();
		
		// ensure we've got a copy of the right inventory
		block.setType(Material.CHEST);
		Chest chest = (Chest) block.getState();
		inv = chest.getInventory();
		for (Integer i :  items.keySet()) {
			inv.setItem(i, items.get(i));
		}
	
		face = ((DirectionalContainer)chest.getData()).getFacing();	
		byte[] bytes = InventoryToBase64.toBase64(inv).getBytes(StandardCharsets.UTF_8);
		new DatabaseInserter("chest")
			.add("block_id", DatabaseMethods.getIdByName("block", name))
			.add("facing", face.name())
			.addBlobName("inventory")
			.executeBlob(bytes);
	}

	@Override
	public void trigger() {
		if (!isOn || !enabled)
			return;
		run();
	}
	@Override
	public void set() {
		if (!isOn || !enabled)
			return;
		
		if (inverted) {
			inv.clear();
			block.setType(Material.AIR);
		} else {
			run();
			/*
			block.setType(Material.CHEST);
			Chest chest = (Chest) block.getState();
			for (Integer i : items.keySet()) {
				chest.getInventory().setItem(i, items.get(i).clone());
			}
			*/
		}
	}
	@Override
	public void reset() {
		if (!isOn || !enabled)
			return;
		
		if (!inverted) {
			inv.clear();
			block.setType(Material.AIR);
		}
	}
	@Override
	public void on() {
		isOn = true;
	}
	@Override
	public void off() {
		isOn = false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		int limit = 10;
		Block tmp = block;
		while (tmp.getRelative(BlockFace.UP).getType() == Material.AIR && limit-- > 0)
		{
			tmp = tmp.getRelative(BlockFace.UP);
		}
		
		FallingBlock fb = tmp.getWorld().spawnFallingBlock(
				tmp.getLocation(), Material.WOOD, (byte)5 );
		fb.setVelocity(new Vector(0,-0.7,0));
		fb.setDropItem(false);
		fallers.add(fb);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	@Override
	public void setEnabled(boolean bool) {
		enabled = bool;
		if (enabled) {
			if (block.getType() == Material.CHEST) {
				Chest chest = (Chest) block.getState();
				chest.getInventory().clear();
			}
			block.setType(Material.AIR);
		}
		else {
			run();
		}
	}
	public boolean isDefaultOnOff() {
		return defaultOnOff;
	}
	public void setDefaultOnOff(boolean defaultOnOff) {
		this.defaultOnOff = defaultOnOff;
	}
	public boolean isInverted() {
		return inverted;
	}
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}
	

	@Override
	public String type() {
		return "chest";
	}
	@Override
	public String name() {
		return name;
	}

	@Override
	public void show(Player p) {
		// TODO Auto-generated method stub
	}

	public Block getMainBlock() {
		return block;
	}

	public List<Map<?, ?>> getItems() {
		List<Map<?, ?>> itemList = new ArrayList<Map<?, ?>>();
		for (Integer i : items.keySet())
		{
			itemList.add(items.get(i).serialize());
		}
		return itemList;
	}
	public List<Integer> getItemLocations() {
		List<Integer> locList = new ArrayList<Integer>();
		for (Integer i : items.keySet())
		{
			locList.add(i);
		}
		return locList;
	}

	public void check(Block b) {
		//Tools.say("check");
		if (b.getX() == block.getX() &&
			b.getY() == block.getY() && 
			b.getZ() == block.getZ() 
			) {
			//Tools.say("arrived.");
			block.setType(Material.CHEST);
			Chest chest = (Chest) block.getState();
			for (Integer i : items.keySet()) {
				chest.getInventory().setItem(i, items.get(i).clone());
			}
			((DirectionalContainer)chest.getData()).setFacingDirection(face);
			chest.update();
			List<Player> players = Tools.getPlayersAroundPoint(b.getLocation(), 24);
			for (Player p : players)
				p.playSound(p.getLocation(), Sound.BLOCK_WOOD_BREAK, 2.0f, 0.5f);
		}
	}

	public BlockFace getFace()
	{
		return face;
	}
	public void setFace(BlockFace face)
	{
		this.face = face;
	}
	
	String prp = "" + ChatColor.LIGHT_PURPLE;
	public void edit(Player p, String key, String value) {
		if (key.toLowerCase().equals("default")) {
			if (value.toLowerCase().equals("on"))
				defaultOnOff = true;
			else
				defaultOnOff = Boolean.parseBoolean(value); 
			p.sendMessage(prp + "  Default set to " + defaultOnOff + ".");
		} else if (key.toLowerCase().equals("inverted")) {
			inverted = Boolean.parseBoolean(value); 
			p.sendMessage(prp + "  Inverted set to " + inverted + ".");	
		}	
	}

	@Override
	public int getX() {
		return block.getX();
	}
	@Override
	public int getY() {
		return block.getY();
	}
	@Override
	public int getZ() {
		return block.getZ();
	}

	@Override
	public boolean hasBlock(Block b) {
		return b.equals(block);
	}

	


	

}
