package me.plasmarob.util;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.util.ParticleEffect;

public class LineEffect extends Effect{
		
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

		Color col1;
		Color col2;
		Location loc;
		Location loc1;
		Location loc2;
		double distance;
		double xDist;
		double yDist;
		double zDist;
		double times;
		double scale = 5;
		
		// Allow 1 or 2 color args
		public LineEffect(EffectManager effectManager, Location loc1, Location loc2, Color col1) {
			super(effectManager);
			lineEffect(loc1, loc2, col1, col1);
		}
		public LineEffect(EffectManager effectManager, Location loc1, Location loc2, Color col1, Color col2) {
			super(effectManager);
			lineEffect(loc1, loc2, col1, col2);
		}
		
		private void lineEffect(Location loc1, Location loc2, Color col1, Color col2) {
			this.loc1 = loc1;
			this.loc2 = loc2;
			this.col1 = col1;
			this.col2 = col2;
			
			location = loc1;
			
			type = EffectType.REPEATING;
			iterations = 300;
			period = 1;
			counter = 0;
			speed = 2;
			
			setLocation(location);
			rand = new Random();
			
			distance = loc1.distance(loc2);
			times = distance*scale;
			xDist = (loc2.getX() - loc1.getX()) / scale;
			yDist = (loc2.getX() - loc1.getY()) / scale;
			zDist = (loc2.getX() - loc1.getX()) / scale;
		}

		@Override
		public void onRun() {
			loc = location.clone();
			for (int i = 0; i < times; i++) {
				reddust.display(null, loc, col1, 64, 0f, 0f, 0f, 0, 1);
				loc.add(xDist, yDist, zDist);
			}
		}

	}
