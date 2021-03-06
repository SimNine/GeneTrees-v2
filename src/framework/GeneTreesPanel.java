package framework;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Timer;

import simulation.Environment;
import simulation.GeneTree;
import simulation.RainDrop;
import simulation.SunSpeck;
import urf.pannablepanel.PannablePanel;

@SuppressWarnings("serial")
public class GeneTreesPanel extends PannablePanel {
	
	private GeneTree trackedTree = null;
	Environment env;
	
	private int tickSpeed = 1;

	private boolean ticking = true;
	private boolean drawing = true;
	
	private Timer time = new Timer(tickSpeed, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	time();
        }
	});
	
	public GeneTreesPanel(int width, int height) {
		super(width, height);
		
		// create environment
		double[] gFreq = { 0.002,
						   0.01,
						   0.04,
						   0.2,
						   0.5 };
		double[] gAmp = { Math.random()*500,
				   		  Math.random()*200,
				   		  Math.random()*80,
				   		  Math.random()*5,
				   		  Math.random()*5 };
		double[] gDisp = { Math.random()*500,
						   Math.random()*500,
						   Math.random()*500,
				   		   Math.random()*500,
				   		   Math.random()*500 };
		env = new Environment(6000, 2000, 600, gFreq, gAmp, gDisp);
		
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_P:
					ticking = !ticking;
					break;
				case KeyEvent.VK_D:
					GeneTrees.debug = !GeneTrees.debug;
					break;
				case KeyEvent.VK_F1:
					Loader.saveGame();
					break;
				case KeyEvent.VK_F2:
					Loader.loadGame();
					break;
				case KeyEvent.VK_R:
					//continuousGenAndSave();
					break;
				case KeyEvent.VK_Q:
					drawing = !drawing;
					break;
				case KeyEvent.VK_M:
					env.multithreading = !env.multithreading;
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				trackedTree = env.getTreeAt(xMouse + xScr, yMouse + yScr);
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
	}
	
	public void time() {
    	if (ticking) {
    		checkKeys();
    		env.tick();
    		
    		if (env.getTickCount() >= 1000) {
    			repaint();
    		}
    	}
    	if (drawing)
    		repaint();
	}

	public void paintComponent(Graphics g) {
		// draw the sky
		g.setColor(new Color(146, 184, 244));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		// draw the ground
		g.drawImage(env.getGroundImg(), -xScr, -yScr, null);
		
		// draw each tree
		for (GeneTree t : env.getTrees()) {
			t.draw(g);
		}
		
		// draw each sunspeck
		for (SunSpeck s : env.getSun()) {
			s.draw(g);
		}
		
		// draw each raindrop
		for (RainDrop d : env.getRain()) {
			d.draw(g);
		}
		
		// draw the environment bounding box
		g.setColor(new Color(255, 0, 0));
		g.drawRect(-xScr, -yScr, env.getSimWidth(), env.getSimHeight());
		
		// draw the selected tree display box
		drawTrackedTree(g);
		
		// draw the debug text
		int fh = 15; // fontHeight
		int ln = 1; // lineNum
		g.setColor(Color.BLACK);
		g.drawString("Tick Speed: " + tickSpeed, 0, fh*ln++);
		g.drawString("Tick number: " + env.getTickCount(), 0, fh*ln++);
		g.drawString("Generation number: " + env.getNumGens(), 0, fh*ln++);
		ln++;
		g.drawString("Screen Top-left: " + xScr + "," + yScr, 0, fh*ln++);
		ln++;
		if (trackedTree == null) {
			g.drawString("Click on a tree to track it.", 0, fh*ln++);
		} else {
			g.drawString("Current tree's energy: " + trackedTree.getEnergy(), 0, fh*ln++);
			g.drawString("Current tree's nutrients: " + trackedTree.getNutrients(), 0, fh*ln++);
			g.drawString("Current tree's fitness: " + trackedTree.getFitness(), 0, fh*ln++);
			g.drawString("Current tree's fitness percentile: " + trackedTree.getFitnessPercentage(), 0, fh*ln++);
		}
		ln++;
		g.drawString("Minimum fitness: " + env.getMinFitness(), 0, fh*ln++);
		g.drawString("Maximum fitness: " + env.getMaxFitness(), 0, fh*ln++);
		g.drawString("Average fitness: " + env.getAvgFitness(), 0, fh*ln++);
		g.drawString("Number of trees: " + env.getTrees().size(), 0, fh*ln++);
		ln++;
		g.drawString("Controls:", 0, fh*ln++);
		g.drawString("Pan screen: arrow keys", 0, fh*ln++);
		g.drawString("Pan faster: LShift", 0, fh*ln++);
		g.drawString("Pause simulation: P", 0, fh*ln++);
		g.drawString("Pause drawing (but continue simulation): Q", 0, fh*ln++);
		g.drawString("Toggle debug features: D", 0, fh*ln++);
		g.drawString("Toggle multithreading: M", 0, fh*ln++);
	}
	
	private void drawTrackedTree(Graphics g) {
		if (trackedTree == null)
			return;
		
		int w = trackedTree.getxMax()-trackedTree.getxMin();
		int h = trackedTree.getyMax()-trackedTree.getyMin();
		
		g.setColor(Color.WHITE);
		g.fillRect(getWidth() - w - 30, getHeight() - h - 30, w + 20, h + 20);
		trackedTree.draw(g, 
						 trackedTree.getxMin() - (getWidth() - w - 20), 
						 trackedTree.getyMin() - (getHeight() - h - 20));
		
		g.setColor(Color.BLACK);
		g.drawString("Selected Tree:", getWidth() - w - 30, getHeight() - h - 15);
	}
	
	public void stopTime() {
		time.stop();
	}
	
	public void startTime() {
		time.start();
	}
	
	public GeneTree getTrackedTree() {
		return trackedTree;
	}
	
	public Environment getEnv() {
		return env;
	}
}