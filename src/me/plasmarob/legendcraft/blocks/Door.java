package me.plasmarob.legendcraft.blocks;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;

import me.plasmarob.legendcraft.LegendCraft;
import me.plasmarob.legendcraft.util.Tools;
import net.md_5.bungee.api.ChatColor;

public class Door implements Receiver {

	private String name;
	private boolean enabled = false;
	private boolean defaultOnOff = true;
	private boolean isOn = true;
	private boolean inverted = false;
	
	private boolean isLockedDoor = false;
	private boolean isCurrentlyLocked = false;
	private ItemFrame frame1;
	private ItemFrame frame2;
	
	private int barring = 0; //binary: 1=01b=front, 2=10b=back, 3=11b=both
	private boolean isBarred = false;
	public boolean isBarred() { return isBarred; }
	public boolean isFrontBarred() { return (barring & 1) != 0; }
	public boolean isBackBarred() { return (barring & 2) != 0; }
	public void setFrontBarred(boolean barred) { barring = (barring | 1); }
	public void setBackBarred(boolean barred) { barring = (barring | 2); }
	
	Material[][][] matList;
	Byte[][][] datList;
	
	private Vector min;
	private Vector max;

	private World world;
	private BlockFace openDirection = BlockFace.UP;
	private boolean isRunning = false;
	
	private BlockFace facing; 
	private BlockFace oppositeFacing; 
	private int dx,dy,dz;
	private int mx,my,mz,Mx,My,Mz;
	private Location bottomCenter;
	
	public Material[][][] getMatList() { return this.matList.clone(); }
	public Byte[][][] getDatList() { return this.datList.clone(); }
	
	public Vector getMin() { return min; }
	public Vector getMax() { return max; }
	public World getWorld() { return world; }

	public Location getBottomCenter() { return bottomCenter; }

	BukkitTask animator;
	boolean animating = false;
	
