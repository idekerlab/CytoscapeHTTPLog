import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class ProductFileCounter {

	private HashMap<String, StringBin> productFileBins = new HashMap<String, StringBin>();
	private Set<String> dates = new HashSet<String>();
	private Set<String> products = new HashSet<String>();

	public void add(LineContent lineContent) {
		StringBin fileBins = productFileBins.get(lineContent.product);
		if (fileBins == null) {
			fileBins = new StringBin();
		}
		fileBins.add(lineContent.file, lineContent.date);
		productFileBins.put(lineContent.product, fileBins);

		dates.add(lineContent.date);
		products.add(lineContent.product);
	}

	public String list() {
		String listVal = "";

		for (String product : productFileBins.keySet()) {
			StringBin fileBin = productFileBins.get(product);
			if (listVal.length() != 0) {
				listVal += "\n";
			}
			listVal += fileBin.list(product);
		}

		return listVal;
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
		HashMap<String, SortedSet<String>> productToSortedFiles = new HashMap<String, SortedSet<String>>();

		int totalFileCount = 0;
		for (String product : sortedProducts) {
			SortedSet<String> sortedFiles = SortUtils
					.sortStrings(productFileBins.get(product).keys());
			totalFileCount += sortedFiles.size();
			productToSortedFiles.put(product, sortedFiles);
		}

		String[][] reportData = new String[1 + sortedDates.size()][1
				+  totalFileCount];

		int row = 0;
		int column = 0;
		reportData[row][column++] = "Date";
		for (String product : sortedProducts) {
			SortedSet<String> sortedFiles = productToSortedFiles.get(product);
			for (String file : sortedFiles) {
				reportData[row][column++] = product + ":" + file;
			}
		}
		row++;

		for (String date : sortedDates) {
			column = 0;
			reportData[row][column++] = date;

			for (String product : sortedProducts) {
				SortedSet<String> sortedFiles = productToSortedFiles
						.get(product);
				StringBin fileBin = productFileBins.get(product);
				for (String file : sortedFiles) {
					reportData[row][column++] = String.valueOf(fileBin.get(
							file, date));
				}
			}
			row++;
		}

		return reportData;
	}
}
