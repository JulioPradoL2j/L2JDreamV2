package com.dream.game.model.quest.pack.custom;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.ActionFailed;

public class NonTalkingNpcs extends Quest
{
	public static void main(String[] args)
	{
		new NonTalkingNpcs();
	}

	private final int NPCs[] =
	{
		31557,
		31606,
		31671,
		31672,
		31673,
		31674,
		32026,
		32030,
		32031,
		32032,
		32038
	};

	public NonTalkingNpcs()
	{
		super(-1, "1000_NonTalkingNpcs", "custom");
		for (int id : NPCs)
		{
			addFirstTalkId(id);
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return null;
	}
}