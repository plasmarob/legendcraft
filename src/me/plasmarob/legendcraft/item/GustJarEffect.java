package me.plasmarob.legendcraft.item;

import java.util.Random;

import me.plasmarob.util.Tools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.ParticleEffect;

public class GustJarEffect extends Effect{
		
		double counter;
		float yaw;
		float yawStart;
		float yawEnd;
		float tempYaw;
		double speed;
		static final ParticleEffect cloud = ParticleEffect.CLOUD;
		
		Random rand;
		
		Location currentLoc; 
		Location location;
		static Vector blank = new Vector();
		int x = 0;
		
		Player player;
		
		public GustJarEffect(EffectManager effectManager, Player player) {
			super(effectManager);
			location = player.getLocation().clone();
			this.player = player;
			
			//location.add(0,-.4,0);
			
			type = EffectType.REPEATING;
			iterations = 15*20;
			period = 2;
			counter = 0;
			speed = 0.75;
			
			/*
			location.setYaw(location.clone().getYaw() - 90);
			float pitch = location.getPitch();
			location.setPitch(0);
			Vector vec = location.getDirection().normalize().multiply(2.5);
			location.add(vec);
			location.setYaw(location.clone().getYaw() + 90);
			location.setPitch(pitch);
			*/
			setLocation(location);
			rand = new Random();
		}

		@Override
		public void onRun() {

			BlockIterator bit = new BlockIterator(player, 11);
			Block next = bit.next();
			while (bit.hasNext())
			{
				if (!Tools.canSeeThrough(next.getType()))
					break;
				next = bit.next();
			}
			
			Location loc = next.getLocation().clone();
			loc.add((rand.nextDouble()-0.5)*2, 
					(rand.nextDouble()-0.5)*2, 
					(rand.nextDouble()-0.5)*2);
			
			location = loc.clone();
			
			/*
			loc.setPitch(loc.getPitch() + ((rand.nextFloat()-0.5f)*2*5) );
			loc.setYaw(loc.getYaw() + ((rand.nextFloat()-0.5f)*2*5) );
			loc.add(loc.getDirection().normalize().multiply( 6 ));
			*/
			
			Vector v = Tools.getDirection(loc, player.getLocation().clone().add(0,1,0));

			cloud.display(v.normalize(), 1.0f, location, 16);
			
			/*
			//location.setYaw(location.clone().getYaw() + 90);
			
			Vector vec = location.getDirection().normalize().multiply(0.9);
			location.add(vec);	
			
			Location tempLoc = location.clone();
			tempLoc.setYaw(tempLoc.clone().getYaw() + 90);
			tempLoc.setPitch(0);
			vec = tempLoc.getDirection().normalize().multiply(0.3);
			for (int i = 0; i < 20; i++)
			{
				tempLoc.add(vec);
				cloud.display(blank, 0, tempLoc, visibleRange);
				
			}
			
			
			//location.subtract(vec);
			*/

		}

	}
