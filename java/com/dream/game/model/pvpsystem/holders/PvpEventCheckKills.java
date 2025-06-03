package com.dream.game.model.pvpsystem.holders;

import com.dream.data.xml.StatSet;

public class PvpEventCheckKills
{
	private boolean _checkHwid;
	
	private final boolean _checkIp;
	private final int _minKillInterval;

	public PvpEventCheckKills(StatSet set)
	{
	    _checkHwid = set.getBool("checkHwid", false);
	    _checkIp = set.getBool("checkIp", false);
	    _minKillInterval = set.getInteger("minKillInterval", 0);
	}

	
	public boolean checkHwid()
	{
		return _checkHwid;
	}


	public boolean checkIp()
	{
		return _checkIp;
	}


	public int getminKillInterval()
	{
		return _minKillInterval;
	}
}
