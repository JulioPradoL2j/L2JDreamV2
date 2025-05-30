package com.dream.game.network.clientpackets;

import java.util.Arrays;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.CTF.CTF;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;

	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		activeChar._inWorld = true;

		L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		if (item == null || item.isWear())
			return;

		if (_slot == L2Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquipped())
			return;

		if ((item.getItemId() == 6718) && (activeChar._event == CTF.getInstance()))
			return;

		if (item.getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
			return;

		if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
			return;

		if (activeChar.isAttackingNow() || activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			return;

		L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);

		for (L2ItemInstance element : unequiped)
		{
			activeChar.checkSSMatch(null, element);
			activeChar.getInventory().updateInventory(element);
		}

		activeChar.broadcastUserInfo();

		if (unequiped.length > 0)
		{
			if (unequiped[0].getEnchantLevel() > 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequiped[0].getEnchantLevel()).addItemName(unequiped[0]));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequiped[0]));
			}

			InventoryUpdate iu = new InventoryUpdate();
			iu.addItems(Arrays.asList(unequiped));
			activeChar.sendPacket(iu);
		}
	}

}