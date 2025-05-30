package com.dream.game.model.quest.pack.ai;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.tools.random.Rnd;

public class Chests extends L2AttackableAIScript
{
	private static final int SKILL_DELUXE_KEY = 2229;

	private static final int BASE_CHANCE = 100;

	private static final int LEVEL_DECREASE = 40;

	private static final int IS_BOX = 40;

	private static final int[] NPC_IDS =
	{
		18265,
		18266,
		18267,
		18268,
		18269,
		18270,
		18271,
		18272,
		18273,
		18274,
		18275,
		18276,
		18277,
		18278,
		18279,
		18280,
		18281,
		18282,
		18283,
		18284,
		18285,
		18286,
		18287,
		18288,
		18289,
		18290,
		18291,
		18292,
		18293,
		18294,
		18295,
		18296,
		18297,
		18298,
		21671,
		21694,
		21717,
		21740,
		21763,
		21786,
		21801,
		21802,
		21803,
		21804,
		21805,
		21806,
		21807,
		21808,
		21809,
		21810,
		21811,
		21812,
		21813,
		21814,
		21815,
		21816,
		21817,
		21818,
		21819,
		21820,
		21821,
		21822
	};

	public static void main(String[] args)
	{

	}

	public Chests()
	{
		super(-1, "chests", "ai");
		registerMobs(NPC_IDS);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc instanceof L2ChestInstance)
		{
			L2ChestInstance chest = (L2ChestInstance) npc;
			int npcId = chest.getNpcId();
			if (!contains(NPC_IDS, npcId))
				return super.onAttack(npc, attacker, damage, isPet);

			if (!chest.isInteracted())
			{
				chest.setInteracted();
				if (Rnd.get(100) < IS_BOX)
				{
					chest.getSpawn().decreaseCount(chest);
					chest.deleteMe();
				}
				else
				{
					L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
					chest.setRunning();
					chest.addDamageHate(originalAttacker, 0, damage * 100 / (chest.getLevel() + 7));
					chest.getAI().setIntention(CtrlIntention.ATTACK, originalAttacker);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc instanceof L2ChestInstance)
		{
			if (!contains(targets, npc))
				return super.onSkillSee(npc, caster, skill, targets, isPet);
			L2ChestInstance chest = (L2ChestInstance) npc;
			int npcId = chest.getNpcId();
			int skillId = skill.getId();
			int skillLevel = skill.getLevel();

			if (!contains(NPC_IDS, npcId))
				return super.onSkillSee(npc, caster, skill, targets, isPet);

			if (!chest.isInteracted())
			{
				chest.setInteracted();
				if (Rnd.get(100) < IS_BOX)
				{
					if (skillId == SKILL_DELUXE_KEY)
					{
						int keyLevelNeeded = chest.getLevel() / 10;
						keyLevelNeeded -= skillLevel;
						if (keyLevelNeeded < 0)
						{
							keyLevelNeeded *= -1;
						}
						int chance = BASE_CHANCE - keyLevelNeeded * LEVEL_DECREASE;

						if (Rnd.get(100) < chance)
						{
							chest.setMustRewardExpSp(false);
							chest.setSpecialDrop();
							chest.reduceCurrentHp(99999999, caster, null);
							return null;
						}
					}
					chest.getSpawn().decreaseCount(chest);
					chest.deleteMe();
				}
				else
				{
					L2Character originalCaster = isPet ? caster.getPet() : caster;
					chest.setRunning();
					chest.addDamageHate(originalCaster, 0, 999);
					chest.getAI().setIntention(CtrlIntention.ATTACK, originalCaster);
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
}