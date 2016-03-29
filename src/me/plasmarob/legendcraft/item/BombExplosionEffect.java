package me.plasmarob.legendcraft.item;

import java.util.Random;

import me.plasmarob.util.Tools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.ParticleEffect;

public class BombExplosionEffect extends Effect{
		
		double counter;
		float yaw;
		float yawStart;
		float yawEnd;
		float tempYaw;
		double speed;
		static final ParticleEffect flame = ParticleEffect.FLAME;
		static final ParticleEffect smoke = ParticleEffect.SMOKE_NORMAL;
		
		Random rand;
		
		Location currentLoc; 
		Location location;
		static Vector blank = new Vector();
		double x = 0,y = 0,z = 0;
		
		Item item;
		int it = 0;
		
		double acc = 16;
		
		public BombExplosionEffect(EffectManager effectManager, Item bomb) {
			super(effectManager);
			location = bomb.getLocation();
			
			//location.add(0,-.4,0);
			
			type = EffectType.REPEATING;
			iterations = 160;
			period = 1;
			counter = 0;
			speed = 2;
			
			setEntity(bomb);
			item = bomb;
			rand = new Random();
		}

		@Override
		public void onRun() {

			/*
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
			
			Vector v = Tools.getDirection(loc, bomb.getLocation().clone().add(0,1,0));
			 */
			
			
			if (it > acc) {
				it = 0;
				acc = acc*0.75;
				// should go off about 12 times, totalling 80 ticks, but speeding up
			}
			x = rand.nextFloat()-0.5;
			y = rand.nextFloat()-0.5;
			z = rand.nextFloat()-0.5;
			if (it == 0) {
				flame.display(new Vector(rand.nextFloat()-0.5,rand.nextFloat()-0.5,rand.nextFloat()-0.5).multiply(0.01), 20.0f, item.getLocation().add(0, 0.8, 0), 32.0);
				flame.display(new Vector(rand.nextFloat()-0.5,rand.nextFloat()-0.5,rand.nextFloat()-0.5).multiply(0.01), 20.0f, item.getLocation().add(0, 0.8, 0), 32.0);	
			}
			flame.display(new Vector(), 20.0f, item.getLocation().add(0, 0.8, 0), 32.0);
			smoke.display(new Vector(), 1.0f, item.getLocation().add(0, 0.8, 0), 32.0);
			it++;
		}

	}
