package communityDetection_with_hashmap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import communityDetection.Node;



public class Host_with_hashmap {
	private HashMap<Integer, Node_with_hashmap> nodes;
	
	private int round = 0;
	private int num_rounds;
	private String prefix;
	private String mainPrefix;
	
	HashMap<Integer, Integer> groundTruth = new HashMap<Integer, Integer>();
	HashMap<Integer, ArrayList<Integer>> groundTruthList = new HashMap<Integer, ArrayList<Integer>>();
	ArrayList<Integer> knownDocuments = new ArrayList<Integer>();

//-------------------------------------------------------------------	
	public Host_with_hashmap(HashMap<Integer, Node_with_hashmap> nodes, int num_rounds, String mainPrefix, String outputPrefix, String groundTruthFile, String[] ambiguous) {
		this.nodes = nodes;
		this.num_rounds = num_rounds;
		this.prefix = outputPrefix;
		this.mainPrefix = mainPrefix;
		
		try {
			File file = new File(groundTruthFile);
			FileInputStream fStream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			int lineNumber = 0;
			int doc;
			ArrayList<Integer> doclist;
			while ((strLine = br.readLine()) != null)   {
				if (strLine.startsWith("%"))
					continue;
				
				String[] truthParts = strLine.split("\t");
				doclist = new ArrayList<Integer>();
				for (int i = 0; i < truthParts.length; i++) {
					try {
						doc = Integer.parseInt(truthParts[i]);
						groundTruth.put(doc, lineNumber);
						doclist.add(doc);
						knownDocuments.add(doc);
					}
					catch (NumberFormatException e) {
						continue;
					}
				}
				groundTruthList.put(lineNumber, doclist);
				lineNumber++;
			}	
			br.close();
		}
		catch (IOException e) {
			System.out.println("IO Exception while reading the ground truth file!");
		}
	}

//-------------------------------------------------------------------
	public void run() {
		this.report();
   
		for (round = 0; round < num_rounds; round++) {
			
			for (int id : this.nodes.keySet())
				diffuse(id);
			
			for (int id : this.nodes.keySet())
				this.nodes.get(id).flush();	
			
			clearUp();
			
			this.report();
			writeNodesAndEdgeList();
		}
	}
	
//-------------------------------------------------------------------	
 private void clearUp() {
	 
	 HashMap<Integer, Float> volume = new HashMap<Integer, Float>();
	 HashMap<Integer, Integer> population = new HashMap<Integer, Integer>();
	 Node_with_hashmap n;
	 Node_with_hashmap neighbor;
	 
	 HashMap<Integer, Float> inventory= new HashMap<Integer, Float>();
	 HashMap<Integer, Integer> distribution = new HashMap<Integer, Integer>();

	 for (int id : this.nodes.keySet()) {
		 n = this. nodes.get(id);
		 if (distribution.containsKey(n.getColor()))
			 distribution.put(n.getColor(), distribution.get(n.getColor()) + 1);
		 else
			 distribution.put(n.getColor(), 1);
		 
		 for(int c : n.getwArray().keySet())
			 volume.put(c, n.getwArray(c));
		 
		 for (int neighborId : n.getNeighbours()) {
				 neighbor = this.nodes.get(neighborId);
				 int nc = neighbor.getColor();
				 if (population.containsKey(nc))
					 population.put(nc, population.get(nc) + 1);
				 else
					 population.put(nc, 1);
					
				 for (int c : neighbor.getwArray().keySet()) {
					 if (volume.containsKey(c))
						 volume.put(c, volume.get(c) + neighbor.getwArray(c));
					 else
						 volume.put(c, neighbor.getwArray(c));
				 }
		}

			
		 int mostPopular = maxArg(volume, n.getColor());

		 if (population.get(mostPopular) != null && population.get(mostPopular) == n.getDegree()) {
			 for(int c : n.getwArray().keySet())
				 if (c != mostPopular && n.getwArray(c) > 0) {
					 if (inventory.containsKey(c))
						 inventory.put(c, inventory.get(c) + n.getwArray(c));
					 else
						 inventory.put(c, n.getwArray(c));
//					 float newAmount = n.getwArray(mostPopular) + n.getwArray(c);
//					 n.setwArray(mostPopular, newAmount);
					 n.setwArray(c, 0);
				 }
		 }
	 }
	 
		
	 HashMap<Integer, Float> share = new HashMap<Integer, Float>();
	 for (int c : distribution.keySet()) {
		 if (distribution.get(c) > 0 && inventory.containsKey(c)) {
			 share.put(c, inventory.get(c) / distribution.get(c));
			 inventory.remove(c);
		 }
	 }
	 for (int id : this.nodes.keySet()) {
		 n = this.nodes.get(id);
		 int c = n.getColor();
//		 share[c] = Math.min(100, inventory[c] / distribution[c]);
		 if (share.containsKey(c))
			 n.setwArray(c, n.getwArray(c) + share.get(c));
	 }
	 
	 
 }
//-------------------------------------------------------------------	
 private void diffuse(int id) {
	 Node_with_hashmap n = this.nodes.get(id);
	 Node_with_hashmap neighbor;
	 
	//---------------------------
	// Calculate the popular color and the population of each color in the neighborhood, based on local knowledge only
	//---------------------------
	 HashMap<Integer, Float> volume = new HashMap<Integer, Float>();
	 HashMap<Integer, Integer> population = new HashMap<Integer, Integer>();
	 
	 for(int c : n.getwArray().keySet())
		 volume.put(c, n.getwArray(c));
	 
	 for (int neighborId : n.getNeighbours()) {
			 neighbor = this.nodes.get(neighborId);
			 int nc = neighbor.getColor();
			 if (population.containsKey(nc))
				 population.put(nc, population.get(nc) + 1);
			 else
				 population.put(nc, 1);
				
			 for (int c : neighbor.getwArray().keySet()) {
				 if (volume.containsKey(c))
					 volume.put(c, volume.get(c) + neighbor.getwArray(c));
				 else
					 volume.put(c, neighbor.getwArray(c));
			 }
	}
	 int mostPopular = maxArg(volume, n.getColor());
	 
	 //---------------------------
	 // Generate a flow per color
	 //---------------------------
	 FlowMap map = new FlowMap();
	 float flow;
	 float left;
	 
	 for (int c : n.getwArray().keySet()) {
		 n.xArray.put(c, n.getwArray(c));
						
		 if (c == mostPopular) {
//			 	if (population.get(c)!= null && population.get(c) == n.getDegree()) {
//			 		n.setColor(c);
//			 		flow = n.getwArray(c) / (float)(n.getDegree());
//			 		left = n.getXarray(c) - flow * n.getDegree();
//					n.xArray.put(c,left);
//					map.addSubFlow(null, c, flow); // send to all neighbors
//			 	} else {
			 		flow = n.getwArray(c) / (float)(n.getDegree() * n.alpha);
			 		left = n.getXarray(c) - flow * n.getDegree();
					n.xArray.put(c,left);
					map.addSubFlow(null, c, flow); // send to all neighbors
//			 	}
		} else {
			if (round > 30 && population.get(c)!= null) {
				 flow = n.getwArray(c) / (float)(population.get(c));
				 n.xArray.remove(c);
				 map.addSubFlow(c, c, flow); // send to neighbors with color c
			 }
			 else {
				flow = n.getwArray(c) / (float)(n.getDegree());
				n.xArray.remove(c);
				map.addSubFlow(null, c, flow); // send to all neighbors
			 }
		}		
		 
//		 if (n.xArray[c] > n.alpha * 1) {	// Cap for the node color
//			 float extra = n.xArray[c] - (n.alpha * (float)1);
//			 flow = (extra / (float)(n.getDegree()));
//			 n.xArray[c] -= extra;
//				
//			 map.addSubFlow(null, c, flow);
//		}
	 }
	
	 //---------------------------
	 // Send the flows to the neighbors based on their color
	 //---------------------------		
	 for (int neighborId : n.getNeighbours()) {
		 neighbor = this.nodes.get(neighborId);
			
		 HashMap<Integer, Float> subflow = map.getFlow(neighbor.getColor());
		 if (subflow != null)
			 neighbor.addFlow(subflow);
		 subflow = map.getDefaultFlow();
		 if (subflow != null)
			 neighbor.addFlow(subflow);
	}
 }
 
//------------------------------------------------------------------------
	private int maxArg(HashMap<Integer, Float> array, int color) {
		int maxIndex = color;
		float maxValue = -1;
		
		for (int i : array.keySet()) {
			if (array.get(i) > maxValue) {
				maxValue = array.get(i);
				maxIndex = i;
			}
		}
		
		return maxIndex;
		
	}
	
//------------------------------------------------------------------------
    private void report() {
        
        HashMap<Integer, Integer> population = new HashMap<Integer, Integer>();
        
        int size = this.nodes.size();
    	
        for (int i = 0; i < size; i++) {
        	Node_with_hashmap node = this.nodes.get(i);
            int nodeColor = node.getColor();
            
            
            if (!population.containsKey(nodeColor))
            	population.put(nodeColor, 1);
            else
            	population.put(nodeColor, population.get(nodeColor) + 1);
            
        }
        
        System.out.print("round:\t" + this.round + "\t");
        
	    resolveEntities();        	

        if (round > 0 && round % (num_rounds -1) == 0)  {		// in the last round, dump the whole graph and communities
	        System.out.println("Entity resolution is complete.");
	        
        }
    }
 //------------------------------------------------------------------------
    private void resolveEntities() {
    	HashMap<Integer, String> entities = new HashMap<Integer, String>();
    	ArrayList<Node_with_hashmap> ambiguousNodes = new ArrayList<Node_with_hashmap>();
    	
    	Node_with_hashmap n;
//    	int fragment;
    	for (int i = 0; i < nodes.size(); i++) {
    		n = nodes.get(i);
    		if (n.getLabel().contains(":")) {
    			String[] parts = n.getLabel().split(":");
//    			fragment = Integer.parseInt(parts[1]) + 1;
//    			entities[n.getColor()] += fragment + " ";

    			String str = "";
    			if (entities.containsKey(n.getColor()))
    				str = entities.get(n.getColor());
    			str += parts[1] + " ";
    			entities.put(n.getColor(), str);
    			
    			ambiguousNodes.add(n);
    		}
    	}
    	
    	String accuracyStr = "";
    	try {
    		accuracyStr = measureAccuracy(entities, ambiguousNodes);
    		System.out.println(accuracyStr);
    	} catch (IOException e) {
    		System.out.println(e.getMessage());
    	}
    	
//    	String filenamePrefix = prefix;
//    	String filename;
    	FileIO.write("", prefix + "all");
    	for (int c : entities.keySet()) {
//	    		filename = filenamePrefix + c;
//	    		System.out.println("Writing to file " + filename);
//	    		FileIO.write(entities[c], filename);
    		FileIO.append(entities.get(c) + "\n", prefix + "all");
    	}
    	FileIO.append(accuracyStr + "\n", prefix + "all");
    	
    }
//------------------------------------------------------------------------
/*    private void graphToFile(PrintStream ps, boolean dump) {
    	String[] colors = {"red", "blue", "green", "yellow", "violet", "pink", "skyblue", "gold", "darkgreen", "maroon", "darkslategray1", "darkseagreen", "crimson", "magenta", "brown", "olivedrab", "cyan", "cyan3", "cornsilk", "violetred", "tomato", "navyblue"};
    	
    	String[] colorString = new String[colors.length];
    	for (int i = 0; i < colors.length; i++)
    		colorString[i] = "\n{node [style=filled, color=" + colors[i] + "]";
    	String greyString = "\n{node [style=filled, color=grey]";
    	
   		ps.println("graph round_" + round + " {\n node [height=\"0.2\",width=\"0.2\",label=\"\"];");					// Neato Style
   		
    	Node_with_hashmap current;
    	for (int id: this.nodes.keySet()) {
    		current = this.nodes.get(id);
    		String label = " " + current.getId() + "[label=\"" + current.getLabel(); 
    		label += "\"]";
    		
    		if (current.getLabel().contains(":"))
    			colorString[current.getColor()] += label;
    		else
    			greyString += label;
    	}  
    	
    	for (int i = 0; i < colors.length; i++)
    		ps.println(colorString[i] + "}");
    	ps.println(greyString + "}");
    	
		ps.println("edge [len = 1];");
    	
    	
    	Node_with_hashmap neighbor;
    	for (int id: this.nodes.keySet()) {
    		current = this.nodes.get(id);
    		if (current.getNeighbours() != null)
    			for (int nid : current.getNeighbours()) {
    				neighbor = this.nodes.get(nid);
   					if (dump == true) {
// 						if (current.getColor() == neighbor.getColor()) {	// Neato Style
//    							ps.println(current.getId() + " -- " +  neighbor.getId() + "[color=" + colors[current.getColor()] + "] [dir = forward] [len=5];");
//    						} else {
    							ps.println(current.getId() + " -- " +  neighbor.getId() + "[color=gray] [dir=forward] [len=5];");
//    						}
    						ps.println();
    					}
    			}
    	}
    	
   		ps.println("}");				// Neato Style
    	
    }
    */

