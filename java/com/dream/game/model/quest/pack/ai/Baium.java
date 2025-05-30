package com.dream.game.model.quest.pack.ai;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.grandbosses.BaiumManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.tools.random.Rnd;

public class Baium extends Quest
{
	public static String QUEST = "baium";
	private final int BAIUM = 29020;
	private final int ARCHANGEL = 29021;
	private final int STATUE = 29025;
	private final int VORTEX = 31862;

	private final int FABRIC = 4295;

	public Baium()
	{
		super(-1, QUEST, "ai");
		addStartNpc(STATUE);
		addStartNpc(VORTEX);
		addTalkId(STATUE);
		addTalkId(VORTEX);
		addAttackId(BAIUM);
		addAttackId(ARCHANGEL);
		addKillId(BAIUM);
		addSkillSeeId(BAIUM);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == ARCHANGEL)
		{
			if (Rnd.get(100) < 10)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(4132, 1);
				if (skill != null)
				{
					npc.setTarget(attacker);
					npc.doCast(skill);
				}
			}
			if (Rnd.get(100) < 5 && npc.getStatus().getCurrentHp() / npc.getMaxHp() * 100 < 50)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(4133, 1);
				if (skill != null)
				{
					npc.setTarget(npc);
					npc.doCast(skill);
				}
			}
		}

		if (npc.getNpcId() == BAIUM)
		{
			BaiumManager.getInstance()._lastAttackTime = System.currentTimeMillis();
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == BAIUM)
		{
			BaiumManager.getInstance().setCubeSpawn();
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (caster.getZ() < 10055 && BaiumManager.getInstance().checkIfInZone(caster.getTarget()))
		{
			caster.reduceCurrentHp(caster.getMaxHp() + caster.getMaxCp() + 1, npc);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "<html><body>You do not meet the requirements of the NPC.</body></html>";

		QuestState st = player.getQuestState(QUEST);

		if (st == null)
		{
			st = newQuestState(player);
		}

		int npcId = npc.getNpcId();
		if (npcId == STATUE)
		{
			if (st.getInt("ok") == 1 || player.isGM())
			{
				if (!npc.isBusy())
				{
					BaiumManager.getInstance().wakeBaium(player);
					npc.setBusy(true);
					npc.setBusyMessage("Attending another player's request");
					htmltext = "<html><body>You wake up the King are done!</body></html>";
				}
			}
			else
			{
				st.exitQuest(true);
				htmltext = "<html><body>You can't wake up the King are done!</body></html>";
				return htmltext;
			}
		}
		else if (npcId == VORTEX)
			if (BaiumManager.getInstance().isEnableEnterToLair())
			{
				if (player.isFlying())
				{
					htmltext = "<html><body>Angelic Vortex:<br>You can not login in flight.</body></html>";
					return htmltext;
				}
				if (st.getQuestItemsCount(FABRIC) >= 1)
				{
					st.takeItems(FABRIC, 1);
					st.set("ok", "1");
					player.teleToLocation(113100, 14500, 10077);
					htmltext = "<html><body>Angelic Vortex:<br>You have successfully entered the lair are done.</body></html>";
				}
				else
				{
					htmltext = "<html><body>Angelic Vortex:<br>You do not have neobhodyh things.</body></html>";
					return htmltext;
				}
			}
			else
			{
				htmltext = "<html><body>Angelic Vortex:<br>You can not enter at this time.</body></html>";
				return htmltext;
			}
		return htmltext;
	}
}