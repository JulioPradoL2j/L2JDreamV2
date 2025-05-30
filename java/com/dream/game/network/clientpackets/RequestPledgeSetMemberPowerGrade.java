package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PledgeShowMemberListUpdate;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
	private int _powerGrade;
	private String _member;

	@Override
	protected void readImpl()
	{
		_member = readS();
		_powerGrade = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;

		final L2ClanMember member = clan.getClanMember(_member);
		if (member == null)
			return;

		if (member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
			return;

		member.setPledgeRank(_powerGrade);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member));
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_PRIVILEGE_CHANGED_TO_S2).addString(member.getName()).addNumber(_powerGrade));
	}

}