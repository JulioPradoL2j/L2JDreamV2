package com.dream.game.model;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.geodata.GeoEngine;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExAutoSoulShot;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class L2FarmPlayableAI
{
	private static final Logger _log = Logger.getLogger(L2FarmPlayableAI.class.getName());
	private ScheduledFuture<?> _task;
	private static final int FARM_RADIUS = 1200;
	
	public L2FarmPlayableAI()
	{
		_log.info("Loaded {1} Auto Hunt task.");
		start();
	}
	
	public void start()
	{
		if (_task == null)
		{
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> players(), 500, 500);
			
		}
	}
	
	private void players()
	{
		for (L2PcInstance players : L2World.getInstance().getAllPlayers())
		{
			if (players != null)
			{
				if (players.isAutoFarm())
				{
					fuctions(players);
				}
				
			}
		}
	}
	
	private void fuctions(L2PcInstance player)
	{
		
		if (!player.isDead())
		{
			if (player.isMoving() || player.isCastingNow() || player.isAttackingNow() || player.isFakeDeath() || player.isSitting() || player.inObserverMode())
				return;
			
			L2MonsterInstance newTarget = selectTarget(player);
			
			if (newTarget != null)
			{
				if (!(player.getCurrentMp() < (player.getMaxMp() * 0.1)))
				{
					attackTarget(player, newTarget);
				}
			}
		}
	}
	
	public void attackTarget(L2PcInstance player, L2MonsterInstance target)
	{
		if (player == null || target == null || player.isDead() || target.isDead())
			return;
		
		if (player.isMoving() || player.isCastingNow() || player.isAttackingNow() || player.isFakeDeath() || player.isSitting() || player.inObserverMode())
			return;
		
		if (!GeoEngine.getInstance().canSeeTarget(player, target))
			return;
		
		if (target.getAutoFarmOwner() == null || target.getAutoFarmOwner() == player)
			target.setAutoFarmOwner(player);
		
		L2ShortCut[] shortcuts = player.getAllShortCuts();
		List<L2ShortCut> offensiveSkills = new ArrayList<>();
		
		// Buff ou auto cura se HP < 50% (F11/F12)
		for (L2ShortCut sc : shortcuts)
		{
			if (sc.getPage() != 0)
				continue;
			
			int slot = sc.getSlot();
			int type = sc.getType();
			
			if ((slot == 11 || slot == 12) && type == L2ShortCut.TYPE_SKILL && player.getCurrentHp() < (player.getMaxHp() * 0.5))
			{
				L2Skill skill = player.getKnownSkill(sc.getId());
				if (skill != null && !player.isSkillDisabled(skill.getId()) && (skill.getTargetType() == SkillTargetType.TARGET_SELF || skill.getTargetType() == SkillTargetType.TARGET_ONE))
				{
					player.useMagic(skill, true, false);
					player.sendPacket(new ExAutoSoulShot(skill.getId(), 1));
					return;
				}
			}
			
			// Ativa toggle (F8)
			if (slot == 8 && type == L2ShortCut.TYPE_SKILL)
			{
				L2Skill skill = player.getKnownSkill(sc.getId());
				if (skill != null && skill.isToggle() && player.getFirstEffect(skill) == null)
				{
					player.useMagic(skill, true, false);
					player.sendPacket(new ExAutoSoulShot(skill.getId(), 1));
					return;
				}
			}
			
			// Skills ofensivas (F1 a F3)
			if (slot >= 0 && slot <= 3 && type == L2ShortCut.TYPE_SKILL)
			{
				offensiveSkills.add(sc);
			}
		}
		
		int currentIndex = player.getScriptValue(1000);
		if (!offensiveSkills.isEmpty())
		{
			if (currentIndex >= offensiveSkills.size())
				currentIndex = 0;
			
			L2ShortCut selected = offensiveSkills.get(currentIndex);
			L2Skill skill = player.getKnownSkill(selected.getId());
			if (skill != null && !player.isSkillDisabled(skill.getId()) && skill.checkCondition(player, target))
			{
				player.useMagic(skill, true, false);
				player.setScriptValue(1000, currentIndex + 1);
				player.sendPacket(new ExAutoSoulShot(skill.getId(), 1));
				return;
			}
			
			player.setScriptValue(1000, currentIndex + 1);
		}
		else
		{
			player.setScriptValue(1000, 0);
		}
		
		for (L2ShortCut sc : shortcuts)
		{
			if (sc.getPage() != 0 || sc.getSlot() != 0)
				continue;
			
			if (sc.getType() == L2ShortCut.TYPE_ACTION && sc.getId() == 2)
			{
				if (!target.isAttackable() && !player.allowPeaceAttack())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (player.isConfused())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (!GeoEngine.getInstance().canSeeTarget(player, target))
				{
					player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (GeoEngine.getInstance().canSeeTarget(player, target) && target.isAttackable())
				{
					if (!player.isAttackingNow() && !player.isCastingNow())
					{
						if (!player.isMageClass())
							player.getAI().setIntention(CtrlIntention.ATTACK, target);
						
						player.sendPacket(new ExAutoSoulShot(2, 1));
						
						if (player.isMageClass())
							player.abortAttack();
					}
				}
				
				return;
			}
		}
	}
	
	private L2MonsterInstance selectTarget(L2PcInstance player)
	{
		L2MonsterInstance monster = findCreature(player);
		
		if (!(player.getCurrentMp() < (player.getMaxMp() * 0.1)))
		{
			if (monster != null)
			{
				
				player.setTarget(monster);
				
				if (monster.isAutoAttackable(player))
				{
					player.sendPacket(new MyTargetSelected(monster.getObjectId(), player.getLevel() - monster.getLevel()));
					
					StatusUpdate su = new StatusUpdate(monster);
					su.addAttribute(StatusUpdate.CUR_HP, (int) monster.getStatus().getCurrentHp());
					su.addAttribute(StatusUpdate.MAX_HP, monster.getMaxHp());
					player.sendPacket(su);
				}
				else
				{
					player.sendPacket(new MyTargetSelected(monster.getObjectId(), 0));
				}
				
				player.sendPacket(new ValidateLocation(monster));
				if (!player.isMageClass())
					player.getAI().setIntention(CtrlIntention.FOLLOW, monster);
				
			}
		}
		
		return monster;
	}
	
	public L2MonsterInstance findCreature(L2PcInstance player)
	{
		L2MonsterInstance closestMonster = null;
		double closestDistance = Double.MAX_VALUE;
		
		for (L2MonsterInstance toTest : L2World.getAroundMonsters(player, 1200, 5))
		{
			if (toTest == null || toTest.isDead() || toTest.isRaid() || toTest instanceof L2ChestInstance)
				continue;
			
			if (toTest.getAutoFarmOwner() != null && toTest.getAutoFarmOwner() != player)
				continue;
			
			if (!GeoEngine.getInstance().canSeeTarget(player, toTest))
				continue;
			
			double distance = Util.calculateDistance(player, toTest, false);
			if (distance < FARM_RADIUS && distance < closestDistance)
			{
				closestDistance = distance;
				closestMonster = toTest;
			}
		}
		return closestMonster;
	}
	
	public void stop()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		
	}
	
	public boolean running()
	{
		return _task != null;
	}
	
	public static L2FarmPlayableAI getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final L2FarmPlayableAI INSTANCE = new L2FarmPlayableAI();
	}
}
