package com.dream.game.network.serverpackets;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class ItemList extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(ItemList.class.getName());
	private final L2ItemInstance[] _items;
	private final boolean _showWindow;

	public ItemList(L2ItemInstance[] items, boolean showWindow)
	{
		_items = items;
		_showWindow = showWindow;
	}

	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x1b);
		writeH(_showWindow ? 0x01 : 0x00);

		int count = _items.length;
		writeH(count);

		int items = 0;
		for (L2ItemInstance temp : _items)
		{
			if (temp == null || temp.getItem() == null)
			{
				continue;
			}

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
			writeD(temp.isAugmented() ? temp.getAugmentation().getAugmentationId() : 0x00);
			writeD(temp.getMana());
			items++;
			if (items > 400)
			{
				_log.warn("Player " + getClient().getActiveChar() + " has more what 400 items, packet overflow");
				break;
			}
		}
	}

}