package com.dream.game.handler;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public interface IExItemHandler
{
	public int[] getItemIds();

	public void useItem(L2PcInstance player, L2ItemInstance item, String[] params);

}