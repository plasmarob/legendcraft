package me.plasmarob.legendcraft.database;

import java.util.logging.Level;

import me.plasmarob.legendcraft.LegendCraft;

public class Error {
    public static void execute(LegendCraft plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(LegendCraft plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
