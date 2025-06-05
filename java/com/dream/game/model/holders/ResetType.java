package com.dream.game.model.holders;

public enum ResetType
{
	DAILY(24 * 60 * 60 * 1000L),
	MONTH(30L * 24 * 60 * 60 * 1000L);
	
	private final long _intervalMillis;
	
	ResetType(long intervalMillis)
	{
		_intervalMillis = intervalMillis;
	}
	
	public long getIntervalMillis()
	{
		return _intervalMillis;
	}
}