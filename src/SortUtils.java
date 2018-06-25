import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortUtils {

	private final static DateFormat dateFormat = new SimpleDateFormat(
			"dd/MMM/yyy");

	public static Comparator<String> DateComparator = new Comparator<String>() {
		public int compare(String date1, String date2) {
			try {
				Date d1 = dateFormat.parse(date1);
				Date d2 = dateFormat.parse(date2);
				return d1.compareTo(d2);
			} catch (Throwable e) {
				System.out.println("Comparison exception (" + date1 + ") ("
						+ date2 + "): " + e);
				return 0;
			}
		}
	};

	public static SortedSet<String> sortDates(Set<String> dates) {
		SortedSet<String> s = new TreeSet<String>(DateComparator);
		s.addAll(dates);
		return s;
	}

	public static SortedSet<String> sortStrings(Set<String> strings) {
		SortedSet<String> s = new TreeSet<String>();
		s.addAll(strings);
		return s;
	}

}
