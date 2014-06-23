package communityDetection;

import java.util.HashMap;

public class FlowMap {
	private HashMap<Integer, float[]> map;
	private int colors;
	
	public FlowMap(int colors) {
		map = new HashMap<Integer, float[]>();
		this.colors = colors;
	}
	
	public void addSubFlow(Integer neighborColor, int flowColor, float flowVolume) {
		
		if (neighborColor != null) {
			float[] flow = map.get(neighborColor);
			if (flow == null) {
				flow = new float[colors];
				for (int c = 0; c < colors; ++c)
					flow[c] = 0;
			}
			
			flow[flowColor] += flowVolume;
			
			map.put(neighborColor, flow);
		}
		else
			for (int c = 0; c < colors; ++c) {
				float[] flow = map.get(c);
				if (flow == null) {
					flow = new float[colors];
					for (int i = 0; i < colors; ++i)
						flow[i] = 0;
				}
				
				flow[flowColor] += flowVolume;
				
				map.put(c, flow);
			}
	}

	public float[] getFlow(int neighborColor) {
		return map.get(neighborColor);
	}
	
	public String toString() {
		String str = "";
		float[] flow;
		for (int c = 0; c < colors; ++c) {
			flow = getFlow(c);
			str += "[";
			for (int i = 0; i < colors; ++i)
				str += (int)flow[i] + ",";
			str += "] ";
		}
		return str;
	}
	
}
