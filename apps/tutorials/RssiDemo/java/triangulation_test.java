import java.util.*;

int gridSizeInches = 6;
int roomSizeInchesX = 720;
int roomSizeInchesY = 240;

HashMap<Rectangle2D, Double> grid;
public static void initializeGrid(){
	grid = new HashMap<Rectangle2D, Double>();
	for(x=0; x<=roomSizeInchesX; x+=gridSizeInches){
		for(y=0; y+=gridSizeInches; y<=roomSizeInchesY){
			grid.put(new Rectangle2D.Float(x,y,gridSizeInches, gridSizeInches), 0);
		}
	}
}

public static void tick(){
        Iterator<Map.Entry<Rectangle2D, Integer>> entries =grid.entrySet().iterator();
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

public static void main(){
	initializeGrid();
	
	addSensorReading(0,0, 24);
	addSensorReading(10,20, 36);
	tick();
	addSensorReading(0,0, 25);
}

