package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PetInstance;

public class PetItemList extends L2GameServerPacket
{
	private final L2PetInstance _activeChar;

	public PetItemList(L2PetInstance character)
	{
		_activeChar = character;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xB2);

		L2ItemInstance[] items = _activeChar.getInventory().getItems();
		int count = items.length;
		writeH(count);

		for (L2ItemInstance temp : items)
		{
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemDisplayId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
		}
	}

}