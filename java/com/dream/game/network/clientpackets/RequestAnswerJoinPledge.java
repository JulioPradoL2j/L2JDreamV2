package com.dream.game.network.clientpackets;

import com.dream.game.manager.CastleManager;
import com.dream.game.manager.FortManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Clan.SubPledge;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.JoinPledge;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.PledgeShowMemberListAdd;
import com.dream.game.network.serverpackets.PledgeShowMemberListAll;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		L2PcInstance requestor = activeChar.getRequest().getPartner();
		if (requestor == null)
			return;
		if (_answer == 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION).addPcName(requestor));
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION).addPcName(activeChar));
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
				return;

			RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			L2Clan clan = requestor.getClan();
			if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType()))
			{
				activeChar.sendPacket(new JoinPledge(requestor.getClanId()));

				activeChar.setPledgeType(requestPacket.getPledgeType());
				if (requestPacket.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
				{
					activeChar.setPledgeRank(9);
					activeChar.setLvlJoinedAcademy(activeChar.getLevel());
				}
				else
				{
					activeChar.setPledgeRank(5);
				}

				clan.addClanMember(activeChar);
				activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPledgeRank()));
				activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);

				if (activeChar.getClan().getHasFort() > 0)
				{
					FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
				}
				if (activeChar.getClan().getHasCastle() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
				}
				activeChar.sendSkillList();

				clan.broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addPcName(activeChar), activeChar);
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				activeChar.sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), 0));
				for (SubPledge sp : activeChar.getClan().getAllSubPledges())
				{
					activeChar.sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), sp.getId()));
				}
				activeChar.setClanJoinExpiryTime(0);
				activeChar.broadcastUserInfo();
			}
		}
		activeChar.getRequest().onRequestResponse();
	}

}