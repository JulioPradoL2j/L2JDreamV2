package com.dream.game.network.clientpackets;

import com.dream.L2DatabaseFactory;
import com.dream.game.cache.CrestCache;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class RequestSetAllyCrest extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestSetAllyCrest.class.getName());
	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 192)
			return;

		_data = new byte[_length];
		readB(_data);
	}

	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}

		if (_length > 192)
		{
			activeChar.sendMessage("The crest file size was too big (max 192 bytes).");
			return;
		}

		if (activeChar.getAllyId() != 0)
		{
			L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());

			if (activeChar.getClanId() != leaderclan.getClanId() || !activeChar.isClanLeader())
			{
				activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
				return;
			}

			CrestCache crestCache = CrestCache.getInstance();

			int newId = IdFactory.getInstance().getNextId();

			if (!crestCache.saveAllyCrest(newId, _data))
			{
				_log.info("Error loading crest of ally:" + leaderclan.getAllyName());
				return;
			}
			if (leaderclan.getAllyCrestId() != 0)
			{
				crestCache.removeAllyCrest(leaderclan.getAllyCrestId());
			}

			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?");
				statement.setInt(1, newId);
				statement.setInt(2, leaderclan.getAllyId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warn("could not update the ally crest id:" + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			for (L2Clan clan : ClanTable.getInstance().getClans())
				if (clan.getAllyId() == activeChar.getAllyId())
				{
					clan.setAllyCrestId(newId);
					for (L2PcInstance member : clan.getOnlineMembers(0))
					{
						member.broadcastUserInfo();
					}
				}
		}
	}

}