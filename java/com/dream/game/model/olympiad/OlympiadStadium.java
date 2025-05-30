package com.dream.game.model.olympiad;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.actor.instance.L2PcInstance;

public class OlympiadStadium
{
	private boolean _freeToUse = true;
	private final int[] _coords = new int[3];
	private final List<L2PcInstance> _spectators;

	public OlympiadStadium(int x, int y, int z)
	{
		_coords[0] = x;
		_coords[1] = y;
		_coords[2] = z;
		_spectators = new ArrayList<>();
	}

	protected void addSpectator(int id, L2PcInstance spec, boolean storeCoords)
	{
		spec.enterOlympiadObserverMode(getCoordinates()[0], getCoordinates()[1], getCoordinates()[2], id, storeCoords);
		_spectators.add(spec);
	}

	public int[] getCoordinates()
	{
		return _coords;
	}

	protected List<L2PcInstance> getSpectators()
	{
		return _spectators;
	}

	public boolean isFreeToUse()
	{
		return _freeToUse;
	}

	protected void removeSpectator(L2PcInstance spec)
	{
		if (_spectators != null && _spectators.contains(spec))
		{
			_spectators.remove(spec);
		}
	}

	public void setStadiaBusy()
	{
		_freeToUse = false;
	}

	public void setStadiaFree()
	{
		_freeToUse = true;
	}
}