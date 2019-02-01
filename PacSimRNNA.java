import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

/**
 * University of Central Florida
 * CAP4630 - Spring 2019
 * Author: Matthew Saucedo
 */

class SortPath implements Comparator<PopulationPath> 
{ 
    // Used for sorting in ascending order of path cost 
    public int compare(PopulationPath a, PopulationPath b) 
    { 
        return a.costPath - b.costPath; 
    } 
}  

class PopulationPath
{
	// overall cost of the path
	public int costPath;
	
	// cost incurred from old tail of path to new tail 
	public int [] moveCost;
	
	// list of the path, starting from PacMan
	public List<Point> path;
	
	// constructor if starting new list
	PopulationPath( int numFood )
	{
		this.moveCost = new int [numFood];
		
		this.path = new ArrayList<>();
	}
	
	// copy constructor (if starting diverging PopulationPath)
	PopulationPath( PopulationPath oldPopPath )
	{	
		// copy cost of path
		this.costPath = oldPopPath.costPath;
		
		// copy cost array
		this.moveCost = new int [oldPopPath.moveCost.length];
		System.arraycopy( oldPopPath.moveCost, 0, this.moveCost, 0, oldPopPath.moveCost.length );
		
		//copy path list
		this.path = copyList( oldPopPath.path );
	}	
	
	// returns cost of shortest path from point to any other point 
	public int showShortestPath( int [][] costTable, int pointIndex, List<Point> path, List<Point> listFood )
	{
		int smallest;
		int [] oneDimCost = new int[costTable.length];
		
		// create cost table with respect to the desired initial point
		for( int i = 0; i < oneDimCost.length; i++ )
		{
			oneDimCost[i] = costTable[pointIndex][i];
		}

		// prevent eaten pellets from being considered in shortest path
		for( int i = 0; i < listFood.size(); i++ )
		{
			if ( path.contains( listFood.get(i) ) )
			{
				oneDimCost[i + 1] = Integer.MAX_VALUE;
			}
		}
		
		// set initial value
		smallest = Integer.MAX_VALUE;
		
		for( int i = 1; i < costTable.length; i++ )
		{
			// update smallest if applicable
			if( oneDimCost[i] < smallest && oneDimCost[i] != 0 )
			{
				smallest = oneDimCost[i];
			}
		}
		
		return smallest;
	}
	
	public void populatePath( int [][] costTable, List<PopulationPath> populationList, List<Point> listFood )
	{
		PopulationPath divergingPop;
	
		// adds amount of points left to path
		for( int k = this.path.size(); k < listFood.size(); k++ )
		{
			// adds one point to path
			for( int i = 0; i < listFood.size(); i++ )
			{
				boolean branch = false;
					
				// compute shortest path value in cost table
				int costTableIndex = listFood.indexOf( this.showTail() ) + 1;
				int shortestPath = showShortestPath( costTable, costTableIndex, this.path, listFood ); 
				
				// add shortest path point to this.path and create new pop object if multiple
				for( int j = 0; j < costTable.length - 1; j++ )
				{
					if( branch == true )
					{
						if( costTable[costTableIndex][j + 1] == shortestPath && !this.path.contains(listFood.get(j)) )
						{
							// create a diverging population
							divergingPop = new PopulationPath( this );
								
							// take off last entry in list 
							divergingPop.path.remove( divergingPop.path.size() - 1 );
							
							// add point to path list
							divergingPop.path.add( listFood.get(j) );
							
							// populate the rest of the path
							divergingPop.populatePath( costTable, populationList, listFood );
							
							// add diverging population to population list
							populationList.add( divergingPop );
						}	
					}
					// point is equal to shortest distance and is not already in the path
					else if( costTable[costTableIndex][j + 1] == shortestPath && !this.path.contains(listFood.get(j)) )
					{	
						// add cost of path to moveCost array
						this.moveCost[k] = shortestPath;
							
						// update total cost of path
						this.costPath += shortestPath;
						
						// add point to path list
						this.path.add( listFood.get(j) );
							
						// indicate that any additional points would branch
						branch = true;
					}
				}
			}
		}
	}
	
