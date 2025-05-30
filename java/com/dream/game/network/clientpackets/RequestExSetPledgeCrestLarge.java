package com.dream.game.network.clientpackets;

import com.dream.L2DatabaseFactory;
import com.dream.game.cache.CrestCache;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestExSetPledgeCrestLarge.class.getName());
	private int _size;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_size = readD();
		if (_size > 2176)
			return;
		if (_size > 0)
		{
			_data = new byte[_size];
			readB(_data);
		}
	}

	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;

		if (_data == null)
		{
			CrestCache.getInstance().removePledgeCrestLarge(clan.getCrestId());

			clan.setHasCrestLarge(false);
			activeChar.sendMessage("The insignia has been removed.");

			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}

			return;
		}

		if (_size > 2176)
		{
			activeChar.sendMessage("The file must not exceed 2176 bytes.");
			return;
		}

		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (clan.getHasCastle() == 0 && clan.getHasHideout() == 0)
			{
				activeChar.sendMessage("Only a clan that owns a clan hall or castle can have their crest displayed.");
				return;
			}

			CrestCache crestCache = CrestCache.getInstance();

			int newId = IdFactory.getInstance().getNextId();

			if (!crestCache.savePledgeCrestLarge(newId, _data))
			{
				_log.info("Error loading large crest of clan:" + clan.getName());
				return;
			}

			if (clan.hasCrestLarge())
			{
				crestCache.removePledgeCrestLarge(clan.getCrestLargeId());
			}

			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?");
				statement.setInt(1, newId);
				statement.setInt(2, clan.getClanId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warn("could not update the large crest id:" + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			clan.setCrestLargeId(newId);
			clan.setHasCrestLarge(true);

			activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);

			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}

		}
	}

}