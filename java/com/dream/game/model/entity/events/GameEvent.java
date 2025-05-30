package com.dream.game.model.entity.events;

import org.apache.log4j.Logger;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public abstract class GameEvent
{
	public static interface IGameEventScript
	{
		public void onFinish(int instanceId);

		public void onStart(int instanceId);
	}

	protected static Logger _log = Logger.getLogger("Events");
	public static final int STATE_INACTIVE = 0;
	public static final int STATE_ACTIVE = 1;

	public static final int STATE_RUNNING = 2;

	protected IGameEventScript _eventScript;

	public boolean canAttack(L2Character attacker, L2Character target)
	{
		return true;
	}

	public boolean canBeSkillTarget(L2Character caster, L2Character target, L2Skill skill)
	{
		return true;
	}

	public boolean canDoAction(L2PcInstance player, int action)
	{
		return true;
	}

	public boolean canDropItems(L2Attackable victim, L2PcInstance killer)
	{
		return true;
	}

	public boolean canGaveExp(L2Attackable victim)
	{
		return true;
	}

	public boolean canInteract(L2Character actor, L2Character target)
	{
		return true;
	}

	public boolean canLogout(L2PcInstance player)
	{
		return false;
	}

	public boolean canLostExpOnDie()
	{
		return getState() != STATE_RUNNING;
	}

	public boolean canRegister(L2PcInstance player)
	{
		return getState() == STATE_ACTIVE && !isParticipant(player) && player._event == null;
	}

	public boolean canTeleportOnDie(L2PcInstance player)
	{
		return getState() != STATE_RUNNING;
	}

	public boolean canUseItem(L2Character actor, L2ItemInstance item)
	{
		return true;
	}

	public boolean canUseSkill(L2Character caster, L2Skill skill)
	{
		return true;
	}

	abstract public boolean finish();

	public int getCharNameColor(L2PcInstance cha, L2PcInstance other)
	{
		return cha.getNameColor();
	}

	public int getCharTitleColor(L2PcInstance cha, L2PcInstance other)
	{
		return cha.getTitleColor();
	}

	abstract public String getName();

	public String getName(L2PcInstance cha, L2PcInstance other)
	{
		return cha.getName();
	}

	public int getRegistredPlayersCount()
	{
		return 0;
	}

	abstract public int getState();

	public String getTitle(L2PcInstance cha, L2PcInstance other)
	{
		return cha.getTitle();
	}

	abstract public boolean isParticipant(L2PcInstance player);

	public boolean isRunning()
	{
		return getState() == STATE_RUNNING;
	}

	abstract public boolean load();

	abstract public void onCommand(L2PcInstance actor, String command, String params);

	abstract public void onKill(L2Character killer, L2Character victim);

	public void onLogin(L2PcInstance player)
	{

	}

	public void onLogout(L2PcInstance player)
	{

	}

	abstract public boolean onNPCTalk(L2Npc l2Npc, L2PcInstance talker);

	public void onRevive(L2Character actor)
	{

	}

	public void onSkillHit(L2Character caster, L2Character target, L2Skill skill)
	{

	}

	abstract public boolean register(L2PcInstance player);

	abstract public void remove(L2PcInstance player);

	public boolean requestRevive(L2PcInstance cha, int _requestedPointType)
	{
		return false;
	}

	public void setEventScript(IGameEventScript script)
	{
		_eventScript = script;
	}

	abstract public boolean start();
}
