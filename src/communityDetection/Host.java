package communityDetection;

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


public class Host {
	private HashMap<Integer, Node> nodes;
	
	private int round = 0;
	private int num_colors;
	private int num_rounds;
	private String prefix;
	private String mainPrefix;

	HashMap<Integer, Integer> groundTruth = new HashMap<Integer, Integer>();
	HashMap<Integer, ArrayList<Integer>> groundTruthList = new HashMap<Integer, ArrayList<Integer>>();
	
	
//-------------------------------------------------------------------	
	public Host(HashMap<Integer, Node> nodes, int num_colors, int num_rounds, String prefix, String outputPrefix, String groundTruthFile, String[] ambiguous) {
		this.nodes = nodes;
		this.num_colors = num_colors;
		this.num_rounds = num_rounds;
		this.prefix = outputPrefix;
		this.mainPrefix = prefix;
		
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
		writeNodesAndEdgeList();

       for (round = 0; round < num_rounds; round++) {
			
			for (int id : this.nodes.keySet())
				diffuse(id);
			
			for (int id : this.nodes.keySet())
				this.nodes.get(id).flush();	
			
			clearUp();
			
			this.report();
			writeNodeCommunities();
			
		}

	}
	
//-------------------------------------------------------------------	
 private void clearUp() {
	 
	 float[] volume = new float[num_colors];
	 int[] population = new int[num_colors];
	 Node n;
	 Node neighbor;
	 
	float[] inventory= new float[num_colors];
	for (int c = 0; c < num_colors; ++c)
		inventory[c] = 0;
		
	 int[] distribution = new int[num_colors];
	 for(int c = 0; c < num_colors; ++c)
		 distribution[c] = 0;
	 
	 
	 for (int id : this.nodes.keySet()) {
		 n = this. nodes.get(id);
		 distribution[n.getColor()]++;
		 
		 for(int c = 0; c < num_colors; ++c) {
			 volume[c] = n.getwArray(c);
			 population[c] = 0;
			 for (int neighborId : n.getNeighbours()) {
				 neighbor = this.nodes.get(neighborId);
					
				 if (neighbor.getColor() == c)
					 population[c]++;
					
				volume[c] += neighbor.getwArray(c);
			}
		}
			
		 int mostPopular = maxArg(volume, n.getColor());

		 if (population[mostPopular] == n.getDegree()) {
			 for(int c = 0; c < num_colors; ++c)
				 if (c != mostPopular) {
					 inventory[c] += n.getwArray(c);
					 n.setwArray(c, 0);
				 }
		 }

	 }
	 
		
	 float[] share = new float[num_colors];
	 for (int c = 0; c < num_colors; ++c) {
		 if (distribution[c] > 1) {
			 share[c] = inventory[c] / distribution[c];
			 inventory[c] = 0;
		 }
	 }
	 for (int id : this.nodes.keySet()) {
		 n = this.nodes.get(id);
		 int c = n.getColor();
//		 share[c] = Math.min(100, inventory[c] / distribution[c]);
	 	 n.setwArray(c, n.getwArray(c) + share[c]);
	 }
	 
 }
//-------------------------------------------------------------------	
 private void diffuse(int id) {
	 Node n = this.nodes.get(id);
	 Node neighbor;
	 
	 
	//---------------------------
	// Calculate the popular color and the population of each color in the neighborhood, based on local knowledge only
	//---------------------------
	 float[] volume = new float[num_colors];
	 int [] population = new int[num_colors];
	 
	 for(int c = 0; c < num_colors; ++c) {
		 volume[c] = n.getwArray(c);
		 population[c] = 0;
			
		 for (int neighborId : n.getNeighbours()) {
			 neighbor = this.nodes.get(neighborId);
				
			 if (neighbor.getColor() == c)
				 population[c]++;
				
			volume[c] += neighbor.getwArray(c);
		}
	}
	 int mostPopular = maxArg(volume, n.getColor());
	 
	 //---------------------------
	 // Generate a flow per color
	 //---------------------------
	 FlowMap map = new FlowMap(num_colors);
	 float flow;

	 for (int c = 0; c < num_colors; ++c) {
		 n.xArray[c] = n.getwArray(c);
						
		 if (c == mostPopular) {
//			 	if (population[c] == n.getDegree()) {
//			 		flow = n.getwArray(c) / (float)(n.getDegree());
//					n.xArray[c] -= flow * n.getDegree();
//					map.addSubFlow(null, c, flow); // send to all neighbors
//			 	} else {
			 		flow = n.getwArray(c) / (float)(n.getDegree() * n.alpha);
					n.xArray[c] -= flow * n.getDegree();
					map.addSubFlow(null, c, flow); // send to all neighbors
//			 	}
		} else {
			if (round > 30) {
				 flow = n.getwArray(c) / (float)(population[c]);
				 n.xArray[c] = 0;
				 map.addSubFlow(c, c, flow); // send to neighbors with color c
			 }
			 else {
				flow = n.getwArray(c) / (float)(n.getDegree());
				n.xArray[c] = 0;
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
			
		 float [] subflow = map.getFlow(neighbor.getColor());
		 neighbor.addFlow(subflow);
	}
 }
 
//------------------------------------------------------------------------
	private int maxArg(float[] array, int color) {
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
    private void report() {
        
//        int[] population = new int[num_colors];
//        for (int i = 0; i < num_colors; i++)
//        	population[i] = 0;
//        
//        int size = this.nodes.size();
//    	
//        for (int i = 0; i < size; i++) {
//        	Node node = this.nodes.get(i);
//            int nodeColor = node.getColor();
//            
//            population[nodeColor]++;
//            
//        }
        
        System.out.print("round:\t" + this.round + "\t");
        
    	resolveEntities();        	

        if (round > 0 && round % (num_rounds -1) == 0)  {		// in the last round, dump the whole graph and communities
	        System.out.println("Entity resolution is complete.");
        }
    }
//------------------------------------------------------------------------
    private void resolveEntities() {
    	String[] entities = new String[num_colors];
    	for (int c = 0; c < num_colors; ++c)
    		entities[c] = "";
    	ArrayList<Node> ambiguousNodes = new ArrayList<Node>();
    	
    	Node n;
    	for (int i = 0; i < nodes.size(); i++) {
    		n = nodes.get(i);
    		if (n.getLabel().contains(":")) {
    			String[] parts = n.getLabel().split(":");
    			entities[n.getColor()] += parts[1] + " ";
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
    	
    	FileIO.write("", prefix + "all");
    	for (int c = 0; c < num_colors; ++c) {
    		if (entities[c].length() > 0) {
	    		FileIO.append(entities[c] + "\n", prefix + "all");
    		}
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
   		
    	Node current;
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
    	
    	
    	Node neighbor;
    	for (int id: this.nodes.keySet()) {
    		current = this.nodes.get(id);
    		if (current.getNeighbours() != null)
    			for (int nid : current.getNeighbours()) {
    				neighbor = this.nodes.get(nid);
   					if (dump == true) {
    						ps.println(current.getId() + " -- " +  neighbor.getId() + "[color=gray] [dir=forward] [len=5];");
    						ps.println();
    					}
    			}
    	}
    	
   		ps.println("}");				// Neato Style
    	
    }*/
        
//------------------------------------------------------------------------
	private String measureAccuracy(String[] entities, ArrayList<Node> ambiguousNodes) throws IOException {
		String[] parts;
		
		ArrayList<Integer> cluster_truth;
		
		
		HashMap<Integer, ArrayList<Integer>> results = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> cluster_result;
		
		float p = 0;
		float r = 0;
		int counter = 0;
		int u;
		int color;
		
		for (int i = 0; i < entities.length; i++) {
			parts = entities[i].split(" ");
			cluster_result = new ArrayList<Integer>();
			for (int j = 0; j < parts.length; j++)
				if (parts[j].length() > 0)
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
	    	
	    	Node current;
	    	String[] idstr;
	    	int docId;
	    	int communityId;
	    	for (int id: nodes.keySet()) {
	    		current = nodes.get(id);
	    		
	    		if (current.getLabel().contains(":")) {
	    			idstr = current.getLabel().split(":");
	    			docId = Integer.parseInt(idstr[1]);
	    			communityId = groundTruth.get(docId) + 1;
	    			pstrNode.println(current.getId() + ";" + current.getLabel() + ";" + communityId + ";" + current.getColor());
	    		}
	    		else
	    			pstrNode.println(current.getId() + ";" + current.getLabel() + ";" + "null" + ";" + "null");	    		
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
	private  void writeNodeCommunities() {
		
		try {
	        
	        String nodeFilename = mainPrefix + "/graph/color_" + round + ".csv";
	        FileOutputStream fosNode = new FileOutputStream(nodeFilename);
	        PrintStream pstrNode = new PrintStream(fosNode);
	        
	
	    	pstrNode.println("Id;Color " + round + ";");
	    	
	    	Node current;
	    	for (int id: nodes.keySet()) {
	    		current = nodes.get(id);
	    		
	    		pstrNode.println(current.getId() + ";" + current.getColor());
	    		
	    	}
	    	
	    	pstrNode.close();
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