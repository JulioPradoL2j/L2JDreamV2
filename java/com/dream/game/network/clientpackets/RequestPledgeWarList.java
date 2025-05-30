package com.dream.game.network.clientpackets;

import java.util.List;

import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.PledgeReceiveWarList;

public class RequestPledgeWarList extends L2GameClientPacket
{
	private int _page;
	private int _tab;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_tab = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;

		final List<Integer> list;
		if (_tab == 0)
		{
			list = clan.getWarList();
		}
		else
		{
			list = clan.getAttackerList();

			// The page, reaching the biggest section, should send back to 0.
			_page = Math.max(0, _page > list.size() / 13 ? 0 : _page);
		}

		activeChar.sendPacket(new PledgeReceiveWarList(list, _tab, _page));
	}
}