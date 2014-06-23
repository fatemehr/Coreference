package communityDetection_with_hashmap;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class CommunityDetector_with_hashmap {

//-------------------------------------------------------------------	
	public static HashMap<Integer, Node_with_hashmap> readGraph(String filename) {
		
		HashMap<Integer, Node_with_hashmap> nodes = new HashMap<Integer, Node_with_hashmap>();
		
		try {
			String strLine;
			File file = new File(filename);
			FileInputStream fStream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			int numNodes = 0;
			int numEdges = 0;
			
			while ((strLine = br.readLine()) != null)   {
				System.out.println(strLine);
					
				if (strLine.startsWith("%") || strLine.startsWith("#"))
					continue;
			
				String[] parts = strLine.split(" ");
				numNodes = Integer.parseInt(parts[0]);
				numEdges = Integer.parseInt(parts[1]);
				break;
			 }
			
			System.out.println("-----------------------> nodes: " + numNodes + ", edges: " + numEdges);

			int count = 0;
			String label;
			ArrayList<Integer> neighbours = new ArrayList<Integer>();
			
			int edges = 0;
			while ((strLine = br.readLine()) != null)   {
			
				neighbours.clear();
				String[] parts = strLine.split("\t");
					
				// Note: parts[0] contains the label of the current node 
				// Note: parts[1] contains the list of document ids that the current node has appeared in
				for (int i = 2; i < parts.length; i++) {
					neighbours.add(Integer.parseInt(parts[i]) - 1);
					edges++;
				}
				
				ArrayList<Integer> documents = new ArrayList<Integer>();
				String documentList = parts[1].substring(1, parts[1].length() - 1);
				String []documentIds = documentList.split(",");
				for (int d = 0; d < documentIds.length ; d++)
					documents.add(Integer.parseInt(documentIds[d].trim()));
				
				label = (parts.length == 0 ? "*" : parts[0]);
				Node_with_hashmap node = new Node_with_hashmap(count, label, documents);
				node.setNeighbours(neighbours);
				nodes.put(count, node);
				
				count++;
			 }			
			
			System.out.println("read " + count + " nodes and " + edges/2 + " edges.");
			
			in.close();
		} catch (IOException e) {
			System.err.println("can not read from file " + filename);
		}
		
		return nodes;
	}

//------------------------------------------------------------------------------------------------
}