  //------------------------------------------------------------------------
	private String measureAccuracy(HashMap<Integer, String> entities, ArrayList<Node_with_hashmap> ambiguousNodes) throws IOException {
		String[] parts;
		
		ArrayList<Integer> cluster_truth;
		
		HashMap<Integer, ArrayList<Integer>> results = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> cluster_result;
		
		float p = 0;
		float r = 0;
		int counter = 0;
		int u;
		int color;
		
		for (int i : entities.keySet()) {
			parts = entities.get(i).split(" ");
			cluster_result = new ArrayList<Integer>();
			for (int j = 0; j < parts.length; j++)
				if (parts[j].length() > 0 && knownDocuments.contains(Integer.parseInt(parts[j])))
					cluster_result.add(Integer.parseInt(parts[j]));
			results.put(i, cluster_result);
			
			
			for (int j = 0; j < cluster_result.size(); j++) {
				if (!groundTruth.containsKey(cluster_result.get(j)))
					continue;
				color = groundTruth.get(cluster_result.get(j));
				cluster_truth = groundTruthList.get(color);
				
				u = union(cluster_result, cluster_truth);
				p += (float) 100 * u / cluster_result.size();
				r += (float) 100 * u / cluster_truth.size();
				counter++;
			}
		}
		float precision = p / counter;
		float recall = r / counter;
		float F1 = 2 * (precision * recall) / (precision + recall); 
		return "\tprecision:\t" + precision + "\trecall:\t" + recall + "\tF1:\t" + F1;  
	}

