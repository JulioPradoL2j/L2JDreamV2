package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.SeedProduction;

public class RequestSetSeed extends L2GameClientPacket
{
	private int _size;
	private int _manorId;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if (_size * 12 > _buf.remaining() || _size > 500 || _size < 1)
		{
			_size = 0;
			return;
		}

		_items = new int[_size * 3];
		for (int i = 0; i < _size; i++)
		{
			int itemId = readD();
			_items[i * 3] = itemId;
			int sales = readD();
			_items[i * 3 + 1] = sales;
			int price = readD();
			_items[i * 3 + 2] = price;
		}
	}

	@Override
	protected void runImpl()
	{
		if (_size < 1)
			return;

		List<SeedProduction> seeds = new ArrayList<>();
		for (int i = 0; i < _size; i++)
		{
			int id = _items[i * 3];
			int sales = _items[i * 3 + 1];
			int price = _items[i * 3 + 2];
			if (id > 0)
			{
				SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
				seeds.add(s);
			}
		}

		CastleManager.getInstance().getCastleById(_manorId).setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(_manorId).saveSeedData(CastleManorManager.PERIOD_NEXT);
		}
	}

}