package com.dream.game.handler;

import java.util.logging.Logger;

import com.dream.game.model.actor.instance.L2PcInstance;

public interface ITutorialHandler
{
	public static Logger _log = Logger.getLogger(ITutorialHandler.class.getName());

       public boolean useLink(String command, L2PcInstance activeChar, String params);

       public String[] getLinkList();
}