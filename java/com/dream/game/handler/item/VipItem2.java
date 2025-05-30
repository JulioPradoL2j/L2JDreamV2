package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;

public class VipItem2 implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		Config.VIP_ITEM2
	};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{

	}

	@Override
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) playable;

		if (player.isVip())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("Dear " + player.getName() + " you are VIP already.");
		}
		else
		{
			if (playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{
				player.setVip(true);
				player.setEndTime("vip", Config.VIP_DIAS2);
				player.sendMessage("Dear " + player.getName() + " now you have VIP Status " + " you have acess granted in VIPs NPC's," + " remember you have " + Config.VIP_DIAS2 + " VIP days.");
				player.broadcastUserInfo();
			}
		}
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}