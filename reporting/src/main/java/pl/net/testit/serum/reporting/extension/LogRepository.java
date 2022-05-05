package pl.net.testit.serum.reporting.extension;

import java.util.ArrayList;
import java.util.List;

class LogRepository {

  private static LogRepository instance = null;
  private final List<LogEntry> stepLogs = new ArrayList<>();

  private LogRepository() {
  }

  public static synchronized LogRepository getInstance() {
    if (instance == null) {
      instance = new LogRepository();
    }
    return instance;
  }

  public void addLog(String header, String content) {
    if (content != null && !content.trim().isEmpty()) {
      stepLogs.add(new LogEntry(header, content));
    }
  }

  public List<LogEntry> getAndClearLogs() {
    List<LogEntry> logs = new ArrayList<>(stepLogs);
    stepLogs.clear();
    return logs;
  }

  public static class LogEntry {

    private final String header;
    private final String content;

    public LogEntry(String header, String content) {
      this.header = header;
      this.content = content;
    }

    public String getHeader() {
      return header;
    }

    public String getContent() {
      return content;
    }
  }
}
