package measurements;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class Measure {
	
	public static int totalTruth = 0;
	public static int totalResults = 0;
	public static int totalUnion = 0;
	
	public static String compare(String filename1, String filename2) {
		
		String outputStr = "";
		
		try {
			File file0 = new File("unambiguous.txt");
			FileInputStream fStream0 = new FileInputStream(file0);
			DataInputStream in0 = new DataInputStream(fStream0);
			BufferedReader br0 = new BufferedReader(new InputStreamReader(in0));
			
			String strLine;
			int number;
			ArrayList<Integer> list0 = new ArrayList<Integer>();
			while ((strLine = br0.readLine()) != null)   {
				String[] parts = strLine.split(" ");
				for (int i = 0; i < parts.length; i++) {
					try {
						number = Integer.parseInt(parts[i]);
						if (!list0.contains(number))
							list0.add(number);
					}
					catch (NumberFormatException e) {
						continue;
					}
				}
			 }			
			System.out.println("total un-ambiguous entities = " + list0.size());
			outputStr += "total un-ambiguous entities = " + list0.size() + "\n";
			in0.close();
			
			File file1 = new File(filename1);
			FileInputStream fStream1 = new FileInputStream(file1);
			DataInputStream in1 = new DataInputStream(fStream1);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
			
			ArrayList<Integer> list1 = new ArrayList<Integer>();
			while ((strLine = br1.readLine()) != null)   {
				String[] parts = strLine.split(" ");
				int id;
				for (int i = 0; i < parts.length; i++) {
					id = Integer.parseInt(parts[i]);
					if (list0.contains(id) && !list1.contains(id))
						list1.add(id);
				}
			 }			
			System.out.println("results.size() = " + list1.size());
			outputStr += "results.size() = " + list1.size() + "\n";
			totalResults += list1.size();
			in1.close();
			
			File file2 = new File(filename2);
			FileInputStream fStream2 = new FileInputStream(file2);
			DataInputStream in2 = new DataInputStream(fStream2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			
			ArrayList<Integer> list2 = new ArrayList<Integer>();
			while ((strLine = br2.readLine()) != null)   {
				String[] parts = strLine.split(" ");
				int id;
				for (int i = 0; i < parts.length; i++) {
					id = Integer.parseInt(parts[i]);
					if (!list2.contains(id))
						list2.add(id);
				}
			 }
			System.out.println("truth.size() = " + list2.size());
			outputStr += "truth.size() = " + list2.size() + "\n";
			totalTruth += list2.size();
			in2.close();
			
			// Now compute the union
			float precision = 0;
			float recall = 0;
			int union = 0;
			
			for (int i: list1)
				if (list2.contains(i))
					union += 1;
			
			totalUnion += union;
			
			precision = (float) 100 * union / list1.size(); // if list 1 is the retrieved entities
			recall = (float) 100 * union / list2.size();    // and list 2 is the relevant (ground truth) entities
			float F1 = 2 * (precision * recall) / (precision + recall); 
			
			System.out.println("union: " + union + "\tprecision: " + precision + "%\trecall: " + recall + "%\t F1-score: " + F1);
			outputStr += "union: " + union + "\tprecision: " + precision + "%\trecall: " + recall  + "%\t F1-score: " + F1 + "%\n";
		} catch (IOException e) {
			System.err.println("can not read from file.");
		}
		
		return outputStr;
	}
	
	//-------------------------------------------------------------------	
	
	public static void main(String[] args) {
		
		String filename = "CBZ9z/accuracy_20colors-withBlankInitialEntities.txt";
		
	       try {
	            // initialize output streams
	            FileOutputStream fos = new FileOutputStream(filename);
	            System.out.println("Writing to file " + filename);
	            PrintStream ps = new PrintStream(fos);
			
	            System.out.println("---\nChris Anderson: The former editor-in-chief of the Wired magazine:");
	            ps.println("---\nChris Anderson: The former editor-in-chief of the Wired magazine:");
	            ps.println(Measure.compare("CBZ9z/resolved_9", "CA_Wired.txt"));
	            
	            
	            System.out.println("---\nChris Anderson: The TED curator:");
	            ps.println("---\nChris Anderson: The TED curator:");
	            ps.println(Measure.compare("CBZ9z/resolved_16", "CA_TED.txt"));
	            
	            System.out.println("---\nChris Anderson: Detective Lt.:");
	            ps.println("---\nChris Anderson: Detective Lt.:");
	            ps.println(Measure.compare("CBZ9z/resolved_13", "CA_detective.txt"));
	            
	            System.out.println("---\ntotalResults=" + totalResults + "\ttotalTruth=" + totalTruth + "\ttotalUnion=" + totalUnion);
	            ps.println("---\ntotalResults=" + totalResults + "\ttotalTruth=" + totalTruth + "\ttotalUnion=" + totalUnion);

	            float precision = (float) 100 * totalUnion / totalResults;
	            System.out.println("total precision = " + precision + "%");
	            ps.println("total precision = " + precision + "%");

	            float recall = (float) 100 * totalUnion / totalTruth;
	            System.out.println("total recall = " + recall + "%");
	            ps.println("total recall = " + recall + "%");
	            
	            float F1 = 2 * (precision * recall) / (precision + recall); 
	            System.out.println("total F1-score = " + F1);
	            ps.println("total F1-score = " + F1);
	            
	            fos.close();
	            // initialize output streams
//	          String fname = "outputGraph";
//	          FileOutputStream fos = new FileOutputStream(fname);
//	          System.out.println("Writing to file " + fname);
//	          PrintStream pstr = new PrintStream(fos);
//	          Main.writeGraphToNeato(pstr);
//	          fos.close();
	   
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	        

		
	}
}
