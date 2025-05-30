package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.ShowMiniMap;

public class RequestShowMiniMap extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected final void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isMiniMapOpen())
		{
			activeChar.setMiniMapOpen(false);
		}
		else
		{
			if (activeChar.isInsideZone(L2Zone.FLAG_NOMAP))
			{
				activeChar.sendMessage("Can't use Minimap Here!");
				return;
			}
			activeChar.setMiniMapOpen(true);
		}
		activeChar.sendPacket(new ShowMiniMap());
	}

}