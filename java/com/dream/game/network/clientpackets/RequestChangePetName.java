package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.PetNameTable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.util.Util;

public class RequestChangePetName extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2Summon pet = activeChar.getPet();
		if (pet == null)
			return;

		if (pet.getName() != null && pet.getName().trim().length() != 0)
		{
			activeChar.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
			return;
		}
		else if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().getNpcId()))
		{
			activeChar.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
			return;
		}
		else if (_name.length() < 3 || _name.length() > 8)
		{
			activeChar.sendPacket(SystemMessageId.NAMING_PETNAME_UP_TO_8CHARS);
			return;
		}
		else if (!Util.isValidPlayerName(_name))
		{
			activeChar.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
			return;
		}
		pet.setName(_name);
		pet.broadcastFullInfo();

		if (pet instanceof L2PetInstance)
		{
			L2ItemInstance controlItem = pet.getOwner().getInventory().getItemByObjectId(pet.getControlItemId());
			if (controlItem != null)
			{
				controlItem.setCustomType2(1);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(controlItem);
				activeChar.sendPacket(iu);
			}
		}
	}

}