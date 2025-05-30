package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.manager.CastleManorManager.CropProcure;

public class RequestSetCrop extends L2GameClientPacket
{
	private int _size, _manorId;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if (_size * 13 > _buf.remaining() || _size > 500 || _size < 1)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 4];
		for (int i = 0; i < _size; i++)
		{
			int itemId = readD();
			_items[i * 4] = itemId;
			int sales = readD();
			_items[i * 4 + 1] = sales;
			int price = readD();
			_items[i * 4 + 2] = price;
			int type = readC();
			_items[i * 4 + 3] = type;
		}
	}

	@Override
	protected void runImpl()
	{
		if (_size < 1)
			return;

		List<CropProcure> crops = new ArrayList<>();
		for (int i = 0; i < _size; i++)
		{
			int id = _items[i * 4];
			int sales = _items[i * 4 + 1];
			int price = _items[i * 4 + 2];
			int type = _items[i * 4 + 3];
			if (id > 0)
			{
				CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				crops.add(s);
			}
		}

		CastleManager.getInstance().getCastleById(_manorId).setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(_manorId).saveCropData(CastleManorManager.PERIOD_NEXT);
		}
	}

}