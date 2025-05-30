package com.dream.game.network.clientpackets;

import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExAskJoinMPCC;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestExAskJoinMPCC extends L2GameClientPacket
{
	private static void askJoinMPCC(L2PcInstance requestor, L2PcInstance target)
	{
		if (!requestor.getParty().isInCommandChannel())
		{
			boolean hasRight = false;
			if (requestor.getClan() != null && requestor.getClan().getLeaderId() == requestor.getObjectId() && requestor.getClan().getLevel() >= 5)
			{
				hasRight = true;
			}
			else
			{
				for (L2Skill skill : requestor.getAllSkills())
					if (skill.getId() == 391)
					{
						hasRight = true;
						break;
					}
			}

			if (!hasRight)
				if (requestor.destroyItemByItemId("MPCC", 8871, 1, requestor, false))
				{
					hasRight = true;
					requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(8871).addNumber(1));
				}

			if (!hasRight)
			{
				requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
				return;
			}
		}

		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			target.getParty().getLeader().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_INVITING_YOU_TO_COMMAND_CHANNEL_CONFIRM).addString(requestor.getName()));
			target.getParty().getLeader().sendPacket(new ExAskJoinMPCC(requestor.getName()));
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
		}
	}

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2PcInstance player = L2World.getInstance().getPlayer(_name);

		if (activeChar == null)
			return;
		if (player == null)
			return;
		if (activeChar.isInParty() && player.isInParty() && activeChar.getParty().equals(player.getParty()))
			return;
		if (activeChar.isInParty())
		{
			L2Party activeParty = activeChar.getParty();
			if (activeParty.getLeader() == activeChar)
			{
				if (activeParty.isInCommandChannel() && activeParty.getCommandChannel().getChannelLeader().equals(activeChar))
				{
					if (player.isInParty())
					{
						if (player.getParty().isInCommandChannel())
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
						}
						else
						{
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
					}
				}
				else if (activeParty.isInCommandChannel() && !activeParty.getCommandChannel().getChannelLeader().equals(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
				}
				else if (player.isInParty())
				{
					if (player.getParty().isInCommandChannel())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
					}
					else
					{
						askJoinMPCC(activeChar, player);
					}
				}
				else
				{
					activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
			}
		}
	}

}