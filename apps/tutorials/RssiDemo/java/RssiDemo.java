/*
 * Copyright (c) 2005 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holders nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

/*
 * This is a modified version of TestSerial.java, from apps/tests/TestSerial
 * from TinyOS 2.x (www.tinyos.net)
 */

/**
 * Triangulation application
 *
 * Based on:
 * Java-side application for testing the RSSI demo
 * from Tiny-OS tutorial/default documentation.
 * 
 * @author Chris Lockfort <clockfort@csh.rit.edu>
 * @author Phil Levis <pal@cs.berkeley.edu>
 * @author Dimas Abreu Dutra <dimas@dcc.ufmb.br>
 */

import java.io.IOException;

import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

import java.util.*; //HashMap et al
import java.awt.*; //Shapes et al
import java.awt.geom.*; //Ellipse2D
import java.io.*; //InputStream et al

import javax.swing.*; //JFrame for GUI

public class RssiDemo implements MessageListener {

  private MoteIF moteIF;
  static int gridSizeInches = 6;
  static int roomSizeInchesX = 360;
  static int roomSizeInchesY = 240;
  private HashMap<Rectangle2D, Double> grid;
  private HashMap<Integer, Double> sensorData = new HashMap<Integer, Double>(); //<source_node_id, rssi_dBm>
  private HashMap<Integer, Tuple<Double,Double>> nodeLocation = new HashMap<Integer, Tuple<Double,Double>>(); //<source_node_id, (x,y)>
  private HashMap<Integer, Boolean> sensorSeen = new HashMap<Integer, Boolean>();
  private int readings_since_tick = 0;
	
	private JPanel canvas; //GUI
	
	private class Tuple<X, Y> {
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		}
	}
	
  public void askSensorLocation(int source){
	try{
   InputStreamReader istream = new InputStreamReader(System.in) ;
   BufferedReader bufRead = new BufferedReader(istream);
   System.out.print("Enter x coordinate for sensor #" + source + ": ");
   String strX = bufRead.readLine();
   double x = Double.parseDouble(strX);
   System.out.print("\nEnter y coordinate for sensor #" + source +": ");
   String strY = bufRead.readLine();
   double y = Double.parseDouble(strY);
   nodeLocation.put(source, new Tuple<Double,Double>(x,y));
	} catch(IOException e){
		System.out.println(e.getMessage());
	}
  }

  public void checkExistence(int source){
    if(!nodeLocation.containsKey(source))
      askSensorLocation(source);
  }

  public void updateSensor(int source, double rssi_dbm){
    checkExistence(source);
    sensorData.put(source, rssi_dbm);
    readings_since_tick++;
    sensorSeen.put(source, true);
    if(!sensorSeen.containsValue(false)){
      tick();
      Iterator<Map.Entry<Integer, Double>> entries = sensorData.entrySet().iterator();
      while(entries.hasNext()){
        Map.Entry<Integer, Double> entry = entries.next();
        double rssi = entry.getValue();
        checkExistence(source);
        double x = nodeLocation.get(source).x;
	double y = nodeLocation.get(source).y;
        addSensorReading(x,y,rssiConvert(rssi));         
      }
    }    
  }

  public RssiDemo(MoteIF moteIF) {
    this.moteIF = moteIF;
    this.moteIF.registerListener(new RssiMsg(), this);
	
	initializeGrid();
	
	/*
	 * GUI
	 */
	JFrame frame = new JFrame();
	class DrawPanel extends JPanel {
		public void paintComponent(Graphics g) {
			g.fillRect(0, 0, 360, 240);
		}
	}
	//Canvas
	canvas = new DrawPanel();
	canvas.setPreferredSize(new Dimension(360,240));
	frame.add(canvas);
	
	//Frame Properties
	frame.setTitle("Localization");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.pack();
	frame.setLocationRelativeTo(null);

	frame.setVisible(true);
	
	/*
	 * End GUI
	 */
  }
    
  public void messageReceived(int to, Message message) {
    RssiMsg msg = (RssiMsg) message;
    double rssi = msg.get_rssi();
    double vrssi = (3*rssi/1024);
    double rssi_dbm = (-51.3*vrssi-49.2);

    int source = message.getSerialPacket().get_header_src();
    System.err.println("Rx packet, node=" + source + 
		       " rssi_raw= " +  msg.get_rssi() +" rssi_dbm: " + rssi_dbm + " dBm");
    updateSensor(source, rssi_dbm);
  }
  

  private static void usage() {
    System.err.println("usage: RssiDemo [-comm <source>]");
  }
  
  public void initializeGrid(){
    grid = new HashMap<Rectangle2D, Double>();
    for(int x=0; x<=roomSizeInchesX; x+=gridSizeInches){
      for(int y=0; y<=roomSizeInchesY; y+=gridSizeInches){
        grid.put(new Rectangle2D.Double(x,y,gridSizeInches, gridSizeInches), 0.0);
      }
    }
  }

