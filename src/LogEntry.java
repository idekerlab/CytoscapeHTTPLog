public class LogEntry {
	public String IP;
	public String date;
	public String time;
	public String command;
	public String URL;
	public String status;
	public String referrer;
	public String agent;

	public LogEntry(String line) throws Exception {
		String[] logEntry = line.split(" ");
		if (logEntry.length < 12) {
			throw new Exception("Invalid log entry: " + line);
		}
		IP = logEntry[0];
		String userID = logEntry[2];
		String timeDate = logEntry[3];
		date = timeDate.substring(1, 12);
		time = timeDate.substring(13);
		String timezone = logEntry[4];
		String rawCommand = logEntry[5];
		command = rawCommand.substring(1);
		URL = logEntry[6];
		String HTTPVersion = logEntry[7];
		status = logEntry[8];
		String downloadLength = logEntry[9];		
		referrer = logEntry[10];
		agent = logEntry[11];
	}

}
