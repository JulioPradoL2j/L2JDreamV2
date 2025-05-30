package com.dream.game.network.serverpackets;

import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.entity.siege.Castle;

public class ExShowCastleInfo extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(ExShowCastleInfo.class.getName());

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x14);
		Map<Integer, Castle> castles = CastleManager.getInstance().getCastles();
		writeD(castles.size());
		for (Castle castle : castles.values())
		{
			writeD(castle.getCastleId());
			if (castle.getOwnerId() > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
				if (owner != null)
				{
					writeS(owner.getName());
				}
				else
				{
					_log.warn("Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!");
					writeS("");
				}
			}
			else
			{
				writeS("");
			}
			writeD(castle.getTaxPercent());
			writeD((int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		}
	}

}