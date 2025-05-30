package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.L2FriendSay;

public class RequestSendFriendMsg extends L2GameClientPacket
{
	private static Logger _logChat = Logger.getLogger("chat");
	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		_message = readS();
		_reciever = readS();
	}

	@Override
	protected void runImpl()
	{
		if (_message == null || _message.isEmpty() || _message.length() > 300)
			return;

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);

		if (targetPlayer == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		if (Config.LOG_CHAT)
		{
			_logChat.info("PRIV_MSG" + "[" + activeChar.getName() + " to " + _reciever + "]" + _message);
		}

		targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _reciever, _message));
	}

}