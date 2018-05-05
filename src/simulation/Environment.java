package simulation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import framework.GeneTrees;

public class Environment {
	private HashSet<GeneTree> trees = new HashSet<GeneTree>();
	private HashSet<SunSpeck> sun = new HashSet<SunSpeck>();
	private HashSet<GeneSeed> seeds = new HashSet<GeneSeed>();
	private HashSet<RainDrop> rain = new HashSet<RainDrop>();
	
	private BufferedImage groundImg;
	private double[] groundFreq = { 0.002,
									0.01,
									0.04,
									0.2 };
	private double[] groundAmp = { Math.random()*500,
								   Math.random()*200,
								   Math.random()*80,
								   Math.random()*5 };
	private double[] groundDisp = { Math.random()*500,
									Math.random()*500,
									Math.random()*500,
									Math.random()*500};
	
	private int simWidth = 3000;
	private int simHeight = 2000;

	private long minFitness = 0;
	private long maxFitness = 0;
	private long avgFitness = 0;

	private long tickCount = 0;
	private int ticksThisSec = 0;
	private long prevTime = System.currentTimeMillis();
	
	public boolean multithreading = false;
	
	public Environment() {
		groundImg = new BufferedImage(simWidth, simHeight, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < groundImg.getWidth(); x++) {
			double m = getGroundLevel(x);
			
			for (int y = (int)m; y < groundImg.getHeight(); y++) {
				groundImg.setRGB(x, y, new Color(183, 85, 23).getRGB());
			}
		}
		
		warmup(1000);
		
		// populate the list of trees
		for (int i = 0; i < 100; i++) {
			double xPos = Math.random()*simWidth;
			trees.add(new GeneTree((int)(xPos), (int)getGroundLevel(xPos)));
		}
	}
	
	public void warmup(int numTicks) {
		for (int i = 0; i < numTicks; i++) {
			// add new sunspecks
			for (int j = 0; j < 1; j++) {
				double pct = Math.random();
				pct = pct * pct;
				sun.add(new SunSpeck((int)(pct * (double)simWidth), 0));
			}
			
			// check collision of sunspecks with ground
			HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
			for (SunSpeck s : sun) { // for each sunspeck
				s.tick(); // tick each sunspeck
				if (s.getYPos() > getGroundLevel(s.getXPos())) { // check if sunspeck has hit ground
					remSun.add(s);
				}
			}
			sun.removeAll(remSun); // remove all specks that have hit the ground
			
			// add new raindrops
			for (int j = 0; j < 1; j++) {
				double pct = Math.random();
				pct = 1.0 - (pct * pct);
				rain.add(new RainDrop((int)(pct * (double)simWidth), 0));
			}
			
			// check collision of raindrops with ground
			HashSet<RainDrop> remRain = new HashSet<RainDrop>();
			for (RainDrop d : rain) { // for each raindrop
				d.tick(); // tick each raindrop
				if (d.getYPos() > getGroundLevel(d.getXPos())) { // check if raindrop has hit ground
					remRain.add(d);
				}
			}
			rain.removeAll(remRain); // remove all drops that have hit the ground
		}
	}
	
	public GeneTree getTreeAt(int x, int y) {
		for (GeneTree t : trees) {
			if (t.isOverTree(x, y)) {
				return t;
			}
		}
		return null;
	}
	
	public void computeSun() {
		// add new sunspecks
		for (int i = 0; i < 2; i++) {
			double pct = Math.random();
			pct = pct * pct;
			sun.add(new SunSpeck((int)(pct * (double)simWidth), 0));
		}
		
		// check collision of sunspecks with ground
		HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
		for (SunSpeck s : sun) { // for each sunspeck
			s.tick(); // tick each sunspeck
			if (s.getYPos() > getGroundLevel(s.getXPos())) { // check if sunspeck has hit ground
				remSun.add(s);
			}
		}
		sun.removeAll(remSun); // remove all specks that have hit the ground
		
		// check collision of sunspecks with each tree
		for (GeneTree t : trees) {
			for (TreeNode n : t.getAllNodes()) {
				HashSet<SunSpeck> rem = new HashSet<SunSpeck>();
				for (SunSpeck ss : sun) {
					int sx = ss.getXPos();
					int sy = ss.getYPos();
					int nx = n.getXPos();
					int ny = n.getYPos();
					int nd = n.getSize() / 2;

					// if the sunspeck hits this node, remove it
					if ((nx - sx) * (nx - sx) + (ny - sy) * (ny - sy) < nd * nd) {
						rem.add(ss);

						// if this node is a leaf, increment its fitness
						if (n.getType() == NodeType.Leaf) {
							n.setActivated(true);
							t.setEnergy(t.getEnergy() + ss.getPower());
						}
					}
				}
				sun.removeAll(rem);
			}
		}
	}
	
