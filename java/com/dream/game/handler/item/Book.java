package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class Book implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5588,
		6317,
		7561,
		7063,
		7064,
		7065,
		7066,
		7082,
		7083,
		7084,
		7085,
		7086,
		7087,
		7088,
		7089,
		7090,
		7091,
		7092,
		7093,
		7094,
		7095,
		7096,
		7097,
		7098,
		7099,
		7100,
		7101,
		7102,
		7103,
		7104,
		7105,
		7106,
		7107,
		7108,
		7109,
		7110,
		7111,
		7112,
		8059
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();

		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/help/" + itemId + ".htm");
		html.setItemId(itemId);
		activeChar.sendPacket(html);

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}