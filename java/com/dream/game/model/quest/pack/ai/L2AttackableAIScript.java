package com.dream.game.model.quest.pack.ai;

import static com.dream.game.ai.CtrlIntention.ATTACK;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RiftInvaderInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.util.Util;

public class L2AttackableAIScript extends Quest
{
	public L2AttackableAIScript(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if (attacker == null)
			return null;

		L2Character originalAttackTarget = isPet ? attacker.getPet() : attacker;
		if (attacker.isInParty() && attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();

			if (caller instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
				return null;
		}

		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (caster == null)
			return null;

		if (!(npc instanceof L2Attackable))
			return null;

		L2Attackable attackable = (L2Attackable) npc;
		int skillAggroPoints = skill.getAggroPoints();

		if (caster.getPet() != null)
			if (targets.length == 1 && Util.contains(targets, caster.getPet()))
			{
				skillAggroPoints = 0;
			}

		if (skillAggroPoints > 0)
			if (attackable.hasAI() && attackable.getAI().getIntention() == ATTACK)
			{
				L2Object npcTarget = attackable.getTarget();
				for (L2Object skillTarget : targets)
					if (npcTarget == skillTarget || npc == skillTarget)
					{
						L2Character originalCaster = isPet ? caster.getPet() : caster;
						attackable.addDamageHate(originalCaster, 0, skillAggroPoints * 150 / (attackable.getLevel() + 7));
					}
			}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	public void registerMobs(int[] mobs)
	{
		for (int id : mobs)
		{
			addEventId(id, Quest.QuestEventType.ON_ATTACK);
			addEventId(id, Quest.QuestEventType.ON_KILL);
			addEventId(id, Quest.QuestEventType.ON_SPAWN);
			addEventId(id, Quest.QuestEventType.ON_SPELL_FINISHED);
			addEventId(id, Quest.QuestEventType.ON_SKILL_SEE);
			addEventId(id, Quest.QuestEventType.ON_FACTION_CALL);
			addEventId(id, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}

}