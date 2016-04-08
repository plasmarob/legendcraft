package me.plasmarob.legendcraft.blocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.legendcraft.LegendCraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("unused")
public class MobTemplate {

	//Global storage
	public static File mobsFolder;
	public static ConcurrentHashMap<String, MobTemplate> mobs = new ConcurrentHashMap<String, MobTemplate>();
	public static ConcurrentHashMap<String, FileConfiguration> mobConfigs = new ConcurrentHashMap<String, FileConfiguration>();
		
	
	
	
	
	
	
	
	
	
	String mobFileName;
	String customName;
	EntityType type;
	double health;
	ItemStack helm,chest,legs,boots,hand;
	float helmProb, chestProb, legProb, bootProb, handProb;
	Collection<PotionEffect> potioneffects;
	
	public MobTemplate(LivingEntity mob, String myname)
	{
		mobFileName = myname;
		customName = mob.getCustomName();
		type = mob.getType();
		health = mob.getHealth();
		potioneffects = mob.getActivePotionEffects();
		
		helm = mob.getEquipment().getHelmet();
		chest = mob.getEquipment().getChestplate();
		legs = mob.getEquipment().getLeggings();
		boots = mob.getEquipment().getBoots();
		hand = mob.getEquipment().getItemInMainHand();
		
		helmProb = mob.getEquipment().getHelmetDropChance();
		chestProb = mob.getEquipment().getChestplateDropChance();
		legProb = mob.getEquipment().getLeggingsDropChance();
		bootProb = mob.getEquipment().getBootsDropChance();
		handProb = mob.getEquipment().getItemInMainHandDropChance();
	}
	
	@SuppressWarnings("deprecation")
	public MobTemplate(FileConfiguration config, String myname) {
		mobFileName = myname;
		customName = config.getString("customname");
		type = EntityType.fromName(config.getString("type"));
		health = config.getDouble("health");
		
		potioneffects = new ArrayList<PotionEffect>();
		PotionEffect pe;
		boolean potionEnd = false;
		int i = 1;
		String type;
		int duration;
		int amplifier;
		boolean ambient;
		boolean particles;
		while (!potionEnd) {
			type = config.getString("potioneffects.p" + Integer.toString(i) + ".type");
			if (type != null) 
			{
				duration = config.getInt("potioneffects.p" + Integer.toString(i) + ".duration");
				amplifier = config.getInt("potioneffects.p" + Integer.toString(i) + ".amplifier");
				ambient = config.getBoolean("potioneffects.p" + Integer.toString(i) + ".ambient");
				particles = config.getBoolean("potioneffects.p" + Integer.toString(i) + ".particles");
				potioneffects.add(
						new PotionEffect(
								PotionEffectType.getByName(type),
								duration,
								amplifier,
								ambient,
								particles));
			} 
			else
				potionEnd = true;
			i++;
		}
		
		boolean itemEnd;
		itemEnd = false;
		helm = new ItemStack(Material.getMaterial(config.getString("helmet.type")));
		helm.setAmount(config.getInt("helmet.amount"));
		helm.setData(new MaterialData((byte)config.getInt("helmet.data")));
		helm.setDurability((short)config.getInt("helmet.durability"));
		helmProb = (float)config.getDouble("helmet.chance");
		Map<Enchantment, Integer> helmEnchants = new HashMap<Enchantment, Integer>();
		i = 1;
		while (!itemEnd)
		{
			String name = config.getString("helmet.enchant.p" + Integer.toString(i) + ".name");
			if (name != null)
			{
				Integer level = config.getInt("helmet.enchant.p" + Integer.toString(i) + ".level");
				helmEnchants.put(Enchantment.getByName(name),level);
			}
			else 
				itemEnd = true;
			i++;
		}
		helm.addEnchantments(helmEnchants);
		
		itemEnd = false;
		chest = new ItemStack(Material.getMaterial(config.getString("chestplate.type")));
		chest.setAmount(config.getInt("chestplate.amount"));
		chest.setData(new MaterialData((byte)config.getInt("chestplate.data")));
		chest.setDurability((short)config.getInt("chestplate.durability"));
		chestProb = (float)config.getDouble("chestplate.chance");
		Map<Enchantment, Integer> chestEnchants = new HashMap<Enchantment, Integer>();
		i = 1;
		while (!itemEnd)
		{
			String name = config.getString("chestplate.enchant.p" + Integer.toString(i) + ".name");
			if (name != null)
			{
				Integer level = config.getInt("chestplate.enchant.p" + Integer.toString(i) + ".level");
				chestEnchants.put(Enchantment.getByName(name),level);
			}
			else 
				itemEnd = true;
			i++;
		}
		chest.addEnchantments(chestEnchants);
		
		itemEnd = false;
		legs = new ItemStack(Material.getMaterial(config.getString("leggings.type")));
		legs.setAmount(config.getInt("leggings.amount"));
		legs.setData(new MaterialData((byte)config.getInt("leggings.data")));
		legs.setDurability((short)config.getInt("leggings.durability"));
		legProb = (float)config.getDouble("leggings.chance");
		Map<Enchantment, Integer> legsEnchants = new HashMap<Enchantment, Integer>();
		i = 1;
		while (!itemEnd)
		{
			String name = config.getString("leggings.enchant.p" + Integer.toString(i) + ".name");
			if (name != null)
			{
				Integer level = config.getInt("leggings.enchant.p" + Integer.toString(i) + ".level");
				legsEnchants.put(Enchantment.getByName(name),level);
			}
			else 
				itemEnd = true;
			i++;
		}
		legs.addEnchantments(legsEnchants);
		
		itemEnd = false;
		boots = new ItemStack(Material.getMaterial(config.getString("boots.type")));
		boots.setAmount(config.getInt("boots.amount"));
		boots.setData(new MaterialData((byte)config.getInt("boots.data")));
		boots.setDurability((short)config.getInt("boots.durability"));
		bootProb = (float)config.getDouble("boots.chance");
		Map<Enchantment, Integer> bootsEnchants = new HashMap<Enchantment, Integer>();
		i = 1;
		while (!itemEnd)
		{
			String name = config.getString("boots.enchant.p" + Integer.toString(i) + ".name");
			if (name != null)
			{
				Integer level = config.getInt("boots.enchant.p" + Integer.toString(i) + ".level");
				bootsEnchants.put(Enchantment.getByName(name),level);
			}
			else 
				itemEnd = true;
			i++;
		}
		boots.addEnchantments(bootsEnchants);
		
	    itemEnd = false;
	    hand = new ItemStack(Material.getMaterial(config.getString("hand.type")));
	    hand.setAmount(config.getInt("hand.amount"));
	    hand.setData(new MaterialData((byte)config.getInt("hand.data")));
	    hand.setDurability((short)config.getInt("hand.durability"));
	    handProb = (float)config.getDouble("hand.chance");
	    Map<Enchantment, Integer> handEnchants = new HashMap<Enchantment, Integer>();
	    i = 1;
	    while (!itemEnd)
	    {
	    	String name = config.getString("hand.enchant.p" + Integer.toString(i) + ".name");
	    	if (name != null)
	    	{
	            Integer level = config.getInt("hand.enchant.p" + Integer.toString(i) + ".level");
	            handEnchants.put(Enchantment.getByName(name),level);
	    	}
	    	else 
	    		itemEnd = true;
	    	i++;
	    }
	    hand.addEnchantments(handEnchants);
		
	}

