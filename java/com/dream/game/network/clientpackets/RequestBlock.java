package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public final class RequestBlock extends L2GameClientPacket
{
	private final static int BLOCK = 0;
	private final static int UNBLOCK = 1;
	private final static int BLOCKLIST = 2;
	private final static int ALLBLOCK = 3;
	private final static int ALLUNBLOCK = 4;
	private int _type;
	private String _name;

	@Override
	protected void readImpl()
	{
		_type = readD();
		if (_type == BLOCK || _type == UNBLOCK)
		{
			_name = readS();
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		switch (_type)
		{
			case BLOCK:
			{
				activeChar.getBlockList().add(_name);
				break;
			}
			case UNBLOCK:
			{
				activeChar.getBlockList().remove(_name);
				break;
			}
			case BLOCKLIST:
			{
				activeChar.getBlockList().sendListToOwner();
				break;
			}
			case ALLBLOCK:
			{
				activeChar.getBlockList().setBlockingAll(true);
				break;
			}
			case ALLUNBLOCK:
			{
				activeChar.getBlockList().setBlockingAll(false);
				break;
			}
		}
	}

}