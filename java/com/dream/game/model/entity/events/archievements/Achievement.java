package com.dream.game.model.entity.events.archievements;

import com.dream.game.model.actor.instance.L2PcInstance;

import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

public class Achievement
{
	private int _id;
	private String _name;
	private String _reward;
	private String _description = "No Description!";
	private boolean _repeatable;
	
	private FastMap<Integer, Long> _rewardList;
	private FastList<Condition> _conditions;
	
	private static Logger _log = Logger.getLogger(Achievement.class.getName());
	
	public Achievement(int id, String name, String description, String reward, boolean repeatable, FastList<Condition> conditions)
	{
		_rewardList = new FastMap<>();
		_id = id;
		_name = name;
		_description = description;
		_reward = reward;
		_conditions = conditions;
		_repeatable = repeatable;
		
		createRewardList();
	}
	
	private void createRewardList()
	{
		for (String s : _reward.split(";"))
		{
			if (s == null || s.isEmpty())
				continue;
			
			String[] split = s.split(",");
			Integer item = 0;
			Long count = null;
			try
			{
				item = Integer.valueOf(split[0]);
				count = Long.valueOf(split[1]);
			}
			catch (NumberFormatException nfe)
			{
				_log.warning("[AchievementsEngine] Error: Wrong reward " + nfe);
			}
			_rewardList.put(item, count);
		}
	}
	
	public boolean meetAchievementRequirements(L2PcInstance player)
	{
		for (Condition c : getConditions())
		{
			if (!c.meetConditionRequirements(player))
			{
				return false;
			}
		}
		return true;
	}
	
	public int getID()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public String getReward()
	{
		return _reward;
	}
	
	public boolean isRepeatable()
	{
		return _repeatable;
	}
	
	public FastMap<Integer, Long> getRewardList()
	{
		return _rewardList;
	}
	
	public FastList<Condition> getConditions()
	{
		return _conditions;
	}
}