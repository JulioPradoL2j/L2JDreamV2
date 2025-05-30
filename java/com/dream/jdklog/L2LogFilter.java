package com.dream.jdklog;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public abstract class L2LogFilter implements Filter
{
	protected abstract String getLoggerName();

	@Override
	public boolean isLoggable(LogRecord record)
	{
		return getLoggerName().equals(record.getLoggerName());
	}
}
