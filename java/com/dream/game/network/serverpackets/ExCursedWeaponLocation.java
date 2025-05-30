package com.dream.game.network.serverpackets;

import java.util.List;

import com.dream.game.model.world.Location;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
	public static class CursedWeaponInfo
	{
		public final Location loc;
		public final int id;
		public final int activated;

		public CursedWeaponInfo(Location pLoc, int ID, int status)
		{
			loc = pLoc;
			id = ID;
			activated = status;
		}
	}

	private final List<CursedWeaponInfo> _cursedWeaponInfo;

	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x46);

		if (!_cursedWeaponInfo.isEmpty())
		{
			writeD(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);

				writeD(w.loc.getX());
				writeD(w.loc.getY());
				writeD(w.loc.getZ());
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}

}