	/**
	 * Constructor 1: create a new door
	 */
	@SuppressWarnings("deprecation")
	public Door(Player player, String name) {
		this.name = name;
		
		Selection sel = LegendCraft.worldEditPlugin.getSelection(player);
		min = sel.getNativeMinimumPoint();
        max = sel.getNativeMaximumPoint();
        mx = min.getBlockX();
        my = min.getBlockY();
        mz = min.getBlockZ();
        Mx = max.getBlockX();
        My = max.getBlockY();
        Mz = max.getBlockZ();
        
        dx = Mx-mx+1;
        dy = My-my+1;
        dz = Mz-mz+1;
        matList = new Material[dx][dy][dz];
        datList = new Byte[dx][dy][dz];
        
        
        
        this.world = player.getWorld();
        

		Block tmpB;
        for (int x = mx; x <= Mx; x++) {
        	for (int y = my; y <= My; y++) {
        		for (int z = mz; z <= Mz; z++) {
        			tmpB = world.getBlockAt(x, y, z);
        			matList[x-mx][y-my][z-mz] = tmpB.getType();
        			datList[x-mx][y-my][z-mz] = tmpB.getData();
                }
            }
        }
        
        facing = Tools.playerCompassFace(player).getOppositeFace();
        oppositeFacing = facing.getOppositeFace();
        //Tools.say(facing);

        bottomCenter = world.getBlockAt(mx, my, mz).getLocation().add(dx/2+0.5, 0, dz/2+0.5);
        
		player.sendMessage(ChatColor.LIGHT_PURPLE + "Door created!");
	}
	
	
	/**
	 * Constructor 2: Create door copy from existing door
	 */
	@SuppressWarnings("deprecation")
	public Door(Player player, Door oldDoor, String name) {
		this.name = name;
		this.world = player.getWorld();
		//grab and rotate data from template door
		facing = Tools.playerCompassFace(player).getOppositeFace();
		oppositeFacing = facing.getOppositeFace();
		matList = oldDoor.getMatList().clone();
		datList = oldDoor.getDatList().clone();
		matList = rotateMat(matList, oldDoor.dx,oldDoor.dy,oldDoor.dz, compassAngle(oldDoor.facing.toString(), facing.toString()));
	    datList = rotateData(datList, oldDoor.dx,oldDoor.dy,oldDoor.dz, compassAngle(oldDoor.facing.toString(), facing.toString()));
	    
	    
	    
	    //Tools.say(oldDoor.facing.toString());
	   // Tools.say(facing.toString());
	    //Tools.say(compassAngle(oldDoor.facing.toString(), facing.toString()));
	   
	    //Tools.say(o);
	    // get dx,dy,dz, flipping X and Z widths if needed
	    dy = oldDoor.dy;
	    if (facing.equals(BlockFace.NORTH) || facing.equals(BlockFace.SOUTH)) {
	    	if (oldDoor.facing.equals(BlockFace.EAST) || oldDoor.facing.equals(BlockFace.WEST)) {
	    		dx = oldDoor.dz;
	    		dz = oldDoor.dx;
	    	} else { // equals N / S
	    		dx = oldDoor.dx;
	    		dz = oldDoor.dz;
	    	}
	    } else { // equals EAST or WEST
	    	if (oldDoor.facing.equals(BlockFace.NORTH) || oldDoor.facing.equals(BlockFace.SOUTH)) {
	    		dx = oldDoor.dz;
	    		dz = oldDoor.dx;
	    	} else { // equals E / W
	    		dx = oldDoor.dx;
	    		dz = oldDoor.dz;
	    	}
	    }
	    
	    
	    //find min and max
	    my = player.getLocation().getBlockY();
		My = my + dy-1;
		Block bottom = player.getLocation().getBlock().getRelative(facing);
		mx = 0;
		Mx = 0;
		mz = 0;
		Mz = 0;
	    if (facing.equals(BlockFace.NORTH)) {
	    	Mz = bottom.getLocation().getBlockZ();
	    	mz = Mz - (dz-1);
	    	mx = bottom.getLocation().getBlockX() - (int)Math.floor((dx-1)/2.0);
	    	Mx = mx + (dx-1);
	    } else if (facing.equals(BlockFace.SOUTH)) {
	    	mz = bottom.getLocation().getBlockZ();
	    	Mz = mz + (dz-1);
	    	mx = bottom.getLocation().getBlockX() - (int)Math.ceil((dx-1)/2.0);
	    	Mx = mx + (dx-1);
	    } else if (facing.equals(BlockFace.WEST)) {
	    	Mx = bottom.getLocation().getBlockX();
	    	mx = Mx - (dx-1);
	    	mz = bottom.getLocation().getBlockZ() - (int)Math.ceil((dz-1)/2.0);
	    	Mz = mz + (dz-1);
	    } else if (facing.equals(BlockFace.EAST)) {
	    	mx = bottom.getLocation().getBlockX();
	    	Mx = mx + (dx-1);
	    	mz = bottom.getLocation().getBlockZ() - (int)Math.floor((dz-1)/2.0);
	    	Mz = mz + (dz-1);
	    }
	    min = new Vector(mx,my,mz);
	    max = new Vector(Mx,My,Mz);
	    
	    Tools.say(mx + "," + my + "," + mz);
	    Tools.say(Mx + "," + My + "," + Mz);
	    
	    //set it all, accounting for if shape flipped
	    Block tmpB;
	   
        for (int x = mx; x <= Mx; x++) {
        	for (int y = my; y <= My; y++) {
        		for (int z = mz; z <= Mz; z++) {
        			tmpB = world.getBlockAt(x, y, z);
        			//Tools.say(matList[x-mx][y-my][z-mz].toString());
        			tmpB.setType(matList[x-mx][y-my][z-mz]);
        			tmpB.setData(datList[x-mx][y-my][z-mz]);
                }
            }
        }
        
        bottomCenter = world.getBlockAt(mx, my, mz).getLocation().add(dx/2+0.5, 0, dz/2+0.5);
        
        isLockedDoor = oldDoor.isLockedDoor;
        if (isLockedDoor) {
        	unlock();
        }
        
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Door copied!");
	}
	
