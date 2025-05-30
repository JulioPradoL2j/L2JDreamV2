package com.dream.game.model.quest.pack.ai;

import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.manager.grandbosses.SailrenManager;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.zone.L2BossZone;
import com.dream.game.model.zone.L2Zone;

public class Sailren extends Quest
{
	public static int VELOCIRAPTOR = 22218;
	public static int PTEROSAUR = 22199;
	public static int TYRANNOSAURUS = 22217;
	public static int SAILREN = 29065;
	public static String QUEST = "sailren";
	private final int STATUE = 32109;

	private final int GAZKH = 8784;

	public Sailren()
	{
		super(-1, QUEST, "ai");
		for (L2Spawn s : SpawnTable.getInstance().findAllNpc(TYRANNOSAURUS))
		{
			SpawnTable.getInstance().deleteSpawn(s, true);
		}

		addStartNpc(STATUE);
		addTalkId(STATUE);
		addKillId(VELOCIRAPTOR);
		addKillId(PTEROSAUR);
		addKillId(TYRANNOSAURUS);
		addKillId(SAILREN);

	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2BossZone zone = (L2BossZone) npc.getZone("Boss");
		if (zone == null || zone.getBoss() != L2Zone.Boss.SAILREN)
			return null;
		int npcId = npc.getNpcId();
		if (npcId == VELOCIRAPTOR)
		{
			SailrenManager.getInstance().setSailrenSpawnTask(PTEROSAUR);
		}
		else if (npcId == PTEROSAUR)
		{
			SailrenManager.getInstance().setSailrenSpawnTask(TYRANNOSAURUS);
		}
		else if (npcId == TYRANNOSAURUS)
		{
			SailrenManager.getInstance().setSailrenSpawnTask(SAILREN);
		}
		else if (npcId == SAILREN)
		{
			SailrenManager.getInstance().setCubeSpawn();
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState("sailren");
		if (st == null)
			return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		int npcId = npc.getNpcId();
		if (npcId == STATUE)
			if (st.getQuestItemsCount(GAZKH) != 0)
			{
				int ENTRY_SATAT = SailrenManager.getInstance().canIntoSailrenLair(player);
				if (ENTRY_SATAT == 1 || ENTRY_SATAT == 2)
				{
					st.exitQuest(true);
					return "<html><body>Shilen's Stone Statue:<br>Another adventurers have already fought against the sailren. Do not obstruct them.</body></html>";
				}
				else if (ENTRY_SATAT == 3)
				{
					st.exitQuest(true);
					return "<html><body>Shilen's Stone Statue:<br>The sailren is very powerful now. It is not possible to enter the inside.</body></html>";
				}
				else if (ENTRY_SATAT == 4)
				{
					st.exitQuest(true);
					return "<html><body>Shilen's Stone Statue:<br>You seal the sailren alone? You should not do so! Bring the companion.</body></html>";
				}
				else if (ENTRY_SATAT == 0)
				{
					st.takeItems(GAZKH, 1);
					SailrenManager.getInstance().entryToSailrenLair(player);
					return "<html><body>Shilen's Stone Statue:<br>Please seal the sailren by your ability.</body></html>";
				}
			}
			else
			{
				st.exitQuest(true);
				return "<html><body>Shilen's Stone Statue:<br><font color=\"LEVEL\">Gazkh</font> is necessary for seal the sailren.</body></html>";
			}
		return null;
	}
}
