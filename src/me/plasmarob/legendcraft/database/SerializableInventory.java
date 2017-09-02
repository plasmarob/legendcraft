package me.plasmarob.legendcraft.database;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.plasmarob.legendcraft.nbt.ItemNBTAPI.SimpleJsonTestObject;
import me.plasmarob.legendcraft.nbt.NBTItem;
import me.plasmarob.legendcraft.nbt.NBTList;
import me.plasmarob.legendcraft.nbt.NBTType;

public class SerializableInventory {

	/**
	 * 
	 */
	//private static final long serialVersionUID = 7286896137012090628L;
	SerializableInventory(ItemStack is) {
		
		
		ItemMeta im = is.getItemMeta();
		im.getItemFlags();
		
	}
	
	
	/**
	 * Serializes an entire inventory
	 * Semicolon and Comma-delim Format:
	 * - slot
	 * - material name
	 * - material data
	 * - amount
	 * - ItemMeta:
	 * - - display name
	 * - - localized name
	 * - - lore
	 * - - 
	 * @param inventory
	 * @return a byte[] for a BLOB
	 */
	@SuppressWarnings("deprecation")
	public static byte[] Serialize(Inventory inventory) {
		
		StringJoiner string = new StringJoiner(",");
		ItemStack item = null;
		ItemMeta itemMeta = null;
		for (int i = 0; i < inventory.getSize(); i++) {
			item = inventory.getItem(i);
			string.add(Integer.toString(i));
			string.add(item.getType().name());
			string.add(Byte.toString(item.getData().getData()));
			string.add(Integer.toString(item.getAmount()));
		
			itemMeta = item.getItemMeta();
			string.add(itemMeta.getDisplayName());
			string.add(itemMeta.getLocalizedName());
			for (String s : itemMeta.getLore())
				string.add(s);
			Map<Enchantment,Integer> enchants = itemMeta.getEnchants();
			for (Enchantment e : enchants.keySet()) {
				string.add(e.getName());
				string.add(Integer.toString(enchants.get(e)));
			}
			string.add(Boolean.toString(itemMeta.isUnbreakable()));
			
			for (ItemFlag it : itemMeta.getItemFlags()) {
				string.add(it.name());
			}
			
			
			
			NBTItem nbti = new NBTItem(item);
			Set<String> keys = nbti.getKeys();
			for (String key : keys) {
				nbti.getString(key);
			}
			
			SimpleJsonTestObject simple = nbti.getObject("", SimpleJsonTestObject.class);
			NBTList list = nbti.getList("Attributes", NBTType.NBTTagCompound);
			
		}
		
		
		
		
		
		
		return null;
	}
	
	public static Inventory Deserialize(byte[] bytes) {
		
		String string = new String(bytes, StandardCharsets.UTF_8);
		
		String line = "foo,bar,c;qual=\"baz,blurb\",d;junk=\"quux,syzygy\"";
        String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // --> ;
        
		return null;
	}
	
}