	/**
	 * Constructor 3: Load door from file
	 */
	public Door(World world, FileConfiguration doorConfig, String name) {
		this.world = world;
		this.name = doorConfig.getString("name");
		facing = BlockFace.valueOf(doorConfig.getString("direction"));
		oppositeFacing = facing.getOppositeFace();

		//keyMat = Material.getMaterial(doorConfig.getString("keymaterial"));
		//keyDat = (byte) doorConfig.getInt("keydata");
		
		List<Integer> minXYZ = doorConfig.getIntegerList("min");
		List<Integer> maxXYZ = doorConfig.getIntegerList("max");
		min = new Vector(minXYZ.get(0), minXYZ.get(1), minXYZ.get(2));
		max = new Vector(maxXYZ.get(0), maxXYZ.get(1), maxXYZ.get(2));
		mx = min.getBlockX();
        my = min.getBlockY();
        mz = min.getBlockZ();
        Mx = max.getBlockX();
        My = max.getBlockY();
        Mz = max.getBlockZ();
		
		
		dx = max.getBlockX() - min.getBlockX() + 1;
		dy = max.getBlockY() - min.getBlockY() + 1;
		dz = max.getBlockZ() - min.getBlockZ() + 1;
		matList = new Material[dx][dy][dz];
        datList = new Byte[dx][dy][dz];
        int i = 1;
		for (int x = 0; x <= dx-1; x++) {
        	for (int y = 0; y <= dy-1; y++) {
        		for (int z = 0; z <= dz-1; z++) {
        			matList[x][y][z] = Material.getMaterial(doorConfig.getString("m" + i));
        			datList[x][y][z] = (byte)doorConfig.getInt("d" + i++);
                }
            }
        }
		bottomCenter = world.getBlockAt(mx, my, mz).getLocation().add(dx/2+0.5, 0, dz/2+0.5);
		
		try {
			isLockedDoor = doorConfig.getBoolean("locked");
			barring = doorConfig.getInt("barred");
		} catch (Exception e) {}
		
        if (isLockedDoor) {
        	unlock();
        }
	}
	
	public void dbInsert() {
		
	}