	public void computeRain() {
		// add new raindrops
		for (int i = 0; i < 2; i++) {
			double pct = Math.random();
			pct = 1.0 - (pct * pct);
			rain.add(new RainDrop((int)(pct * (double)simWidth), 0));
		}
		
		// check collision of raindrops with ground
		HashSet<RainDrop> remRain = new HashSet<RainDrop>();
		for (RainDrop d : rain) { // for each raindrop
			d.tick(); // tick each raindrop
			if (d.getYPos() > getGroundLevel(d.getXPos())) { // check if raindrop has hit ground
				remRain.add(d);
			}
		}
		rain.removeAll(remRain); // remove all drops that have hit the ground
		
		// check collision of raindrops with each tree
		for (GeneTree t : trees) {
			for (TreeNode n : t.getAllNodes()) {
				if (n.getType() != NodeType.Raincatcher) {
					continue;
				}
				
				HashSet<RainDrop> rem = new HashSet<RainDrop>();
				for (RainDrop rd : rain) {
					int rx = rd.getXPos();
					int ry = rd.getYPos();
					int nx = n.getXPos();
					int ny = n.getYPos();
					int nd = n.getSize() / 2;

					// if the raindrop hits this node, remove it
					if ((nx - rx) * (nx - rx) + (ny - ry) * (ny - ry) < nd * nd) {
						n.setActivated(true);
						rem.add(rd);
						t.setEnergy(t.getEnergy() + rd.getPower());
					}
				}
				rain.removeAll(rem);
			}
		}
	}
	
	public void computeFitness() {		
		// recompute fitness of each tree
		long totalFitness = 0;
		maxFitness = Integer.MIN_VALUE;
		minFitness = Integer.MAX_VALUE;
		for (GeneTree t : trees) {
			long thisFitness = t.getFitness();
			totalFitness += thisFitness;
			if (thisFitness > maxFitness) maxFitness = thisFitness;
			if (thisFitness < minFitness) minFitness = thisFitness;
		}
		avgFitness = totalFitness/trees.size();
		
		// evaluate each tree's chance to reproduce
		List<GeneTree> treesSorted = new ArrayList<GeneTree>(trees);
		Collections.sort(treesSorted);
		for (int i = 0; i < treesSorted.size(); i++) {
			treesSorted.get(i).setFitnessPercentage((double)i / (double)treesSorted.size());
		}
	}
	
	public void tick() {
		long currTime = System.currentTimeMillis();
		if (currTime - prevTime > 1000) {
			System.out.println("ticks per sec: " + ticksThisSec + " - multithreading: " + multithreading);
			ticksThisSec = 0;
			prevTime = currTime;
		}
		
		Thread sunChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeSun();
			}
		};
		
		Thread rainChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeRain();
			}
		};
		
		Thread fitnessChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeFitness();
			}
		};
		
		if (multithreading) {
			sunChecker.start();
			rainChecker.start();
			fitnessChecker.start();
			try {
				sunChecker.join();
				rainChecker.join();
				fitnessChecker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			sunChecker.run();
			rainChecker.run();
			fitnessChecker.run();
		}
		
		// tick each tree's root nodes and wasted fitness
		for (GeneTree t : trees) {
			t.tick();
		}
		
		// each 1000 ticks, try to reproduce or kill each tree
		tickCount++;
		if (tickCount % 1000 == 0)
			reproduce();
		
		ticksThisSec++;
	}
	
	private void reproduce() {
		tickCount = 0;

		List<GeneTree> treesSorted = new ArrayList<GeneTree>(trees);
		Collections.sort(treesSorted);
		
		HashSet<GeneTree> toRemove = new HashSet<GeneTree>();
		for (GeneTree t : trees) {
			if (t.getFitness() < 0) { // ( || t.getFitnessPercentage() < 0.4) {
				toRemove.add(t);
			}
		}
		trees.removeAll(toRemove);
		
		HashSet<GeneTree> toAdd = new HashSet<GeneTree>();
		for (GeneTree t : trees) {
			for (int i = 0; i < (int)((t.getFitnessPercentage() - 0.4)/0.2); i++) {
				double newXPos = t.getRoot().getXPos() + (Math.random()-0.5)*200;
				GeneTree newTree = new GeneTree(t, (int)newXPos, (int)getGroundLevel(newXPos));
				if (newTree.getxMin() < simWidth && newTree.getxMax() > 0)
					toAdd.add(newTree);
			}
			t.resetFitness();
		}
		trees.addAll(toAdd);
		
		// if there are less than 100 trees left, repopulate
		while (trees.size() < 100) {
			double xPos = Math.random()*simWidth;
			trees.add(new GeneTree((int)(xPos), (int)getGroundLevel(xPos)));
		}
	}
	
	public double getGroundLevel(double x) {
		return Math.cos(groundFreq[0]*x + groundDisp[0])*groundAmp[0] + 
			   Math.cos(groundFreq[1]*x + groundDisp[1])*groundAmp[1] + 
			   Math.cos(groundFreq[2]*x + groundDisp[2])*groundAmp[2] + 
			   Math.cos(groundFreq[3]*x + groundDisp[3])*groundAmp[3] +
			   600;
	}
	
	public BufferedImage getGroundImg() {
		return groundImg;
	}
	
	public HashSet<GeneTree> getTrees() {
		return trees;
	}
	
	public HashSet<SunSpeck> getSun() {
		return sun;
	}
	
	public HashSet<RainDrop> getRain() {
		return rain;
	}
	
	public HashSet<GeneSeed> getSeeds() {
		return seeds;
	}
	
	public int getSimWidth() {
		return simWidth;
	}

	public int getSimHeight() {
		return simHeight;
	}

	public long getMinFitness() {
		return minFitness;
	}

	public long getMaxFitness() {
		return maxFitness;
	}

	public long getAvgFitness() {
		return avgFitness;
	}
}
