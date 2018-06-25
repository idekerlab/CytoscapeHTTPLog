import java.util.HashMap;
import java.util.Set;

public class StringBin {

	private HashMap<String, CountingBin> stringBin = new HashMap<String, CountingBin>();

	public void add(String s, String d) {
		CountingBin bins = stringBin.get(s);
		if (bins == null) {
			bins = new CountingBin();
		}
		bins.add(d);
		stringBin.put(s, bins);
	}

	void remove(String s) {
		stringBin.remove(s);
	}

	boolean has(String s) {
		return stringBin.containsKey(s);
	}

	public Integer get(String s, String d) {
		CountingBin bins = stringBin.get(s);
		return (bins == null ? 0 : bins.get(d));
	}

	public String list(String prefix) {
		String listVal = "";

		for (String s : stringBin.keySet()) {
			CountingBin bin = stringBin.get(s);
			if (listVal.length() != 0) {
				listVal += "\n";
			}
			listVal += bin.list((prefix == null ? "" : (prefix + "\t")) + s);
		}

		return listVal;
	}

	public Set<String> keys() {
		return stringBin.keySet();
	}
}
