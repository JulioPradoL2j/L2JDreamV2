package com.dream.game.handler;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;

public interface IItemHandler
{
	public static Logger _log = Logger.getLogger(IItemHandler.class.getName());

	public int[] getItemIds();

	public void useItem(L2Playable playable, L2ItemInstance item);

	public void useItem(L2Playable playable, L2ItemInstance item, boolean animation);

}