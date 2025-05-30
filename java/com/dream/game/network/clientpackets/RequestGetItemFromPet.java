package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.Util;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());
	private int _objectId;
	private int _amount;
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
		_unknown = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || !(player.getPet() instanceof L2PetInstance))
			return;
		L2PetInstance pet = (L2PetInstance) player.getPet();

		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit And Got Banned!", Config.DEFAULT_PUNISH);
			return;
		}
		if (_amount < 0)
		{
			Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] count < 0! ban! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if (_amount == 0)
			return;

		if (player.getDistanceSq(pet) > 40000)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_TOO_FAR));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			_log.info("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}

}