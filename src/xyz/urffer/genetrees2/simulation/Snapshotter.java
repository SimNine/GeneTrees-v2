package xyz.urffer.genetrees2.simulation;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import xyz.urffer.genetrees2.environment.Environment;

public class Snapshotter {
	
	public static String SNAPSHOT_FOLDER = "snapshots";
	
	private String runIdentifier;
	private long generationSnapshotIncrement = 0;
	
	public Snapshotter() {
		this.runIdentifier = Long.toString(System.currentTimeMillis());
	}
	
	public void takeSnapshot(Simulation sim) {
		Environment env = sim.getEnv();
		
		BufferedImage image = new BufferedImage(env.getEnvWidth(), env.getEnvHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D imageGraphics = image.createGraphics();
		sim.draw(imageGraphics, 0, 0, env.getEnvWidth(), env.getEnvHeight());
		
		File snapshotDirectory = new File("./" + Snapshotter.SNAPSHOT_FOLDER + "/");
		if (!snapshotDirectory.exists() && !snapshotDirectory.isDirectory()) {
			snapshotDirectory.mkdir();
		}
		
		File runDirectory = new File(snapshotDirectory.getPath() + "/" + this.runIdentifier + "/");
		if (!runDirectory.exists() && !runDirectory.isDirectory()) {
			runDirectory.mkdir();
		}
	    try {
            if (ImageIO.write(image, "png", new File(runDirectory.getPath() + "/" + this.generationSnapshotIncrement + ".png"))) {
                System.out.println("environment image saved");
                this.generationSnapshotIncrement++;
            }
	    } catch (IOException e) {
            e.printStackTrace();
	    }
	}
	
}
