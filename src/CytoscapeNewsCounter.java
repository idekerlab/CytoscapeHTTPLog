import java.io.File;
import java.io.PrintWriter;

public class CytoscapeNewsCounter {

	private static final long REPORT_INTERVAL = 10000;
	private static final long SAMPLE_SIZE = -1; // 30000; // -1;
	private static final String HTTP_OK = "200";

	private static final String PROCESSED_HTTP_FILENAME = "processed_news.log";
	private static final String PRODUCT_TSV_FILENAME = "news.tsv";

	private static ProductCounter productCounter = new ProductCounter();

	private static boolean isCytoscapeDownload(LogEntry logEntry) {
		boolean isCytoscapeDownload = logEntry.URL
				.matches("/cytoscape-news/news.html");
		return isCytoscapeDownload && logEntry.status.equals(HTTP_OK)
				&& logEntry.command.equals("GET");
	}

	private static class CollectStatistics extends HTTPLogFile {

		private PrintWriter pw;
		private String HTTPProcessed;
		private String productTSV;

		public CollectStatistics(String HTTPLog, String HTTPProcessed,
				String productTSV) {
			super(HTTPLog);
			this.HTTPProcessed = HTTPProcessed;
			this.productTSV = productTSV;
		}

		private void printReport(String outFile, String[][] report)
				throws Exception {
			PrintWriter pw = new PrintWriter(outFile, "UTF-8");
			for (int row = 0; row < report.length; row++) {
				for (int col = 0; col < report[row].length; col++) {
					if (col != 0) {
						pw.print("\t");
					}
					pw.print(report[row][col]);
				}
				pw.println();
			}
			pw.close();
		}

		protected void startFile() throws Exception {
			pw = new PrintWriter(HTTPProcessed);
		}

		protected boolean isCountable(LogEntry logEntry) {
			return CytoscapeNewsCounter.isCytoscapeDownload(logEntry);
		}

		protected void processLine(String line, LogEntry logEntry) {
			pw.println(line);
			LineContent lineContent = new LineContent(logEntry);
			productCounter.add(lineContent);
		}

		protected void endLine() {
			super.endLine();
			if (lineCount % REPORT_INTERVAL == 0) {
				System.out.println("Processing " + lineCount);
			}
		}

		protected void endFile() throws Exception {
			pw.close();

			printReport(productTSV, productCounter.report());

			System.out.println("Lines read: " + lineCount);
		}
	}

	private static String normalizePath(String path) {
		if (path == null) {
			return "";
		} else if (path.endsWith(":") || path.endsWith(File.separator)) {
			return path;
		} else {
			return path + File.separator;
		}
	}

	public static void main(String[] args) {

		/*
		 * This program will read an HTTP log file that contains Cytoscape
		 * downloads and produce the following files:
		 * 
		 * processed_news.log -- a list of the HTTP log entries included in the
		 * Cytoscape statistics
		 * 
		 * news.tsv -- a breakdown of Cytoscape startups per day
		 * 
		 * We assume that bots are not skewing the statistics, and so don't
		 * account for them.
		 * 
		 * Successive runs of this program should be preceded by copying bots.log to 
		 * prevbots.log.
		 * 
		 * There should be two parameters to this program:
		 * - Name of the file containing all HTTP log entries since the beginning of time
		 * - Name of the path to write the output files to
		 * 
		 * Before you run this program, move the bots.log file to prevbots.log.

		 */

		try {
			if (args.length > 0) {
				String HTTPLog = args[0];

				String resultPath;
				if (args.length > 1) {
					resultPath = normalizePath(args[1]);
				} else {
					resultPath = normalizePath((new File(HTTPLog)).getParent());
				}
				String HTTPProcessed = resultPath + PROCESSED_HTTP_FILENAME;
				String productTSV = resultPath + PRODUCT_TSV_FILENAME;

				(new CollectStatistics(HTTPLog, HTTPProcessed, productTSV))
						.ReadLog(SAMPLE_SIZE, false);
			} else {
				System.out
						.println("Supply the name of the HTTP log to parse and (optionally) the name of the directory to write output files");
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}
