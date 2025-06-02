package com.dream.game.model;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.geodata.GeoEngine;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.util.Util;

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
			_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> players(), 500, 250);
			
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
			
			L2MonsterInstance newTarget = selectTarget(player);
			
			if (newTarget != null)
			{
				if (!(player.getCurrentMp() < (player.getMaxMp() * 0.1)))
					attackTarget(player, newTarget);
			}
		}
	}
	
	public void attackTarget(L2PcInstance player, L2MonsterInstance target)
	{
		if (player == null || target == null || player.isDead() || target.isDead())
			return;
		
		if (player.isCastingNow() || player.isAttackingNow() || player.isFakeDeath() || player.isSitting() || player.inObserverMode())
			return;
		
		if (!GeoEngine.getInstance().canSeeTarget(player, target))
			return;
		
		L2ShortCut[] shortcuts = player.getAllShortCuts();
		
		for (L2ShortCut sc : shortcuts)
		{
			if (sc.getPage() != 0)
				continue;
			
			int slot = sc.getSlot();
			int type = sc.getType();
			
			// Auto cura/buff se HP abaixo de 50% - F11 e F12
			if ((slot == 11 || slot == 12) && type == L2ShortCut.TYPE_SKILL && player.getCurrentHp() < (player.getMaxHp() * 0.5))
			{
				L2Skill skill = player.getKnownSkill(sc.getId());
				if (skill != null && !player.isSkillDisabled(skill.getId()) && (skill.getTargetType() == SkillTargetType.TARGET_SELF || skill.getTargetType() == SkillTargetType.TARGET_ONE))
				{
					player.useMagic(skill, true, false);
					return;
				}
			}
			
			// Toggle skill - F8
			if (slot == 8 && type == L2ShortCut.TYPE_SKILL)
			{
				L2Skill skill = player.getKnownSkill(sc.getId());
				if (skill != null && skill.isToggle() && player.getFirstEffect(skill) == null)
				{
					player.useMagic(skill, true, false);
					return;
				}
			}
			
			// Skills ofensivas - F2 a F3
			if (slot >= 1 && slot <= 3 && type == L2ShortCut.TYPE_SKILL)
			{
				L2Skill skill = player.getKnownSkill(sc.getId());
				if (skill != null && !player.isSkillDisabled(skill.getId()) && skill.checkCondition(player, target))
				{
					player.useMagic(skill, true, false);
					return;
				}
			}
			
			// F1 (slot 0): skill ofensiva mágica OU ataque físico
			if (slot == 0)
			{
				if (player.isMageClass())
				{
					// Magos só usam skill, nunca ataque físico
					if (type == L2ShortCut.TYPE_SKILL)
					{
						L2Skill skill = player.getKnownSkill(sc.getId());
						if (skill != null && !player.isSkillDisabled(skill.getId()) && skill.checkCondition(player, target))
						{
							player.useMagic(skill, true, false);
							return;
						}
					}
				}
				else
				{
					// Fighters atacam fisicamente se for o slot de ataque básico (action id 2)
					if (type == L2ShortCut.TYPE_ACTION && sc.getId() == 2)
					{
						target.onForcedAttack(player);
						return;
					}
				}
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
