package com.dream.game.network.clientpackets;

import com.dream.game.network.AuthServerThread;
import com.dream.game.network.AuthServerThread.SessionKey;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.serverpackets.L2GameServerPacket;

public class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1, _playKey2, _loginKey1, _loginKey2;


	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}

	@Override
	protected void runImpl()
	{
		if (!getClient().isProtocolOk())
			return;

		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);

		L2GameClient client = getClient();

		if (client.getAccountName() == null)
		{
			client.setAccountName(_loginName);
			AuthServerThread.getInstance().addGameServerLogin(_loginName, client);
			AuthServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
		}
		else
		{
			client.close((L2GameServerPacket) null);
		}
	}

}