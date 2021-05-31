package hw1;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Segmented_Least_Squares {	// used a JFREECHART for Visual Part 
										// refer that "https://sourceforge.net/projects/jfreechart/files/"
	
	private static final String		  fileName 	= "Points.txt";
	private static ArrayList<Point2D> pointList = new ArrayList<>();// list for Points.txt (X,Y)
	private static ArrayList<Point2D> lineXY 	= new ArrayList<>();// list that has the Start & End XY of Segments
	private static int		   		 C; 							// coefficient taken from the User
	private static int				 N; 							// length of points
	private static double []		 lookupOPT;						// look-up table for OPT
	private static int	  []		 INDEX;							// index list for OPT
	private static double [][]		 Eij;							// list of Eij
	private static double [][][] 	 ABlist;						// list for constants a, b in 0,1 order
	private static double BOUND =	 Double.POSITIVE_INFINITY;		// max value any integer can get
	private static double 			 result;
	
	private static void readPoints() throws FileNotFoundException{

		Scanner inFile = new Scanner(new FileReader(fileName)); // reading file
		while(inFile.hasNext()){ 								// getting all the points as X,Y and adding them to pointList
			double x; double y;
			x = Double.valueOf(inFile.next());
			y = Double.valueOf(inFile.next());
			Point2D.Double pointDouble = new Point2D.Double(x, y);
		    pointList.add(pointDouble);
		}
		inFile.close();
		
		N = pointList.size();
		lookupOPT 	= new double[N + 1];
		INDEX		= new int	[N + 1];
        Eij			= new double[N + 1][N + 1];
        ABlist		= new double[2][N + 1][N + 1]; 
	}

	private static void getInput(){ 				// gets coefficient C from the user
		Scanner reader = new Scanner(System.in);
		System.out.print("Enter a coef: ");
		C = reader.nextInt();
		reader.close();
		System.out.println("COEF: "+C);
	}
	
	private static void computeOPT(){
		//ninja
		computeEij();
		lookupOPT[0] = 0;
		for(int j = 1; j <= N; j++){ double min = BOUND;
			int inx = 0;
			for(int i = 1; i <= j; i++){
				double tmp = Eij[i][j] + lookupOPT[i-1] + C;
				if(tmp < min){min = tmp;inx = i;}
			}
			lookupOPT[j] = min;
			INDEX [j] = inx;
		}
		result = lookupOPT[N];
	}
	
	private static void computeEij(){				//pre-computing Eij
		for(int j = 1; j <= N; j++){
			for(int i = 1; i <= j; i++){
				if(i == j) 	{ Eij[i][j] = BOUND; }
				else		{ Eij[i][j] = getSSE(i,j); }
			}
		}
	}
	
	private static double getSSE(int i, int j){ 	//finding SSE
		double 	 val	= 0;
		int 	 n		= j - i + 1;
		double 	 a		= 0;
		double 	 b 		= 0;
		double 	 sumXY	= 0;
		double 	 sumX 	= 0;
		double 	 sumY 	= 0;
		double 	 sumX2	= 0;
		
		for(int k = i; k < j; k++){ 				// Calculating sub-parts for a & b
			double X = pointList.get(k).getX();
			double Y = pointList.get(k).getY();
			sumXY 	+= X * Y; 
			sumX 	+= X; 
			sumY 	+= Y;
			sumX2 	+= X * X;
		}
		
		double num = ((n * sumXY) - (sumX * sumY));
		if(num != 0)
		{
			double denum = ((n * sumX2) - (sumX * sumX));
			if(denum != 0){ a = num / denum;} 		// making sure that a has real value
		}
		b = (sumY-(a*sumX))/n;
		
		ABlist[0][i][j] = a;
		ABlist[1][i][j] = b;
		
		for(int k = i; k <= j; k++){ 				//calculating result part using a & b
			double X = pointList.get(k-1).getX();
			double Y = pointList.get(k-1).getY();
			double tmp = Y - a * X - b;
			val += tmp * tmp;
		}
		
		return val;
	}
	
	private static void stSegment(){				//getting index
		Stack<Integer> st = new Stack<Integer>();
        for(int endIndex = N, startIndex = INDEX[N]; endIndex > 0; endIndex = startIndex - 1, startIndex = INDEX[endIndex]) {
            st.push(endIndex);
            st.push(startIndex);
        }
        printAll(st);
    }

    private static void printAll(Stack<Integer> st){
        System.out.println("COST: " + result);
        System.out.println("*********************************** SOLUTION ************************************");
        int k = 1;
        while(!st.isEmpty()){
        	int i = st.peek(); st.pop();			//start
    		int j = st.peek(); st.pop();			//end
    		System.out.println("Line Segment "+k+": " + i + " End Index: " +j + " //*// a: "+ ABlist[0][i][j]+" b: "+ ABlist[1][i][j]);
    		k++; getLineXY(i,j);					// sending start & end to get a line segment
        }
    }
    
    private static void getLineXY(int i, int j){ 	//getting X,Y from equation with start & end points
    		double a = ABlist[0][i][j];
    		double b = ABlist[1][i][j];
    		double X = pointList.get(i-1).getX();
    		double Y = a * X + b;
    		lineXY.add(new Point2D.Double(X,Y));	// start
    		X = pointList.get(j-1).getX();
    		Y = a * X + b;
    		lineXY.add(new Point2D.Double(X,Y)); 	// end
    }
    
    private static void drawPoints(){ 				// graph for Points & Lines
		SwingUtilities.invokeLater(() -> {
			Graph graph = new Graph(pointList,lineXY);
			graph.pack();
			graph.setSize(800, 400);
			graph.setLocationRelativeTo(null);
			graph.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			graph.setVisible(true);
        });	
	}
    
	public static void main(String[] args) throws FileNotFoundException {
		readPoints();
		getInput();
		computeOPT();
		stSegment();
		drawPoints();	
	}
}


