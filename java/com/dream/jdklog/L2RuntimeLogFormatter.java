package com.dream.jdklog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.io.IOUtils;

import javolution.text.TextBuilder;

public abstract class L2RuntimeLogFormatter extends L2LogFormatter
{
	protected final static void appendThrown(LogRecord record, TextBuilder tb)
	{
		if (record.getThrown() != null)
		{
			StringWriter sw = null;
			PrintWriter pw = null;
			try
			{
				sw = new StringWriter();
				pw = new PrintWriter(sw);

				record.getThrown().printStackTrace(pw);

				appendNewline(tb);
				tb.append(sw);
			}
			finally
			{
				IOUtils.closeQuietly(pw);
				IOUtils.closeQuietly(sw);
			}
		}
	}

	@Override
	protected final void format0(LogRecord record, TextBuilder tb)
	{
		tb.append(record.getLevel()).append(" ");

		appendDateSys(record, tb);

		if (record.getLevel().intValue() > Level.INFO.intValue() || record.getThrown() != null)
		{
			tb.append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append("(): ");
		}

		appendMessage(record, tb);
		appendThrown(record, tb);
	}
}
