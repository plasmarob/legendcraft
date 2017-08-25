package me.plasmarob.legendcraft.database;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.plasmarob.legendcraft.LegendCraft;

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
	public static void insertDungeon(int world_id, String name, int[] corners) {	
		db.updateQuery("REPLACE INTO dungeon (id,world_id,name,min,max) " +
					"VALUES(NULL,'"+ Integer.toString(world_id) + "','" + name + "'," +
				locationAsString(corners[0],corners[1],corners[2]) + "," +
				locationAsString(corners[3],corners[4],corners[5]) + ",);");
		return;
	}
	
		
	public static String locationAsString(int x, int y, int z) {
		return Integer.toString(x) + "," + Integer.toString(y) + "," + Integer.toString(z);
	}	
	
	
	
	
	
}
