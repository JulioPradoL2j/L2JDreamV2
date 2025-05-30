package com.dream.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class L2SiegeStatus
{
	public class PlayerInfo
	{
		public int _clanID;
		public int _playerId;
		public int _kill;
		public int _death;
	}

	private static final L2SiegeStatus _instance = new L2SiegeStatus();

	public static final L2SiegeStatus getInstance()
	{
		return _instance;
	}

	private final Map<Integer, PlayerInfo> _clansInfoList = new HashMap<>();

	private L2SiegeStatus()
	{

	}

	public void addStatus(int clanId, int playerID)
	{
		synchronized (_clansInfoList)
		{
			PlayerInfo player = _clansInfoList.get(playerID);
			if (player == null)
			{
				player = new PlayerInfo();
				player._playerId = playerID;
				player._clanID = clanId;
				player._kill = 0;
				player._death = 0;
				_clansInfoList.put(playerID, player);
			}
		}
	}

	public void addStatus(int clanId, int playerID, boolean killer)
	{
		synchronized (_clansInfoList)
		{
			PlayerInfo player = _clansInfoList.get(playerID);
			if (player != null)
			{
				if (killer == true)
				{
					player._kill += 1;
				}
				else
				{
					player._death += 1;
				}
			}
			else
			{
				player = new PlayerInfo();
				player._playerId = playerID;
				player._clanID = clanId;
				if (killer == true)
				{
					player._kill += 1;
				}
				else
				{
					player._death += 1;
				}

				_clansInfoList.put(playerID, player);
			}
		}
	}

	public void clearClanStatus(int clanID)
	{
		for (PlayerInfo temp : getMembers(clanID))
		{
			synchronized (_clansInfoList)
			{
				_clansInfoList.remove(temp._playerId);
			}
		}
	}

	public List<PlayerInfo> getMembers(int clanID)
	{
		List<PlayerInfo> result = new ArrayList<>();
		synchronized (_clansInfoList)
		{
			for (PlayerInfo temp : _clansInfoList.values())
				if (temp != null)
					if (temp._clanID == clanID)
					{
						result.add(temp);
					}
		}
		return result;
	}

	public void shutdown()
	{

	}
}