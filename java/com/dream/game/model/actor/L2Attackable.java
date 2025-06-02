/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.actor;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2AttackableAI;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2SiegeGuardAI;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.manager.EventsDropManager;
import com.dream.game.manager.ItemsAutoDestroy;
import com.dream.game.manager.clanhallsiege.FortResistSiegeManager;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2CommandChannel;
import com.dream.game.model.L2DropCategory;
import com.dream.game.model.L2DropData;
import com.dream.game.model.L2Manor;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2FortSiegeGuardInstance;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MinionInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.actor.knownlist.AttackableKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.base.SoulCrystal;
import com.dream.game.model.entity.events.EventDroplist;
import com.dream.game.model.entity.events.EventDroplist.DateDrop;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;
import com.dream.util.SingletonMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javolution.util.FastMap;

public class L2Attackable extends L2Npc
{
	public final class AbsorberInfo
	{
		protected L2PcInstance _absorber;
		protected int _crystalId;
		protected double _absorbedHP;
		
		AbsorberInfo(L2PcInstance attacker, int pCrystalId, double pAbsorbedHP)
		{
			_absorber = attacker;
			_crystalId = pCrystalId;
			_absorbedHP = pAbsorbedHP;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj instanceof AbsorberInfo)
				return ((AbsorberInfo) obj)._absorber == _absorber;
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _absorber.getObjectId();
		}
	}
	
	public final class AggroInfo
	{
		protected L2Character _attacker;
		
		protected int _hate;
		
		protected int _damage;
		
		AggroInfo(L2Character pAttacker)
		{
			_attacker = pAttacker;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj instanceof AggroInfo)
				return ((AggroInfo) obj)._attacker == _attacker;
			return false;
		}
		
		public L2Character getAttacker()
		{
			return _attacker;
		}
		
		public int getDamage()
		{
			return _damage;
		}
		
		public int getHate()
		{
			return _hate;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	private class CommandChannelTimer implements Runnable
	{
		private final L2Attackable _monster;
		private final L2CommandChannel _channel;
		
		public CommandChannelTimer(L2Attackable monster, L2CommandChannel channel)
		{
			_monster = monster;
			_channel = channel;
		}
		
		@Override
		public void run()
		{
			_monster.setCommandChannelTimer(null);
			_monster.setFirstCommandChannelAttacked(null);
			for (L2Character player : _monster.getAggroListRP().keySet())
				if (player.isInParty() && player.getParty().isInCommandChannel())
					if (player.getParty().getCommandChannel().equals(_channel))
					{
						// if a player which is in first attacked
						// CommandChannel, restart the timer ;)
						_monster.setCommandChannelTimer(this);
						_monster.setFirstCommandChannelAttacked(_channel);
						ThreadPoolManager.getInstance().scheduleGeneral(this, 300000); // 5 min
						break;
					}
		}
	}
	
	protected final class RewardInfo
	{
		protected L2Character _attacker;
		protected int _dmg = 0;
		
		public RewardInfo(L2Character pAttacker, int pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}
		
		public void addDamage(int pDmg)
		{
			_dmg += pDmg;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj instanceof RewardInfo)
				return ((RewardInfo) obj)._attacker == _attacker;
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	public final class RewardItem
	{
		protected int _itemId;
		protected int _count;
		
		public RewardItem(int itemId, int count)
		{
			_itemId = itemId;
			_count = count;
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public void setCount(int cnt)
		{
			_count = cnt;
		}
	}
	
	protected final static Logger _log = Logger.getLogger(L2Attackable.class.getName());
	
	private static boolean checkBlueDrop(L2PcInstance player, boolean isSweep)
	{
		return Config.DEEPBLUE_DROP_RULES;
	}
	
	private final Map<L2Character, AggroInfo> _aggroList = new SingletonMap<L2Character, AggroInfo>().setShared();
	
	private final Map<L2Character, AggroInfo> _damageContributors = new SingletonMap<L2Character, AggroInfo>().setShared();
	
	private boolean _isReturningToSpawnPoint = false;
	
	private boolean _canReturnToSpawnPoint = true;
	
	/** Table containing all Items that a Dwarf can Sweep on this L2Attackable */
	private RewardItem[] _sweepItems;
	
	/** crops */
	private RewardItem[] _harvestItems;
	
	private boolean _seeded;
	
	private int _seedType = 0;
	
	private L2PcInstance _seeder = null;
	
	/**
	 * true if an over-hit enabled skill has successfully landed on the L2Attackable
	 */
	private boolean _overhit;
	
	/**
	 * Stores the extra (over-hit) damage done to the L2Attackable when the attacker uses an over-hit enabled skill
	 */
	private double _overhitDamage;
	
	/**
	 * Stores the attacker who used the over-hit enabled skill on the L2Attackable
	 */
	private L2Character _overhitAttacker;
	/**
	 * First CommandChannel who attacked the L2Attackable and meet the requirements
	 **/
	private L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	/** true if a Soul Crystal was successfuly used on the L2Attackable */
	private boolean _absorbed;
	
	/**
	 * The table containing all L2PcInstance that successfuly absorbed the soul of this L2Attackable
	 */
	private final Map<L2PcInstance, AbsorberInfo> _absorbersList = new SingletonMap<L2PcInstance, AbsorberInfo>().setShared();
	
	/** Have this L2Attackable to reward Exp and SP on Die? **/
	private boolean _mustGiveExpSp;
	
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_mustGiveExpSp = true;
	}
	
	public void absorbSoul()
	{
		_absorbed = true;
		
	}
	
	public void addAbsorber(L2PcInstance attacker, int crystalId)
	{
		if (!(this instanceof L2MonsterInstance))
			return;
		
		if (attacker == null)
			return;
		
		if (getAbsorbLevel() == 0)
			return;
		
		AbsorberInfo ai = _absorbersList.get(attacker);
		
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker, crystalId, getStatus().getCurrentHp());
			_absorbersList.put(attacker, ai);
		}
		else
		{
			ai._absorber = attacker;
			ai._crystalId = crystalId;
			ai._absorbedHP = getStatus().getCurrentHp();
		}
		
		absorbSoul();
	}
	
	public void addDamage(L2Character attacker, int damage)
	{
		addDamageHate(attacker, damage, damage, null);
	}
	
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		addDamageHate(attacker, damage, damage, skill);
	}
	
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		addDamageHate(attacker, damage, aggro, null);
	}
	
	public void addDamageHate(L2Character attacker, int damage, int aggro, L2Skill skill)
	{
		
		if (attacker == null)
			return;
		if (getNpcId() == 35368)
			if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getClan() != null)
			{
				FortResistSiegeManager.getInstance().addSiegeDamage(((L2PcInstance) attacker).getClan(), damage);
			}
		// Get the AggroInfo of the attacker L2Character from the _aggroList of
		// the L2Attackable
		AggroInfo ai = getAggroListRP().get(attacker);
		if (ai == null)
		{
			ai = new AggroInfo(attacker);
			ai._damage = 0;
			ai._hate = 0;
			getAggroListRP().put(attacker, ai);
			if ((attacker instanceof L2PcInstance || attacker instanceof L2Summon) && !attacker.isAlikeDead())
			{
				Quest[] q = getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
				L2PcInstance targetPlayer = attacker instanceof L2PcInstance ? (L2PcInstance) attacker : ((L2Summon) attacker).getOwner();
				if (q != null)
				{
					for (Quest quest : q)
					{
						try
						{
							quest.notifyAggroRangeEnter(this, targetPlayer, attacker instanceof L2Summon);
						}
						catch (Exception e)
						{
							continue;
						}
					}
				}
			}
		}
		
		// If aggro is negative, its comming from SEE_SPELL, buffs use constant
		// 150
		if (aggro < 0)
		{
			ai._hate -= aggro * 150 / (getLevel() + 7);
			aggro = -aggro;
		}
		// if damage == 0 -> this is case of adding only to aggro list, dont
		// apply formula on it
		else if (damage == 0)
		{
			ai._hate += aggro;
			// else its damage that must be added using constant 100
		}
		else
		{
			ai._hate += aggro * 100 / (getLevel() + 7);
		}
		
		// Add new damage and aggro (=damage) to the AggroInfo object
		ai._damage += damage;
		ai._hate += aggro * 100 / (getLevel() + 7);
		
		// we will do some special treatments for _attacker but _attacker is not
		// for sure a L2PlayableInstance...
		if (attacker instanceof L2Playable)
		{
			// attacker L2PcInstance could be the the attacker or the owner of
			// the attacker
			L2PcInstance _attacker = attacker instanceof L2PcInstance ? (L2PcInstance) attacker : ((L2Summon) attacker).getOwner();
			AggroInfo damageContrib = getDamageContributors().get(_attacker);
			if (damageContrib != null)
			{
				damageContrib._damage += damage;
				damageContrib._hate += aggro;
			}
			else
			{
				damageContrib = new AggroInfo(_attacker);
				damageContrib._damage = damage;
				damageContrib._hate = aggro;
				getDamageContributors().put(_attacker, damageContrib);
			}
		}
		
		// Set the intention to the L2Attackable to ACTIVE
		if (aggro > 0 && getAI().getIntention() == CtrlIntention.IDLE)
		{
			getAI().setIntention(CtrlIntention.ACTIVE);
		}
		
		// Notify the L2Attackable AI with EVT_ATTACKED
		if (/* !isDead() && */damage > 0)
		{
			getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
			
			try
			{
				if (attacker instanceof L2PcInstance || attacker instanceof L2Summon)
				{
					L2PcInstance player = attacker instanceof L2PcInstance ? (L2PcInstance) attacker : ((L2Summon) attacker).getOwner();
					
					if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null)
					{
						for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
						{
							quest.notifyAttack(this, player, damage, attacker instanceof L2Summon, skill);
						}
					}
				}
			}
			catch (Exception e)
			{
				
			}
		}
		
	}
	
	private RewardItem calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
			return null;
		
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		int champRate;
		
		int deepBlueDrop = 1;
		if (Config.DEEPBLUE_DROP_RULES)
			if (levelModifier > 0)
			{
				deepBlueDrop = 3;
			}
		
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
		}
		
		categoryDropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		
		categoryDropChance = Math.round(categoryDropChance);
		
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(isRaid() && !isRaidMinion());
			
			if (drop == null)
				return null;
			
			if (isChampion())
			{
				if (drop.getItemId() == 57 || drop.getItemId() == 5575 || drop.getItemId() >= 6360 && drop.getItemId() <= 6362)
				{
					champRate = Config.CHAMPION_ADENA;
				}
				else
				{
					champRate = Config.CHAMPION_REWARDS;
				}
			}
			else
			{
				champRate = 1;
			}
			
			int dropChance = drop.getChance();
			
			if (lastAttacker.isVip())
			{
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.VIP_DROP_RATE;
			}
			else
			{
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}
			
			dropChance = Math.round(dropChance) * champRate;
			
			if (dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			int itemCount = 0;
			
			if (dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
			{
				int multiplier = dropChance / L2DropData.MAX_CHANCE;
				
				if (min < max)
				{
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				}
				else if (min == max)
				{
					itemCount += min * multiplier;
				}
				else
				{
					itemCount += multiplier;
				}
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				if (min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if (min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}
				
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (!Config.MULTIPLE_ITEM_DROP && !ItemTable.getInstance().getTemplate(drop.getItemId()).isStackable() && itemCount > 1)
			{
				itemCount = 1;
			}
			
			if (drop.getItemId() == 57)
			{
				itemCount *= Config.RATE_DROP_ADENA;
			}
			
			if (drop.getItemId() >= 6360 && drop.getItemId() <= 6362)
			{
				itemCount *= Config.RATE_DROP_SEAL_STONES;
			}
			if (itemCount > 0)
				return new RewardItem(drop.getItemId(), itemCount * champRate);
			else if (itemCount == 0 && Config.DEBUG)
			{
				_log.warn("Roll produced no drops.");
			}
		}
		return null;
	}
	
	private int[] calculateExpAndSp(int diff, int damage)
	{
		double xp;
		double sp;
		
		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		xp = (double) getExpReward() * damage / getMaxHp();
		
		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		sp = (double) getSpReward() * damage / getMaxHp();
		
		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if (Config.ALT_GAME_EXPONENT_XP == 0 && Config.ALT_GAME_EXPONENT_SP == 0)
		{
			if (diff > 5) // formula revised May 07
			{
				double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
			{
				sp = 0;
			}
		}
		
		int[] tmp =
		{
			(int) xp,
			(int) sp
		};
		
		return tmp;
	}
	
	private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	{
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int highestLevel = lastAttacker.getLevel();
			
			if (getAttackByList() != null && !getAttackByList().isEmpty())
			{
				for (L2Character atkChar : getAttackByList())
					if (atkChar != null && atkChar.getLevel() > highestLevel)
					{
						highestLevel = atkChar.getLevel();
					}
			}
			
			if (highestLevel - 9 >= getLevel())
				return (highestLevel - (getLevel() + 8)) * 9;
		}
		return 0;
	}
	
	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done
		// relative to the total (maximum) ammount of HP on the L2Attackable
		double overhitPercentage = getOverhitDamage() * 100 / getMaxHp();
		
		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}
		
		// Get the overhit exp bonus according to the above over-hit damage
		// percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and
		// so on...)
		double overhitExp = overhitPercentage / 100 * normalExp;
		
		// Return the rounded ammount of exp points to be added to the player's
		// normal exp reward
		long bonusOverhit = Math.round(overhitExp);
		return bonusOverhit;
	}
	
	private RewardItem calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		float dropChance = drop.getChance();
		int champRate;
		
		int deepBlueDrop = 1;
		if (checkBlueDrop(lastAttacker, isSweep))
			if (levelModifier > 0)
			{
				deepBlueDrop = 3;
				if (drop.getItemId() == 57)
				{
					deepBlueDrop *= isRaid() && !isRaidMinion() ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ADENA;
				}
			}
		
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
		}
		
		if (isChampion())
		{
			if (drop.getItemId() == 57 || drop.getItemId() == 5575 || drop.getItemId() == 6360 || drop.getItemId() == 6361 || drop.getItemId() == 6362)
			{
				champRate = Config.CHAMPION_ADENA;
			}
			else
			{
				champRate = Config.CHAMPION_REWARDS;
			}
		}
		else
		{
			champRate = 1;
		}
		
		if (drop.getItemId() == 57)
		{
			if (isChampion())
			{
				dropChance *= Config.CHAMPION_ADENA;
			}
			else if (this instanceof L2RaidBossInstance)
			{
				dropChance *= Config.ADENA_RAID;
			}
			else if (this instanceof L2GrandBossInstance)
			{
				dropChance *= Config.ADENA_BOSS;
			}
			else if (this instanceof L2MinionInstance)
			{
				dropChance *= Config.ADENA_MINION;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ADENA;
			}
		}
		else if (drop.getItemId() == 6660 || drop.getItemId() == 6658 || drop.getItemId() == 6661 || drop.getItemId() == 6657 || drop.getItemId() == 6656 || drop.getItemId() == 8191 || drop.getItemId() == 6662 || drop.getItemId() == 6659)
		{
			if (this instanceof L2RaidBossInstance)
			{
				dropChance *= Config.JEWEL_BOSS;
			}
			else if (this instanceof L2GrandBossInstance)
			{
				dropChance *= Config.JEWEL_BOSS;
			}
			else if (this instanceof L2MinionInstance)
			{
				dropChance *= Config.JEWEL_BOSS;
			}
			else if (lastAttacker.isVip())
			{
				dropChance *= Config.VIP_DROP_RATE;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ITEMS;
				
			}
		}
		else if (isSweep)
		{
			if (this instanceof L2RaidBossInstance)
			{
				dropChance *= Config.SPOIL_RAID;
			}
			else if (this instanceof L2GrandBossInstance)
			{
				dropChance *= Config.SPOIL_BOSS;
			}
			else if (this instanceof L2MinionInstance && this.isRaid())
			{
				dropChance *= Config.SPOIL_MINION;
			}
			else if (lastAttacker.isVip())
			{
				dropChance *= Config.VIP_SPOIL_RATE;
			}
			else
			{
				dropChance *= Config.RATE_DROP_SPOIL;
			}
		}
		else if (drop.getItemId() != 57 || drop.getItemId() != 6660 || drop.getItemId() != 6658 || drop.getItemId() != 6661 || drop.getItemId() != 6657 || drop.getItemId() != 6656 || drop.getItemId() != 8191 || drop.getItemId() != 6662 || drop.getItemId() != 6659)
		{
			if (this instanceof L2RaidBossInstance)
			{
				dropChance *= Config.ITEMS_RAID;
			}
			else if (this instanceof L2GrandBossInstance)
			{
				dropChance *= Config.ITEMS_BOSS;
			}
			else if (this instanceof L2MinionInstance && this.isRaid())
			{
				dropChance *= Config.ITEMS_MINION;
			}
			else if (lastAttacker.isVip())
			{
				dropChance *= Config.VIP_DROP_RATE;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ITEMS;
			}
		}
		else
		{
			dropChance *= Config.RATE_DROP_ITEMS;
		}
		
		dropChance = Math.round(dropChance) * champRate;
		
		if (dropChance < 1)
		{
			dropChance = 1;
		}
		
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();
		int itemCount = 0;
		
		if (dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
		{
			int multiplier = (int) dropChance / L2DropData.MAX_CHANCE;
			
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount * multiplier;
			}
			else
			{
				itemCount += multiplier;
			}
			
			dropChance = dropChance % L2DropData.MAX_CHANCE;
		}
		
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		while (random < dropChance)
		{
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (itemCount > 0)
			return new RewardItem(drop.getItemId(), itemCount * champRate);
		else if (itemCount == 0 && _log.isDebugEnabled())
		{
			_log.info("Roll produced 0 items to drop...");
		}
		
		return null;
	}
	
	/**
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) and L2Party in progress</li>
	 * <li>Calculate the Experience and SP rewards in function of the level difference</li>
	 * <li>Add Exp and SP rewards to L2PcInstance (including Summon penalty) and to Party members in the known area of the last attacker</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR>
	 * <BR>
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	
	@SuppressWarnings("null")
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		// Creates an empty list of rewards
		FastMap<L2Character, RewardInfo> rewards = new FastMap<L2Character, RewardInfo>().shared();
		
		try
		{
			if (getAggroListRP().isEmpty())
				return;
			
			// int rewardCount = 0;
			int damage;
			L2Character attacker, ddealer;
			RewardInfo reward;
			
			L2PcInstance maxDealer = null;
			int maxDamage = 0;
			
			// While iterating over this map removing objects is not allowed
			synchronized (getAggroList())
			{
				// Go through the _aggroList of the L2Attackable
				for (AggroInfo info : getAggroListRP().values())
				{
					if (info == null)
					{
						continue;
					}
					
					// Get the L2Character corresponding to this attacker
					attacker = info._attacker;
					
					// Get damages done by this attacker
					damage = info._damage;
					
					// Prevent unwanted behavior
					if (damage > 1)
					{
						if (attacker instanceof L2SummonInstance || attacker instanceof L2PetInstance && ((L2PetInstance) attacker).getPetData().getOwnerExpTaken() > 0)
						{
							ddealer = ((L2Summon) attacker).getOwner();
						}
						else
						{
							ddealer = info._attacker;
						}
						
						// Check if ddealer isn't too far from this (killed
						// monster)
						if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
						{
							continue;
						}
						
						// Calculate real damages (Summoners should get own
						// damage plus summon's damage)
						reward = rewards.get(ddealer);
						
						if (reward == null)
						{
							reward = new RewardInfo(ddealer, damage);
							// rewardCount++;
						}
						else
						{
							reward.addDamage(damage);
						}
						rewards.put(ddealer, reward);
						
						if (ddealer.getActingPlayer() != null && reward._dmg > maxDamage)
						{
							maxDealer = ddealer.getActingPlayer();
							maxDamage = reward._dmg;
						}
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the L2Attackable
			doItemDrop(maxDealer != null && maxDealer.isOnline() == 1 ? maxDealer : lastAttacker);
			
			// Manage drop of Special Events created by GM for a defined period
			doEventDrop(maxDealer != null && maxDealer.isOnline() == 1 ? maxDealer : lastAttacker);
			
			if (!getMustRewardExpSP())
				return;
			
			if (!rewards.isEmpty())
			{
				L2Party attackerParty;
				long exp;
				int levelDiff, partyDmg, partyLvl, sp;
				float partyMul, penalty;
				RewardInfo reward2;
				int[] tmp;
				
				for (FastMap.Entry<L2Character, RewardInfo> entry = rewards.head(), end = rewards.tail(); (entry = entry.getNext()) != end;)
				{
					if (entry == null)
					{
						continue;
					}
					
					reward = entry.getValue();
					if (reward == null)
					{
						continue;
					}
					
					// Penalty applied to the attacker's XP
					penalty = 0;
					
					// Attacker to be rewarded
					attacker = reward._attacker;
					
					// Total amount of damage done
					damage = reward._dmg;
					
					// If the attacker is a Pet, get the party of the owner
					if (attacker instanceof L2PetInstance)
					{
						attackerParty = attacker.getParty();
					}
					else if (attacker instanceof L2PcInstance)
					{
						attackerParty = attacker.getParty();
					}
					else
						return;
						
					// If this attacker is a L2PcInstance with a summoned
					// L2SummonInstance, get Exp Penalty applied for the current
					// summoned L2SummonInstance
					if (attacker instanceof L2PcInstance && attacker.getPet() instanceof L2SummonInstance)
					{
						penalty = ((L2SummonInstance) ((L2PcInstance) attacker).getPet()).getExpPenalty();
					}
					
					// We must avoid "over damage", if any
					if (damage > getMaxHp())
					{
						damage = getMaxHp();
					}
					// If there's NO party in progress
					if (attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(this))
						{
							// Calculate the difference of level between this
							// attacker (L2PcInstance or L2SummonInstance owner)
							// and the L2Attackable
							// mob = 24, atk = 10, diff = -14 (full xp)
							// mob = 24, atk = 28, diff = 4 (some xp)
							// mob = 24, atk = 50, diff = 26 (no xp)
							levelDiff = attacker.getLevel() - getLevel();
							
							tmp = calculateExpAndSp(levelDiff, damage);
							exp = tmp[0];
							exp *= 1 - penalty;
							sp = tmp[1];
							
							// Check for an over-hit enabled strike
							if (attacker instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) attacker;
								if (isOverhit() && attacker == getOverhitAttacker())
								{
									int overHitExp = (int) calculateOverhitExp(exp);
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_BONUS_EXPERIENCE_THROUGH_OVER_HIT).addNumber(overHitExp));
									exp += overHitExp;
								}
								if (player.isVip() && Config.ALLOW_VIP_XPSP)
								{
									exp *= Config.VIP_XP;
									sp *= Config.VIP_SP;
								}
							}
							
							// Distribute the Exp and SP between the
							// L2PcInstance and its L2Summon
							if (isChampion())
							{
								exp *= Config.CHAMPION_EXP_SP;
								sp *= Config.CHAMPION_EXP_SP;
							}
							
							if (!attacker.isDead())
							{
								long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
								int addsp = (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
								
								attacker.addExpAndSp(addexp, addsp);
							}
						}
					}
					else
					{
						// share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;
						
						// Get all L2Character that can be rewarded in the party
						List<L2Playable> rewardedMembers = new ArrayList<>();
						
						// Go through all L2PcInstance in the party
						List<L2PcInstance> groupMembers;
						if (attackerParty.isInCommandChannel())
						{
							groupMembers = attackerParty.getCommandChannel().getMembers();
						}
						else
						{
							groupMembers = attackerParty.getPartyMembers();
						}
						
						for (L2PcInstance pl : groupMembers)
						{
							if (pl == null || pl.isDead())
							{
								continue;
							}
							
							// Get the RewardInfo of this L2PcInstance from
							// L2Attackable rewards
							reward2 = rewards.get(pl);
							
							// If the L2PcInstance is in the L2Attackable
							// rewards add its damages to party damages
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									partyDmg += reward2._dmg; // Add
									// L2PcInstance
									// damages to
									// party damages
									rewardedMembers.add(pl);
									if (pl.getLevel() > partyLvl)
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = pl.getLevel();
										}
								}
								rewards.remove(pl); // Remove the L2PcInstance
								// from the L2Attackable
								// rewards
							}
							else // Add L2PcInstance of the party (that have
									// attacked or not) to members that can be
									// rewarded
									// and in range of the monster.
							if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
							{
								rewardedMembers.add(pl);
								if (pl.getLevel() > partyLvl)
									if (attackerParty.isInCommandChannel())
									{
										partyLvl = attackerParty.getCommandChannel().getLevel();
									}
									else
									{
										partyLvl = pl.getLevel();
									}
							}
							L2Playable summon = pl.getPet();
							if (summon != null && summon instanceof L2PetInstance)
							{
								reward2 = rewards.get(summon);
								if (reward2 != null) // Pets are only added if
								// they have done damage
								{
									if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true))
									{
										partyDmg += reward2._dmg; // Add summon
										// damages
										// to party
										// damages
										rewardedMembers.add(summon);
										if (summon.getLevel() > partyLvl)
										{
											partyLvl = summon.getLevel();
										}
									}
									rewards.remove(summon); // Remove the summon
									// from the
									// L2Attackable
									// rewards
								}
							}
						}
						
						// If the party didn't killed this L2Attackable alone
						if (partyDmg < getMaxHp())
						{
							partyMul = (float) partyDmg / (float) getMaxHp();
						}
						
						// Avoid "over damage"
						if (partyDmg > getMaxHp())
						{
							partyDmg = getMaxHp();
						}
						
						int newLevel = 0;
						for (L2Character member : rewardedMembers)
							if (member.getLevel() > newLevel)
							{
								newLevel = member.getLevel();
							}
							
						// Calculate the level difference between Party and
						// L2Attackable
						levelDiff = partyLvl - getLevel();
						
						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(levelDiff, partyDmg);
						exp = tmp[0];
						sp = tmp[1];
						
						exp *= partyMul;
						sp *= partyMul;
						
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to
						// the whole party and splitted proportionally through
						// the party members)
						if (attacker instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) attacker;
							if (isOverhit() && attacker == getOverhitAttacker())
							{
								player.sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp(exp);
							}
							if (player.isVip() && Config.ALLOW_VIP_XPSP)
							{
								exp *= Config.VIP_XP;
								sp *= Config.VIP_SP;
							}
						}
						
						// champion xp/sp :)
						if (isChampion())
						{
							exp *= Config.CHAMPION_EXP_SP;
							sp *= Config.CHAMPION_EXP_SP;
						}
						
						// Distribute Experience and SP rewards to L2PcInstance
						// Party members in the known area of the last attacker
						if (partyDmg > 0)
						{
							attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, this, partyDmg, isChampion());
						}
					}
				}
			}
			
			rewards = null;
		}
		catch (Exception e)
		{
			
		}
	}
	
	public boolean canReduceHp(double damage, L2Character attacker)
	{
		return true;
	}
	
	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}
	
	public void clearAggroList()
	{
		getAggroList().clear();
	}
	
	public void clearDamageContributors()
	{
		getDamageContributors().clear();
	}
	
	public boolean containsTarget(L2Character player)
	{
		return getAggroListRP().containsKey(player);
	}
	
	public AggroInfo[] copyAggroList()
	{
		return _aggroList.values().toArray(new AggroInfo[_aggroList.size()]);
	}
	
	/**
	 * Kill the L2Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members</li>
	 * <li>Notify the Quest Engine of the L2Attackable death if necessary</li>
	 * <li>Kill the L2NpcInstance (the corpse disappeared after 7 seconds)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR>
	 * <BR>
	 * @param killer The L2Character that has killed the L2Attackable
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer))
			return false;
		
		if (_event != null)
		{
			_event.onKill(killer, this);
		}
		
		// Enhance soul crystals of the attacker if this L2Attackable had its
		// soul absorbed
		try
		{
			if (killer instanceof L2PcInstance)
			{
				levelSoulCrystals(killer);
			}
		}
		catch (Exception e)
		{
			
		}
		
		// Notify the Quest Engine of the L2Attackable death if necessary
		try
		{
			if (killer != null)
			{
				L2PcInstance player = null;
				player = killer.getActingPlayer();
				
				if (player != null)
				{
					// only 1 randomly choosen quest of all quests registered to
					// this character can be applied
					Quest[] allOnKillQuests = getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL);
					if (allOnKillQuests != null && allOnKillQuests.length > 0)
					{
						for (Quest quest : allOnKillQuests)
						{
							try
							{
								quest.notifyKill(this, player, killer instanceof L2Summon);
							}
							catch (Exception e)
							{
								continue;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			
		}
		
		setChampion(false);
		
		return true;
	}
	
	/**
	 * Manage Special Events drops created by GM for a defined period.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * During a Special Event all L2Attackable can drop extra Items. Those extra Items are defined in the table <B>allNpcDateDrops</B> of the EventDroplist. Each Special Event has a start and end date to stop to drop extra Items automaticaly. <BR>
	 * <BR>
	 * <B><U> Actions</U> : <I>If an extra drop must be generated</I></B><BR>
	 * <BR>
	 * <li>Get an Item Identifier (random) from the DateDrop Item table of this Event</li>
	 * <li>Get the Item quantity dropped (random)</li>
	 * <li>Create this or these L2ItemInstance corresponding to this Item Identifier</li>
	 * <li>If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, give this or these Item(s) to the L2PcInstance that has killed the L2Attackable</li>
	 * <li>If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these Item(s) in the world as a visible object at the position where mob was last</li><BR>
	 * <BR>
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doEventDrop(L2Character lastAttacker)
	{
		L2PcInstance player = null;
		if (lastAttacker instanceof L2PcInstance)
		{
			player = (L2PcInstance) lastAttacker;
		}
		else if (lastAttacker instanceof L2Summon)
		{
			player = ((L2Summon) lastAttacker).getOwner();
		}
		if (player == null)
			return;
		
		if (player.getLevel() - getLevel() > 9)
			return;
		
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
			if (Rnd.get(L2DropData.MAX_CHANCE) < drop.chance)
			{
				RewardItem item = new RewardItem(drop.items[Rnd.get(drop.items.length)], Rnd.get(drop.min, drop.max));
				if (Config.AUTO_LOOT_RAID && isRaid() && player.isAutoLootEnabled())
				{
					player.doAutoLoot(this, item);
				}
				else if (player.isAutoLootEnabled() && !isRaid())
				{
					player.doAutoLoot(this, item);
				}
				else
				{
					dropItem(player, item);
				}
			}
	}
	
	public void doItemDrop(L2Character mainDamageDealer)
	{
		doItemDrop(getTemplate(), mainDamageDealer);
	}
	
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character mainDamageDealer)
	{
		if (mainDamageDealer == null)
			return;
		
		L2PcInstance player = mainDamageDealer.getActingPlayer();
		
		if (player == null)
			return;
		
		if (player._event != null && player._event == _event && _event.isRunning())
			if (!_event.canDropItems(this, player))
				return;
			
		int levelModifier = calculateLevelModifierForDrop(player);
		if (this instanceof L2FortSiegeGuardInstance)
		{
			levelModifier = 0;
		}
		
		CursedWeaponsManager.getInstance().checkDrop(this, player);
		
		if (npcTemplate.getDropData() != null)
		{
			for (L2DropCategory cat : npcTemplate.getDropData())
			{
				RewardItem item = null;
				if (cat.isSweep())
				{
					// according to sh1ny, seeded mobs CAN be spoiled and swept.
					if (isSpoil())
					{
						List<RewardItem> sweepList = new ArrayList<>();
						
						for (L2DropData drop : cat.getAllDrops())
						{
							item = calculateRewardItem(player, drop, levelModifier, true);
							if (item == null)
							{
								continue;
							}
							sweepList.add(item);
						}
						
						// Set the table _sweepItems of this L2Attackable
						if (!sweepList.isEmpty())
						{
							_sweepItems = sweepList.toArray(new RewardItem[sweepList.size()]);
						}
					}
				}
				else
				{
					if (isSeeded() && !L2Manor.getInstance().isAlternative(_seedType))
					{
						L2DropData drop = cat.dropSeedAllowedDropsOnly();
						if (drop == null)
						{
							continue;
						}
						
						item = calculateRewardItem(player, drop, levelModifier, false);
					}
					else
					{
						item = calculateCategorizedRewardItem(player, cat, levelModifier);
					}
					
					if (item != null)
					{
						if (Config.DEBUG)
						{
							_log.warn("Item id to drop: " + item.getItemId() + " amount: " + item.getCount());
						}
						
						L2Item itemCheck = ItemTable.getInstance().getTemplate(item.getItemId());
						
						final boolean isAdena = itemCheck.getItemId() == 57;
						final boolean canReceiveAdena = isAdena && player.getAdena() + item.getCount() <= Integer.MAX_VALUE;
						
						boolean itemDelivered = false;
						
						if (isAdena && Config.AUTO_LOOT_ADENA && canReceiveAdena)
						{
							player.addAdena("Loot", item.getCount(), this, true);
							itemDelivered = true;
						}
						
						if (!itemDelivered && ((!isRaid() && player.isAutoLootEnabled()) || (isRaid() && Config.AUTO_LOOT_RAID) || Config.AUTO_LOOT_HERBS))
						{
							if (player.getInventory().validateCapacity(item.getItemId()))
							{
								player.doAutoLoot(this, item);
								itemDelivered = true;
							}
						}
						
						if (!itemDelivered)
						{
							dropItem(player, item);
						}
						
						if (this instanceof L2Boss)
						{
							broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2).addCharName(this).addItemName(item.getItemId()).addNumber(item.getCount()));
						}
					}
				}
			}
		}
		
		if (isChampion() && Math.abs(getLevel() - player.getLevel()) <= Config.CHAMPION_SPCL_LVL_DIFF && Config.CHAMPION_SPCL_CHANCE > 0 && Rnd.get(100) < Config.CHAMPION_SPCL_CHANCE)
		{
			int champqty = Rnd.get(Config.CHAMPION_SPCL_QTY) + 1;
			RewardItem item = new RewardItem(Config.CHAMPION_SPCL_ITEM, champqty);
			if (player.isAutoLootEnabled())
			{
				player.doAutoLoot(this, item);
			}
			else
			{
				dropItem(player, item);
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateRewardItem(npcTemplate, mainDamageDealer);
			if (rewardItem[0] > 0 && rewardItem[1] > 0)
			{
				RewardItem item = new RewardItem(rewardItem[0], rewardItem[1]);
				if (player.isAutoLootEnabled())
				{
					player.doAutoLoot(this, item);
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (getTemplate().dropHerbs())
		{
			boolean _hp = false;
			boolean _mp = false;
			boolean _spec = false;
			
			int random = Rnd.get(1000);
			if (random < Config.RATE_DROP_SPECIAL_HERBS && !_spec)
			{
				RewardItem item = new RewardItem(8612, 1);
				if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
				{
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				}
				else
				{
					dropItem(player, item);
				}
				_spec = true;
			}
			else
			{
				for (int i = 0; i < 3; i++)
				{
					random = Rnd.get(100);
					if (random < Config.RATE_DROP_COMMON_HERBS)
					{
						RewardItem item = null;
						switch (i)
						{
							case 0:
								item = new RewardItem(8606, 1); // Herb of Power
								break;
							case 1:
								item = new RewardItem(8608, 1); // Herb of Atk. Spd.
								break;
							default:
								item = new RewardItem(8610, 1); // Herb of Critical
								// Attack - Rate
								break;
						}
						if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
						{
							player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
						}
						else
						{
							dropItem(player, item);
						}
						break;
					}
				}
			}
			// mtk - matk type enhance
			random = Rnd.get(1000); // note *10
			if (random < Config.RATE_DROP_SPECIAL_HERBS && !_spec)
			{
				RewardItem item = new RewardItem(8613, 1); // Herb of Mystic
				if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
				{
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				}
				else
				{
					dropItem(player, item);
				}
				_spec = true;
			}
			else
			{
				for (int i = 0; i < 2; i++)
				{
					random = Rnd.get(100);
					if (random < Config.RATE_DROP_COMMON_HERBS)
					{
						RewardItem item = null;
						
						if (i == 0)
						{
							item = new RewardItem(8607, 1); // Herb of Magic
						}
						else
						{
							item = new RewardItem(8609, 1); // Herb of Casting
							// Speed
						}
						
						if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
						{
							player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
						}
						else
						{
							dropItem(player, item);
						}
						break;
					}
				}
			}
			// hp+mp type
			random = Rnd.get(1000); // note *10
			if (random < Config.RATE_DROP_SPECIAL_HERBS && !_spec)
			{
				RewardItem item = new RewardItem(8614, 1); // Herb of Recovery
				if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
				{
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				}
				else
				{
					dropItem(player, item);
				}
				_mp = true;
				_hp = true;
				_spec = true;
			}
			// hp - restore hp type
			if (!_hp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_MP_HP_HERBS)
				{
					RewardItem item = new RewardItem(8600, 1); // Herb of Life
					if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					}
					else
					{
						dropItem(player, item);
					}
					_hp = true;
				}
			}
			if (!_hp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_GREATER_HERBS)
				{
					RewardItem item = new RewardItem(8601, 1); // Greater Herb
					// of Life
					if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					}
					else
					{
						dropItem(player, item);
					}
					_hp = true;
				}
			}
			if (!_hp)
			{
				random = Rnd.get(1000);
				if (random < Config.RATE_DROP_SUPERIOR_HERBS)
				{
					RewardItem item = new RewardItem(8602, 1);
					if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					}
					else
					{
						dropItem(player, item);
					}
				}
			}
			if (!_mp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_MP_HP_HERBS)
				{
					RewardItem item = new RewardItem(8603, 1);
					if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					}
					else
					{
						dropItem(player, item);
					}
					_mp = true;
				}
			}
			if (!_mp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_GREATER_HERBS)
				{
					RewardItem item = new RewardItem(8604, 1);
					if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					}
					else
					{
						dropItem(player, item);
					}
					_mp = true;
				}
			}
			if (!_mp)
			{
				random = Rnd.get(1000);
				if (random < Config.RATE_DROP_SUPERIOR_HERBS)
				{
					RewardItem item = new RewardItem(8605, 1);
					if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					}
					else
					{
						dropItem(player, item);
					}
				}
			}
			
			random = Rnd.get(100);
			if (random < Config.RATE_DROP_COMMON_HERBS)
			{
				RewardItem item = new RewardItem(8611, 1);
				if (player.isAutoLootEnabled() && Config.AUTO_LOOT_HERBS)
				{
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
	}
	
	public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	{
		return dropItem(lastAttacker, new RewardItem(itemId, itemCount));
	}
	
	public L2ItemInstance dropItem(L2PcInstance mainDamageDealer, RewardItem item)
	{
		int randDropLim = 70;
		
		L2ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			// Randomize drop position
			int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			
			int newZ = Math.max(getZ(), mainDamageDealer.getZ()) + 20;
			
			if (ItemTable.getInstance().getTemplate(item.getItemId()) != null)
			{
				ditem = ItemTable.createItem("Loot", item.getItemId(), item.getCount(), mainDamageDealer, this);
				ditem.setRewardId(mainDamageDealer.getObjectId());
				ditem.getDropProtection().protect(mainDamageDealer);
				ditem.dropMe(this, newX, newY, newZ);
				
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
					if (Config.AUTODESTROY_ITEM_AFTER > 0 && ditem.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && ditem.getItemType() == L2EtcItemType.HERB)
					{
						ItemsAutoDestroy.getInstance().addItem(ditem);
					}
				ditem.setProtected(false);
				
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
			else
			{
				_log.error("Item doesn't exist so cannot be dropped. Item ID: " + item.getItemId());
			}
		}
		return ditem;
	}
	
	private void exchangeCrystal(L2PcInstance player, int takeid, int giveid, boolean broke)
	{
		L2ItemInstance Item = player.getInventory().destroyItemByItemId("SoulCrystal", takeid, 1, player, this);
		if (Item != null)
		{
			// Prepare inventory update packet
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addRemovedItem(Item);
			
			// Add new crystal to the killer's inventory
			Item = player.getInventory().addItem("SoulCrystal", giveid, 1, player, this);
			playerIU.addItem(Item);
			
			// Send a sound event and text message to the player
			if (broke)
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
			}
			else
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
			}
			
			// Send system message
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(giveid));
			
			// Send inventory update packet
			player.sendPacket(playerIU);
		}
	}
	
	@Override
	public void firstSpawn()
	{
		super.onSpawn();
		setWalking();
	}
	
	public L2Character[] get2MostHated()
	{
		if (getAggroListRP().isEmpty() || isAlikeDead())
			return null;
		
		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		L2Character[] result = new L2Character[2];
		
		// While iterating over this map removing objects is not allowed
		synchronized (getAggroList())
		{
			// Go through the aggroList of the L2Attackable
			for (AggroInfo ai : getAggroListRP().values())
			{
				if (ai == null)
				{
					continue;
				}
				if (ai._attacker.isAlikeDead() || !getKnownList().knowsObject(ai._attacker) || !ai._attacker.isVisible())
				{
					ai._hate = 0;
				}
				if (ai._hate > maxHate)
				{
					secondMostHated = mostHated;
					mostHated = ai._attacker;
					maxHate = ai._hate;
				}
			}
		}
		result[0] = mostHated;
		if (getAttackByList().contains(secondMostHated))
		{
			result[1] = secondMostHated;
		}
		
		return result;
	}
	
	private int getAbsorbLevel()
	{
		return getTemplate().getAbsorbLevel();
	}
	
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	public final Map<L2Character, AggroInfo> getAggroList()
	{
		return _aggroList;
	}
	
	public final Map<L2Character, AggroInfo> getAggroListRP()
	{
		return _aggroList;
	}
	
	/**
	 * Return the L2Character AI of the L2Attackable and if its null create a new one.<BR>
	 * <BR>
	 */
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2AttackableAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}
	
	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}
	
	public final Map<L2Character, AggroInfo> getDamageContributors()
	{
		return _damageContributors;
	}
	
	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}
	
	public int getHating(L2Character target)
	{
		if (getAggroListRP().isEmpty())
			return 0;
		if (getAggroListRP().get(target) == null)
			return 0;
		
		AggroInfo ai = getAggroListRP().get(target);
		if (ai == null)
			return 0;
		if (ai._attacker instanceof L2PcInstance && (((L2PcInstance) ai._attacker).getAppearance().isInvisible() || ai._attacker.isInvul()))
		{
			getAggroList().remove(target);
			return 0;
		}
		if (!ai._attacker.isVisible())
		{
			getAggroList().remove(target);
			return 0;
		}
		if (ai._attacker.isAlikeDead())
		{
			ai._hate = 0;
			return 0;
		}
		return ai._hate;
	}
	
	@Override
	public AttackableKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new AttackableKnownList(this);
		}
		
		return (AttackableKnownList) _knownList;
	}
	
	public L2Character getMostHated()
	{
		if (getAggroListRP().isEmpty() || isAlikeDead())
			return null;
		
		L2Character mostHated = null;
		int maxHate = 0;
		
		// While iterating over this map removing objects is not allowed
		synchronized (getAggroList())
		{
			// Go through the aggroList of the L2Attackable
			for (AggroInfo ai : getAggroListRP().values())
			{
				if (ai == null)
				{
					continue;
				}
				if (ai._attacker.isAlikeDead() || !getKnownList().knowsObject(ai._attacker) || !ai._attacker.isVisible())
				{
					ai._hate = 0;
				}
				if (ai._hate > maxHate)
				{
					mostHated = ai._attacker;
					maxHate = ai._hate;
				}
			}
		}
		return mostHated;
	}
	
	public synchronized boolean getMustRewardExpSP()
	{
		if (_event != null && _event.isRunning())
			return _event.canGaveExp(this);
		
		return _mustGiveExpSp;
	}
	
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}
	
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	public L2PcInstance getSeeder()
	{
		return _seeder;
	}
	
	public int getSeedType()
	{
		return _seedType;
	}
	
	/**
	 * Check if the server allows Random Animation.<BR>
	 * <BR>
	 */
	// This is located here because L2Monster and L2FriendlyMob both extend this
	// class. The other non-pc instances extend either L2NpcInstance or
	// L2MonsterInstance.
	@Override
	public boolean hasRandomAnimation()
	{
		return Config.MAX_MONSTER_ANIMATION > 0;
	}
	
	public boolean isAbsorbed()
	{
		return _absorbed;
	}
	
	/**
	 * Return true.<BR>
	 * <BR>
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public boolean isMob()
	{
		return true; // This means we use MAX_MONSTER_ANIMATION instead of
		// MAX_NPC_ANIMATION
	}
	
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public boolean isSeeded()
	{
		return _seeded;
	}
	
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}
	
	private void levelSoulCrystals(L2Character attacker)
	{
		if (!(attacker instanceof L2Playable))
		{
			resetAbsorbList();
			return;
		}
		
		int maxAbsorbLevel = getAbsorbLevel();
		int minAbsorbLevel = 0;
		
		if (maxAbsorbLevel == 0)
		{
			resetAbsorbList();
			return;
		}
		if (maxAbsorbLevel > 10)
		{
			minAbsorbLevel = maxAbsorbLevel > 12 ? 12 : 10;
		}
		
		boolean isSuccess = true;
		boolean doLevelup = true;
		boolean isBossMob = maxAbsorbLevel > 10;
		
		L2NpcTemplate.AbsorbCrystalType absorbType = getTemplate().getAbsorbType();
		
		L2PcInstance killer = attacker instanceof L2Summon ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker;
		
		if (!isBossMob)
		{
			if (!isAbsorbed())
			{
				resetAbsorbList();
				return;
			}
			
			AbsorberInfo ai = _absorbersList.get(killer);
			if (ai == null || ai._absorber.getObjectId() != killer.getObjectId())
			{
				isSuccess = false;
			}
			
			if (ai != null && ai._absorbedHP > getMaxHp() / 2.0)
			{
				isSuccess = false;
			}
			
			if (!isSuccess)
			{
				resetAbsorbList();
				return;
			}
		}
		
		String[] crystalNFO = null;
		
		int dice = Rnd.get(100);
		int crystalQTY = 0;
		int crystalLVL = 0;
		int crystalOLD = 0;
		int crystalNEW = 0;
		
		List<L2PcInstance> players;
		
		if (absorbType == L2NpcTemplate.AbsorbCrystalType.FULL_PARTY && killer.isInParty())
		{
			players = killer.getParty().getPartyMembers();
		}
		else if (absorbType == L2NpcTemplate.AbsorbCrystalType.PARTY_ONE_RANDOM && killer.isInParty())
		{
			// This is a naive method for selecting a random member. It gets any
			// random party member and
			// then checks if the member has a valid crystal. It does not select
			// the random party member
			// among those who have crystals, only. However, this might actually
			// be correct (same as retail).
			players = Collections.singletonList(killer.getParty().getPartyMembers().get(Rnd.get(killer.getParty().getMemberCount())));
		}
		else
		{
			players = Collections.singletonList(killer);
		}
		
		for (L2PcInstance player : players)
		{
			if (player == null)
			{
				continue;
			}
			QuestState st = player.getQuestState("350_EnhanceYourWeapon");
			if (st == null)
			{
				continue;
			}
			if (st.getState() != State.STARTED)
			{
				continue;
			}
			
			crystalQTY = 0;
			
			L2ItemInstance[] inv = player.getInventory().getItems();
			for (L2ItemInstance item : inv)
			{
				int itemId = item.getItemId();
				for (int id : SoulCrystal.SoulCrystalTable)
					// Find any of the 39 possible crystals.
					if (id == itemId)
					{
						// Keep count but make sure the player has no more than
						// 1 crystal
						if (++crystalQTY > 1)
						{
							isSuccess = false;
							break;
						}
						
						// Validate if the crystal has already leveled
						if (id != SoulCrystal.RED_NEW_CRYSTAL && id != SoulCrystal.GRN_NEW_CYRSTAL && id != SoulCrystal.BLU_NEW_CRYSTAL)
						{
							try
							{
								if (item.getItem().getName().contains("Stage"))
								{
									// Split the name of the crystal into 'name'
									// & 'level'
									crystalNFO = item.getItem().getName().trim().replace(" Stage ", "").split("-");
									// Get Level
									crystalLVL = Integer.parseInt(crystalNFO[1].trim());
								}
								// Allocate current and levelup ids' for higher
								// level crystals
								if (crystalLVL > 9)
								{
									for (int[] element : SoulCrystal.HighSoulConvert)
										// Get the next stage above 10 using
										// array.
										if (id == element[0])
										{
											crystalNEW = element[1];
											break;
										}
								}
								else
								{
									crystalNEW = id + 1;
								}
							}
							catch (NumberFormatException nfe)
							{
								_log.warn("An attempt to identify a soul crystal failed, " + "verify the names have not changed in etcitem " + "table.", nfe);
								
								player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_ERROR_CONTACT_GM));
								
								isSuccess = false;
								break;
							}
							catch (Exception e)
							{
								_log.warn(e.getMessage(), e);
								isSuccess = false;
								break;
							}
						}
						else
						{
							crystalNEW = id + 1;
						}
						
						// Done
						crystalOLD = id;
						break;
					}
				if (!isSuccess)
				{
					break;
				}
			}
			
			// If the crystal level is way too high for this mob, say that we
			// can't increase it
			if (crystalLVL < minAbsorbLevel || crystalLVL >= maxAbsorbLevel)
			{
				doLevelup = false;
			}
			
			// The player doesn't have any crystals with him get to the next
			// player.
			if (crystalQTY != 1 || !isSuccess || !doLevelup)
			{
				// Too many crystals in inventory.
				if (crystalQTY > 1)
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				}
				else if (!doLevelup && crystalQTY > 0)
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
				}
				
				crystalQTY = 0;
				continue;
			}
			
			int chanceLevelUp = isBossMob ? 70 : SoulCrystal.LEVEL_CHANCE;
			
			// If succeeds or it is a full party absorb, level up the crystal.
			if (absorbType == L2NpcTemplate.AbsorbCrystalType.FULL_PARTY && doLevelup || dice <= chanceLevelUp)
			{
				// Give staged crystal
				exchangeCrystal(player, crystalOLD, crystalNEW, false);
			}
			else
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
			}
		}
	}
	
	public boolean noTarget()
	{
		return getAggroListRP().isEmpty();
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Clear mob spoil,seed
		setSpoil(false);
		// Clear all aggro char from list
		clearAggroList();
		// Clear all damage dealers info from list
		clearDamageContributors();
		// Clear Harvester Rewrard List
		_harvestItems = null;
		// Clear mod Seeded stat
		setSeeded(false);
		
		_sweepItems = null;
		resetAbsorbList();
		
		setWalking();
		
		// check the region where this mob is, do not activate the AI if region
		// is inactive.
		if (!isInActiveRegion())
		{
			getAI().stopAITask();
		}
	}
	
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		// CommandChannel
		if (_commandChannelTimer == null && attacker != null && isRaid() && attacker.isInParty() && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
			_commandChannelTimer = new CommandChannelTimer(this, attacker.getParty().getCommandChannel());
			ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 300000); // 5 min
			_firstCommandChannelAttacked.broadcastToChannelMembers(new CreatureSay(0, SystemChatChannelId.Chat_Party_Room, "", "You have looting rights!"));
		}
		
		// Add damage and hate to the attacker AggroInfo of the L2Attackable
		// _aggroList
		if (attacker != null)
		{
			addDamage(attacker, (int) damage, skill);
		}
		
		// If this L2Attackable is a L2MonsterInstance and it has spawned
		// minions, call its minions to battle
		
		// Reduce the current HP of the L2Attackable and launch the doDie Task
		// if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	public void reduceHate(L2Character target, int amount)
	{
		if (getAI() instanceof L2SiegeGuardAI)
		{
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.IDLE, null, null);
			return;
		}
		if (target == null) // whole aggrolist
		{
			L2Character mostHated = getMostHated();
			if (mostHated == null) // makes target passive for a moment more
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				return;
			}
			
			for (L2Character aggroed : getAggroListRP().keySet())
			{
				AggroInfo ai = getAggroListRP().get(aggroed);
				if (ai == null)
					return;
				ai._hate -= amount;
			}
			
			amount = getHating(mostHated);
			if (amount <= 0)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = getAggroListRP().get(target);
		if (ai == null)
			return;
		ai._hate -= amount;
		
		if (ai._hate <= 0)
			if (getMostHated() == null)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.ACTIVE);
				setWalking();
			}
	}
	
	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}
	
	@Override
	public void returnHome()
	{
		if (isAlikeDead() || isOutOfControl() || isImmobilized() || isInCombat() || isAfraid() || _isMoving || isReturningToSpawnPoint() || getMostHated() != null)
			return;
		
		if (getSpawn() != null)
			if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 1200, false))
			{
				setIsAfraid(false);
				
				clearAggroList();
				getAI().setIntention(CtrlIntention.ACTIVE);
				getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			}
	}
	
	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}
	
	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}
	
	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	public final void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	public synchronized void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}
	
	public void setOverhitValues(L2Character attacker, double damage)
	{
		double overhitDmg = (getStatus().getCurrentHp() - damage) * -1;
		if (overhitDmg < 0)
		{
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}
	
	/**
	 * Sets state of the mob to seeded. Paramets needed to be set before.
	 */
	public void setSeeded()
	{
		if (_seedType != 0 && _seeder != null)
		{
			setSeeded(_seedType, _seeder.getLevel());
		}
	}
	
	public void setSeeded(boolean seeded)
	{
		_seeded = seeded;
	}
	
	/**
	 * @param id
	 * @param seederLvl
	 */
	public void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;
		
		Map<Integer, L2Skill> skills = getTemplate().getSkills();
		
		if (skills != null)
		{
			for (int skillId : skills.keySet())
			{
				switch (skillId)
				{
					case 4303: // Strong type x2
						count *= 2;
						break;
					case 4304: // Strong type x3
						count *= 3;
						break;
					case 4305: // Strong type x4
						count *= 4;
						break;
					case 4306: // Strong type x5
						count *= 5;
						break;
					case 4307: // Strong type x6
						count *= 6;
						break;
					case 4308: // Strong type x7
						count *= 7;
						break;
					case 4309: // Strong type x8
						count *= 8;
						break;
					case 4310: // Strong type x9
						count *= 9;
						break;
				}
			}
		}
		
		int diff = getLevel() - (L2Manor.getInstance().getSeedLevel(_seedType) - 5);
		
		// hi-lvl mobs bonus
		if (diff > 0)
		{
			count += diff;
		}
		
		_harvestItems = new RewardItem[]
		{
			new RewardItem(L2Manor.getInstance().getCropType(_seedType), (int) (count * Config.RATE_DROP_MANOR))
		};
	}
	
	/**
	 * Sets the seed parametrs, but not the seed state
	 * @param id - id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if (!_seeded)
		{
			_seedType = id;
			_seeder = seeder;
		}
	}
	
	public void stopHating(L2Character target)
	{
		if (target == null)
			return;
		AggroInfo ai = getAggroListRP().get(target);
		if (ai == null)
			return;
		ai._hate = 0;
	}
	
	public synchronized RewardItem[] takeHarvest()
	{
		RewardItem[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}
	
	public synchronized RewardItem[] takeSweep()
	{
		RewardItem[] sweep = _sweepItems;
		
		_sweepItems = null;
		
		return sweep;
	}
}