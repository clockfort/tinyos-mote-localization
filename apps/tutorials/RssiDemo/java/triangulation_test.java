import java.util.*;

int gridSizeInches = 6;
int roomSizeInchesX = 720;
int roomSizeInchesY = 240;

HashMap<Rectangle2D, Integer> grid;
public static void initializeGrid(){
	grid = new HashMap<Rectangle2D, Integer>();
	for(x=0; x<=roomSizeInchesX; x+=gridSizeInches){
		for(y=0; y+=gridSizeInches; y<=roomSizeInchesY){
			grid.put(new Rectangle2D.Float(x,y,gridSizeInches, gridSizeInches), 0);
		}
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
}

