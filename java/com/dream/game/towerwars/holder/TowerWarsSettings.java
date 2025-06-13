package com.dream.game.towerwars.holder;

import java.util.List;

public class TowerWarsSettings
{
	private final boolean _enabled;
	private final int _duration;
	private final int _preparation;
	private final List<Integer> _days;
	private final List<String> _times;
	
	public TowerWarsSettings(boolean enabled, int duration, int preparation, List<Integer> days, List<String> times)
	{
		_enabled = enabled;
		_duration = duration;
		_preparation = preparation;
		_days = days;
		_times = times;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public int getDuration()
	{
		return _duration;
	}
	
	public int getPreparation()
	{
		return _preparation;
	}
	
	public List<Integer> getDays()
	{
		return _days;
	}
	
	public List<String> getTimes()
	{
		return _times;
	}
}