	public List<Point> copyList( List<Point> oldList)
	{
		List<Point> copy = new ArrayList<Point>(oldList.size());
		for( Point p : oldList )
		{
			copy.add(new Point(p));
		}
		
		return copy;
	}
	
	// returns the last point in the path
	public Point showTail()
	{
		return this.path.get( this.path.size() - 1 );
	}
	
	// adds point to path
	public void addPoint( Point point, int [][] costTable, List<Point> listFood )
	{	
		// find index of tail and new point in cost table
		int costTableIndexTail = listFood.indexOf( showTail() ) + 1;
		int costTableIndexPoint = listFood.indexOf( point ) + 1;
	
		// store the cost from the tail to the new point
		moveCost[this.path.size()] = costTable[costTableIndexTail][costTableIndexPoint];
		
		// update costPath
		costPath += moveCost[this.path.size()];
		
		// add point to path
		this.path.add( point );
	}
}

public class PacSimRNNA implements PacAction 
{
	private List<Point> solutionPath;
	private int simTime;
      
	public PacSimRNNA( String fname ) 
	{
      PacSim sim = new PacSim( fname );
      sim.init(this);
	}
   
	public static void main( String[] args ) 
	{         
      System.out.println( "\nTSP using Repetitive Nearest Neighbor Algorithm by Matthew Saucedo:");
      System.out.println( "\nMaze : " + args[ 0 ] + "\n" );
      new PacSimRNNA( args[ 0 ] );
	}

	// Use this method to reset any variables that must be re-initialized between runs.
	@Override
	public void init() 
	{
		simTime = 0;
		//solutionPath = new ArrayList<>();
		solutionPath = null;
	}
	
	// method to format printing of vars of Point class
	public String toStringPoint(Point point)
	{
		return "(" + point.x + "," + point.y + ")";
	}
	
	public List<Point> printFood( List<Point> listFood )
	{	
		// formatting for food location table
		System.out.println( "Food Array:" + "\n" );
		
		// iterate over each pellet of food
		for( int i = 0; i < listFood.size(); i++ )
		{
			// print the food pellet location as an ordered pair
			System.out.println( i + " : " + toStringPoint( listFood.get(i) ) );
		}
		
		// formatting
		System.out.println();
		
		return listFood;
	}
	
	// prints a 2d array to screen with specified formatting
	public void printMatrix ( int [][] costTable )
	{
		// formatting
		System.out.println("Cost Table:\n");
		
		// iterate through array
		for( int i = 0; i < costTable.length; i++ )
		{
			// for formatting, tab
			System.out.print("\t");
			
			for( int j = 0; j < costTable.length; j++)
			{
				// print cost tab for formatting
				System.out.print( costTable[i][j] + "\t" );
			}
			
			// newline for formatting
			System.out.println();
		}
		// two newlines for formatting
		System.out.println("\n");
	}
	
	// returns costTable and calls method that prints it
	public int [][] generateCostTable( PacCell [][] grid, List<Point> listFood, PacmanCell pc )
	{
		int i, j;
		int [][] costTable = new int [listFood.size() + 1][listFood.size() + 1];
		
		// distance from PacMan to PacMan
		costTable[0][0] = 0;
		
		// generate first row of cost table, which is distance of PacMan to each food pellet
		for( i = 1; i < listFood.size() + 1; i++ )
		{
			costTable[0][i] = new BFSPath( grid, pc.getLoc(), listFood.get(i-1) ).getPath().size();
		}
		
		// generate rest of cost table
		for( i = 1; i < listFood.size() + 1; i++ )
		{
			// first space in cost table is distance of PacMan from food pellet i
			costTable[i][0] = costTable[0][i];																		
			
			for( j = 1; j < listFood.size() + 1; j++ )															
			{
				// cost is the size of the path from pellet i to pellet j
				costTable[i][j] = new BFSPath( grid, listFood.get(i-1), listFood.get(j-1) ).getPath().size();	
			}
		}
		
		// print the cost table
		printMatrix(costTable);
		
		return costTable;
	}
	
