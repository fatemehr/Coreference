package communityDetection_with_hashmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Node_with_hashmap {	

	private int id;
	private String entityLabel;
	private int color;
	private int initColor;
	private ArrayList<Integer> neighbours;
	private HashMap<Integer, Float> wArray;
	public float alpha = 3;
	public HashMap<Integer, Float> xArray;
	public HashMap<Integer, Float> income;
	private int num_colors;
	
	boolean randomInitialColoring = false;
	boolean graphIsOnlyMadeofAmbiguousDocuments = false;
	private static HashMap<Integer, Integer> mapDocumentColor = new HashMap<Integer, Integer>();
	
//-------------------------------------------------------------------	
	public Node_with_hashmap(int id, String label, ArrayList<Integer> documents) {
		this.id = id;
		this.entityLabel = label;
		this.neighbours = new ArrayList<Integer>();
		
		wArray = new HashMap<Integer, Float>();
		xArray = new HashMap<Integer, Float>();
		income = new HashMap<Integer, Float>();
		
		
		if (randomInitialColoring == true) {
			int c = (new Random()).nextInt(num_colors);
			wArray.put(c, alpha);
		}
		else if (graphIsOnlyMadeofAmbiguousDocuments == false) {
			if (!this.entityLabel.contains(":"))
				for (int color: documents) {
					wArray.put(color,  alpha);			
					xArray.put(color,  alpha);
				}
			else
				this.color = documents.get(0);
		}
		else {
			int c;
			if (!this.entityLabel.contains(":"))
				for (int docid: documents) {
					if (mapDocumentColor.containsKey(docid))
						c = mapDocumentColor.get(docid);
					else {
						c = mapDocumentColor.size();
						mapDocumentColor.put(docid, c);
					}
					
					wArray.put(c, alpha);			
					xArray.put(c, alpha);
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
		float amount;
		if (wArray.containsKey(c))
			amount = wArray.get(c);
		else
			amount = 0;
		
		return amount;
	}
//------------------------------------------------------------------------
	public float getIncome(int c) {
		float amount;
		if (income.containsKey(c))
			amount = income.get(c);
		else
			amount = 0;
		
		return amount;
	}
//------------------------------------------------------------------------
	public float getXarray(int c) {
		float amount;
		if (xArray.containsKey(c))
			amount = xArray.get(c);
		else
			amount = 0;
		
		return amount;
	}
//------------------------------------------------------------------------
	public void setwArray(int c, float amount) {
		wArray.put(c, amount);
	}
//------------------------------------------------------------------------
	public HashMap<Integer, Float> getwArray() {
		return wArray;
	}	
//------------------------------------------------------------------------
	public void addFlow(HashMap<Integer, Float> flow) {
		float amount;
		for (int c : flow.keySet()) {
			amount = flow.get(c) + getIncome(c);
			income.put(c, amount);
		}
	}	
//------------------------------------------------------------------------
	private int maxArg(HashMap<Integer, Float> array) {
		int maxIndex = color;
		float maxValue = 0;
		
		for (int i : array.keySet()) {
			if (array.get(i) > maxValue) {
				maxValue = array.get(i);
				maxIndex = i;
			}
		}
		
		return maxIndex;
	}
//------------------------------------------------------------------------
	public void flush() {
		ArrayList<Integer> activeColors = new ArrayList<Integer>();
		
		for (int c : xArray.keySet()) {
			if (xArray.get(c) > 0)
				activeColors.add(c);
		}
		for (int c : income.keySet()) {
			if (income.get(c) > 0 && !activeColors.contains(c))
				activeColors.add(c);
		}
		
		wArray.clear();
		for (int c: activeColors) {
			wArray.put(c, getXarray(c) + getIncome(c));
		}
		
		income.clear();
		xArray.clear();
		for (int c: wArray.keySet())
			xArray.put(c, wArray.get(c));
		
		
		
		this.color = maxArg(wArray);
			
	}	
//------------------------------------------------------------------------
	@Override
	public String toString() {
		return "id: " + id + ", color: " + color + ", neighbours: " + neighbours + "\n";
	}
}
