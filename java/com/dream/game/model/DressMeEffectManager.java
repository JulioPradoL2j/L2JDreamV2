package com.dream.game.model;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.DressMeHolder;
import com.dream.game.model.world.L2World;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.util.Broadcast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class DressMeEffectManager
{
	
	private final Map<Integer, ActiveEffect> _activeEffects = new ConcurrentHashMap<>();
	
	private static class ActiveEffect
	{
		private final int _skillId;
		private final ScheduledFuture<?> _task;
		
		public ActiveEffect(int skillId, ScheduledFuture<?> task)
		{
			_skillId = skillId;
			_task = task;
		}
	}
	
	private DressMeEffectManager()
	{
	}
	
	public void startEffect(L2PcInstance player, DressMeHolder skin)
	{
		if (player == null || skin.getEffect() == null)
			return;
		
		final int playerId = player.getObjectId();
		final int skillId = skin.getEffect().getSkillId();
		final int skillLevel = skin.getEffect().getLevel();
		final int interval = skin.getEffect().getInterval(); // em segundos
		
		stopEffect(player); // Cancela qualquer anterior
		
		applySkill(player, skillId, skillLevel); // Aplica imediatamente
		
		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			
			L2PcInstance checkOnplayer = L2World.getInstance().getPlayer(player.getObjectId());
			
			if (checkOnplayer != null)
			{
				applySkill(player, skillId, skillLevel);
			}
			else
			{
				stopEffect(player);
			}
			
		}, interval * 1000L, interval * 1000L);
		
		_activeEffects.put(playerId, new ActiveEffect(skillId, task));
	}
	
	public void stopEffect(L2PcInstance player)
	{
		if (player == null)
			return;
		
		ActiveEffect effect = _activeEffects.remove(player.getObjectId());
		if (effect != null)
		{
			effect._task.cancel(false);
			player.stopSkillEffects(effect._skillId); // Remove o efeito visual
		}
	}
	
	private static void applySkill(L2PcInstance player, int skillId, int skillLevel)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if (skill != null)
		{
			Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, player, skill.getId(), 1, 0, 0, false));
			
		}
	}
	
	public static DressMeEffectManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final DressMeEffectManager INSTANCE = new DressMeEffectManager();
	}
}