package com.dream.game.model.holders;

import com.dream.data.xml.StatSet;
import com.dream.game.model.actor.instance.L2PcInstance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ResetPrize
{
	private final boolean _enabled;
	private final ResetType _type;
	private final LocalTime _time;
	private final List<PrizeHolder> _prizes = new ArrayList<>();
	
	public ResetPrize(StatSet set)
	{
		_enabled = set.getBool("enable");
		_type = ResetType.valueOf(set.getString("type").toUpperCase());
		_time = LocalTime.parse(set.getString("time"));
	}
	
	public void addPrize(PrizeHolder prize)
	{
		_prizes.add(prize);
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public ResetType getType()
	{
		return _type;
	}
	
	public LocalTime getTime()
	{
		return _time;
	}
	
	public List<PrizeHolder> getPrizes()
	{
		return _prizes;
	}
	
	public void giveReward(L2PcInstance player, int resets, int position)
	{
		for (PrizeHolder prize : _prizes)
		{
			if (prize.getPosition() == position)
			{
				for (IntIntHolder reward : prize.getRewards())
				{
					player.addItem("ResetPrize", reward.getId(), reward.getValue(), player, true);
				}
				
				player.sendMessage("You received a reward for being in the position " + position + " in the reset ranking (" + _type + ").");
				return;
			}
		}
	}
	
}
