package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private int _isMemberSelected;
	private String _memberName;
	private int _newPledgeType;
	private String _selectedMember;

	private L2ClanMember member2;

	@Override
	protected void readImpl()
	{
		_isMemberSelected = readD();
		_memberName = readS();
		_newPledgeType = readD();
		_selectedMember = readS();
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

		member2 = null;
		L2ClanMember member1 = clan.getClanMember(_memberName);

		if (_isMemberSelected == 0)
		{
			if (clan.getSubPledgeMembersCount(_newPledgeType) >= clan.getMaxNrOfMembers(_newPledgeType))
			{
				if (_newPledgeType == 0)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL).addString(clan.getName()));
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
				}
				return;
			}
			if (member1 == null)
				return;
		}
		else
		{
			member2 = clan.getClanMember(_selectedMember);
			if (member1 == null || member2 == null)
				return;
		}

		int oldPledgeType = member1.getPledgeType();
		if (oldPledgeType == -1 || _newPledgeType == -1)
			return;
		if (oldPledgeType == _newPledgeType)
			return;

		member1.setSubPledgeType(_newPledgeType);
		if (_isMemberSelected != 0)
		{
			member2.setSubPledgeType(oldPledgeType);
		}
		clan.broadcastClanStatus();
	}

}