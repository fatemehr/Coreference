package main;

import java.util.HashMap;

import communityDetection.CommunityDetector;
import communityDetection.Host;
import communityDetection.Node;
import communityDetection_with_hashmap.CommunityDetector_with_hashmap;
import communityDetection_with_hashmap.Host_with_hashmap;
import communityDetection_with_hashmap.Node_with_hashmap;


public class ER {
//------------------------------------------------------------------------
	// input parameters for CBZ9z
	private static String prefix = "CBZ9z_2013Aug12";
	private static String ambiguousEntity = "CBZ9z";
	private static int num_colors = 1190;
	private static int rounds = 50;
	private static String graphFile = prefix + "/CBZ9z_filtered.graph";
	private static String resolvedFilesPrefix = prefix + "/CBZ9z_resolved_";
	private static String groundTruthFile = prefix + "/CBZ9z_groundTruth_filtered.txt";
	
	// input parameters for john smith
//	private static String prefix = "xmljs";
//	private static String ambiguousEntity = "John Smith";
//	private static int num_colors = 197;
//	private static int rounds = 50;
//	private static String graphFile = prefix + "/xmljs.graph";
//	private static String resolvedFilesPrefix = prefix + "/js_resolved_";
//	private static String groundTruthFile = prefix + "/groundTruth.txt";

	//------------------------------------------------------------------------
	
	public static void main(String[] args) {
		
		// run community detection
//		HashMap<Integer, Node> graph = CommunityDetector.readGraph(graphFile, num_colors);
//		String[] ambiguous = {ambiguousEntity};
//		Host host = new Host(graph, num_colors, rounds, prefix, resolvedFilesPrefix, groundTruthFile, ambiguous);
//		host.run();

//		// run community detection _ with hashmap
		HashMap<Integer, Node_with_hashmap> graph = CommunityDetector_with_hashmap.readGraph(graphFile);
		String[] ambiguous = {ambiguousEntity};
		Host_with_hashmap host = new Host_with_hashmap(graph, rounds, prefix, resolvedFilesPrefix, groundTruthFile, ambiguous);
		host.run();
		
	}
	
}
