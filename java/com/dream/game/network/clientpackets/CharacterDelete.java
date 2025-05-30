package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.network.serverpackets.CharDeleteFail;
import com.dream.game.network.serverpackets.CharDeleteSuccess;
import com.dream.game.network.serverpackets.CharSelectionInfo;

public class CharacterDelete extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(CharacterDelete.class.getName());
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.info("deleting slot:" + _charSlot);
		}

		try
		{
			byte answer = getClient().markToDeleteChar(_charSlot);

			switch (answer)
			{
				default:
				case -1:
					break;
				case 0:
					sendPacket(CharDeleteSuccess.STATIC_PACKET);
					break;
				case 1:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				case 2:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
			}
		}
		catch (Exception e)
		{
			_log.fatal("Error:", e);
		}

		CharSelectionInfo cl = new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

}