package com.dream.game.network.clientpackets;

import com.dream.game.model.ClanInfo;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AllyInfo;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestAllyInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final int allianceId = activeChar.getAllyId();
		if (allianceId > 0)
		{
			final AllyInfo ai = new AllyInfo(allianceId);
			activeChar.sendPacket(new AllyInfo(allianceId));

			activeChar.sendPacket(SystemMessageId.ALLIANCE_INFO_HEAD);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1).addString(ai.getName()));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1).addString(ai.getLeaderC()).addString(ai.getLeaderP()));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2).addNumber(ai.getOnline()).addNumber(ai.getTotal()));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1).addNumber(ai.getAllies().length));

			for (final ClanInfo aci : ai.getAllies())
			{
				activeChar.sendPacket(SystemMessageId.CLAN_INFO_HEAD);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1).addString(aci.getClan().getName()));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1).addString(aci.getClan().getLeaderName()));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1).addNumber(aci.getClan().getLevel()));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2).addNumber(aci.getOnline()).addNumber(aci.getTotal()));
				activeChar.sendPacket(SystemMessageId.CLAN_INFO_SEPARATOR);
			}

			activeChar.sendPacket(SystemMessageId.CLAN_INFO_FOOT);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
		}
	}

}