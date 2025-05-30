package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		List<String> manorsName = new ArrayList<>();
		manorsName.add("gludio");
		manorsName.add("dion");
		manorsName.add("giran");
		manorsName.add("oren");
		manorsName.add("aden");
		manorsName.add("innadril");
		manorsName.add("goddard");
		manorsName.add("rune");
		manorsName.add("schuttgart");
		ExSendManorList manorlist = new ExSendManorList(manorsName);
		player.sendPacket(manorlist);
	}

}