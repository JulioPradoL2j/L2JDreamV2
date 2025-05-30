package com.dream.game.network.clientpackets;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.serverpackets.PartyMemberPosition;

public class CannotMoveAnymore extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
			return;

		if (getClient().getActiveChar().getAI() != null)
		{
			getClient().getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new L2CharPosition(_x, _y, _z, _heading));
		}
		if (getClient().getActiveChar() != null && getClient().getActiveChar().getParty() != null)
		{
			getClient().getActiveChar().getParty().broadcastToPartyMembers(getClient().getActiveChar(), new PartyMemberPosition(getClient().getActiveChar()));
		}
	}

}