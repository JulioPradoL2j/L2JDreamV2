package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.serverpackets.KeyPacket;
import com.dream.game.network.serverpackets.L2GameServerPacket;

import org.apache.log4j.Logger;

public final class ProtocolVersion extends L2GameClientPacket
{
	
	private static final Logger _log = Logger.getLogger(ProtocolVersion.class);
	
	private int _version;
	
	@Override
	protected void readImpl()
	{
		_version = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		
		if (_version >= Config.MIN_PROTOCOL_REVISION && _version <= Config.MAX_PROTOCOL_REVISION)
		{
			client.setProtocolOk(true);
			client.setProtocolVer(_version);
			client.sendPacket(new KeyPacket(client.enableCrypt(), 1));
			_log.info("Client accepted with Protocol Version: {" + client.getProtocolVer() + "} Andress: {" + client.getInetAddress() + "}");
		}
		else
		{
			_log.warn("Client rejected. Invalid Protocol Version: {" + _version + "}. Allowed range: {" + Config.MIN_PROTOCOL_REVISION + "} - {" + Config.MIN_PROTOCOL_REVISION + "-" + Config.MAX_PROTOCOL_REVISION + "}");
			
			client.sendPacket(new KeyPacket(client.enableCrypt(), 0));
			client.setProtocolOk(false);
			client.close((L2GameServerPacket) null);
		}
	}
}
