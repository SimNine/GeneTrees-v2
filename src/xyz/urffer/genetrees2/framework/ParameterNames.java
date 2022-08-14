package xyz.urffer.genetrees2.framework;

public class ParameterNames {

	// simulation
	public static final String SIMULATION_SEED = "simulationSeed";
	public static final String NUM_THREADS = "numThreads";
	public static final String TAKE_SNAPSHOTS = "takeSnapshots";
	
	// environment
	public static final String ENVIRONMENT_WIDTH = "width";
	public static final String ENVIRONMENT_HEIGHT = "height";
	public static final String ENVIRONMENT_GROUND_ELEVATION = "groundLevel";
	public static final String ENVIRONMENT_NUM_WARMUP_TICKS = "numWarmupTicks";
	public static final String ENVIRONMENT_TICKS_PER_GENERATION = "ticksPerGeneration";
	public static final String ENVIRONMENT_SPONTANEOUS_TREES_PER_GENERATION = "spontaneousTreesPerGeneration";
	
	// particles
	public static final String RAINDROP_BASE_POWER = "rainParticleBasePower";
	public static final String RAINDROP_TICK_POWER_DELTA = "rainParticlePowerChangePerTick";
	public static final String SUNSPECK_BASE_POWER = "sunParticleBasePower";
	public static final String SUNSPECK_TICK_POWER_DELTA = "sunParticlePowerChangePerTick";
	
	// fitness
	public static final String NODE_ROOT_NUTRIENT_COLLECTION_PER_SIZE = "rootNutrientCollectionPerSize";
	public static final String NODE_ROOT_NUTRIENT_COLLECTION_PER_DEPTH = "rootNutrientCollectionPerDepth";
	public static final String NODE_STRUCT_FITNESS_DECAY_PER_SIZE = "structFitnessDecayPerSize";
	public static final String NODE_ACTIVE_FITNESS_DECAY_PER_SIZE = "activeNodeFitnessDecayPerSize";
	public static final String NODE_INACTIVE_FITNESS_DECAY_PER_SIZE = "inactiveNodeFitnessDecayPerSize";
	public static final String TREE_FITNESS_GAIN_PER_NUTRIENT_AND_POWER = "treeFitnessGainPerEnergyAndNutrient";
	public static final String TREE_FITNESS_PER_CHILD_PER_NODE = "treeFitnessRequirementPerChildPerNode";
	
	// mutation
	public static final String TREE_BASE_MUTATION_CHANCE = "treeBaseMutationChance";
	public static final String NODE_MINIMUM_SIZE = "nodeMinumumSize";
	public static final String NODE_MINIMUM_DISTANCE = "nodeMinimumDistance";
	public static final String NODE_MUTATE_TYPE_CHANCE = "nodeTypeMutationChance";
	public static final String NODE_MUTATE_SIZE_CHANCE = "nodeSizeMutationChance";
	public static final String NODE_MUTATE_ANGLE_CHANCE = "nodeAngleMutationChance";
	public static final String NODE_MUTATE_DISTANCE_CHANCE = "nodeDistanceMutationChance";
	public static final String NODE_ADD_CHILD_CHANCE = "nodeAddChildMutationChance";
	public static final String NODE_LOSE_CHILD_CHANCE = "nodeLoseChildMutationChance";
	
}
