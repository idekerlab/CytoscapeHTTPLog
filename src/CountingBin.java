import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;

public class CountingBin {

	private HashMap<String, Integer> bins = new HashMap<String, Integer>();

	public void add(String s) {
		Integer count = bins.get(s);
		if (count == null) {
			count = new Integer(0);
		}
		count++;
		bins.put(s, count);
	}
	
	public void remove(String s) {
		bins.remove(s);
	}
	
	public boolean has(String s) {
		return bins.containsKey(s);
	}

	public Integer get(String s) {
		Integer count = bins.get(s);
		return (count == null ? 0 : count);
	}

	public String list(String preface) {
		String listVal = "";

		for (String s : bins.keySet()) {
			Integer count = bins.get(s);
			if (listVal.length() != 0) {
				listVal += "\n";
			}
			listVal += preface + "\t" + s + "\t" + count;
		}

		return listVal;
	}

	public int report(SortedSet<String> ss, String[] reportRow, int column) {
		for (String s : ss) {
			reportRow[column++] = String.valueOf(get(s));
		}

		return column;
	}
	
	public Set<String> keys() {
		return bins.keySet();
	}
}
