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

import java.util.*;

public class RssiDemo implements MessageListener {

  private MoteIF moteIF;
  int gridSizeInches = 6;
  int roomSizeInchesX = 720;
  int roomSizeInchesY = 240;
  HashMap<Rectangle2D, Double> grid;
  HashMap<Integer, Double> sensorData; //<source_node_id, rssi_dBm>
  HashMap<Integer, Tuple<Double,Double>> nodeLocation; //<source_node_id, (x,y)>

  int readings_since_tick = 0;

  public static void updateSensor(int source, double rssi_dbm){
    sensorData.put(source, rssi_dbm);
    if(readings_since_tick>4){ //kind of arbitrary; but I'll have around 4 nodes
      tick();
      Iterator<Map.Entry<Integer, Double>> entries = grid.entrySet().iterator();
      while(entries.hasNext()){
        Map.Entry<Integer, Double> entry = entries.next();
        int source = entry.getKey();
        double irssi = entry.getValue();
        double x = nodeLocation.get(source).get(0);
	double y = nodeLocation.get(source).get(1);

        addSensorReading(x,y,rssiConvert(rssi));         
      }
    }    
  }

  public static void 
  public RssiDemo(MoteIF moteIF) {
    this.moteIF = moteIF;
    this.moteIF.registerListener(new RssiMsg(), this);
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
  
  public static void initializeGrid(){
    grid = new HashMap<Rectangle2D, Double>();
    for(x=0; x<=roomSizeInchesX; x+=gridSizeInches){
      for(y=0; y+=gridSizeInches; y<=roomSizeInchesY){
        grid.put(new Rectangle2D.Float(x,y,gridSizeInches, gridSizeInches), 0);
      }
    }
  }

public static void tick(){
        readings_since_tick=0;
        Iterator<Map.Entry<Rectangle2D, Integer>> entries = grid.entrySet().iterator();
        while(entries.hasNext()){
                Map.Entry<Rectangle2D, Integer> entry = entries.next();
                square = entry.getKey();
                probability = entry.getValue();
                grid.put(square, probability*0.5); //probability decay with one half life per tick
        }
}

/**
 * This really out to be an exponential, but real-life testing
 * (these are regression-based values from readings/measurements we took)
 * results in a curve that is too wonky to fit with any sane exponential or even 5-power polynomial.
 * As a result, I've approximated with a piecewise function of varying linear regressions.
 **/
public static double rssiConvert(double rssi){
	rssi_delta_from_noise_floor = 52.506;
	rssi_delta = abs(rssi)-rssi_delta_from_noise_floor;
	if(rssi_delta<0){
		if(rssi_delta<-2.0){ //if we're less than a foot off, whatever. I don't need to hear about it.
			System.err.printLn("WARNING: Noise floor was lower than expected. Difference is "+rssi_delta+" dBm!");
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
		return ((144.0-72.0)/23.1004354941*rssi_delta + 72;
	}
	
}

public static void addSensorReading(x,y,rssiRangeInches){
	variance = gridSizeInches/2;
        Shape minCircle = new Ellipse2D.Float(x,y, rssiRangeInches-variance, rssiRangeInches-variance));
        Shape maxCircle = new Ellipse2D.Float(x,y, rssiRangeInches+variance, rssiRangeInches+variance);

        Area rangeDonut = new Area(maxCircle);
        rangeDonut.subtract(new Area(minCircle));

        Iterator<Map.Entry<Rectangle2D, Integer>> entries =grid.entrySet().iterator();
        while(entries.hasNext()){
                Map.Entry<Rectangle2D, Integer> entry = entries.next();
                square = entry.getKey();
                probability = entry.getValue();
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
    initializeGrid();
    MoteIF mif = new MoteIF(phoenix);
    RssiDemo serial = new RssiDemo(mif);
  }


}
