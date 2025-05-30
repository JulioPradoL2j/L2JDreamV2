package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;

	public ExDuelUpdateUserInfo(L2PcInstance cha)
	{
		_activeChar = cha;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeD(_activeChar.getClassId().getId());
		writeD(_activeChar.getLevel());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}

}