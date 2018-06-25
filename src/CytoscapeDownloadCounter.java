import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class CytoscapeDownloadCounter {

	private static final long REPORT_INTERVAL = 10000;
	private static final long SAMPLE_SIZE = -1; // 1000000; // -1;
	private static final String HTTP_OK = "200";
	private static final String NO_REFERRER = "\"-\"";

	private static final String PROCESSED_HTTP_FILENAME = "processed.log";
	private static final String PRODUCT_TSV_FILENAME = "product.tsv";
	private static final String PRODUCT_FILE_TSV_FILENAME = "productfile.tsv";
	private static final String BOT_LOG_FILENAME = "bots.log";
	private static final String PREV_BOT_LOG_FILENAME = "prevbots.log";
	private static final String SOURCE_IP_LOG_FILENAME = "sourceIPs.log";

	private static ProductCounter productCounter = new ProductCounter();
	private static ProductFileCounter productFileCounter = new ProductFileCounter();

	private static boolean isCytoscapeDownload(LogEntry logEntry) {
		boolean isCytoscapeDownload = logEntry.URL
				.matches("/cytoscape-[0-9][^/]*/Cytoscape.*");
		if (!isCytoscapeDownload) {
			isCytoscapeDownload = logEntry.URL
					.matches("/Cyto-[0-9][^/]*/Cytoscape.*");
		}
		return isCytoscapeDownload && logEntry.status.equals(HTTP_OK)
				&& logEntry.command.equals("GET");
	}

	private static class FindBots extends HTTPLogFile {

		private static final int MINIMUM_BOT_ACTIVITY = 10;

		private CountingBin ipTally = new CountingBin();
		private Set<String> dateSet = new HashSet<String>();
		private String botLog;
		private String prevBotLog;
		private int botAccessCount = 0;

		public FindBots(String HTTPLog, String botLog, String prevBotLog) {
			super(HTTPLog);
			this.botLog = botLog;
			this.prevBotLog = prevBotLog;
		}

		public Set<String> getValidIPs() {
			return ipTally.keys();
		}

		public int getBotAccessCount() {
			return botAccessCount;
		}

		protected boolean isCountable(LogEntry logEntry) {
			return CytoscapeDownloadCounter.isCytoscapeDownload(logEntry);
		}

		protected void processLine(String line, LogEntry logEntry) {
			ipTally.add(logEntry.IP);
			dateSet.add(logEntry.date);
		}

		protected void endLine() {
			super.endLine();
			if (lineCount % REPORT_INTERVAL == 0) {
				System.out.println("Checking for bot on line " + lineCount);
			}
		}

		protected void endFile() throws Exception {
			int bounceThreshold = Math
					.max(dateSet.size(), MINIMUM_BOT_ACTIVITY);
			Set<String> tossList = new HashSet<String>();
			Set<String> ipList = ipTally.keys();
			PrintWriter botLogFile = new PrintWriter(botLog);

			BufferedReader prevBotLogFile = new BufferedReader(new FileReader(
					prevBotLog));
			String line;
			while ((line = prevBotLogFile.readLine()) != null) {
				int endIP = line.indexOf("\t");
				if (endIP > 0) {
					String ip = line.substring(0, endIP);
					if (ip.length() >= 7 /* minimum IP address */) {
						tossList.add(ip);
					}
					System.out.println("Tossing previous bot " + ip);
					botLogFile.println(ip + "\t" + -1 + "\t" + bounceThreshold);
				}
			}
			prevBotLogFile.close();

			for (String ip : ipList) {
				int count = ipTally.get(ip);
				if (count > bounceThreshold) {
					tossList.add(ip);
					botAccessCount += count;
					System.out.println("Tossing bot " + ip + " for " + count
							+ " > " + bounceThreshold);
					botLogFile.println(ip + "\t" + count + "\t"
							+ bounceThreshold);
				}
			}

			for (String ip : tossList) {
				ipTally.remove(ip);
			}

			botLogFile.close();
		}
	}

	private static class CollectStatistics extends HTTPLogFile {

		private long dupLineCount;
		private long disqualifiedCount;

		private PrintWriter pwHTTPs;
		private PrintWriter pwIPs;
		private String HTTPProcessed;
		private String productTSV;
		private String productFileTSV;
		private Set<String> validIPs;
		private String sourceIPsLog;
		private int botAccessCount;

		public CollectStatistics(String HTTPLog, String HTTPProcessed,
				String productTSV, String productFileTSV, Set<String> validIPs, String sourceIPsLog,
				int botAccessCount) {
			super(HTTPLog);
			this.HTTPProcessed = HTTPProcessed;
			this.productTSV = productTSV;
			this.productFileTSV = productFileTSV;
			this.validIPs = validIPs;
			this.sourceIPsLog = sourceIPsLog;
			this.botAccessCount = botAccessCount;
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
			dupLineCount = 0;
			disqualifiedCount = 0;
			pwHTTPs = new PrintWriter(HTTPProcessed);
			pwIPs = new PrintWriter(sourceIPsLog);
		}

		protected boolean isCountable(LogEntry logEntry) {
			boolean isCytoscapeDownload = CytoscapeDownloadCounter
					.isCytoscapeDownload(logEntry);
			if (isCytoscapeDownload) {
				isCytoscapeDownload = validIPs.contains(logEntry.IP)
						|| !logEntry.referrer.equals(NO_REFERRER);
			}
			return isCytoscapeDownload;
		}

		protected void processLine(String line, LogEntry logEntry) {
			pwHTTPs.println(line);
			LineContent lineContent = new LineContent(logEntry);
			productCounter.add(lineContent);
			productFileCounter.add(lineContent);
		}

		protected void sawDupLine() {
			dupLineCount++;
		}

		protected void disqualifiedLine() {
			disqualifiedCount++;
		}

		protected void endLine() {
			super.endLine();
			if (lineCount % REPORT_INTERVAL == 0) {
				System.out.println("Processing " + lineCount);
			}
		}

		protected void endFile() throws Exception {
			pwHTTPs.close();

			printReport(productTSV, productCounter.report());
			printReport(productFileTSV, productFileCounter.report());
			
			for (String ip : validIPs)
				pwIPs.println(ip);
			pwIPs.close();

			System.out.println("Lines read: " + lineCount + ", lines ignored: "
					+ disqualifiedCount + " (" + botAccessCount
					+ " were bots), duplicates ignored: " + dupLineCount
					+ ", lines accepted: "
					+ (lineCount - disqualifiedCount - dupLineCount));
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
		 * processed.log -- a list of the HTTP log entries included in the
		 * Cytoscape statistics
		 * 
		 * product.tsv -- a breakdown of downloads by Cytoscape product
		 * 
		 * productfile.tsv -- a breakdown of downloads by actual file
		 * 
		 * bots.log -- a list of bots that were inferred and not counted
		 * 
		 * sourceIPs.log -- a list of valid IPs downloaded to
		 * 
		 * To generate the bots.log file, the prevbots.log file is read to find
		 * out what bots were previously found. Successive runs of this program
		 * should be preceded by copying bots.log to prevbots.log.
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
				String productFileTSV = resultPath + PRODUCT_FILE_TSV_FILENAME;
				String botLog = resultPath + BOT_LOG_FILENAME;
				String prevBotLog = resultPath + PREV_BOT_LOG_FILENAME;
				String sourceIPsLog = resultPath + SOURCE_IP_LOG_FILENAME;

				FindBots findBots = new FindBots(HTTPLog, botLog, prevBotLog);
				findBots.ReadLog(SAMPLE_SIZE, true);
				Set<String> validIPs = findBots.getValidIPs();
				int botAccessCount = findBots.getBotAccessCount();

				(new CollectStatistics(HTTPLog, HTTPProcessed, productTSV,
						productFileTSV, validIPs, sourceIPsLog, botAccessCount)).ReadLog(
						SAMPLE_SIZE, true);
			} else {
				System.out
						.println("Supply the name of the HTTP log to parse and (optionally) the name of the directory to write output files");
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}
