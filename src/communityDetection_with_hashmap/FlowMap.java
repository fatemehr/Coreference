package communityDetection_with_hashmap;

import java.util.HashMap;

public class FlowMap {
	private HashMap<Integer, HashMap<Integer, Float>> map;
	
	public FlowMap() {
		map = new HashMap<Integer, HashMap<Integer,Float>>();
	}
	
	public void addSubFlow(Integer neighborColor, int flowColor, float flowVolume) {
		
		if (neighborColor == null)
			neighborColor = -1;
		
		if (neighborColor != null) {
			HashMap<Integer, Float> flow = map.get(neighborColor);
			if (flow == null) {
				flow = new HashMap<Integer, Float>();
			}
			
			float amount = flowVolume;
			if (flow.containsKey(flowColor))
				amount += flow.get(flowColor);
			
			flow.put(flowColor, amount);
			
			map.put(neighborColor, flow);
		}
//		else
//			for (int c : map.keySet()) {
//				HashMap<Integer, Float> flow = map.get(c);
//				if (flow == null) {
//					flow = new float[colors];
//					for (int i = 0; i < colors; ++i)
//						flow[i] = 0;
//				}
//				
//				flow[flowColor] += flowVolume;
//				
//				map.put(c, flow);
//			}
	}

	public HashMap<Integer, Float> getFlow(int neighborColor) {
		return map.get(neighborColor);
	}
	
	public HashMap<Integer, Float> getDefaultFlow() {
		return map.get(-1);
	}
	
	public String toString() {
		String str = "";
		HashMap<Integer, Float> flow;
		for (int c : map.keySet()) {
			flow = getFlow(c);
			str += "[";
			for (int i : flow.keySet())
				str += flow.get(i) + ",";
			str += "] ";
		}
		return str;
	}
	
}
