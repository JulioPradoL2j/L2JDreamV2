package com.dream.game.model;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.ResetData;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.holders.IntIntHolder;
import com.dream.game.model.holders.ResetHolder;
import com.dream.game.model.holders.ResetPrize;
import com.dream.game.model.holders.ResetType;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.ShortCutInit;
import com.dream.game.network.serverpackets.SkillCoolTime;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.taskmanager.ResetTask;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class ResetManager
{
	public static Logger LOGGER = Logger.getLogger(ResetManager.class);
	
	private Map<Integer, Integer> _dailyRanking = new HashMap<>();
	private Map<Integer, Integer> _monthlyRanking = new HashMap<>();
	
	private ScheduledFuture<?> _greetingChecker;
	
	public void start()
	{
		loadRankingsFromDatabase();
		for (ResetHolder holder : ResetData.getInstance().getResets())
		{
			for (ResetPrize prize : holder.getPrizes())
			{
				if (!prize.isEnabled())
					continue;
				
				long initialDelay = computeInitialDelay(prize.getTime());
				long interval = prize.getType().getIntervalMillis();
				
				if (_greetingChecker == null)
				{
					_greetingChecker = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ResetTask(), initialDelay, interval);
				}
			}
		}
		
	}
	
	public boolean tryReset(L2PcInstance player)
	{
		for (ResetHolder holder : ResetData.getInstance().getResets())
		{
			if (player.getLevel() >= holder.getLevelMax() && player.getPvpKills() >= holder.getRequiredPvps())
			{
				if (player.getClassId().level() >= 3)
				{
					StringBuilder missingItems = new StringBuilder();
					boolean hasAllItems = true;
					
					for (IntIntHolder req : holder.getRequiredItems())
					{
						int itemId = req.getId();
						long requiredCount = req.getValue();
						long playerCount = player.getInventory().getInventoryItemCount(itemId, -1);
						
						if (playerCount < requiredCount)
						{
							hasAllItems = false;
							L2Item item = ItemTable.getInstance().getTemplate(itemId);
							
							missingItems.append(String.format("%s - You have: %d / It needs: %d\n", item.getName(), playerCount, requiredCount));
						}
					}
					
					if (!hasAllItems)
					{
						player.sendMessage("You do not have the items required for the reset:");
						for (String line : missingItems.toString().split("\n"))
						{
							if (!line.isEmpty())
							{
								player.sendMessage(line);
							}
						}
						return false;
					}
					
					for (IntIntHolder req : holder.getRequiredItems())
					{
						player.getInventory().destroyItemByItemId("Reset", req.getId(), req.getValue(), player, null);
					}
					
					applyReset(player, holder);
					applyReward(player, holder);
				}
				else
				{
					player.sendMessage("You need to complete the 3rd class change to perform a reset.");
				}
				return true;
			}
		}
		return false;
	}
	
	private static void applyReward(L2PcInstance player, ResetHolder holder)
	{
		
		for (IntIntHolder reward : holder.getRewardItems())
		{
			int itemId = reward.getId();
			int itemCount = reward.getValue();
			
			player.getInventory().addItem("ResetReward", itemId, itemCount, player, null);
			
			L2Item item = ItemTable.getInstance().getTemplate(itemId);
			String itemName = (item != null) ? item.getName() : "ItemID " + itemId;
			player.sendMessage(String.format("You received: %s x%d", itemName, itemCount));
		}
		
		for (IntIntHolder skillReward : holder.getRewardSkills())
		{
			int skillId = skillReward.getId();
			int maxLevel = skillReward.getValue();
			
			L2Skill currentSkill = player.getKnownSkill(skillId);
			int currentLevel = (currentSkill != null) ? currentSkill.getLevel() : 0;
			
			if (currentLevel < maxLevel)
			{
				int nextLevel = currentLevel + 1;
				
				L2Skill nextSkill = SkillTable.getInstance().getInfo(skillId, nextLevel);
				if (nextSkill != null)
				{
					player.addSkill(nextSkill, true);
					player.sendMessage(String.format("You learned: %s (Lv. %d)", nextSkill.getName(), nextLevel));
					
					if (holder.isDebug())
					{
						LOGGER.info(String.format(player.getName() + " learned: %s (Lv. %d)", nextSkill.getName(), nextLevel));
					}
				}
				else
				{
					player.sendMessage("Error adding skill: Invalid skill in database.");
				}
				break;
			}
		}
	}
	
	private void applyReset(L2PcInstance player, ResetHolder holder)
	{
		for (L2Skill oldSkill : player.getAllSkills())
		{
			boolean isRewardSkill = false;
			
			for (IntIntHolder skillReward : holder.getRewardSkills())
			{
				if (oldSkill.getId() == skillReward.getId())
				{
					isRewardSkill = true;
					break;
				}
			}
			
			if (!isRewardSkill)
			{
				player.removeSkill(oldSkill, true, true);
			}
		}
		
		player.removeExpAndSp(player.getStat().getExp(), player.getStat().getSp());
		player.getStat().setLevel((byte) 1);
		
		ClassId root = getRootClassId(player.getClassId());
		int rootClassId = root.getId();
		
		player.setResetClassId(rootClassId);
		player.setBaseClass(rootClassId);
		
		player.broadcastUserInfo();
		
		player.stopAllEffects();
		player.clearCharges();
		
		for (L2ItemInstance temp : player.getInventory().getAugmentedItems())
		{
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().removeBonus(player);
			}
		}
		
		if (player.isInParty())
		{
			if (Config.MAX_PARTY_LEVEL_DIFFERENCE > 0)
			{
				for (L2PcInstance p : player.getParty().getPartyMembers())
				{
					if (Math.abs(p.getLevel() - player.getLevel()) > Config.MAX_PARTY_LEVEL_DIFFERENCE)
					{
						player.getParty().removePartyMember(player);
						player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(player, Message.MessageId.MSG_REMOVE_FROM_PARTY_BIG_LVL_DIF));
						break;
					}
				}
			}
			else
			{
				player.getParty().recalculatePartyLevel();
			}
			
		}
		
		if (player.getPet() != null && player.getPet() instanceof L2SummonInstance)
		{
			player.getPet().unSummon(player);
		}
		
		if (!player.getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : player.getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			player.getCubics().clear();
		}
		
		player.abortCast();
		
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.restoreDeathPenaltyBuffLevel();
		
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			player.checkAllowedSkills();
		}
		
		player.updateEffectIcons();
		
		player.getInventory().restoreEquipedItemsPassiveSkill();
		player.getInventory().restoreArmorSetPassiveSkill();
		player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
		player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
		player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_L_HAND);
		player.sendPacket(new ItemList(player, false));
		player.broadcastUserInfo();
		player.refreshOverloaded();
		player.refreshExpertisePenalty();
		player.setExpBeforeDeath(0);
		
		player.sendPacket(new ShortCutInit(player));
		player.broadcastPacket(new SocialAction(player, 15));
		player.sendPacket(new SkillCoolTime(player));
		player.broadcastClassIcon();
		
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item.isEquipped() && item.getItem() instanceof L2Armor)
			{
				player.getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
		}
		
		player.rewardSkills();
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		player.getStatus().setCurrentCp(player.getMaxCp());
		player.getStatus().setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		updateRankings(player);
	}
	
	private static ClassId getRootClassId(ClassId classId)
	{
		while (classId.getParent() != null)
		{
			classId = classId.getParent();
		}
		return classId;
	}
	
	private void updateRankings(L2PcInstance player)
	{
		int objectId = player.getObjectId();
		
		ResetHolder holder = ResetData.getInstance().getResets().get(0);
		
		boolean updateDaily = false;
		boolean updateMonthly = false;
		
		for (ResetPrize prize : holder.getPrizes())
		{
			if (prize.getType() == ResetType.DAILY && prize.isEnabled())
			{
				int points = holder.getDailyPoints();
				_dailyRanking.merge(objectId, points, Integer::sum);
				updateDaily = true;
			}
			else if (prize.getType() == ResetType.MONTH && prize.isEnabled())
			{
				int points = holder.getMonthlyPoints();
				_monthlyRanking.merge(objectId, points, Integer::sum);
				updateMonthly = true;
			}
		}
		
		if (!updateDaily && !updateMonthly)
			return;
		
		StringBuilder sql = new StringBuilder("INSERT INTO reset_rankings (player_id");
		if (updateDaily)
			sql.append(", daily_count");
		if (updateMonthly)
			sql.append(", monthly_count");
		sql.append(") VALUES (?");
		
		if (updateDaily)
			sql.append(", ").append(holder.getDailyPoints());
		if (updateMonthly)
			sql.append(", ").append(holder.getMonthlyPoints());
		sql.append(") ON DUPLICATE KEY UPDATE ");
		
		List<String> updates = new ArrayList<>();
		if (updateDaily)
			updates.add("daily_count = daily_count + " + holder.getDailyPoints());
		if (updateMonthly)
			updates.add("monthly_count = monthly_count + " + holder.getMonthlyPoints());
		
		sql.append(String.join(", ", updates));
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString()))
		{
			ps.setInt(1, objectId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Error updating reset ranking in database for playerId: " + objectId, e);
		}
	}
	
	public Map<Integer, Integer> getRanking(ResetType type)
	{
		return type == ResetType.DAILY ? _dailyRanking : _monthlyRanking;
	}
	
	private void loadRankingsFromDatabase()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement("SELECT player_id, daily_count, monthly_count FROM reset_rankings"); ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int playerId = rs.getInt("player_id");
				int daily = rs.getInt("daily_count");
				int monthly = rs.getInt("monthly_count");
				
				_dailyRanking.put(playerId, daily);
				_monthlyRanking.put(playerId, monthly);
			}
			
			LOGGER.info("ResetManager: Rankings loaded successfully.");
		}
		catch (Exception e)
		{
			LOGGER.error("Error loading reset rankings from database.", e);
		}
	}
	
	public void resetRanking(ResetType type)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(type == ResetType.DAILY ? "UPDATE reset_rankings SET daily_count = 0" : "UPDATE reset_rankings SET monthly_count = 0"))
		{
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Error resetting ranking " + type.name(), e);
		}
		
		if (type == ResetType.DAILY)
			_dailyRanking.clear();
		else
			_monthlyRanking.clear();
		
		for (ResetHolder holder : ResetData.getInstance().getResets())
		{
			if (!holder.isRemoveResetSkills())
				continue;
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				for (IntIntHolder skillReward : holder.getRewardSkills())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillReward.getId(), skillReward.getValue());
					if (skill != null && player.getKnownSkill(skill.getId()) != null)
					{
						player.removeSkill(skill, true, true);
					}
				}
				player.sendSkillList();
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				for (IntIntHolder skill : holder.getRewardSkills())
				{
					try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills WHERE skill_id = ?"))
					{
						ps.setInt(1, skill.getId());
						int affected = ps.executeUpdate();
						
						if (holder.isDebug())
							LOGGER.info("ResetManager: Removed skillId " + skill.getId() + " from " + affected + " offline characters.");
					}
				}
			}
			catch (Exception e)
			{
				if (holder.isDebug())
					LOGGER.error("Error removing reset skills from offline characters", e);
			}
		}
		
	}
	
	private static long computeInitialDelay(LocalTime targetTime)
	{
		LocalTime now = LocalTime.now();
		long delay;
		
		if (now.isAfter(targetTime))
		{
			delay = Duration.between(now, targetTime.plusHours(24)).toMillis();
		}
		else
		{
			delay = Duration.between(now, targetTime).toMillis();
		}
		
		return delay;
	}
	
	public static ResetManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		private static final ResetManager _instance = new ResetManager();
	}
	
}