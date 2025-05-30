package com.dream.game.network.serverpackets;

import java.util.List;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private final List<Integer> _cursedWeaponIds;

	public ExCursedWeaponList(List<Integer> cursedWeaponIds)
	{
		_cursedWeaponIds = cursedWeaponIds;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x45);

		writeD(_cursedWeaponIds.size());
		for (Integer i : _cursedWeaponIds)
		{
			writeD(i.intValue());
		}
	}

}