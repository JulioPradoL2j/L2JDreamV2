package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PledgeShowMemberListDelete;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestOustPledgeMember extends L2GameClientPacket
{
	private String _target;

	@Override
	protected void readImpl()
	{
		_target = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_DISMISS) != L2Clan.CP_CL_DISMISS)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (activeChar.getName().equalsIgnoreCase(_target))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF);
			return;
		}

		L2Clan clan = activeChar.getClan();

		L2ClanMember member = clan.getClanMember(_target);
		if (member == null)
			return;

		if (member.isOnline() && member.getPlayerInstance().isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}

		clan.removeClanMember(member.getObjectId(), 0);
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_JOIN_DAYS * 86400000L);
		clan.updateClanInDB();
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(member.getName()));
		activeChar.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER);
		activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);

		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));

		if (member.isOnline())
		{
			L2PcInstance player = member.getPlayerInstance();
			player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}
	}

}