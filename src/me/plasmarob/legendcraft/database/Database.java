package me.plasmarob.legendcraft.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import me.plasmarob.legendcraft.database.Error; // YOU MUST IMPORT THE CLASS ERROR, AND ERRORS!!!
import me.plasmarob.legendcraft.database.Errors;


import me.plasmarob.legendcraft.LegendCraft;


public abstract class Database {
	LegendCraft plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    public String table = "world";
    public int tokens = 0;
    public Database(LegendCraft instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    // attempts to get results
    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ''");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }
    
/*

    // These are the methods you can use to get things out of your database. You of course can make new ones to return different things in the database.
    // This returns the number of people the player killed.
    public Integer getTokens(String string) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE player = '"+string+"';");
    
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("player").equalsIgnoreCase(string.toLowerCase())){ // Tell database to search for the player you sent into the method. e.g getTokens(sam) It will look for sam.
                    return rs.getInt("kills"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }
    
    
    // Exact same method here, Except as mentioned above i am looking for total!
    public Integer getTotal(String string) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE player = '"+string+"';");
    
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("player").equalsIgnoreCase(string.toLowerCase())){
                    return rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }


	// Now we need methods to save things to the database
    public void setTokens(Player player, Integer tokens, Integer total) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + table + " (player,kills,total) VALUES(?,?,?)"); // IMPORTANT. In SQLite class, We made 3 colums. player, Kills, Total.
            ps.setString(1, player.getName().toLowerCase());                                             // YOU MUST put these into this line!! And depending on how many
                                                                                                         // colums you put (say you made 5) All 5 need to be in the brackets
                                                                                                         // Seperated with comma's (,) AND there needs to be the same amount of
                                                                                                         // question marks in the VALUES brackets. Right now i only have 3 colums
                                                                                                         // So VALUES (?,?,?) If you had 5 colums VALUES(?,?,?,?,?)
                                                                                                 
            ps.setInt(2, tokens); // This sets the value in the database. The colums go in order. Player is ID 1, kills is ID 2, Total would be 3 and so on. you can use
                                  // setInt, setString and so on. tokens and total are just variables sent in, You can manually send values in as well. p.setInt(2, 10) <-
                                  // This would set the players kills instantly to 10. Sorry about the variable names, It sets their kills to 10 i just have the variable called
                                  // Tokens from another plugin :/
            ps.setInt(3, total);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;        
    }
*/
    /*
    public ResultSet readQuery(Connection conn, PreparedStatement ps) {
		try {
            ResultSet rs = ps.executeQuery();
			return rs;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
        return null;    
    }
    */
    
    
    /**
	 * Queries the Database, for queries which return results.
	 *
	 * @param query Query to run
	 * @return Result set of ran query
	 */
	public List<Map<String, Object>> readQuery(String query) {
		Connection conn = null;
    	PreparedStatement ps = null; 	
		try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(query); 
            ResultSet rs = ps.executeQuery();
			return resultSetAsList(rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;     
	}
	
	/**
	 * Queries the Databases, for queries which modify data.
	 *
	 * @param query Query to run
	 */
	public boolean updateQuery(String query) {
		Connection conn = null;
    	PreparedStatement ps = null;
    	
    	try {
    		conn = getSQLConnection();
            ps = conn.prepareStatement(query); 		  
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
                return false;
            }
        }
        return true;    
	}
    
	
	/**
	 * Queries the Databases, for queries which modify data.
	 *
	 * @param query Query to run
	 */
	public int insertQuery(String query) {
		Connection conn = null;
    	PreparedStatement ps = null;
    	
    	try {
    		conn = getSQLConnection();
            ps = conn.prepareStatement(query); 		  
            ps.executeUpdate();
            ps.close();
            
            ps = conn.prepareStatement("select last_insert_rowid() as id;"); 
            ResultSet rs = ps.executeQuery();
            return Integer.parseInt(rs.getString("id"));
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
                return 0;
            }
        }
    	return 0; 
	}
	
	/**
	 * Queries the Databases, for queries which modify data.
	 *
	 * @param query Query to run
	 */
	public int insertBLOBQuery(String query, byte[] blob) {
		Connection conn = null;
    	PreparedStatement ps = null;
    	
    	try {
    		conn = getSQLConnection();
            ps = conn.prepareStatement(query); 		
            ps.setBytes(1, blob);
            ps.executeUpdate();
            ps.close();
            
            ps = conn.prepareStatement("select last_insert_rowid() as id;"); 
            ResultSet rs = ps.executeQuery();
            return Integer.parseInt(rs.getString("id"));
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
                return 0;
            }
        }
    	return 0; 
	}
	
	/*
	public boolean containsWorld(UUID uuid) {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	
    	try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT uuid,name FROM world where uuid = ?"); 					  
            ps.setString(1, uuid.toString());                                                                                                                                                                                                                           					  
            rs = ps.executeQuery();
            while(rs.next()){
            	if (UUID.fromString(rs.getString("uuid")).equals(uuid))
	            	return true;
            }
            return false;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    	
		return false;
	}
	*/
	
	
    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
    
    public List<Map<String, Object>> resultSetAsList(ResultSet rs) throws SQLException{
    	ResultSetMetaData md = rs.getMetaData();
    	int columns = md.getColumnCount();
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	while (rs.next()){
    		Map<String, Object> row = new HashMap<String, Object>(columns);
    		for(int i=1; i<=columns; ++i){           
    			row.put(md.getColumnName(i),rs.getObject(i));
    		}
    		list.add(row);
    	}
		return list;
    }

	
}
