package com.dream.game.network.clientpackets;

import com.dream.game.network.serverpackets.CharSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		try
		{
			getClient().markRestoredChar(_charSlot);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		CharSelectionInfo cl = new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

}