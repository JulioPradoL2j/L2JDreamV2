package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.model.BlockList;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.AskJoinParty;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestJoinParty extends L2GameClientPacket
{
	private static void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		if (requestor.getParty().getMemberCount() + requestor.getParty().getPendingInvitationNumber() >= 9)
		{
			requestor.sendPacket(SystemMessageId.PARTY_FULL);
			return;
		}
		if (!requestor.getParty().isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
			return;
		}
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), requestor.getParty().getLootDistribution()));
			requestor.getParty().increasePendingInvitationNumber();
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addString(target.getName()));
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
		}
	}

	private String _name;

	private int _itemDistribution;

	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));

			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().increasePendingInvitationNumber();
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addString(target.getName()));
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
		}
	}

	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_name);

		if (requestor == null)
			return;
		if (target == null || target.isGM() && target.getAppearance().isInvisible() && !requestor.isGM())
		{
			requestor.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		if (target.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			requestor.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		if (requestor.getSecondRefusal() || target.getSecondRefusal())
		{
			requestor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (BlockList.isBlocked(target, requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addCharName(target));
			return;
		}
		if (target.isInParty())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY).addString(target.getName()));
			return;
		}
		if (target.isOfflineTrade())
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if (target.isPartyInvProt())
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if (Config.BLOCK_PARTY_INVITE_ON_COMBAT)
		{
		if (requestor.isInCombat() || target.isInCombat())
			{
				requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
				requestor.sendMessage("Dear " + requestor.getName() + " you cannot create a party or enter in a party on combat mode.");
				return;
			}
		}
		if (target == requestor)
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if (target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if ((requestor._event != null) && !requestor._event.canInteract(requestor, target))
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		if (target.isInJail() || requestor.isInJail())
		{
			requestor.sendMessage("The player you tried to invite is currently jailed.");
			return;
		}
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
			return;
		if (target.isInDuel() || requestor.isInDuel())
			return;
		if (!requestor.isInParty())
		{
			createNewParty(target, requestor);
		}
		else if (requestor.getParty().isInDimensionalRift())
		{
			requestor.sendMessage("The player is in Dimensional Rift, the invitation is not possible.");
		}
		else
		{
			addTargetToParty(target, requestor);
		}
	}

}