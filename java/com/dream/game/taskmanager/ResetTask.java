package com.dream.game.taskmanager;

import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.xml.ResetData;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.ResetManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.IntIntHolder;
import com.dream.game.model.holders.PrizeHolder;
import com.dream.game.model.holders.ResetHolder;
import com.dream.game.model.holders.ResetPrize;
import com.dream.game.model.holders.ResetType;
import com.dream.game.model.world.L2World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ResetTask implements Runnable
{
	public static Logger LOGGER = Logger.getLogger(ResetTask.class);
	
	@Override
	public void run()
	{
		for (ResetHolder holder : ResetData.getInstance().getResets())
		{
			for (ResetPrize prize : holder.getPrizes())
			{
				if (prize.isEnabled())
				{
					distributeRewards(prize, prize.getType());
				}
			}
		}
	}
	
	public static void distributeRewards(ResetPrize prize, ResetType type)
	{
		Map<Integer, Integer> ranking = ResetManager.getInstance().getRanking(type);
		List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(ranking.entrySet());
		sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
		
		int position = 1;
		
		for (Map.Entry<Integer, Integer> entry : sorted)
		{
			if (position > prize.getPrizes().size())
				break;
			
			int objectId = entry.getKey(); // charId
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			
			if (player != null)
			{
				prize.giveReward(player, entry.getValue(), position);
			}
			else
			{
				insertOfflineReward(objectId, prize, position);
			}
			
			position++;
		}
		
		ranking.clear();
		ResetManager.getInstance().resetRanking(type);
	}
	
	private static void insertOfflineReward(int charId, ResetPrize prize, int position)
	{
		PrizeHolder rewardPrize = null;
		
		for (PrizeHolder p : prize.getPrizes())
		{
			if (p.getPosition() == position)
			{
				rewardPrize = p;
				break;
			}
		}
		
		if (rewardPrize == null)
			return;
		
		for (IntIntHolder reward : rewardPrize.getRewards())
		{
			if (reward.getId() <= 0 || reward.getValue() <= 0)
				continue;
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO items (owner_id, object_id, item_id, count, enchant_level, loc, loc_data, process, first_owner_id, creation_time) VALUES (?, ?, ?, ?, ?, 'INVENTORY', 0, 'ResetPrize', ?, ?)"))
			{
				int newObjectId = IdFactory.getInstance().getNextId();
				
				ps.setInt(1, charId);
				ps.setInt(2, newObjectId);
				ps.setInt(3, reward.getId());
				ps.setInt(4, reward.getValue());
				ps.setInt(5, 0);
				ps.setInt(6, charId);
				ps.setLong(7, System.currentTimeMillis());
				
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.warn("Error delivering offline reward to charId " + charId + ": " + e.getMessage());
			}
		}
	}
	
}
