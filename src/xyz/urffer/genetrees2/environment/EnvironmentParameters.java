package xyz.urffer.genetrees2.environment;

public class EnvironmentParameters {

	// environment

	public static final int ENVIRONMENT_WIDTH = 3000;
	public static final int ENVIRONMENT_HEIGHT = 2000;
	public static final int ENVIRONMENT_GROUND_ELEVATION = 1200;

	public static final int ENVIRONMENT_NUM_WARMUP_TICKS = 1000;
	public static final int ENVIRONMENT_TICKS_PER_GENERATION = 1000;
	
	public static final int ENVIRONMENT_SPONTANEOUS_TREES_PER_GENERATION = 200;
	
	
	
	// particles
	
	public static final long RAINDROP_BASE_POWER = -50000;
	public static final long RAINDROP_TICK_POWER_DELTA = 55;
	
	public static final long SUNSPECK_BASE_POWER = 70000;
	public static final long SUNSPECK_TICK_POWER_DELTA = -45;
	
	
	
	// fitness
	
	public static final double NODE_ROOT_NUTRIENT_COLLECTION_PER_SIZE = 3;
	public static final double NODE_ROOT_NUTRIENT_COLLECTION_PER_DEPTH = 0.03;
	
	public static final long NODE_STRUCT_FITNESS_DECAY_PER_SIZE = 1;
	public static final long NODE_ACTIVE_FITNESS_DECAY_PER_SIZE = 2;
	public static final long NODE_INACTIVE_FITNESS_DECAY_PER_SIZE = 10;

	public static final long TREE_FITNESS_GAIN_PER_NUTRIENT_AND_POWER = 2;
	
	public static final long TREE_FITNESS_PER_CHILD_PER_NODE = 5000;
	
	
	
	// mutation
	
	public static final double TREE_BASE_MUTATION_CHANCE = 0.3;
	
	public static final int NODE_MINIMUM_SIZE = 10;
	public static final double NODE_MINIMUM_DISTANCE = 40.0;

	public static final double NODE_MUTATE_TYPE_CHANCE = 0.15;
	public static final double NODE_MUTATE_SIZE_CHANCE = 0.30;
	public static final double NODE_MUTATE_ANGLE_CHANCE = 0.25;
	public static final double NODE_MUTATE_DISTANCE_CHANCE = 0.15;
	
	public static final double NODE_ADD_CHILD_CHANCE = 0.30;
	public static final double NODE_LOSE_CHILD_CHANCE = 0.10;

}
