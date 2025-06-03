package com.dream.game.model.pvpsystem.holders;

public class PvPRankSettings
{
	private final boolean _enabled;
		
	public PvPRankSettings(boolean enabled)
	{
		_enabled = enabled;

	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
}