	/**
	 * Save storage block to config
	 * @param adConfig
	 */
	public void setConfig(FileConfiguration adConfig) {
		
		adConfig.set("name", name);
		adConfig.set("world", world.getName());
		adConfig.set("direction", facing.toString());
		//adConfig.set("keymaterial", keyMat.toString());
		//adConfig.set("keydata", (int)keyDat);
		
		adConfig.set("min", Arrays.asList(min.getX(),min.getY(),min.getZ()));
		adConfig.set("max", Arrays.asList(max.getX(),max.getY(),max.getZ()));
		
		dx = max.getBlockX() - min.getBlockX() + 1;
		dy = max.getBlockY() - min.getBlockY() + 1;
		dz = max.getBlockZ() - min.getBlockZ() + 1;
		int i = 1;
		for (int x = 0; x <= dx-1; x++) {
        	for (int y = 0; y <= dy-1; y++) {
        		for (int z = 0; z <= dz-1; z++) {
        			adConfig.set("m"+Integer.toString(i),matList[x][y][z].name());
        			adConfig.set("d"+Integer.toString(i++),datList[x][y][z]);
                }
            }
        }
		
		adConfig.set("locked", isLockedDoor);
		adConfig.set("barred", barring);
		/*
		for (int i = 0; i < matList.size(); i++) {
			adConfig.set("m"+Integer.toString(i+1),matList.get(i).name());
			adConfig.set("d"+Integer.toString(i+1),Integer.valueOf(datList.get(i)));
		}
		*/
	}
	
	
	public void update() {
		if (!isRunning)
			return;
		
		if (openDirection == BlockFace.UP) {
			// iterate the thing up
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
	        	for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
	        		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
	        			//tmpB = player.getWorld().getBlockAt(x, y, z);
	        			//matList.add(tmpB.getType());
	        			//datList.add(tmpB.getData());
	                }
	            }
	        }
		}
	}
	
	
	//player
	//rotateMat(matList, dx,dy,dz, compassAngle(,"north"));
	
	public static int compassAngle(String from, String to) {
		from = from.substring(0, 1).toUpperCase();
		to = to.substring(0, 1).toUpperCase();
		if (from.equals(to))
			return 0;
		
		if (from.equals("N")) {
			if (to.equals("E")) {
				return 90;
			} else if (to.equals("W")) {
				return -90;
			} else if (to.equals("S"))
				return 180;
		} else if (from.equals("S")) {
			if (to.equals("E")) {
				return -90;
			} else if (to.equals("W")) {
				return 90;
			} else if (to.equals("N"))
				return 180;
		} else if (from.equals("E")) {
			if (to.equals("N")) {
				return -90;
			} else if (to.equals("S")) {
				return 90;
			} else if (to.equals("W"))
				return 180;
		} else if (from.equals("W")) {
			if (to.equals("N")) {
				return 90;
			} else if (to.equals("S")) {
				return -90;
			} else if (to.equals("E"))
				return 180;
		}
		return 0;
	}
	public Material[][][] rotateMat(Material[][][] input, int xw, int yw, int zw, int angle) {
		Material[][][] output;
		if (Math.abs(angle) == 90)
			output = new Material[zw][yw][xw];
		else
			output = new Material[xw][yw][zw];
		
		if (angle == -90) {
			for (int x = 0; x <= zw-1; x++) {
				for (int y = 0; y <= yw-1; y++) {
					for (int z = 0; z <= xw-1; z++) {
						output[x][y][z] = input[xw-z-1][y][x];
					}
				}
			}
			return output;
		} else if (angle == 90) {
			for (int x = 0; x <= zw-1; x++) {
				for (int y = 0; y <= yw-1; y++) {
					for (int z = 0 ; z <= xw-1; z++) {
						output[x][y][z] = input[z][y][zw-x-1];
					}
				}
			}
			return output;
		} else if (Math.abs(angle) == 180) {
			for (int x = 0; x <= xw-1; x++) {
				for (int y = 0; y <= yw-1; y++) {
					for (int z = 0; z <= zw-1; z++) {
						output[x][y][z] = input[xw-x-1][y][zw-z-1];
					}
				}
			}
			return output;
		}
		else return input;
		//return output;
	}
	
	
	public Byte[][][] rotateData(Byte[][][] input, int xw, int yw, int zw, int angle) {
		Byte[][][] output;
		if (Math.abs(angle) == 90)
			output = new Byte[zw][yw][xw];
		else
			output = new Byte[xw][yw][zw];
		
		if (angle == -90) {
			for (int x = 0; x <= zw-1; x++) {
				for (int y = 0; y <= yw-1; y++) {
					for (int z = 0; z <= xw-1; z++) {
						output[x][y][z] = input[xw-z-1][y][x];
					}
				}
			}
			return output;
		} else if (angle == 90) {
			for (int x = 0; x <= zw-1; x++) {
				for (int y = 0; y <= yw-1; y++) {
					for (int z = 0 ; z <= xw-1; z++) {
						output[x][y][z] = input[z][y][zw-x-1];
					}
				}
			}
			return output;
		} else if (Math.abs(angle) == 180) {
			for (int x = 0; x <= xw-1; x++) {
				for (int y = 0; y <= yw-1; y++) {
					for (int z = 0; z <= zw-1; z++) {
						output[x][y][z] = input[xw-x-1][y][zw-z-1];
					}
				}
			}
			return output;
		}
		else return input;
	}
	
	public void stopAnimating() {
		animating = false;
		if (animator != null) {
			animator.cancel();
		}
	}
	
	public int doorThickness() {
		//Tools.say(openDirection.toString());
		if (facing.equals(BlockFace.NORTH) || facing.equals(BlockFace.SOUTH))
			return dz;
		else // EAST || WEST
			return dx;
	}
	
	public BlockFace getFacing() { return facing; }
	public BlockFace getOppositeFacing() { return oppositeFacing; }
	
	@Override
	public void trigger() {
		if (!inverted) {
			if (barring != 0 && !isLockedDoor) {
				isBarred = true;
				bar();
			} else if (!isLockedDoor) {
				isBarred = false;
				unbar();
			}	
		} else {
			
		}
	}

	@Override
	public void set() {
		if (barring != 0)
			isBarred = true;
		bar();
	}

	@Override
	public void reset() {
		isBarred = false;
		unbar();
	}

	@Override
	public void on() {
		if (enabled)
			isOn = true;
	}
	@Override
	public void off() {
		if (enabled)
			isOn = false;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setEnabled(boolean bool) {
		enabled = bool;
		
		Block tmpB;
		
		
		for (int x = mx; x <= Mx; x++) {
        	for (int y = my; y <= My; y++) {
        		for (int z = mz; z <= Mz; z++) {
        			tmpB = world.getBlockAt(x, y, z);

        			tmpB.setType(matList[x-mx][y-my][z-mz]);
        			tmpB.setData(datList[x-mx][y-my][z-mz]);
        			/*
        			if (!inverted) {
	        			if ((barring & 0x1) == 0x1) // front
	        				if (tmpB.getRelative(facing).getType().equals(Material.AIR)) 
	        					tmpB.getRelative(facing).setType(Material.IRON_FENCE);
	        			else
	        				if (tmpB.getRelative(facing).getType().equals(Material.IRON_FENCE)) 
	        					tmpB.getRelative(facing).setType(Material.AIR);
	        				
	        			if ( (barring & 0x2) == 0x2) // back
	        				if (tmpB.getRelative(oppositeFacing).getType().equals(Material.AIR)) 
	        					tmpB.getRelative(oppositeFacing).setType(Material.IRON_FENCE);
	        			else
	        				if (tmpB.getRelative(oppositeFacing).getType().equals(Material.IRON_FENCE)) 
	        					tmpB.getRelative(oppositeFacing).setType(Material.AIR);
        			}
        			*/
                }
            }
        }
        
		
		if (enabled) {
			if (isLockedDoor) {
				unlock();
				lock();
			}
			else if (barring != 0) {
				isBarred = true;
				unlock();
				bar();
			}
		} else if (!enabled) {
			if (isLockedDoor) {
				unlock();
			}	
			else if (barring != 0) {
				unlock();
				isBarred = false;
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public String type() {
		return "door";
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int getX() {
		return bottomCenter.getBlockX();
	}

	@Override
	public int getY() {
		return bottomCenter.getBlockY();
	}

	@Override
	public int getZ() {
		return bottomCenter.getBlockZ();
	}

	static String prp = "" + ChatColor.LIGHT_PURPLE;
	static String r = "" + ChatColor.RESET;
	@Override
	public void show(Player p) {
		p.sendMessage(prp + "Door \"" + name + "\":");

		p.sendMessage(r + "  Locked"+prp+" with key: " + isLockedDoor);
		p.sendMessage(prp+"  Currently locked: " + isCurrentlyLocked);
		
		String barred = "";
		switch (barring) {
			case 0:
				barred = "NONE:front:back:both"; break;
			case 1:
				barred = "none:FRONT:back:both"; break;
			case 2:
				barred = "none:front:BACK:both"; break;
			case 3:
				barred = "none:front:back:BOTH"; break;
		}
		
		
		p.sendMessage(r + "  Barred"+prp+": " + barred);
		
		String enable = "enabled";
		if (!enabled) enable = "disabled";
		p.sendMessage(prp + "  Currently " + enable + ".");
		
		String def = "ON";
		if (!defaultOnOff) def = "OFF";
		String on = "ON";
		if (!isOn) on = "OFF";
		p.sendMessage(prp + "  Is " + on + ","+r+" default"+prp+"s to " + def + ".");
		
		p.sendMessage(r + "  Inverted"+prp+"?: " + inverted);
		
		p.sendMessage(prp + "  Min XYZ: " + min.getX() + " / " + min.getY() + " / " + min.getZ() + "");
		p.sendMessage(prp + "  Max XYZ: " + max.getX() + " / " + max.getY() + " / " + max.getZ() + "");
		
		
	}

	

	@Override
	public void edit(Player p, String key, String value) {
		key = key.toLowerCase();
		value = value.toLowerCase();
		if (key.equals("barred")) {
			if (value.equals("front"))
				barring = 1;
			else if (value.equals("back"))
				barring = 2;
			else if (value.equals("both"))
				barring = 3;
			else if (value.equals("neither") || value.equals("off") || value.equals("none"))
				barring = 0;
			else
				barring = 0;
			p.sendMessage(prp + "Barring set.");
		}	
		
		if (key.equals("locked")) {
			if (value.equals("true")) {
				isLockedDoor = true;
				lock();
			} else if (value.equals("false")) {
				isLockedDoor = false;
				unlock();
			}
				
		}
	}
	
	@Override
	public boolean hasBlock(Block b) {
		
		int northSouth = 0;
		int eastWest = 0;
		if (isLockedDoor) {
			if (facing.equals(BlockFace.NORTH) || facing.equals(BlockFace.SOUTH)) {
				northSouth = 2;
			} else if (facing.equals(BlockFace.EAST) || facing.equals(BlockFace.WEST)) {
				eastWest = 2;
			}
		} else if (barring != 0) {
			if (facing.equals(BlockFace.NORTH) || facing.equals(BlockFace.SOUTH)) {
				northSouth = 1;
			} else if (facing.equals(BlockFace.EAST) || facing.equals(BlockFace.WEST)) {
				eastWest = 1;
			}
		}
		
		if (b.getX() >= min.getBlockX() - eastWest && b.getX() <= max.getBlockX() + eastWest &&
				b.getY() >= min.getBlockY() && b.getY() <= max.getBlockY() &&
				b.getZ() >= min.getBlockZ() - northSouth && b.getZ() <= max.getBlockZ() + northSouth)
			return true;	
		
		/*
		if (b.getType() == Material.ITEM_FRAME) {
			if ( (b.getX() == min.getBlockX() - 2 || b.getX() == max.getBlockX() + 2 ||
					b.getZ() == min.getBlockZ() - 2 && b.getZ() == max.getBlockZ() + 2) &&
					b.getY() >= min.getBlockY() && b.getY() <= max.getBlockY() 
					) {
				return true;
			}
				
		}
		*/
		
		return false;
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
	
	//TODO: GET RID OF THIS, THERE'S A PROPER WAY
	public boolean isOn() {
		// TODO Auto-generated method stub
		return true;
	}
	public boolean isUnlocked() {
		return !isLockedDoor;
	}
	public void tryOpen(Player p, BlockFace clicked) {
		if (animating)
			return;

		if ( isCurrentlyLocked == false &&
				!(clicked.equals(facing) && isFrontBarred()) &&
				!(clicked.equals(facing.getOppositeFace()) && isBackBarred())
				) {
			if (p.getLocation().distance(bottomCenter) < doorThickness()+1) {
				animating = true;
				animator = LegendCraft.plugin.getServer().getScheduler().runTaskTimer(LegendCraft.plugin, new DoorAnimator(this, p), 2, 2);	
			}
		} else if (isLockedDoor && isCurrentlyLocked){
			Material handItemType = p.getInventory().getItemInMainHand().getType();
			if (handItemType.equals(Material.GOLD_NUGGET)) {
				//int keyCount = p.getInventory().getItemInMainHand().getAmount();
				//p.getInventory().getItemInMainHand().getAmount()
				p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount()-1);
				unlock();
			}	
			//TODO: in DUNGEON code, store players in dungeon when active, take away their keys when they leave and it resets
		}
	}
	
	public void lock() {
		Block b;
		isCurrentlyLocked = true;
		int fx = Mx-(Mx-mx)/2;
		int fy = My-(My-my)/2;
		int fz = Mz-(Mz-mz)/2;
		
		if (facing.equals(BlockFace.NORTH) || 
			facing.equals(BlockFace.SOUTH))
		{
			for (int y = my; y <= My; y++) {
				for (int x = mx; x <= Mx; x++) {
					b = this.getWorld().getBlockAt(x, y, Mz+1);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
					b = this.getWorld().getBlockAt(x, y, mz-1);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
				}
			}
			
			try {
				getWorld().getBlockAt(fx,fy,Mz+2).setType(Material.AIR);
				getWorld().getBlockAt(fx,fy,mz-2).setType(Material.AIR);
				frame1 = (ItemFrame) getWorld().spawn(getWorld().getBlockAt(fx,fy,Mz+2).getLocation(),ItemFrame.class);
	            frame1.setItem(new ItemStack(Material.INK_SACK,1,(byte)6));
	            frame2 = (ItemFrame) getWorld().spawn(getWorld().getBlockAt(fx,fy,mz-2).getLocation(),ItemFrame.class);
	            frame2.setItem(new ItemStack(Material.INK_SACK,1,(byte)6));
			} catch (Exception e) {} 
		}
		
		if (facing.equals(BlockFace.EAST) || 
			facing.equals(BlockFace.WEST))
		{
			for (int y = my; y <= My; y++) {
				for (int z = mz; z <= Mz; z++) {
					b = this.getWorld().getBlockAt(Mx+1, y, z);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
					b = this.getWorld().getBlockAt(mx-1, y, z);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
				}
			}
			
			try {
				getWorld().getBlockAt(Mx+2,fy,fz).setType(Material.AIR);
				getWorld().getBlockAt(mx-2,fy,fz).setType(Material.AIR);
	            frame1 = (ItemFrame) getWorld().spawn(getWorld().getBlockAt(Mx+2,fy,fz).getLocation(),ItemFrame.class);
	            frame1.setItem(new ItemStack(Material.INK_SACK,1,(byte)6));
	            frame2 = (ItemFrame) getWorld().spawn(getWorld().getBlockAt(mx-2,fy,fz).getLocation(),ItemFrame.class);
	            frame2.setItem(new ItemStack(Material.INK_SACK,1,(byte)6));
			}catch (Exception e) {}
		}
	}
	
	public void unlock() {
		isCurrentlyLocked = false;
		if (frame1 != null)
			frame1.remove();
		if (frame2 != null)
			frame2.remove();
		unbar();
	}
	public boolean hasFrame(ItemStack is) {
		if (frame1 == null || frame2 == null)
			return false;
		if (is.equals(frame1.getItem()) || is.equals(frame2.getItem()))
			return true;
		return false;
	}
	
	
	public void bar() {
		Block b;
		if ((facing.equals(BlockFace.SOUTH) && isFrontBarred()) || 
			(facing.equals(BlockFace.NORTH) && isBackBarred()))
		{
			for (int y = my; y <= My; y++) {
				for (int x = mx; x <= Mx; x++) {
					b = this.getWorld().getBlockAt(x, y, Mz+1);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
				}
			}
		}
		if ((facing.equals(BlockFace.SOUTH) && isBackBarred()) || 
				(facing.equals(BlockFace.NORTH) && isFrontBarred()))
		{
			for (int y = my; y <= My; y++) {
				for (int x = mx; x <= Mx; x++) {
					b = this.getWorld().getBlockAt(x, y, mz-1);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
				}
			}
		}
		
		if ((facing.equals(BlockFace.WEST) && isFrontBarred()) || 
				(facing.equals(BlockFace.EAST) && isBackBarred()))
		{
			for (int y = my; y <= My; y++) {
				for (int z = mz; z <= Mz; z++) {
					b = this.getWorld().getBlockAt(mx-1, y, z);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
				}
			}
		}
		if ((facing.equals(BlockFace.WEST) && isBackBarred()) || 
				(facing.equals(BlockFace.EAST) && isFrontBarred()))
		{
			for (int y = my; y <= My; y++) {
				for (int z = mz; z <= Mz; z++) {
					b = this.getWorld().getBlockAt(Mx+1, y, z);
					if (b.getType().equals(Material.AIR))
						b.setType(Material.IRON_FENCE);
				}
			}
		}	
	}
	
	public void unbar() {
		Block b;
		if (facing.equals(BlockFace.NORTH) || 
				facing.equals(BlockFace.SOUTH))
		{
			for (int y = my; y <= My; y++) {
				for (int x = mx; x <= Mx; x++) {
					b = this.getWorld().getBlockAt(x, y, Mz+1);
					if (b.getType().equals(Material.IRON_FENCE))
						b.setType(Material.AIR);
					b = this.getWorld().getBlockAt(x, y, mz-1);
					if (b.getType().equals(Material.IRON_FENCE))
						b.setType(Material.AIR);
				}
			}
		}
		
		if (facing.equals(BlockFace.EAST) || 
			facing.equals(BlockFace.WEST))
		{
			for (int y = my; y <= My; y++) {
				for (int z = mz; z <= Mz; z++) {
					b = this.getWorld().getBlockAt(Mx+1, y, z);
					if (b.getType().equals(Material.IRON_FENCE))
						b.setType(Material.AIR);
					b = this.getWorld().getBlockAt(mx-1, y, z);
					if (b.getType().equals(Material.IRON_FENCE))
						b.setType(Material.AIR);
				}
			}
		}
	}
	 
}
