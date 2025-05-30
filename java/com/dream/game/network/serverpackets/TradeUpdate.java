package com.dream.game.network.serverpackets;

import com.dream.game.model.TradeList;
import com.dream.game.model.TradeList.TradeItem;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class TradeUpdate extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private final TradeItem[] _trade_items;

	public TradeUpdate(TradeList trade, L2PcInstance activeChar)
	{
		_items = activeChar.getInventory().getItems();
		_trade_items = trade.getItems();
	}

	private int getItemCount(int objectId)
	{
		for (L2ItemInstance item : _items)
			if (item.getObjectId() == objectId)
				return item.getCount();
		return 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x74);

		writeH(_trade_items.length);
		for (TradeItem item : _trade_items)
		{
			int count = getItemCount(item.getObjectId()) - item.getCount();
			boolean stackable = item.getItem().isStackable();
			if (count == 0)
			{
				count = 1;
				stackable = false;
			}
			writeH(stackable ? 3 : 2);
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(count);
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(0x00); // ?
			writeH(0x00);
		}
	}

}