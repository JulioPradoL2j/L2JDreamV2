package com.dream.game.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class SellList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2MerchantInstance _lease;
	private final int _money;
	private final List<L2ItemInstance> _selllist = new ArrayList<>();

	public SellList(L2PcInstance player)
	{
		_activeChar = player;
		_lease = null;
		_money = _activeChar.getAdena();
		doLease();
	}

	public SellList(L2PcInstance player, L2MerchantInstance lease)
	{
		_activeChar = player;
		_lease = lease;
		_money = _activeChar.getAdena();
		doLease();
	}

	private void doLease()
	{
		if (_lease == null)
		{
			for (L2ItemInstance item : _activeChar.getInventory().getItems())
				if (!item.isEquipped() && item.isSellable() && (_activeChar.getPet() == null || item.getObjectId() != _activeChar.getPet().getControlItemId()))
				{
					_selllist.add(item);
				}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x10);
		writeD(_money);
		writeD(0x00);
		writeH(_selllist.size());

		for (L2ItemInstance item : _selllist)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0x00);
			writeD(item.getItem().getReferencePrice() / 2);
		}
	}

}