@SuppressWarnings("serial")
class Graph extends JFrame {  									// VISUAL PART
	ArrayList<Point2D> pointList = new ArrayList<>();
	ArrayList<Point2D> lineList = new ArrayList<>();
	
	public Graph() { 											// default constructor
		initUI();
	}
	public Graph(ArrayList<Point2D> pL, ArrayList<Point2D> lL) {// constructor for point & line in written order
		pointList = pL;
		lineList = lL;
		initUI();
	}
	private void initUI(){										// initializing
		XYDataset dataset = createDataset();    				// creating Data Set
		JFreeChart chart = ChartFactory.createScatterPlot(  	// setting chart for the DS
				"Segmented Least Squares",   
				"X-Axis",
				"Y-Axis",
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false );
		
		//settings for visual parts color etc.***
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinesVisible(true);
	    plot.setRangeGridlinePaint(Color.BLACK);
	    plot.setDomainGridlinesVisible(true);
	    plot.setDomainGridlinePaint(Color.BLACK);
	    XYLineAndShapeRenderer lsRenderer = new XYLineAndShapeRenderer();
	    lsRenderer.setSeriesLinesVisible(0, false);
	    lsRenderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(lsRenderer);
        final NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
        ChartPanel panel = new ChartPanel(chart);		
		setContentPane(panel);  
	}
	private XYDataset createDataset() {
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		XYSeries series1 = new XYSeries("Points"); 	// default point series
		for(Point2D i:pointList){
			series1.add(i.getX(), i.getY());
	    }
		dataset.addSeries(series1);
		
		XYSeries series2 =new XYSeries("");			// blank series to get point with a line
		dataset.addSeries(series2);
		
		XYSeries series3 = new XYSeries("Line"); 	// series that is point with a line
		for(Point2D i:lineList){
        	series3.add(i.getX(), i.getY());
        }
		dataset.addSeries(series3);

	    return dataset;
	}  
}
