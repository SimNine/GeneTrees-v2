package xyz.urffer.genetrees2.framework;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ParameterLoader {
	
	private static JSONObject params = null;
	
	private static String paramFileName = "treesConfig.json";
	
	public static Object getParam(String category, String fieldName) {
		if (params == null) {
			try {
				params = loadParams();
			} catch (IOException | ParseException e) {
				JSONObject tempParams = generateParams();
				saveParams(tempParams);
			}
		}
		
		try {
			if (params == null) {
				params = loadParams();
			}
			
			JSONObject categoryJson = (JSONObject)params.get(category);
			return categoryJson.get(fieldName);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Malformed " + paramFileName);
			System.err.println("Delete " + paramFileName + " and restart GeneTrees-v2.");
			System.exit(1);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject generateParams() {
		JSONObject out = new JSONObject();
		
		// simulation
		LinkedHashMap<Object, Object> simMap = new LinkedHashMap<>();
		simMap.put(ParameterNames.SIMULATION_SEED, 
			  ParameterDefaultValues.SIMULATION_SEED);
		simMap.put(ParameterNames.NUM_THREADS, 
			  ParameterDefaultValues.NUM_THREADS);
		simMap.put(ParameterNames.TAKE_SNAPSHOTS, 
			  ParameterDefaultValues.TAKE_SNAPSHOTS);
		out.put("simulation", simMap);
		
		// environment
		LinkedHashMap<Object, Object> envMap = new LinkedHashMap<>();
		envMap.put(ParameterNames.ENVIRONMENT_WIDTH, 
			  ParameterDefaultValues.ENVIRONMENT_WIDTH);
		envMap.put(ParameterNames.ENVIRONMENT_HEIGHT, 
			  ParameterDefaultValues.ENVIRONMENT_HEIGHT);
		envMap.put(ParameterNames.ENVIRONMENT_GROUND_ELEVATION, 
			  ParameterDefaultValues.ENVIRONMENT_GROUND_ELEVATION);
		envMap.put(ParameterNames.ENVIRONMENT_NUM_WARMUP_TICKS, 
				  ParameterDefaultValues.ENVIRONMENT_NUM_WARMUP_TICKS);
		envMap.put(ParameterNames.ENVIRONMENT_TICKS_PER_GENERATION, 
				  ParameterDefaultValues.ENVIRONMENT_TICKS_PER_GENERATION);
		envMap.put(ParameterNames.ENVIRONMENT_SPONTANEOUS_TREES_PER_GENERATION, 
				  ParameterDefaultValues.ENVIRONMENT_SPONTANEOUS_TREES_PER_GENERATION);
		out.put("environment", envMap);
		
		// particles
		LinkedHashMap<Object, Object> particleMap = new LinkedHashMap<>();
		particleMap.put(ParameterNames.RAINDROP_BASE_POWER, 
			  ParameterDefaultValues.RAINDROP_BASE_POWER);
		particleMap.put(ParameterNames.RAINDROP_TICK_POWER_DELTA, 
			  ParameterDefaultValues.RAINDROP_TICK_POWER_DELTA);
		particleMap.put(ParameterNames.SUNSPECK_BASE_POWER, 
			  ParameterDefaultValues.SUNSPECK_BASE_POWER);
		particleMap.put(ParameterNames.SUNSPECK_TICK_POWER_DELTA, 
				  ParameterDefaultValues.SUNSPECK_TICK_POWER_DELTA);
		out.put("particles", particleMap);
		
		// fitness
		LinkedHashMap<Object, Object> fitnessMap = new LinkedHashMap<>();
		fitnessMap.put(ParameterNames.NODE_ROOT_NUTRIENT_COLLECTION_PER_SIZE, 
			  ParameterDefaultValues.NODE_ROOT_NUTRIENT_COLLECTION_PER_SIZE);
		fitnessMap.put(ParameterNames.NODE_ROOT_NUTRIENT_COLLECTION_PER_DEPTH, 
			  ParameterDefaultValues.NODE_ROOT_NUTRIENT_COLLECTION_PER_DEPTH);
		fitnessMap.put(ParameterNames.NODE_STRUCT_FITNESS_DECAY_PER_SIZE, 
			  ParameterDefaultValues.NODE_STRUCT_FITNESS_DECAY_PER_SIZE);
		fitnessMap.put(ParameterNames.NODE_ACTIVE_FITNESS_DECAY_PER_SIZE, 
				  ParameterDefaultValues.NODE_ACTIVE_FITNESS_DECAY_PER_SIZE);
		fitnessMap.put(ParameterNames.NODE_INACTIVE_FITNESS_DECAY_PER_SIZE, 
				  ParameterDefaultValues.NODE_INACTIVE_FITNESS_DECAY_PER_SIZE);
		fitnessMap.put(ParameterNames.TREE_FITNESS_GAIN_PER_NUTRIENT_AND_POWER, 
				  ParameterDefaultValues.TREE_FITNESS_GAIN_PER_NUTRIENT_AND_POWER);
		fitnessMap.put(ParameterNames.TREE_FITNESS_PER_CHILD_PER_NODE, 
				  ParameterDefaultValues.TREE_FITNESS_PER_CHILD_PER_NODE);
		out.put("fitness", fitnessMap);
		
		// mutation
		LinkedHashMap<Object, Object> mutationMap = new LinkedHashMap<>();
		mutationMap.put(ParameterNames.TREE_BASE_MUTATION_CHANCE, 
			  ParameterDefaultValues.TREE_BASE_MUTATION_CHANCE);
		mutationMap.put(ParameterNames.NODE_MINIMUM_SIZE, 
			  ParameterDefaultValues.NODE_MINIMUM_SIZE);
		mutationMap.put(ParameterNames.NODE_MINIMUM_DISTANCE, 
			  ParameterDefaultValues.NODE_MINIMUM_DISTANCE);
		mutationMap.put(ParameterNames.NODE_MUTATE_TYPE_CHANCE, 
				  ParameterDefaultValues.NODE_MUTATE_TYPE_CHANCE);
		mutationMap.put(ParameterNames.NODE_MUTATE_SIZE_CHANCE, 
				  ParameterDefaultValues.NODE_MUTATE_SIZE_CHANCE);
		mutationMap.put(ParameterNames.NODE_MUTATE_ANGLE_CHANCE, 
				  ParameterDefaultValues.NODE_MUTATE_ANGLE_CHANCE);
		mutationMap.put(ParameterNames.NODE_MUTATE_DISTANCE_CHANCE, 
				  ParameterDefaultValues.NODE_MUTATE_DISTANCE_CHANCE);
		mutationMap.put(ParameterNames.NODE_ADD_CHILD_CHANCE, 
				  ParameterDefaultValues.NODE_ADD_CHILD_CHANCE);
		mutationMap.put(ParameterNames.NODE_LOSE_CHILD_CHANCE, 
				  ParameterDefaultValues.NODE_LOSE_CHILD_CHANCE);
		out.put("mutation", mutationMap);
		
		return out;
	}
	
	private static JSONObject loadParams() throws FileNotFoundException, IOException, ParseException {
		Object obj = new JSONParser().parse(new FileReader(paramFileName));
		return (JSONObject) obj;
	}
	
	private static void saveParams(JSONObject parameterObj) {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement je = JsonParser.parseString(parameterObj.toJSONString());
			String prettyJsonString = gson.toJson(je);
			PrintWriter pw = new PrintWriter(paramFileName);
	        pw.write(prettyJsonString);
	          
	        pw.flush();
	        pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
//	public static long getSimulationSeed() {
//		
//	}
	
}
