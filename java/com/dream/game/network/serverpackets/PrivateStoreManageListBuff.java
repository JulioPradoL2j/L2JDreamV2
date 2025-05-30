package com.dream.game.network.serverpackets;

import java.util.List;
import java.util.Map;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;

public class PrivateStoreManageListBuff extends L2GameServerPacket
{
	private final int _objId;
	private final int _playerAdena;
	private final L2PcInstance _activeChar;
	private final List<L2ItemInstance> _buffList;
	private final Map<Integer, int[]> _currentBuffList;
	
	public PrivateStoreManageListBuff(L2PcInstance player)
	{
		player.setIsBuffShop(true);
		_objId = player.getObjectId();
		_activeChar = player;
		if (Config.SELL_BY_ITEM)
		{
			final CreatureSay cs11 = new CreatureSay(0, SystemChatChannelId.Chat_None, "SYS", "Atention the store system is based on " + Config.COIN_TEXT + "!");//
			_activeChar.sendPacket(cs11);
			_playerAdena = _activeChar.getItemCount(Config.SELL_ITEM, -1);
		}
		else
			_playerAdena = player.getAdena();

		_buffList = player.getAvailableSkillsForBuffShop();
		_currentBuffList = player.getBuffShopSellList();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9a);
		writeD(_objId);
		writeD(0);
		writeD(_playerAdena);
		writeD(_buffList.size());
		for (L2ItemInstance buff : _buffList)
		{
			writeD(5);
			writeD(buff.getObjectId());
			writeD(buff.getItemId());
			writeD(1);
			writeH(0);
			writeH(0);
			writeH(0);
			writeD(0);
			writeD(0);
		}
		writeD(_currentBuffList.size());
		for (Map.Entry<Integer, int[]> buff : _currentBuffList.entrySet())
		{
			int[] values = buff.getValue();
			writeD(5);
			writeD(values[0]);
			writeD(buff.getKey().intValue());
			writeD(1);
			writeH(0);
			writeH(0);
			writeH(0);
			writeD(0);
			writeD(values[1]);
			writeD(0);
		}
	}
	
	@Override
	public String getType()
	{
		return null;
	}
}