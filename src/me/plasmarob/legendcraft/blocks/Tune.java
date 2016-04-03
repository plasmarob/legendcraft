package me.plasmarob.legendcraft.blocks;

import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.plasmarob.util.Tools;
import net.minecraft.server.v1_9_R1.PacketPlayOutNamedSoundEffect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Tune {
	
	public static ConcurrentSet<Tune> tunes = new ConcurrentSet<Tune>();
	public static ConcurrentHashMap<Player, String> tuneStrings = new ConcurrentHashMap<Player, String>();
	
	
	Player player;
	List<String> song;
	List<Player> players = new ArrayList<Player>();
	float volume = 1f;
	Sound sound = Sound.BLOCK_NOTE_HARP;
	
	String next = "";
	String instrument = "note.harp";
	int step = -1;
	int note;
	// $ note.bass = wood (base guitar)
	// note.bassattack = same??? NOT USED
	// ! note.bd = stone (base drum)
	// - note.harp = air (piano)
	// ^ note.hat = glass (clicks)
	// ~ note.pling = ELONGATED PIANO
	// " note.snare = sand
	
	@SuppressWarnings("serial")
	static final List<String> noteArray = new ArrayList<String>() {{
		add("F#"); add("G"); add("G#"); add("A"); add("A#"); add("B"); add("C"); 
		add("C#"); add("D"); add("D#"); add("E"); add("F"); add("f#"); add("g"); 
		add("g#"); add("a"); add("a#"); add("b"); add("c"); add("c#"); add("d"); 
		add("d#"); add("e"); add("f"); add("f*");
	}};
	int pauseTime = 0;
	
	public Tune(Player player, ArrayList<String> song)
	{
		if (tunes.contains(this))
			return;
		this.player = player;
		this.song = song;
		//collect nearby players to play it for them too
		List<Entity> hearers = player.getNearbyEntities(16, 16, 16);
		for (Iterator<Entity> it = hearers.iterator(); it.hasNext();) {
	        Entity e = it.next();
			if (e instanceof Player)
				players.add((Player)e);
		}
		players.add(player);
		tunes.add(this);
	}
	
	public Tune(ArrayList<String> song, Location location, int radius, float volume) {
		if (tunes.contains(this))
			return;

		this.song = song;
		this.volume = volume;
		//collect nearby players to play it for them too
		List<Entity> hearers = Tools.getEntitiesAroundPoint(location, radius);
		for (Iterator<Entity> it = hearers.iterator(); it.hasNext();) {
	        Entity e = it.next();
			if (e instanceof Player)
				players.add((Player)e);
		}
		tunes.add(this);
	}
	
	
	/**
	 * Processing / creation method
	 * @param player
	 * @param input
	 */
	public Tune(Player player, final String input)
	{
		//float note = getPitch(Integer.valueOf(args[1]));
		
		// $ note.bass = wood (base guitar)
		// note.bassattack = same??? NOT USED
		// ! note.bd = stone (base drum)
		// - note.harp = air (piano)
		// ^ note.hat = glass (clicks)
		// ~ note.pling = ELONGATED PIANO
		// " note.snare = sand
		ArrayList<String> tune = new ArrayList<String>();
		for (int i = 0; i < input.length(); i++)
		{
			switch (input.charAt(i))
			{
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
				if ( i+1 < input.length() && (input.charAt(i+1) == '#' || 
				(input.charAt(i) == 'f' && input.charAt(i+1) == '*') )) {
					tune.add(input.substring(i,i+2));
					i++;
				}
				else
					tune.add(input.substring(i,i+1));
				break;
			case '$':	// base guitar
			case '!':	// base drum
			case '-':	// piano default
			case '^':	// hat
			case '~':	// elongated harp
			case '\"':	// snare
			case '_':	// mute
				tune.add(input.substring(i,i+1));
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				int sum = 1;
				while (true)
				{
					if (i+sum == input.length())
						break;
					if (input.charAt(i+sum) == '0' ||
						input.charAt(i+sum) == '1' ||
						input.charAt(i+sum) == '2' ||
						input.charAt(i+sum) == '3' ||
						input.charAt(i+sum) == '4' ||
						input.charAt(i+sum) == '5' ||
						input.charAt(i+sum) == '6' ||
						input.charAt(i+sum) == '7' ||
						input.charAt(i+sum) == '8' ||
						input.charAt(i+sum) == '9')
						sum++;
					else
						break;
				}
				tune.add(input.substring(i,i+sum));
				i = i + sum-1;
				break;
			default:
				player.sendMessage(ChatColor.RED + "Invalid tune.");
				player.sendMessage(ChatColor.RED + "Usage: /lc ps <tune>");
				i = input.length(); // force out of the loop
				return; // Cancel by getting out of this constructor
			}
		}
		
		if (tuneStrings.containsKey(player))
			tuneStrings.remove(player);
		tuneStrings.put(player, input);
		new Tune(player, tune);	
	}
	
	
	
	
	/**
	 *  MAIN EXTERNAL CONSTRUCTOR
	 * @param block
	 * @param input
	 * @param radius
	 * @param volume
	 */
	public Tune(Block block, final String input, int radius, float volume) {
		// TODO Auto-generated constructor stub
		//TODO: I need to refactor this code to add the player to the list of hearing players.
		//I need to get entities within a radius of a block to play the sound to
		//I need to be able to call a tune externally w/o a player

		//float note = getPitch(Integer.valueOf(args[1]));
		
		// $ note.bass = wood (base guitar)
		// note.bassattack = same??? NOT USED
		// ! note.bd = stone (base drum)
		// - note.harp = air (piano)
		// ^ note.hat = glass (clicks)
		// ~ note.pling = ELONGATED PIANO
		// " note.snare = sand
		ArrayList<String> tune = new ArrayList<String>();
		for (int i = 0; i < input.length(); i++)
		{
			switch (input.charAt(i))
			{
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
				if ( i+1 < input.length() && (input.charAt(i+1) == '#' || 
				(input.charAt(i) == 'f' && input.charAt(i+1) == '*') )) {
					tune.add(input.substring(i,i+2));
					i++;
				}
				else
					tune.add(input.substring(i,i+1));
				break;
			case '$':	// base guitar
			case '!':	// base drum
			case '-':	// piano default
			case '^':	// hat
			case '~':	// elongated harp
			case '\"':	// snare
			case '_':	// mute
				tune.add(input.substring(i,i+1));
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				int sum = 1;
				while (true)
				{
					if (i+sum == input.length())
						break;
					if (input.charAt(i+sum) == '0' ||
						input.charAt(i+sum) == '1' ||
						input.charAt(i+sum) == '2' ||
						input.charAt(i+sum) == '3' ||
						input.charAt(i+sum) == '4' ||
						input.charAt(i+sum) == '5' ||
						input.charAt(i+sum) == '6' ||
						input.charAt(i+sum) == '7' ||
						input.charAt(i+sum) == '8' ||
						input.charAt(i+sum) == '9')
						sum++;
					else
						break;
				}
				tune.add(input.substring(i,i+sum));
				i = i + sum-1;
				break;
			default:
				i = input.length(); // force out of the loop
				return; // Cancel by getting out of this constructor
			}
		}
		
		new Tune(tune, block.getLocation(), radius, volume);	
	}



	public boolean progress() {	
		
		if (pauseTime > 0) {
			pauseTime--;
			return false;
		}

		boolean noErr = true;
		while (noErr)
		{
			noErr = false;
			step++;
			if (step >= song.size() )
			{
				tunes.remove(this);
				return false;
			}
			
			next = song.get(step);
			note = noteArray.indexOf(next);
			if (note != -1)
				break;
			
			if (next.matches(".*\\d.*"))
			{
				try {
					pauseTime = Integer.parseInt(next);
				} catch (Exception e) {
					e.printStackTrace();
					tunes.remove(this);
					return false;
				}
				pauseTime--;
				return false;
			}
			
			
			switch (next)
			{
			case "$":
				instrument = "note.bass";
				sound = Sound.BLOCK_NOTE_BASS;
				noErr = true;
				break;
			case "!":
				instrument = "note.bd";
				sound = Sound.BLOCK_NOTE_BASEDRUM;
				noErr = true;
				break;
			case "^":
				instrument = "note.hat";
				sound = Sound.BLOCK_NOTE_HAT;
				noErr = true;
				break;
			case "~":
				instrument = "note.pling";
				sound = Sound.BLOCK_NOTE_PLING;
				noErr = true;
				break;
			case "\"":
				instrument = "note.snare";
				sound = Sound.BLOCK_NOTE_SNARE;
				noErr = true;
				break;
			case "_":
				return false;
			case "-":
			default:
				instrument = "note.harp";
				sound = Sound.BLOCK_NOTE_HARP;
				noErr = true;
				break;
			}
	
		}
		
	
		
		//The packet witchcraft that makes this all possible
		for (Player p : players)
		{
			p.playSound(p.getEyeLocation(), sound, volume, getPitch(note));
			/*
			PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(
					instrument, 
					p.getEyeLocation().getX(),p.getEyeLocation().getY(),p.getEyeLocation().getZ(), 
					volume, getPitch(note));
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(sound);
			*/
		}

		return false;
	}
	
	public static void progressAll() {
		if (tunes.size() > 0)
			for (Tune t : tunes)
				t.progress();
	}
	public static boolean progress(Tune t) {
		if (tunes.contains(t))
			return t.progress();
		return false;
	}
	
	
	
	
	
	static final float[] notes = {0.5f, 0.53f, 0.56f, 0.6f, 0.63f, 
		0.67f, 0.7f, 0.75f, 0.8f, 0.85f, 
		0.9f, 0.95f, 1.0f, 1.05f, 1.1f, 
		1.2f, 1.25f, 1.32f, 1.4f, 1.5f, 
		1.6f, 1.7f, 1.8f, 1.9f, 2.0f};
	public static float getPitch(int n)
	{
		if (n > 24 || n < 0) n = 0;
		return notes[n];
	}
}