	@SuppressWarnings("deprecation")
	public FileConfiguration getConfig() {
		
		FileConfiguration config = new YamlConfiguration();
		config.set("customname", customName);
		config.set("type", type.toString());
		config.set("health", health);
		int i = 1;
		for (PotionEffect pe : potioneffects)
		{
			config.set("potioneffects.p" + Integer.toString(i) + ".type", pe.getType().getName());
			config.set("potioneffects.p" + Integer.toString(i) + ".duration", pe.getDuration());
			config.set("potioneffects.p" + Integer.toString(i) + ".amplifier", pe.getAmplifier());
			config.set("potioneffects.p" + Integer.toString(i) + ".ambient", pe.isAmbient());
			config.set("potioneffects.p" + Integer.toString(i) + ".particles", pe.hasParticles());
			i++;
		}
		
		config.set("helmet.type", helm.getType().toString());
		config.set("helmet.amount", helm.getAmount());
		config.set("helmet.data", helm.getData().getData());
		config.set("helmet.durability", helm.getDurability());
		config.set("helmet.chance", helmProb);
		Map<Enchantment, Integer> helmEnchants =  helm.getEnchantments();
		i = 1;
		for (Enchantment e : helmEnchants.keySet())
		{
			config.set("helmet.enchant.p" + Integer.toString(i) + ".name", e.getName());
			config.set("helmet.enchant.p" + Integer.toString(i) + ".level", helmEnchants.get(e));
			i++;
		}
			
		config.set("chestplate.type", chest.getType().toString());
		config.set("chestplate.amount", chest.getAmount());
		config.set("chestplate.data", chest.getData().getData());
		config.set("chestplate.durability", chest.getDurability());
		config.set("chestplate.chance", chestProb);
		Map<Enchantment, Integer> chestEnchants =  chest.getEnchantments();
		i = 1;
		for (Enchantment e : chestEnchants.keySet())
		{
			config.set("chestplate.enchant.p" + Integer.toString(i) + ".name", e.getName());
			config.set("chestplate.enchant.p" + Integer.toString(i) + ".level", chestEnchants.get(e));
			i++;
		}
	
		config.set("leggings.type", legs.getType().toString());
		config.set("leggings.amount", legs.getAmount());
		config.set("leggings.data", legs.getData().getData());
		config.set("leggings.durability", legs.getDurability());
		config.set("leggings.chance", legProb);
		Map<Enchantment, Integer> legsEnchants =  legs.getEnchantments();
		i = 1;
		for (Enchantment e : legsEnchants.keySet())
		{
			config.set("leggings.enchant.p" + Integer.toString(i) + ".name", e.getName());
			config.set("leggings.enchant.p" + Integer.toString(i) + ".level", legsEnchants.get(e));
			i++;
	    }	
		
		config.set("boots.type", boots.getType().toString());
		config.set("boots.amount", boots.getAmount());
		config.set("boots.data", boots.getData().getData());
		config.set("boots.durability", boots.getDurability());
		config.set("boots.chance", bootProb);
		Map<Enchantment, Integer> bootsEnchants =  boots.getEnchantments();
		i = 1;
		for (Enchantment e : bootsEnchants.keySet())
		{
			config.set("boots.enchant.p" + Integer.toString(i) + ".name", e.getName());
			config.set("boots.enchant.p" + Integer.toString(i) + ".level", bootsEnchants.get(e));
			i++;
		}
		
		config.set("hand.type", hand.getType().toString());
		config.set("hand.amount", hand.getAmount());
		config.set("hand.data", hand.getData().getData());
		config.set("hand.durability", hand.getDurability());
		config.set("hand.chance", handProb);
		Map<Enchantment, Integer> handEnchants =  hand.getEnchantments();
		i = 1;
		for (Enchantment e : handEnchants.keySet())
		{
			config.set("hand.enchant.p" + Integer.toString(i) + ".name", e.getName());
			config.set("hand.enchant.p" + Integer.toString(i) + ".level", handEnchants.get(e));
			i++;
		}
		
		return config;
	}
	