	// adds the in-between points to food order solution
	public List<Point> findSolutionPath( PacmanCell pc, List<Point> path, PacCell [][] grid ) 
	{
		List<Point> solution = new ArrayList<>();
		int pathLength = path.size() - 1;
		
		// add PacMan starting position to solution
		solution.add( pc.getLoc() );
		
		// add path from PacMan to first food pellet
		PacUtils.appendPointList( solution, BFSPath.getPath(grid, pc.getLoc(), path.get(0)) );
		
		// add points (firstPellet, lastPellet] to solution
		for( int i = 0; i < pathLength; i++ )
		{
			PacUtils.appendPointList( solution, BFSPath.getPath(grid, path.get(i), path.get(i+1)) );
		}
		
		return solution;
	}
	
	@Override
	public PacFace action( Object state ) 
	{
		// take game grid from parameter
		PacCell[][] grid = (PacCell[][]) state;
		
		// store location of PacMan
		PacmanCell pc = PacUtils.findPacman( grid );
		
		// store location of food
		List<Point> listFood;
		listFood = PacUtils.findFood( grid );
		int totalFood = listFood.size();
      
		// make sure Pac-Man is in this game
		if( pc == null ) return null;
        
        long timeForPlan;
		if( solutionPath == null ) 
		{
            timeForPlan = System.currentTimeMillis();
        
			// print cost table and store in variable
			int [][] costTable = generateCostTable( grid, listFood, pc  );
			
			// print food to screen
			printFood( listFood );
		
			// create list to hold populations
			List<PopulationPath> populationList = new ArrayList<>();
			
			// create list to hold diverging populations
			List<PopulationPath> divergingList = new ArrayList<>();
			
			// create population objects for listFood and add them to population list
			for ( int i = 0; i < totalFood; i++ )
			{
				// create path object
				populationList.add( new PopulationPath( totalFood ) );
				
				// set cost from PacMan to this food pellet
				populationList.get(i).costPath = costTable[0][i + 1];
				
				// add cost to moveCost array
				populationList.get(i).moveCost[0] = populationList.get(i).costPath;
			
				// add point to path list
				populationList.get(i).path.add( listFood.get(i) );
			}
			
			// sort population list
			Collections.sort( populationList, new SortPath());
			
			// print population step 1
			System.out.println("Population at step 1 :");
			for( int i = 0; i < populationList.size(); i++ )
			{
				System.out.println( "\t" + i + " : cost=" + populationList.get(i).costPath +
				" : [(" + populationList.get(i).showTail().x + "," + populationList.get(i).showTail().y + 
				")," + populationList.get(i).moveCost[0] + "]");
			}
			System.out.println();
					
			//populatePath( int [][] costTable, List<PopulationPath> populationList, List<Point> listFood )
			for( int i = 0; i < totalFood; i++ )
			{
				populationList.get(i).populatePath( costTable, populationList, listFood );
			}		
					
			// sort population list
			Collections.sort( populationList, new SortPath());
			
            // store the solution path
            solutionPath = findSolutionPath( pc, populationList.get(0).path, grid);
            
			// store time taken to generate solution path
            timeForPlan = System.currentTimeMillis() - timeForPlan;
        
			// print population last step
			System.out.println("Population at step " + totalFood + " :");
			for( int i = 0; i < populationList.size(); i++ )
			{
				// format for population path
				System.out.print( "\t" + i + " : cost=" + populationList.get(i).costPath +" : ");
				
				// print path
				for( int j = 0; j < totalFood; j++ )
				{
					System.out.print( "[(" + populationList.get(i).path.get(j).x + "," +
					populationList.get(i).path.get(j).y + 
					")," + populationList.get(i).moveCost[j] + "]");		
				}
				System.out.println();
			}
			System.out.println();
			
            // show time to generate move
            System.out.println("Time to generate plan: " + timeForPlan + " msec");
        
            // formatting for solution moves
            System.out.println("Solution moves:");
		}
		
		// take the next step on the current path
		Point next = solutionPath.get((int)simTime + 1);
		PacFace face = PacUtils.direction( pc.getLoc(), next );
		System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", ++simTime, pc.getLoc().x, pc.getLoc().y, face );
		
		return face;
   }
}