public void tick(){
	System.out.println("Tick!");
	Rectangle2D square;
	double probability;
		
        readings_since_tick=0;
	Iterator<Map.Entry<Integer, Boolean>> sensors = sensorSeen.entrySet().iterator();
        while(sensors.hasNext()){
          Map.Entry<Integer, Boolean> entry = sensors.next();
          entry.setValue(false);
        }

        Iterator<Map.Entry<Rectangle2D, Double>> entries = grid.entrySet().iterator();
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
	//g2.setColor(Color.red);
	while(entries.hasNext()){
                Map.Entry<Rectangle2D, Double> entry = entries.next();
                square = entry.getKey();
                probability = entry.getValue();
                grid.put(square, probability*0.4); //probability decay with one half life per tick
				
				//Drawing on GUI
				float redness = (float)probability / 5.0f;
				redness=redness < 1? redness : 1; //clamp redness 
				g2.setColor(new Color((float)probability / 6, (float)0.0, (float)0.0));
				g2.fill(square);
        }
	printBestGuess();
}

public void printBestGuess(){
	Rectangle2D square = new Rectangle2D.Double(0,0,0,0);
	double probability;
	
	double max_probability=0;
	Rectangle2D best = new Rectangle2D.Double(0,0,0,0);

        Iterator<Map.Entry<Rectangle2D, Double>> entries = grid.entrySet().iterator();
        while(entries.hasNext()){
                Map.Entry<Rectangle2D, Double> entry = entries.next();
                square = entry.getKey();
                probability = entry.getValue();
		if(probability>max_probability){
			max_probability=probability;
			best = square;
		}
        }
	System.err.println("Best guess is currently: (" + square.getX() +", "+ square.getY() +") @ P="+max_probability);
}
/**
 * This really ought to be an exponential, but real-life testing
 * (these are regression-based values from readings/measurements we took)
 * results in a curve that is too wonky to fit with any sane exponential or even 5-power polynomial.
 * As a result, I've approximated with a piecewise function of varying linear regressions.
 **/
public double rssiConvert(double rssi){
	double rssi_delta_from_noise_floor = 52.506;
	double rssi_delta = Math.abs(rssi)-rssi_delta_from_noise_floor;
	if(rssi_delta<0){
		if(rssi_delta<-2.0){ //if we're less than a foot off, whatever. I don't need to hear about it.
			System.err.println("WARNING: Noise floor was lower than expected. Difference is "+rssi_delta+" dBm!");
		}
		rssi_delta=0; //continue on, assuming we're right right next to the receiver
	}
	if(rssi_delta>0 && rssi_delta<=0.3669944586){ //1-6 inches
		return ((6.0-1.0)/0.3669944586)*rssi_delta + 1;
	}
	else if(rssi_delta<=3.013016183){ //6-18 inches
		return ((18.0-6.0)/3.013016183)*rssi_delta + 6;
	}
	else if(rssi_delta<=12.230090332){ //18-36 inches
		return ((36.0-18.0)/12.230090332)*rssi_delta + 18;
	}
	else if(rssi_delta<=12.4568404797){ //36-72 inches
		return ((72.0-36.0)/12.4568404797)*rssi_delta + 36;
	}
	else{ //72-144(-infinity) inches
		return ((144.0-72.0)/23.1004354941)*rssi_delta + 72;
	}

}

public void addSensorReading(double x, double y, double rssiRangeInches){
	double variance = gridSizeInches/2;
	double radius = (rssiRangeInches-variance) / 2.0;
        Shape minCircle = new Ellipse2D.Double(x-radius,y-radius, rssiRangeInches-variance, rssiRangeInches-variance);
	radius = (rssiRangeInches+variance) / 2.0;
        Shape maxCircle = new Ellipse2D.Double(x-radius,y-radius, rssiRangeInches+variance, rssiRangeInches+variance);

        Area rangeDonut = new Area(maxCircle);
        rangeDonut.subtract(new Area(minCircle));

        Iterator<Map.Entry<Rectangle2D, Double>> entries =grid.entrySet().iterator();
        while(entries.hasNext()){
                Map.Entry<Rectangle2D, Double> entry = entries.next();
                Rectangle2D square = entry.getKey();
                double probability = entry.getValue();
                if(rangeDonut.intersects(square)){
                        grid.put(square, probability+1);
                }
        }
}

  public static void main(String[] args) throws Exception {
    String source = null;
    if (args.length == 2) {
      if (!args[0].equals("-comm")) {
	usage();
	System.exit(1);
      }
      source = args[1];
    }
    else if (args.length != 0) {
      usage();
      System.exit(1);
    }
    
    PhoenixSource phoenix;
    
    if (source == null) {
      phoenix = BuildSource.makePhoenix(PrintStreamMessenger.err);
    }
    else {
      phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
    }
    //initializeGrid();
    MoteIF mif = new MoteIF(phoenix);
    RssiDemo serial = new RssiDemo(mif);
  }


}
