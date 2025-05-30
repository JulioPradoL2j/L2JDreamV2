package com.dream.game.manager;

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsDropManager
{
	public class rewardRule
	{
		public int _rewardCnt = 0;
		public String _eventName;
		public ruleType _ruleType = null;
		public boolean _levDifferenceControl;
		public List<Integer> _mobId = new ArrayList<>();
		public L2Zone _zone = null;
		public List<Integer> _itemId = new ArrayList<>();
		public List<Integer> _itemCnt = new ArrayList<>();
		public List<Integer> _itemChance = new ArrayList<>();
	}

	private class rewards
	{
		public int _rewardId;
		public int _rewardCnt;

		public rewards(int Id, int Cnt)
		{
			_rewardId = Id;
			_rewardCnt = Cnt;
		}
	}

	public static enum ruleType
	{
		ALL_NPC,
		BY_NPCID,
		BY_ZONE
	}

	private static EventsDropManager _instance;

	public static final EventsDropManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new EventsDropManager();
		}
		return _instance;
	}

	private boolean _haveActiveEvent = true;

	private final Map<Integer, rewardRule> _rewardRules = new HashMap<>();

	public void addRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addRule(event, type, itemId, itemCnt, itemChance, true);
	}

	public void addRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardRules.put(_rewardRules.size() + 1, rule);
		_haveActiveEvent = true;
	}

	public void addRule(String event, ruleType type, int npcId[], int itemId[], int itemCnt[], int itemChance[])
	{
		addRule(event, type, npcId, itemId, itemCnt, itemChance, true);
	}

	public void addRule(String event, ruleType type, int npcId[], int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int element : npcId)
		{
			rule._mobId.add(element);
		}
		for (int element : itemId)
		{
			rule._itemId.add(element);
		}
		for (int element : itemCnt)
		{
			rule._itemCnt.add(element);
		}
		for (int element : itemChance)
		{
			rule._itemChance.add(element);
		}
		_rewardRules.put(_rewardRules.size() + 1, rule);
		_haveActiveEvent = true;
	}

	public void addRule(String event, ruleType type, L2Zone zone, int itemId[], int itemCnt[], int itemChance[])
	{
		addRule(event, type, zone, itemId, itemCnt, itemChance, true);
	}

	public void addRule(String event, ruleType type, L2Zone zone, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._zone = zone;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int element : itemId)
		{
			rule._itemId.add(element);
		}
		for (int element : itemCnt)
		{
			rule._itemCnt.add(element);
		}
		for (int element : itemChance)
		{
			rule._itemChance.add(element);
		}
		_rewardRules.put(_rewardRules.size() + 1, rule);
		_haveActiveEvent = true;
	}

	public int[] calculateRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		List<rewards> _rewards = new ArrayList<>();
		if (_rewardRules.size() > 0)
		{
			for (rewardRule tmp : _rewardRules.values())
			{
				if (tmp._levDifferenceControl && (lvlDif > 7 || lvlDif < -7))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= tmp._rewardCnt - 1)
					{
						try
						{
							if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
							{
								_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
							}
						}
						catch (Exception e)
						{
							return new int[]
							{
								0,
								0
							};
						}
						cnt++;
					}
				}
				if (tmp._ruleType == ruleType.BY_NPCID)
					if (tmp._mobId.contains(npcTemplate.getIdTemplate()))
					{
						int cnt = 0;
						while (cnt <= tmp._rewardCnt - 1)
						{
							if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
							{
								_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
							}
							cnt++;
						}
					}
				if (tmp._ruleType == ruleType.BY_ZONE)
					if (tmp._zone.isCharacterInZone(lastAttacker))
					{
						int cnt = 0;
						while (cnt <= tmp._rewardCnt - 1)
						{
							if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
							{
								_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
							}
							cnt++;
						}
					}
			}
		}
		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}

	public boolean haveActiveEvent()
	{
		return _haveActiveEvent;
	}

	@SuppressWarnings("unlikely-arg-type")
	public void removeEventRules(String event)
	{
		for (rewardRule tmp : _rewardRules.values())
			if (tmp._eventName == event)
			{
				_rewardRules.remove(tmp);
			}
		if (_rewardRules.size() == 0)
		{
			_haveActiveEvent = true;
		}
	}
}