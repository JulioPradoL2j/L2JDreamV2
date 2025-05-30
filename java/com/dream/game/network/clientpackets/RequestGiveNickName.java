package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestGiveNickName extends L2GameClientPacket
{
	private String _target, _title;

	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		// Noblesse can bestow a title to themselves
		if (activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(SystemMessageId.TITLE_CHANGED);
			activeChar.broadcastTitleInfo();
		}
		else
		{
			// Can the player change/give a title?
			if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) != L2Clan.CP_CL_GIVE_TITLE)
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			if (activeChar.getClan().getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
				return;
			}

			final L2ClanMember member = activeChar.getClan().getClanMember(_target);
			if (member != null)
			{
				final L2PcInstance playerMember = member.getPlayerInstance();
				if (playerMember != null)
				{
					playerMember.setTitle(_title);

					playerMember.sendPacket(SystemMessageId.TITLE_CHANGED);
					if (activeChar != playerMember)
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_TITLE_CHANGED_TO_S2).addPcName(playerMember).addString(_title));

					playerMember.broadcastTitleInfo();
				}
				else
					activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			}
			else
				activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
		}
	}

}