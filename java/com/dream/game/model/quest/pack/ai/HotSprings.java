package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.tools.random.Rnd;

public class HotSprings extends Quest
{
	private final int NPC[] =
	{
		21314,
		21316,
		21317,
		21319,
		21321,
		21322
	};
	private final List<Integer> LIST_MOB_CHOLERA = new ArrayList<>();
	private final List<Integer> LIST_MOB_FLU = new ArrayList<>();
	private final List<Integer> LIST_MOB_MALARIA = new ArrayList<>();
	private final List<Integer> LIST_MOB_RHEUMATISM = new ArrayList<>();

	public HotSprings()
	{
		super(-1, "HotSprings", "ai");
		for (int id : NPC)
		{
			addAttackId(id);
		}
		addMobs();
	}

	private void addMobs()
	{
		LIST_MOB_RHEUMATISM.add(21314);
		LIST_MOB_RHEUMATISM.add(21321);
		LIST_MOB_MALARIA.add(21314);
		LIST_MOB_MALARIA.add(21316);
		LIST_MOB_MALARIA.add(21317);
		LIST_MOB_MALARIA.add(21319);
		LIST_MOB_MALARIA.add(21321);
		LIST_MOB_MALARIA.add(21322);
		LIST_MOB_CHOLERA.add(21316);
		LIST_MOB_CHOLERA.add(21319);
		LIST_MOB_FLU.add(21317);
		LIST_MOB_FLU.add(21322);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (LIST_MOB_RHEUMATISM.contains(npcId))
			if (Rnd.get(100) < Config.HS_DEBUFF_CHANCE)
				if (player.getFirstEffect(4551) != null)
				{
					int rheumatism = player.getFirstEffect(4551).getLevel();
					if (Rnd.get(100) < 50)
						if (rheumatism < 10)
						{
							int lvl = rheumatism + 1;
							npc.setTarget(player);
							player.stopSkillId(4551);
							SkillTable.getInstance().getInfo(4551, lvl).getEffects(npc, player);
						}
						else
						{
							npc.setTarget(player);
							player.stopSkillId(4551);
							SkillTable.getInstance().getInfo(4551, 1).getEffects(npc, player);
						}
				}
				else
				{
					npc.setTarget(player);
					SkillTable.getInstance().getInfo(4551, 1).getEffects(npc, player);
				}
		if (LIST_MOB_CHOLERA.contains(npcId))
			if (Rnd.get(100) < Config.HS_DEBUFF_CHANCE)
				if (player.getFirstEffect(4552) != null)
				{
					int cholera = player.getFirstEffect(4552).getLevel();
					if (Rnd.get(100) < 50)
						if (cholera < 10)
						{
							int lvl = cholera + 1;
							npc.setTarget(player);
							player.stopSkillId(4552);
							SkillTable.getInstance().getInfo(4552, lvl).getEffects(npc, player);
						}
						else
						{
							npc.setTarget(player);
							player.stopSkillId(4552);
							SkillTable.getInstance().getInfo(4552, 1).getEffects(npc, player);
						}
				}
				else
				{
					npc.setTarget(player);
					SkillTable.getInstance().getInfo(4552, 1).getEffects(npc, player);
				}
		if (LIST_MOB_FLU.contains(npcId))
			if (Rnd.get(100) < Config.HS_DEBUFF_CHANCE)
				if (player.getFirstEffect(4553) != null)
				{
					int flu = player.getFirstEffect(4553).getLevel();
					if (Rnd.get(100) < 50)
						if (flu < 10)
						{
							int lvl = flu + 1;
							npc.setTarget(player);
							player.stopSkillId(4553);
							SkillTable.getInstance().getInfo(4553, lvl).getEffects(npc, player);
						}
						else
						{
							npc.setTarget(player);
							player.stopSkillId(4553);
							SkillTable.getInstance().getInfo(4553, 1).getEffects(npc, player);
						}
				}
				else
				{
					npc.setTarget(player);
					SkillTable.getInstance().getInfo(4553, 1).getEffects(npc, player);
				}
		if (LIST_MOB_MALARIA.contains(npcId))
			if (Rnd.get(100) < Config.HS_DEBUFF_CHANCE)
				if (player.getFirstEffect(4554) != null)
				{
					int malaria = player.getFirstEffect(4554).getLevel();
					if (Rnd.get(100) < 50)
						if (malaria < 10)
						{
							int lvl = malaria + 1;
							npc.setTarget(player);
							player.stopSkillId(4554);
							SkillTable.getInstance().getInfo(4554, lvl).getEffects(npc, player);
						}
						else
						{
							npc.setTarget(player);
							player.stopSkillId(4554);
							SkillTable.getInstance().getInfo(4554, 1).getEffects(npc, player);
						}
				}
				else
				{
					npc.setTarget(player);
					SkillTable.getInstance().getInfo(4554, 1).getEffects(npc, player);
				}
		return super.onAttack(npc, player, damage, isPet);
	}
}