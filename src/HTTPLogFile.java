import java.io.BufferedReader;
import java.io.FileReader;

public class HTTPLogFile {

	private String HTTPLog;
	protected long lineCount = 0;

	public HTTPLogFile(String HTTPLog) {
		this.HTTPLog = HTTPLog;
	}

	protected void startFile() throws Exception {
	}

	protected void startLine(String line) {
	}

	protected boolean isCountable(LogEntry logEntry) {
		return true;
	}

	protected void processLine(String line, LogEntry logEntry) {
		System.out.println(line);
	}

	protected void sawDupLine() {
	}

	protected void disqualifiedLine() {
	}

	protected void endLine() {
		lineCount++;
	}

	protected void endFile() throws Exception {
	}

	public void ReadLog(long sampleSize, boolean tossDuplicates)
			throws Exception {
		startFile();
		BufferedReader file = new BufferedReader(new FileReader(HTTPLog));
		String line;

		LogEntry prevLogEntry = null;
		while ((line = file.readLine()) != null
				&& (lineCount < sampleSize || sampleSize < 0)) {
			startLine(line);
			try {
				LogEntry logEntry = new LogEntry(line);
				if (isCountable(logEntry)) {
					if (!tossDuplicates || prevLogEntry == null
							|| !prevLogEntry.IP.equals(logEntry.IP)
							|| !prevLogEntry.URL.equals(logEntry.URL)) {
						processLine(line, logEntry);
						prevLogEntry = logEntry;
					} else {
						sawDupLine();
					}
				} else {
					disqualifiedLine();
				}
			} catch (Throwable e) {
				System.out.println("Could not process log entry " + lineCount
						+ ": " + e);
			}
			endLine();
		}
		endFile();

		file.close();
	}
}
