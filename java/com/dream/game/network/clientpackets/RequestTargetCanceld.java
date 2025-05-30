package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.L2Character;

public class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;

	@Override
	protected void readImpl()
	{
		_unselect = readH();
	}

	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar != null)
			if (_unselect == 0)
			{
				if (activeChar.isCastingNow())
				{
					activeChar.abortCast();
				}
				else if (activeChar.getTarget() != null)
				{
					activeChar.setTarget(null);
				}
			}
			else if (activeChar.getTarget() != null)
			{
				activeChar.setTarget(null);
			}
	}

}