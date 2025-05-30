package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.Disconnection;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.L2GameClient.GameClientState;
import com.dream.game.network.serverpackets.CharSelected;
import com.dream.game.network.serverpackets.SSQInfo;

import org.apache.log4j.Logger;

public class CharacterSelect extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(CharacterSelect.class.getName());
	
	private int _charSlot;
	@SuppressWarnings("unused")
	private int _unk1, _unk2, _unk3, _unk4;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (client.getActiveCharLock().tryLock())
		{
			try
			{
				if (client.getActiveChar() == null)
				{
					final L2PcInstance cha = client.loadCharFromDisk(_charSlot);
					if (cha == null)
						return;
					
					if (cha.isBanned())
					{
						new Disconnection(getClient(), cha).defaultSequence(false);
						return;
					}
					
					cha.setClient(client);
					client.setActiveChar(cha);
					cha.setOnlineStatus(true);
					sendPacket(new SSQInfo());
					
					client.setState(GameClientState.IN_GAME);
					
					sendPacket(new CharSelected(cha, client.getSessionId().playOkID1));
				}
			}
			finally
			{
				client.getActiveCharLock().unlock();
			}
		}
	}
	
}