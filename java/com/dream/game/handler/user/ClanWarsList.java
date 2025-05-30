package com.dream.game.handler.user;

import com.dream.L2DatabaseFactory;
import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClanWarsList implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		88,
		89,
		90
	};
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
	
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2])
			return false;
		
		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return false;
		}
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			if (id == 88)
			{
				activeChar.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON);
				statement = con.prepareStatement("SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, clan.getClanId());
			}
			else if (id == 89)
			{
				activeChar.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
				statement = con.prepareStatement("SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, clan.getClanId());
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.WAR_LIST);
				statement = con.prepareStatement("SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, clan.getClanId());
			}
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String clanName = rset.getString("clan_name");
				int ally_id = rset.getInt("ally_id");
				if (ally_id > 0)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(clanName).addString(rset.getString("ally_name")));
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(clanName));
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return true;
	}
}