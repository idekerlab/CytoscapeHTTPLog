public class LineContent {
	public String date;
	public String product;
	public String file;

	public LineContent(LogEntry logEntry) {
		date = logEntry.date;
		String[] pathComponents = logEntry.URL.split("/");
		product = pathComponents[1];
		file = pathComponents[2];
		int paramIndex = file.indexOf("?");
		if (paramIndex >= 0) {
			file = file.substring(0, paramIndex);
		}
	}
}
