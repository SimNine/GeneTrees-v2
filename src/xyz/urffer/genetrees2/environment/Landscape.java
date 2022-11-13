package xyz.urffer.genetrees2.environment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Landscape {
	
	private BufferedImage groundImg;
	private int groundBaseline;
	private int groundDegree;
	private double[] groundFreq;
	private double[] groundAmp;
	private double[] groundDisp;
	
	private int[][] tileType;
	private int[] groundLevels;
	
	public Landscape(
		int sWidth,
		int sHeight, 
		int gBaseline,
		double[] gFreq,
		double[] gAmp,
		double[] gDisp
	) {
		groundBaseline = gBaseline;
		groundFreq = gFreq;
		groundAmp = gAmp;
		groundDisp = gDisp;
		
		int min = Math.min(groundFreq.length, groundAmp.length);
		min = Math.min(min, groundDisp.length);
		groundDegree = min;
		
		// create the tile contents and background image
		tileType = new int[sWidth][sHeight];
		groundImg = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < groundImg.getWidth(); x++) {
			double m = getAlgorithmicGroundLevel(x);
			
			for (int y = (int)m; y < groundImg.getHeight(); y++) {
				groundImg.setRGB(x, y, new Color(183, 85, 23).getRGB());
				tileType[x][y] = 1;
			}
		}
		
		groundLevels = new int[sWidth];
		for (int x = 0; x < sWidth; x++) {
			ArrayList<Integer> groundLevels = getGroundLevels(x);
			this.groundLevels[x] = groundLevels.get(0);
		}
	}
	
	public boolean isGround(int x, int y) {
		return tileType[x][y] == 1;
	}
	
	public int getGroundLevel(double xPos) {
		if (xPos < 0 || xPos > this.groundImg.getWidth()) {
			return 0;
		}
		return this.groundLevels[(int)xPos];
	}
	
	public ArrayList<Integer> getGroundLevels(double xPos) {
		int x = (int)Math.floor(xPos);
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int y = 1; y < tileType[0].length; y++) {
			if (tileType[x][y] == 1 && tileType[x][y-1] == 0) {
				ret.add(y);
			}
		}
		
		return ret;
	}
	
	private double getAlgorithmicGroundLevel(double xPos) {
		double sum = 0;
		for (int i = 0; i < groundDegree; i++) {
			sum += Math.cos(groundFreq[i]*xPos + groundDisp[i])*groundAmp[i];
		}
		sum += groundBaseline;
		return sum;
	}
	
	public double[] getGroundFreq() {
		return groundFreq;
	}
	
	public double[] getGroundAmp() {
		return groundAmp;
	}
	
	public double[] getGroundDisp() {
		return groundDisp;
	}
	
	public int getGroundBaseline() {
		return groundBaseline;
	}
	
	public int getGroundDegree() {
		return groundDegree;
	}
	
	public BufferedImage getGroundImg() {
		return groundImg;
	}
	
}
