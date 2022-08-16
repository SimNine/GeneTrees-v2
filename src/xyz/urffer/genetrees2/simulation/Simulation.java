package xyz.urffer.genetrees2.simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import xyz.urffer.genetrees2.environment.Environment;
import xyz.urffer.genetrees2.environment.Particle;
import xyz.urffer.genetrees2.environment.RainDrop;
import xyz.urffer.genetrees2.environment.SunSpeck;
import xyz.urffer.genetrees2.environment.genetree.GeneTree;
import xyz.urffer.genetrees2.environment.genetree.NodeType;
import xyz.urffer.genetrees2.environment.genetree.TreeNode;
import xyz.urffer.genetrees2.framework.GeneTrees;
import xyz.urffer.genetrees2.framework.ParameterLoader;
import xyz.urffer.genetrees2.framework.ParameterNames;
import xyz.urffer.urfutils.SetUtils;

public class Simulation {

	// pointer to the environment
	Environment env;
	private Random random;
	
	// variables to compute tick correctly
	private ThreadPoolExecutor threadPool;
	private boolean multithreading = true;
	private boolean running = true;
	private long tickCount = 0;
	private int ticksLastSec = 0;
	private int ticksThisSec = 0;
	private long prevTime = System.currentTimeMillis();
	private long lastGenCompletedAtTime = System.currentTimeMillis();
	private int numGens = 0;
	private Thread tickThread = new Thread(new Runnable() {
		public void run() {
			while (true) {
				if (running) {
					tick();
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	});
	// create sets of particles to remove when collision checking is done - necessary to store outside of function due to threading
	private volatile Set<SunSpeck> remSun = Collections.synchronizedSet(new HashSet<SunSpeck>());
	private volatile Set<RainDrop> remRain = Collections.synchronizedSet(new HashSet<RainDrop>());
	
	// variables to adjust graphs
	private long minAlltime = Long.MAX_VALUE;
	private long maxAlltime = Long.MIN_VALUE;
	private long maxTreesAlltime = Long.MIN_VALUE;
	private long minFitness = 0;
	private long maxFitness = 0;
	private long avgFitness = 0;
	private long avgNutrients = 0;
	private long avgEnergy = 0;
	
	// pointer to the currently tracked tree
	private GeneTree trackedTree = null;
	
	// indicates whether to save an image of the environment on creating a new generation
	private Snapshotter snapshotter = null;
	
	public Simulation(long seed, boolean saveImageOnGeneration) {
		if (saveImageOnGeneration) {
			this.snapshotter = new Snapshotter();
		}
		
		// initialize the random seed
		random = new Random(seed);
		
		// create a thread pool
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
				(int)(long)ParameterLoader.getParam("simulation", ParameterNames.NUM_THREADS)
		);
		
		// create the environment
		double[] gFreq = { 0.002,
						   0.01,
						   0.04,
						   0.2,
						   0.5 };
		double[] gAmp = { random.nextDouble()*500,
						  random.nextDouble()*200,
				   		  random.nextDouble()*80,
				   		  random.nextDouble()*5,
				   		  random.nextDouble()*5 };
		double[] gDisp = { random.nextDouble()*500,
						   random.nextDouble()*500,
						   random.nextDouble()*500,
				   		   random.nextDouble()*500,
				   		   random.nextDouble()*500 };
		env = new Environment(random, 
							  (int)(long)ParameterLoader.getParam("environment", ParameterNames.ENVIRONMENT_WIDTH), 
							  (int)(long)ParameterLoader.getParam("environment", ParameterNames.ENVIRONMENT_HEIGHT), 
							  (int)(long)ParameterLoader.getParam("environment", ParameterNames.ENVIRONMENT_GROUND_ELEVATION), 
							  gFreq, gAmp, gDisp);
		
		// warm up the environment
		warmupEnvironment((int)(long)ParameterLoader.getParam("environment", ParameterNames.ENVIRONMENT_NUM_WARMUP_TICKS));
		
		// start the tick thread
		tickThread.start();
	}
	
	// simulate weather particle production for some arbitrary number of ticks
	public void warmupEnvironment(int numTicks) {
		for (int i = 0; i < numTicks; i++) {
			// add new sunspecks
			for (int j = 0; j < 1; j++) {
				double pct = random.nextDouble();
				pct = pct * pct;
				env.getSun().add(new SunSpeck((int)(pct * (double)env.getEnvWidth()), 0));
			}
			
			// check collision of sunspecks with ground
			HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
			for (SunSpeck s : env.getSun()) { // for each sunspeck
				s.tick(); // tick each sunspeck
				if (s.getYPos() > env.getGroundLevel(s.getXPos())) { // check if sunspeck has hit ground
					remSun.add(s);
				}
			}
			env.getSun().removeAll(remSun); // remove all specks that have hit the ground
			
			// add new raindrops
			for (int j = 0; j < 1; j++) {
				double pct = random.nextDouble();
				pct = 1.0 - (pct * pct);
				env.getRain().add(new RainDrop((int)(pct * (double)env.getEnvWidth()), 0));
			}
			
			// check collision of raindrops with ground
			HashSet<RainDrop> remRain = new HashSet<RainDrop>();
			for (RainDrop d : env.getRain()) { // for each raindrop
				d.tick(); // tick each raindrop
				if (d.getYPos() > env.getGroundLevel(d.getXPos())) { // check if raindrop has hit ground
					remRain.add(d);
				}
			}
			env.getRain().removeAll(remRain); // remove all drops that have hit the ground
		}
	}

	public void tick() {
		// Compute the number of ticks per second
		long currTime = System.currentTimeMillis();
		if (currTime - prevTime > 1000) {
			if (GeneTrees.debug)
				System.out.println("ticks per sec: " + ticksThisSec);
			ticksLastSec = ticksThisSec;
			ticksThisSec = 0;
			prevTime = currTime;
		}

		// Tick the simulation. Implementation differs depending on multithreading or single threading.
		addNewSun();
		addNewRain();
		tickParticles(env.getSun());
		tickParticles(env.getRain());
		collideParticlesWithGround(env.getSun());
		collideParticlesWithGround(env.getRain());
		if (multithreading) {
			// divide particle pools into equal-sized chunks
			List<Set<SunSpeck>> sunSets = SetUtils.partitionSet(env.getSun(), threadPool.getMaximumPoolSize());
			List<Set<RainDrop>> rainSets = SetUtils.partitionSet(env.getRain(), threadPool.getMaximumPoolSize());
			
			
			// create tasks
			for (int i = 0; i < sunSets.size(); i++) {
				final int iCopy = i;
				threadPool.execute(new Runnable() {
					public void run() {
						remSun.addAll(collideSunWithTrees(sunSets.get(iCopy), env.getTrees()));
						remRain.addAll(collideRainWithTrees(rainSets.get(iCopy), env.getTrees()));
					}
				});
			}
			
			// spinlock to wait until tasks are done
			while (threadPool.getActiveCount() > 0) {
				// Thread.onSpinWait(); do nothing; this statement only works w/ Java 9+
			}
			
			// remove particles that have collided
			env.getSun().removeAll(remSun);
			remSun.clear();
			env.getRain().removeAll(remRain);
			remRain.clear();
		} else {
			Set<SunSpeck> remSun = collideSunWithTrees(env.getSun(), env.getTrees());
			env.getSun().removeAll(remSun);
			Set<RainDrop> remRain = collideRainWithTrees(env.getRain(), env.getTrees());
			env.getRain().removeAll(remRain);
		}
		// tick each tree's root nodes and wasted fitness
		for (GeneTree t : env.getTrees()) {
			t.tick();
		}
		computeFitness();
		
		// After a certain number of ticks, try to reproduce or kill each tree
		tickCount++;
		if (tickCount % (int)(long)ParameterLoader.getParam("environment", ParameterNames.ENVIRONMENT_TICKS_PER_GENERATION) == 0) {
			updateGraphs();
			advanceGeneration();
		}
		
		ticksThisSec++;
	}
	
	private void addNewSun() {
		for (int i = 0; i < 2; i++) {
			double pct = random.nextDouble();
			pct = pct * pct;
			env.getSun().add(new SunSpeck((int)(pct * (double)env.getEnvWidth()), 0));
		}
	}
	
	private void addNewRain() {
		for (int i = 0; i < 2; i++) {
			double pct = random.nextDouble();
			pct = 1.0 - (pct * pct);
			env.getRain().add(new RainDrop((int)(pct * (double)env.getEnvWidth()), 0));
		}
	}
	
	private void tickParticles(HashSet<? extends Particle> particles) {
		for (Particle p : particles) {
			p.tick();
		}
	}
	
//	private void removeZeroEnergyParticles(HashSet<? extends Particle> particles) {
//		HashSet<Particle> remParticles = new HashSet<Particle>();
//		for (Particle p : particles) {
//			if (p.getPower() <= 0) {
//				remParticles.add(p);
//			}
//		}
//		particles.removeAll(remParticles);
//	}
	
	private void collideParticlesWithGround(HashSet<? extends Particle> particles) {
		HashSet<Particle> remParticles = new HashSet<Particle>();
		for (Particle p : particles) { // for each particle
			if (p.getYPos() > env.getGroundLevel(p.getXPos())) { // check if particle has hit ground
				remParticles.add(p);
			}
		}
		particles.removeAll(remParticles); // remove all particles that have hit the ground
	}
	
	/**
	 * Computes the collision of all sunspecks with trees. Collects and returns SunSpecks that have collided, and
	 * increments energy of trees that have collected sun.
	 * 
	 * @param trees		Set of trees to check collision with
	 * @return			Set of sunspecks that have collided with trees
	 */
	private Set<SunSpeck> collideSunWithTrees(Set<SunSpeck> sun, Set<GeneTree> trees) {
		// check collision of sunspecks with each tree
		HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
		for (SunSpeck ss : sun) {
			if (ss.isConsumed()) {
				continue;
			}
			
			for (GeneTree t : trees) {
				if (ss.isConsumed()) {
					break;
				}
				
				if (!ss.collidesWithTree(t)) {
					continue;
				}
				
				for (TreeNode n : t.getAllNodes()) {
					if (ss.isConsumed()) {
						break;
					}
					
					// if the sunspeck hits this node, remove it
					if (ss.collidesWithNode(n)) {
						remSun.add(ss);
						ss.consume();
						
						// if this sunspeck is out of power, don't do anything more
						if (ss.getPower() <= 0) {
							continue;
						}

						// if this node is a leaf, increment its fitness
						if (n.getType() == NodeType.Leaf) {
							n.setActivated(true);
							t.setEnergy(t.getEnergy() + ss.getPower());
						}
					}
				}
			}
		}
		
		return remSun;
	}

	/**
	 * Computes the collision of all raindrops with trees. Collects and returns raindrops that have collided, and
	 * increments energy of trees that have collected rain.
	 * 
	 * @param trees		Set of trees to check collision with
	 * @return			Set of raindrops that have collided with trees
	 */
	private Set<RainDrop> collideRainWithTrees(Set<RainDrop> rain, Set<GeneTree> trees) {
		// check collision of raindrops with each tree
		HashSet<RainDrop> remRain = new HashSet<RainDrop>();
		for (RainDrop rd : rain) {
			if (rd.isConsumed()) {
				continue;
			}
			
			for (GeneTree t : trees) {
				if (rd.isConsumed()) {
					break;
				}
				
				if (!rd.collidesWithTree(t)) {
					continue;
				}
				
				for (TreeNode n : t.getAllNodes()) {
					if (rd.isConsumed()) {
						break;
					}
					
					if (n.getType() != NodeType.Raincatcher) {
						continue;
					}
						
					// if the raindrop hits this node, remove it
					if (rd.collidesWithNode(n)) {
						remRain.add(rd);
						rd.consume();
						
						// if this raindrop is out of power, do nothing more
						if (rd.getPower() <= 0) {
							continue;
						}
						
						n.setActivated(true);
						t.setEnergy(t.getEnergy() + rd.getPower());
					}
				}
			}
		}
		
		return remRain;
	}
	
	private void updateGraphs() {
		// get set of trees (for convenience)
		HashSet<GeneTree> trees = env.getTrees();
		
		// update fitnesss graph
		if (maxFitness > maxAlltime)
			maxAlltime = maxFitness;
		if (minFitness < minAlltime)
			minAlltime = minFitness;
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_MAX, numGens, maxFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_AVG, numGens, avgFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_MIN, numGens, minFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_AVG_NUTRIENTS, numGens, this.avgNutrients);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_AVG_ENERGY, numGens, this.avgEnergy);
		
		// update population graph
		if (trees.size() > maxTreesAlltime)
			maxTreesAlltime = trees.size();
		GeneTrees.populationPanel.addPoint(GeneTrees.GRAPHDATA_POPULATION, numGens, trees.size());
		
		// update tree stat graph
		double treeNodes = 0;
		double treeLeaves = 0;
		double treeRaincatchers = 0;
		double treeStructs = 0;
		double treeRoots = 0;
		double treeSeeddroppers = 0;
		for (GeneTree t : trees) {
			for (TreeNode n : t.getAllNodes()) {
				if (n.getType() == NodeType.Leaf)
					treeLeaves++;
				if (n.getType() == NodeType.Raincatcher)
					treeRaincatchers++;
				if (n.getType() == NodeType.Struct)
					treeStructs++;
				if (n.getType() == NodeType.Root)
					treeRoots++;
				if (n.getType() == NodeType.SeedDropper)
					treeSeeddroppers++;
			}
			treeNodes += t.getNumNodes();
		}
		GeneTrees.nodeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_ALL, numGens, treeNodes/trees.size());
		GeneTrees.nodeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_LEAF, numGens, treeLeaves/trees.size());
		GeneTrees.nodeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_RAINCATCHER, numGens, treeRaincatchers/trees.size());
		GeneTrees.nodeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_STRUCTURE, numGens, treeStructs/trees.size());
		GeneTrees.nodeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_ROOT, numGens, treeRoots/trees.size());
		GeneTrees.nodeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_SEEDDROPPER, numGens, treeSeeddroppers/trees.size());
		
		// update weather stat graph
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS, numGens, env.getSun().size());
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS, numGens, env.getRain().size());
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_SEEDS, numGens, env.getSeeds().size());
		
		// update performance graph
		long timeForLastGeneration = System.currentTimeMillis() - this.lastGenCompletedAtTime;
		GeneTrees.performancePanel.addPoint(GeneTrees.GRAPHDATA_PERFORMANCE_MILLISECONDS_PER_GENERATION, numGens, timeForLastGeneration);
	}
	
	private void advanceGeneration() {
		if (this.snapshotter != null) {
			this.snapshotter.takeSnapshot(this);
		}
		
		// reset the current tick number, increment the generation number
		tickCount = 0;
		numGens++;
		this.lastGenCompletedAtTime = System.currentTimeMillis();

		// sort the array of trees in order to remove the ones
		//List<GeneTree> treesSorted = new ArrayList<GeneTree>(trees);
		//Collections.sort(treesSorted);
		
		// get the set of trees
		HashSet<GeneTree> trees = env.getTrees();
		
		// check all trees for which to remove
		HashSet<GeneTree> toRemove = new HashSet<GeneTree>();
		for (GeneTree t : trees) {
			if (t.getFitness() < 0) { // ( || t.getFitnessPercentage() < 0.4) {
				toRemove.add(t);
			}
		}
		trees.removeAll(toRemove);
		
		// check trees for which to reproduce
		HashSet<GeneTree> toAdd = new HashSet<GeneTree>();
		for (GeneTree t : trees) {
//			for (int i = 0; i < (int)((t.getFitnessPercentage() - 0.4)/0.2); i++) {
//				double newXPos = t.getRoot().getPos()[0] + t.getRoot().getSize()/2 + (random.nextDouble()-0.5)*200;
//				GeneTree newTree = new GeneTree(random, t, (int)newXPos, (int)env.getGroundLevel(newXPos) - EnvironmentParameters.NODE_MINIMUM_SIZE);
//				if (newTree.getxMin() < env.getEnvWidth() && newTree.getxMax() > 0)
//					toAdd.add(newTree);
//			}
			for (long i = t.getFitness(); i > 0; i -= (int)(long)ParameterLoader.getParam("fitness", ParameterNames.TREE_FITNESS_PER_CHILD_PER_NODE) * t.getNumNodes()) {
				double newXPos = t.getRoot().getPos()[0] + t.getRoot().getSize()/2 + (random.nextDouble()-0.5)*200;
				GeneTree newTree = new GeneTree(random, t, (int)newXPos, (int)env.getGroundLevel(newXPos) - (int)(long)ParameterLoader.getParam("mutation", ParameterNames.NODE_MINIMUM_SIZE));
				if (newTree.getRoot().getPos()[0] < env.getEnvWidth() && newTree.getRoot().getPos()[0] > 0)
					toAdd.add(newTree);
			}
			t.resetFitness();
		}
		trees.addAll(toAdd);
		
		// Add a certain number of completely random trees regardless of circumstance
		for (int i = 0; i < (int)(long)ParameterLoader.getParam("environment", ParameterNames.ENVIRONMENT_SPONTANEOUS_TREES_PER_GENERATION); i++) {
			double xPos = random.nextDouble()*env.getEnvWidth();
			trees.add(new GeneTree(random, (int)(xPos), (int)env.getGroundLevel(xPos) - (int)(long)ParameterLoader.getParam("mutation", ParameterNames.NODE_MINIMUM_SIZE)));
		}
	}
	
	// computes the fitness of each tree
	public void computeFitness() {
		// get set of trees (for convenience)
		HashSet<GeneTree> trees = env.getTrees();
		
		// recompute fitness, nutrients, and energy of each tree
		long totalFitness = 0;
		long totalNutrients = 0;
		long totalEnergy = 0;
		maxFitness = Integer.MIN_VALUE;
		minFitness = Integer.MAX_VALUE;
		for (GeneTree t : trees) {
			long thisFitness = t.getFitness();
			totalFitness += thisFitness;
			if (thisFitness > maxFitness) maxFitness = thisFitness;
			if (thisFitness < minFitness) minFitness = thisFitness;
			totalNutrients += t.getNutrients();
			totalEnergy += t.getEnergy();
		}
		avgFitness = totalFitness/trees.size();
		avgNutrients = totalNutrients/trees.size();
		avgEnergy = totalEnergy/trees.size();
		
		// evaluate each tree's chance to reproduce
		List<GeneTree> treesSorted = new ArrayList<GeneTree>(trees);
		Collections.sort(treesSorted);
		for (int i = 0; i < treesSorted.size(); i++) {
			treesSorted.get(i).setFitnessPercentage((double)i / (double)treesSorted.size());
		}
	}
	
	public void draw(Graphics g, int xDisp, int yDisp, int viewportWidth, int viewportHeight) {
		// draw the sky
		g.setColor(new Color(146, 184, 244));
		g.fillRect(0, 0, viewportWidth, viewportHeight);
		
		// draw the ground
		g.drawImage(env.getGroundImg(), -xDisp, -yDisp, null);
		
		try {
			// draw each tree
			for (GeneTree t : env.getTrees()) {
				t.draw(g, xDisp, yDisp);
			}
			
			// draw each sunspeck
			for (SunSpeck s : env.getSun()) {
				s.draw(g, xDisp, yDisp);
			}
			
			// draw each raindrop
			for (RainDrop d : env.getRain()) {
				d.draw(g, xDisp, yDisp);
			}
		} catch (ConcurrentModificationException e) {
			// do nothing
		}
		
		// draw the environment bounding box
		g.setColor(new Color(255, 0, 0));
		g.drawRect(-xDisp, -yDisp, env.getEnvWidth(), env.getEnvHeight());
	}
	
	public void setTrackedTreeAt(int xPos, int yPos) {
		trackedTree = env.getTreeAt(xPos, yPos);
	}
	
	public GeneTree getTrackedTree() {
		return trackedTree;
	}
	
	public Environment getEnv() {
		return env;
	}
	
	public void setEnv(Environment e) {
		env = e;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isMultithreading() {
		return this.multithreading;
	}
	
	public void setMultithreading(boolean multithreading) {
		this.multithreading = multithreading;
	}
	
	public long getTickCount() {
		return tickCount;
	}
	
	public int getNumGens() {
		return numGens;
	}
	
	public void setNumGens(int n) {
		numGens = n;
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
	
	public int getTicksLastSec() {
		return ticksLastSec;
	}
}
