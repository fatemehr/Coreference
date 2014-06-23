package communityDetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Node {	

	private int id;
	private String entityLabel;
	private int color;
	private int initColor;
	private ArrayList<Integer> neighbours;
	private float[] wArray;
	public float alpha = 3;
	public float[] xArray;
	public float[] income;
	private int num_colors;
	
	boolean randomInitialColoring = false;
	boolean graphIsOnlyMadeofAmbiguousDocuments = true;
	private static HashMap<Integer, Integer> mapDocumentColor = new HashMap<Integer, Integer>();
	
//-------------------------------------------------------------------	
	public Node(int id, String label, ArrayList<Integer> documents, int num_colors) {
		this.id = id;
		this.entityLabel = label;
		this.num_colors = num_colors;
		this.neighbours = new ArrayList<Integer>();
		
		wArray = new float[num_colors];
		xArray = new float[num_colors];
		income = new float[num_colors];
		
		for (int i = 0; i < num_colors; ++i) {
			this.wArray[i] = 0;
			xArray[i] = wArray[i];
			income[i] = 0;
		}
		
		
		if (randomInitialColoring == true) {
			int c = (new Random()).nextInt(num_colors);
			this.wArray[c] = alpha;
		}
		else if (graphIsOnlyMadeofAmbiguousDocuments == false) {
			if (!this.entityLabel.contains(":"))
				for (int color: documents) {
					this.wArray[color % num_colors] = alpha;			
					xArray[color % num_colors] = wArray[color % num_colors];
				}
			else
				this.color = documents.get(0) % num_colors;
		}
		else {
			int c;
			if (!this.entityLabel.contains(":")) {
				for (int docid: documents) {
					if (mapDocumentColor.containsKey(docid))
						c = mapDocumentColor.get(docid);
					else {
						c = mapDocumentColor.size();
						mapDocumentColor.put(docid, c);
					}
					
					this.wArray[c] = alpha;			
					xArray[c] = wArray[c];
				}
			}
			else {
				for (int docid: documents) {
					if (mapDocumentColor.containsKey(docid))
						c = mapDocumentColor.get(docid);
					else {
						c = mapDocumentColor.size();
						mapDocumentColor.put(docid, c);
					}
					this.color = c;
				}
			}
		}
		
	}

//-------------------------------------------------------------------	
	public void setColor(int color) {
		this.color = color;
	}

//-------------------------------------------------------------------	
	public void setNeighbours(ArrayList<Integer> neighbours) {
		for (int id : neighbours)
			this.neighbours.add(id);
	}
	
//-------------------------------------------------------------------	
	public int getId() {
		return this.id;
	}
//-------------------------------------------------------------------	
	public String getLabel() {
		return this.entityLabel;
	}
//-------------------------------------------------------------------	
	public int getColor() {
		return this.color;
	}
//-------------------------------------------------------------------	
	public int getDegree() {
		return this.neighbours.size();
	}
//-------------------------------------------------------------------	
	public int getInitColor() {
		return this.initColor;
	}

//-------------------------------------------------------------------	
	public ArrayList<Integer> getNeighbours() {
		return this.neighbours;
	}
//------------------------------------------------------------------------
	public float getwArray(int c) {
		return this.wArray[c];
	}
//------------------------------------------------------------------------
	public void setwArray(int c, float amount) {
		this.wArray[c] = amount;
	}
//------------------------------------------------------------------------
	public float[] getwArray() {
		return this.wArray;
	}	
//------------------------------------------------------------------------
	public void addFlow(float[] flow) {
		for (int c = 0; c < num_colors; ++c)
			income[c] += flow[c];
	}	
//------------------------------------------------------------------------
	private int maxArg(float[] array) {
		int maxIndex = color;
		float maxValue = array[color];
		
		for (int i = 0; i < array.length ; ++i) {
			if (array[i] > maxValue) {
				maxValue = array[i];
				maxIndex = i;
			}
		}
		
		return maxIndex;
	}
//------------------------------------------------------------------------
	public void flush() {
		
		for (int c = 0; c < num_colors; ++c) {
			wArray[c] = xArray[c] + income[c]; 
			income[c] = 0;
			xArray[c] = wArray[c];
		}
		
			this.color = maxArg(wArray);
	
	}	
//------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "id: " + id + ", color: " + color + ", neighbours: " + neighbours + "\n";
	}
}
