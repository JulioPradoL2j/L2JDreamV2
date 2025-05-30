package com.dream.game.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.manager.CastleManorManager.SeedProduction;

public final class BuyListSeed extends L2GameServerPacket
{
	private static class Seed
	{
		public final int _itemId, _count, _price;

		public Seed(int itemId, int count, int price)
		{
			_itemId = itemId;
			_count = count;
			_price = price;
		}
	}

	private final int _manorId;
	private List<Seed> _list;

	private final int _money;

	public BuyListSeed(int currentMoney, int castleId, List<SeedProduction> seeds)
	{
		_money = currentMoney;
		_manorId = castleId;

		if (!seeds.isEmpty())
		{
			_list = new ArrayList<>();
			for (SeedProduction s : seeds)
				if (s.getCanProduce() > 0 && s.getPrice() > 0)
				{
					_list.add(new Seed(s.getId(), s.getCanProduce(), s.getPrice()));
				}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);

		writeD(_money); // current money
		writeD(_manorId); // manor id

		if (_list != null && !_list.isEmpty())
		{
			writeH(_list.size()); // list length
			for (Seed s : _list)
			{
				writeH(0x04); // item->type1
				writeD(s._itemId);
				writeD(s._itemId);
				writeD(s._count); // item count
				writeH(0x04); // Custom Type 2
				writeH(0x00); // unknown :)
				writeD(s._price); // price
			}
		}
	}

}