package com.dream.game.model.quest.pack.teleports;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;

public class RaceTrack extends Quest
{
	private static String qn = "1101_teleport_to_race_track";

	public static void main(String[] args)
	{
		new RaceTrack();
	}

	private final int RACE_MANAGER = 30995;
	private final int[] TELEPORTERS =
	{
		30320,
		30256,
		30059,
		30080,
		30899,
		30177,
		30848,
		30233,
		31320,
		31275,
		31964,
		31210
	};

	private final int[][] RETURN_LOCS =
	{
		{
			-80884,
			149770,
			-3040
		},
		{
			-12682,
			122862,
			-3112
		},
		{
			15744,
			142928,
			-2696
		},
		{
			83475,
			147966,
			-3400
		},
		{
			111409,
			219364,
			-3545
		},
		{
			82971,
			53207,
			-1488
		},
		{
			146705,
			25840,
			-2008
		},
		{
			116819,
			76994,
			-2714
		},
		{
			43835,
			-47749,
			-792
		},
		{
			147930,
			-55281,
			-2728
		},
		{
			87386,
			-143246,
			-1293
		},
		{
			12882,
			181053,
			-3560
		}
	};

	public RaceTrack()
	{
		super(1101, qn, "Teleports");
		addTalkId(RACE_MANAGER);
		for (int id : TELEPORTERS)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (contains(TELEPORTERS, npcId))
		{
			player.teleToLocation(12661, 181687, -3560);
			st.setState(State.STARTED);
			int i = 0;
			for (int id : TELEPORTERS)
			{
				if (id == npcId)
				{
					break;
				}
				i++;
			}
			st.set("id", Integer.toString(i));
		}
		else if (npcId == RACE_MANAGER)
			if (st.getState() == State.STARTED && st.getInt("id") >= 0)
			{
				int return_id = st.getInt("id");
				if (return_id < 13)
				{
					player.teleToLocation(RETURN_LOCS[return_id][0], RETURN_LOCS[return_id][1], RETURN_LOCS[return_id][2]);
				}
				else
				{
					player.teleToLocation(12882, 181053, -3560);
				}
				st.exitQuest(true);
			}
		return null;
	}
}