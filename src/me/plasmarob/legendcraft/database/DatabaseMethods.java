package me.plasmarob.legendcraft.database;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.World;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.util.Tools;

public class DatabaseMethods {
	private static DatabaseMethods instance = null;
	private static Database db;
	public static DatabaseMethods getInstance(LegendCraft plugin) {
		if(instance == null) {
			instance = new DatabaseMethods(plugin);
		}
		return instance;
	}
	protected DatabaseMethods(LegendCraft plugin) {
		db = plugin.getDatabase();
	}
	
	
	public static void addWorld(UUID uuid, String name) {
		db.updateQuery("REPLACE INTO world (uuid,name) VALUES('"+ uuid.toString() + "','" + name + "');");
		return;
	}
	public static void removeWorld(UUID uuid) {
		db.updateQuery("DELETE FROM world WHERE uuid = '"+ uuid.toString() + "';");
		return;
	}
	public static int getWorldId(UUID uuid) {
		List<Map<String, Object>> results = db.readQuery("SELECT id from world WHERE uuid = '"+ uuid.toString() + "';");
		for (Map<String,Object> m : results) {
				return (Integer) m.get("id");
		}
		//db.updateQuery("DELETE FROM world WHERE uuid = '"+ uuid.toString() + "';");
		return -1;
	}
	public static boolean containsWorld(UUID uuid) {	
		List<Map<String, Object>> results = db.readQuery("SELECT uuid FROM world where uuid = '" + uuid.toString() + "'");
		for (Map<String,Object> m : results) {
			if (m.get("uuid").equals(uuid.toString()))
				return true;
		}
		return false;		
		/*
		Connection conn = db.getSQLConnection();
        PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("SELECT uuid,name FROM world where uuid = ?");
			ps.setString(1, uuid.toString());                                                                                                                                                                                                                           					  
	        ResultSet rs = plugin.getDatabase().readQuery(conn, ps);
	        while(rs.next()){
	        	if (UUID.fromString(rs.getString("uuid")).equals(uuid))
	            	return true;
	        }
	        return false;
		} catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }		
        return false;
        */
	}
	public static int insertDungeon(World world, String name, int[] corners) {	
		int id = db.insertQuery("INSERT INTO dungeon (id,world_id,name,min,max) " +
						"VALUES(NULL,'"+ 
						Integer.toString(getWorldId(world.getUID())) + "','" + 
						name + "'," +
						"'" + Tools.locationAsString(corners[0],corners[1],corners[2]) + "'," +
						"'" + Tools.locationAsString(corners[3],corners[4],corners[5]) + "');");
		return id;
	}
	public static List<Map<String, Object>> getDungeons() {	
		List<Map<String, Object>> results = db.readQuery("SELECT d.*,w.uuid FROM dungeon AS d JOIN world AS w ON w.id=d.world_id ");
		return results;
	}
	public static List<Map<String, Object>> getBlocks(String type_name) {	
		List<Map<String, Object>> results = db.readQuery("SELECT b.*,bt.id FROM block AS b JOIN blockType AS bt ON bt.id=b.type_id WHERE bt.name = '" + type_name + "'");
		return results;
	}
	public static List<Map<String, Object>> getBlocksIdJoined(String table, String joinedTable) {	
		List<Map<String, Object>> results = db.readQuery("SELECT x.*,y.* FROM `" + table + "` AS x JOIN '" + joinedTable + "' AS y ON y.block_id=x.id");
		return results;
	}
	
	public static int getBlockType(String name) {	
		List<Map<String, Object>> results = db.readQuery("SELECT id FROM blockType WHERE name = '" + name + "'");
		return (int) results.get(0).get("id");
	}
	public static boolean containsBlock(String name) {	
		List<Map<String, Object>> results = db.readQuery("SELECT name FROM world where name = '" + name + "'");
		for (Map<String,Object> m : results) {
			if (m.get("name").equals(name))
				return true;
		}
		return false;		
	}
	public static void insertBlock(World world, String name, int[] corners) {	
		/*
		"`id` INTEGER PRIMARY KEY," +
    		"`dungeon_id` INTEGER NOT NULL," +
    		"`type_id` INTEGER NOT NULL," +
            "`name` varchar(255) NOT NULL," +
            "`location` TEXT," +
            "`default` INTEGER," +
            "`inverted` INTEGER," +
            "`min` TEXT," +
            "`max` TEXT," +
            "`times` INTEGER," 
		*/
		db.updateQuery("INSERT INTO block (id, dungeon_id, type_id, name, min, max) " +
						"VALUES(NULL,'"+ 
						Integer.toString(getWorldId(world.getUID())) + "','" + 
						name + "'," +
						Tools.locationAsString(corners[0],corners[1],corners[2]) + "," +
						Tools.locationAsString(corners[3],corners[4],corners[5]) + ",);");
		return;
	}
	
	
	
	
}