	public String getMyName()
	{
		return mobFileName;
	}
	
	public EntityType getType()
	{
		return type;
	}
	
	public Entity spawn(Location loc)
	{
		LivingEntity mob = (LivingEntity) loc.getWorld().spawnEntity(loc, type);
		
		mob.setRemoveWhenFarAway(false);
		mob.setCustomName(customName);
		mob.setHealth(health);
		
		//EntityEquipment ee = mob.getEquipment();
		
		mob.getEquipment().setHelmet(helm);
		mob.getEquipment().setChestplate(chest);
		mob.getEquipment().setLeggings(legs);
		mob.getEquipment().setBoots(boots);
		mob.getEquipment().setItemInMainHand(hand);
		
		mob.getEquipment().setHelmetDropChance(helmProb);
		mob.getEquipment().setChestplateDropChance(chestProb);
		mob.getEquipment().setLeggingsDropChance(legProb);
		mob.getEquipment().setBootsDropChance(bootProb);
		mob.getEquipment().setItemInMainHandDropChance(handProb);
		
		for (PotionEffect pe: potioneffects) {
			mob.addPotionEffect(pe);
		}
		return mob;
	}
	
	
	public static MobTemplate getMobTemplate(String name)
	{
		if (mobs.containsKey(name))
			return mobs.get(name);
		else
			return null;
	}
	
	public static void loadMobs()
	{
		try {
	    	File[] mobList = mobsFolder.listFiles();
	    	for (int i = 0; i < mobList.length; i++) 
	    	{
	    		if (mobList[i].isFile())
	    		{	
	    			String myname = mobList[i].getName().substring(0, mobList[i].getName().lastIndexOf('.'));
	    			FileConfiguration newMobConfig = new YamlConfiguration();
    				newMobConfig.load(mobList[i]);
    				mobConfigs.put(myname, newMobConfig); 
    				mobs.put(myname, new MobTemplate(newMobConfig,myname));
	    		}
	    	}
	    } catch (Exception e) {e.printStackTrace();}
	}
	
	public static void saveMobs()
	{
		for (String name : mobs.keySet())
		{
			MobTemplate mt = mobs.get(name);
		
			File mobFile = new File(mobsFolder, name + ".yml");
			LegendCraft.plugin.copyYamlsToFile(LegendCraft.plugin.getResource(name + ".yml"), mobFile);
			FileConfiguration mobsConfig = mt.getConfig();
			try {
				mobsConfig.save(mobFile);
			} catch (IOException e) {e.printStackTrace();}

		}
	}
}
