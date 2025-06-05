package com.dream.game.model.holders;

import com.dream.data.xml.StatSet;

import java.util.ArrayList;
import java.util.List;

public class ResetHolder
{
	private final int _levelMax;
	private final int _requiredPvps;
	private final double _expPenalty;
	private List<IntIntHolder> _requiredItems = new ArrayList<>();
	private List<IntIntHolder> _rewardItems = new ArrayList<>();
	private List<IntIntHolder> _rewardSkills = new ArrayList<>();
	private List<ResetPrize> _prizes = new ArrayList<>();
	private int _rankingDisplayLimit = 3;
	private int _dailyPoints = 1;
	private int _monthlyPoints = 1;
	private boolean _removeResetSkills = false;
	private boolean _deBug = false;

	public ResetHolder(StatSet set)
	{
		
		_levelMax = set.getInteger("levelMax");
		_requiredPvps = set.getInteger("requiredPvps");
		_expPenalty = set.getDouble("expPenalty");
		_requiredItems = new ArrayList<>();
		_rewardItems = new ArrayList<>();
		_rewardSkills = new ArrayList<>();
	}
	
	public int getLevelMax()
	{
		return _levelMax;
	}
	
	public int getRequiredPvps()
	{
		return _requiredPvps;
	}
	
	public double getExpPenalty()
	{
		return _expPenalty;
	}
	
	public List<IntIntHolder> getRequiredItems()
	{
		return _requiredItems;
	}
	
	public List<IntIntHolder> getRewardItems()
	{
		return _rewardItems;
	}
	
	public List<IntIntHolder> getRewardSkills()
	{
		return _rewardSkills;
	}
	
	public List<ResetPrize> getPrizes()
	{
		return _prizes;
	}
	
	public void addPrize(ResetPrize prize)
	{
		_prizes.add(prize);
	}
	
	public void setRankingDisplayLimit(int pos)
	{
		_rankingDisplayLimit = pos;
	}
	
	public int getRankingDisplayLimit()
	{
		return _rankingDisplayLimit;
	}
	
	public int getDailyPoints()
	{
		return _dailyPoints;
	}
	
	public void setDailyPoints(int points)
	{
		_dailyPoints = points;
	}
	
	public int getMonthlyPoints()
	{
		return _monthlyPoints;
	}
	
	public void setMonthlyPoints(int points)
	{
		_monthlyPoints = points;
	}
	
	public boolean isRemoveResetSkills()
	{
		return _removeResetSkills;
	}
	
	public void setRemoveResetSkills(boolean value)
	{
		_removeResetSkills = value;
	}
	
	public boolean isDebug()
	{
		return _deBug;
	}
	
	public void setDebug(boolean value)
	{
		_deBug = value;
	}
	
}