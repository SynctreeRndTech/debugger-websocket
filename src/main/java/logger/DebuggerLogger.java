package logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DebuggerLogger {
	private Logger logger;

	public DebuggerLogger(String name) {
		this.logger = Logger.getLogger(name);
	}

	public void info(String msg) {
		this.logger.info(msg);
	}

	public void debug(String msg) {
		this.logger.log(Level.FINE, msg);
	}

	public void error(String msg) {
		this.logger.log(Level.SEVERE, msg);
	}

	public void warn(String msg) {
		this.logger.warning(msg);
	}
}
