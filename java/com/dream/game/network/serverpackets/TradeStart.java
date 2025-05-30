package com.dream.game.network.serverpackets;

import java.util.List;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class TradeStart extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2ItemInstance> _itemList;

	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
		_itemList = _activeChar.getInventory().getAvailableItems(true);
	}

	@Override
	protected final void writeImpl()
	{
		if (_activeChar.getActiveTradeList() == null || _activeChar.getActiveTradeList().getPartner() == null)
			return;

		if (_activeChar.getTrading())
		{
			L2PcInstance partner = _activeChar.getActiveTradeList().getPartner();
			_activeChar.clearActiveTradeList(partner);
		}
		else
		{
			_activeChar.setTrading(true);
		}

		writeC(0x1E);
		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());

		writeH(_itemList.size());
		for (L2ItemInstance item : _itemList)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0x00);

			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(0x00);
		}
	}

}