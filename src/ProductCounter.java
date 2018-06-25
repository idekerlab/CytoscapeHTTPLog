import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class ProductCounter {

	private StringBin productBins = new StringBin();
	private Set<String> dates = new HashSet<String>();
	private Set<String> products = new HashSet<String>();

	public void add(LineContent lineContent) {
		productBins.add(lineContent.product, lineContent.date);
		dates.add(lineContent.date);
		products.add(lineContent.product);
	}

	public String list() {
		return productBins.list(null);
	}

	private SortedSet<String> dateSet() {
		return SortUtils.sortDates(dates);
	}

	private SortedSet<String> productSet() {
		return SortUtils.sortStrings(products);
	}

	public String[][] report() {
		SortedSet<String> sortedDates = dateSet();
		SortedSet<String> sortedProducts = productSet();

		String[][] reportData = new String[1 + sortedDates.size()][1 + sortedProducts
				.size()];

		int row = 0;
		int column = 0;
		reportData[row][column++] = "Date";
		for (String product : sortedProducts) {
			reportData[row][column++] = product;
		}
		row++;

		for (String date : sortedDates) {
			column = 0;
			reportData[row][column++] = date;
			for (String product : sortedProducts) {
				reportData[row][column++] = String.valueOf(productBins.get(
						product, date));
			}
			row++;
		}

		return reportData;
	}
}
