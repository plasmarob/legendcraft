package me.plasmarob.legendcraft.database;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import me.plasmarob.legendcraft.LegendCraft;

public class DatabaseSelecter {
	
	StringBuilder query = new StringBuilder("SELECT * FROM ");
	StringJoiner columns = new StringJoiner(",");
	StringJoiner values = new StringJoiner(",");
	
	public DatabaseSelecter(String table) {
		query.append(table);
	}
	
	public DatabaseSelecter whereEquals(String key, int val) {
		query.append(" WHERE " + key + "=" + val);
		return this;
	}
	public DatabaseSelecter whereEquals(String key, String val) {
		query.append(" WHERE " + key + "='" + val + "'");
		return this;
	}
	
	public List<Map<String, Object>> execute() {	
		query.append(";");
		List<Map<String, Object>> results = LegendCraft.plugin.getDatabase().readQuery(query.toString());
		return results;
	}
}
