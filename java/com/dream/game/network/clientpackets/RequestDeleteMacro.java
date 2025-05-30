package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ConfirmDlg;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestDeleteMacro extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		new ConfirmDlg(SystemMessageId.WISH_TO_DELETE_MACRO.getId());
		player.deleteMacro(_id);
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_D_S2).addString("Delete macro id=" + _id));
	}

}