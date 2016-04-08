package me.plasmarob.legendcraft.item;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.ParticleEffect;

public class FireRodEffect extends Effect{
		
		double counter;
		float yaw;
		float yawStart;
		float yawEnd;
		float tempYaw;
		double speed;
		static final ParticleEffect reddust = ParticleEffect.REDSTONE;
		float r = 0; 
		
		
		Random rand;
		
		Location currentLoc; 
		Location location;
		static Vector blank = new Vector();
		int x = 0;
		Vector dir;
		
		Player player;
		Location loc;
		
		public FireRodEffect(EffectManager effectManager, Player player) {
			super(effectManager);
			location = player.getEyeLocation().clone();
			this.player = player;
			dir = player.getEyeLocation().getDirection().clone().multiply(0.8);
			location.add(dir.clone().normalize());
			
			//location.add(0,-.4,0);
			
			type = EffectType.REPEATING;
			iterations = 30;
			period = 1;
			counter = 0;
			speed = 2;
			
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

			r = r + 0.1f;
			
			
			/*
			BlockIterator bit = new BlockIterator(player, 11);
			Block next = bit.next();
			while (bit.hasNext())
			{
				if (!Tools.canSeeThrough(next.getType()))
					break;
				next = bit.next();
			}
			*/
			location.add(dir);
			Location loc = location.clone();
			loc.add((rand.nextDouble()-0.5)*r, 
					(rand.nextDouble()-0.5)*r, 
					(rand.nextDouble()-0.5)*r);
			
		
			//int count = (int)(1+(int)r*r*r);
			int count = (int)(15*r);
			for (int i = 0; i < count; i++)
			{
				loc = location.clone();
				loc.add((rand.nextDouble()-0.5)*r, 
						(rand.nextDouble()-0.5)*r, 
						(rand.nextDouble()-0.5)*r);
				//Vector v = Tools.getDirection(player.getLocation().clone().add(1,0,0), loc);
				
				
				//reddust.display(v.normalize(), 2.0f, location, 16);
				reddust.display(null, loc, Color.fromRGB(255, 128, 0), 32, 0f, 0f, 0f, 0, 1);
				
				loc = location.clone();
				loc.add((rand.nextDouble()-0.5)*r, 
						(rand.nextDouble()-0.5)*r, 
						(rand.nextDouble()-0.5)*r);
				//ParticleEffect.VILLAGER_ANGRY.display(loc, 32);
				
				ParticleEffect.FLAME.display(dir.clone().multiply(0), 1, loc, 32);
				//reddust.display(null, loc, Color.fromRGB(255, 255, 255), 32, 0f, 0f, 0f, 0, 1);
			}
			
			
			

		}

	}
