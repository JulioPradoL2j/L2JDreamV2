package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EtcStatusUpdate;

public class NoblesseItem implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		Config.NOBLESSE_ITEM
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

		if (player.isNoble())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("Dear " + player.getName() + " you are Noble already.");
		}
		else
		{
			if (playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
			{
				giveNobles(player);
				player.sendPacket(new EtcStatusUpdate(player));
				player.broadcastUserInfo();
			}
		}
	}

	private static void giveNobles(L2PcInstance p)
	{
		if (!p.isNoble())
		{
			p.setNoble(true);
			p.addItem("Quest", 7694, 1, p, true);
			p.sendMessage("Congratulations, " + p.getName() + " you're now a noble!");
		}

	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}