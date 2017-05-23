package org.subzero.core.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This HTML Log Formatter is a simple replacement for the standard Log4J
 * HTMLLayout formatter and replaces the default timestamp (milliseconds,
 * relative to the start of the log) with a more readable timestamp (an example
 * of the default format is 2008-11-21-18:35:21.472-0800).
 * */

public class SubZeroHTMLLayout extends org.apache.log4j.HTMLLayout {

	// output buffer appended to when format() is invoked	  
	private StringBuffer sbuf = new StringBuffer(BUF_SIZE);
	
	static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
	  
	/**
	 * The timestamp format. The format can be overriden by including the
	 * following property in the Log4J configuration file:
	 * 
	 * log4j.appender.<category>.layout.TimestampFormat
	 * 
	 * using the same format string as would be specified with SimpleDateFormat.
	 * 
	 */

	private String timestampFormat = "yyyy-MM-dd HH:mm:ss"; // Default
																// format. yyyy-MM-dd-HH:mm:ss.SZ
																// Example:
																// 2008-11-21-18:35:21.472-0800

	private SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);

	public SubZeroHTMLLayout() {
		super();
	}

	/**
	 * Setter for timestamp format. Called if
	 * log4j.appender.<category>.layout.TimestampFormat property is specfied
	 */

	public void setTimestampFormat(String format) {
		this.timestampFormat = format;
		this.sdf = new SimpleDateFormat(format); // Use the format specified by
													// the TimestampFormat
													// property
	}

	/** Getter for timestamp format being used. */

	public String getTimestampFormat() {
		return this.timestampFormat;
	}
	
	/**
	 * Format
	 */
	@Override
	public String format(LoggingEvent event) {

		if (sbuf.capacity() > MAX_CAPACITY) {
			sbuf = new StringBuffer(BUF_SIZE);
		} else {
			sbuf.setLength(0);
		}

		sbuf.append(Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP);

		sbuf.append("<td>");
		sbuf.append(sdf.format(new Date(event.timeStamp)));		
		sbuf.append("</td>" + Layout.LINE_SEP);

		String escapedThread = Transform.escapeTags(event.getThreadName());
		sbuf.append("<td title=\"" + escapedThread + " thread\">");
		sbuf.append(escapedThread);
		sbuf.append("</td>" + Layout.LINE_SEP);

		sbuf.append("<td title=\"Level\">");
		if (event.getLevel().equals(Level.DEBUG)) {
			sbuf.append("<span class=\"debug\">");
			sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
			sbuf.append("</span>");
		}
		else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
			sbuf.append("<span class=\"warnError\"><strong>");
			sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
			sbuf.append("</strong></span>");
		} 
		else {
			sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
		}
		sbuf.append("</td>" + Layout.LINE_SEP);

		String escapedLogger = Transform.escapeTags(event.getLoggerName());
		sbuf.append("<td title=\"" + escapedLogger + " category\">");
		sbuf.append(escapedLogger);
		sbuf.append("</td>" + Layout.LINE_SEP);

		if (getLocationInfo()) {
			LocationInfo locInfo = event.getLocationInformation();
			sbuf.append("<td>");
			sbuf.append(Transform.escapeTags(locInfo.getFileName()));
			sbuf.append(':');
			sbuf.append(locInfo.getLineNumber());
			sbuf.append("</td>" + Layout.LINE_SEP);
		}

		sbuf.append("<td title=\"Message\">");
		sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
		sbuf.append("</td>" + Layout.LINE_SEP);
		sbuf.append("</tr>" + Layout.LINE_SEP);

		if (event.getNDC() != null) {
			sbuf.append("<tr><td class=\"ndc\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
			sbuf.append("NDC: " + Transform.escapeTags(event.getNDC()));
			sbuf.append("</td></tr>" + Layout.LINE_SEP);
		}

		String[] s = event.getThrowableStrRep();
		if (s != null) {
			sbuf.append("<tr><td class=\"throw\" colspan=\"6\">");
			appendThrowableAsHtml(s, sbuf);
			sbuf.append("</td></tr>" + Layout.LINE_SEP);
		}

		return sbuf.toString();
	}
	
	/**
	 * appendThrowableAsHtml
	 * @param s
	 * @param sbuf
	 */
	private void appendThrowableAsHtml(String[] s, StringBuffer sbuf) {
		if (s != null) {
			int len = s.length;
			if (len == 0)
				return;
			sbuf.append(Transform.escapeTags(s[0]));
			sbuf.append(Layout.LINE_SEP);
			for (int i = 1; i < len; i++) {
				sbuf.append(TRACE_PREFIX);
				sbuf.append(Transform.escapeTags(s[i]));
				sbuf.append(Layout.LINE_SEP);
			}
		}
	}
	
	/**
	 * Returns appropriate HTML headers.
	 */
	@Override
	public String getHeader() {
		StringBuffer sbuf = new StringBuffer();
		//sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" + Layout.LINE_SEP);

		// Close the preceding entries with file rolling appender (footer never called implicitly)
		// => not very clean... but it works !
		sbuf.append("</table></body></html>" + Layout.LINE_SEP);
		
		sbuf.append("<html>" + Layout.LINE_SEP);
		sbuf.append("<head>" + Layout.LINE_SEP);
		sbuf.append("<title>" + getTitle() + "</title>" + Layout.LINE_SEP);
		sbuf.append("<style type=\"text/css\">" + Layout.LINE_SEP);
		sbuf.append("<!--" + Layout.LINE_SEP);
		
		// Changing style ...
		sbuf.append("body, table {font-family: arial,sans-serif; font-size: 10pt;}" + Layout.LINE_SEP);
		sbuf.append("th {background: #000000; color: #FFFFFF; text-align: left;}" + Layout.LINE_SEP);
		sbuf.append("table, td { border-collapse:collapse; border: 1px solid #000000; }" + Layout.LINE_SEP);
		sbuf.append("tr:nth-child(odd)  { background-color:#EEEEEE; }" + Layout.LINE_SEP);
		sbuf.append("tr:nth-child(even) { background-color:#FFFFFF; }" + Layout.LINE_SEP);		
		sbuf.append("span.debug { color:#0B610B; }");
		sbuf.append("span.warnError { color:#FF0000; }");
		sbuf.append("td.ndc { color:#CCCCCC; }");
		sbuf.append("td.throw { color:#FFFFFF; background-color:#DF0101; }");
		
		sbuf.append("-->" + Layout.LINE_SEP);
		sbuf.append("</style>" + Layout.LINE_SEP);
		sbuf.append("</head>" + Layout.LINE_SEP);
		sbuf.append("<body>" + Layout.LINE_SEP);
		sbuf.append("<h2>Log session start time " + new java.util.Date() + "</h2>" + Layout.LINE_SEP);
		sbuf.append("<table cellspacing=\"3\" cellpadding=\"3\" width=\"100%\" class=\"report\">" + Layout.LINE_SEP);
		sbuf.append("<tr>" + Layout.LINE_SEP);
		sbuf.append("<th>Date Time</th>" + Layout.LINE_SEP);
		sbuf.append("<th>Thread</th>" + Layout.LINE_SEP);
		sbuf.append("<th>Level</th>" + Layout.LINE_SEP);
		sbuf.append("<th>Category</th>" + Layout.LINE_SEP);
		if (getLocationInfo()) {
			sbuf.append("<th>File:Line</th>" + Layout.LINE_SEP);
		}
		sbuf.append("<th>Message</th>" + Layout.LINE_SEP);
		sbuf.append("</tr>" + Layout.LINE_SEP);
		return sbuf.toString();
	}

	/**
	 * Returns the appropriate HTML footers.
	 */
	@Override
	public String getFooter() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("</table></body></html>" + Layout.LINE_SEP);
		return sbuf.toString();
	}

}