	//------------------------------------------------------------------------	
	private  void writeNodesAndEdgeList() {
		
		try {
	        String edgeFilename = mainPrefix + "/graph/edgeList_" + round + ".csv";
	        FileOutputStream fosEdge = new FileOutputStream(edgeFilename);
	        PrintStream pstrEdge = new PrintStream(fosEdge);
	        
	        String nodeFilename = mainPrefix + "/graph/nodeList_" + round + ".csv";
	        FileOutputStream fosNode = new FileOutputStream(nodeFilename);
	        PrintStream pstrNode = new PrintStream(fosNode);
	        
	
	    	pstrEdge.println("source;target;");
	    	pstrNode.println("Id;Label;True Community;Detected Community;");
	    	
	    	Node_with_hashmap current;
	    	String[] idstr;
	    	int docId;
	    	int communityId;
	    	for (int id: nodes.keySet()) {
	    		current = nodes.get(id);
	    		
	    		if (current.getLabel().contains(":")) {
	    			idstr = current.getLabel().split(":");
	    			docId = Integer.parseInt(idstr[1]);
	    			communityId = groundTruth.get(docId) + 1;
	    			pstrNode.println(current.getId() + ";" + current.getLabel() + ";" + communityId + ";" + current.getColor() + 1);
	    		}
	    		else
	    			pstrNode.println(current.getId() + ";" + current.getLabel() + ";" + "null" + ";" + "0");

	    		
	    		if (current.getNeighbours() != null)
	    			for (int nid : current.getNeighbours()) {
	    				pstrEdge.println(current.getId() + ";" +  nid + ";");
	    			}
	    	}
	    	
	    	pstrNode.close();
	    	pstrEdge.close();
		}
		catch (IOException e) {
			System.out.println("IO Exception while writing the node and edge list!");
		}
	    	
	}

	//------------------------------------------------------------------------
	private int union (ArrayList<Integer> list1, ArrayList<Integer> list2) {
		int counter = 0;
		
		for (int id: list1)
			if (list2.contains(id))
				counter++;
		
		return counter;
	}	
	
}