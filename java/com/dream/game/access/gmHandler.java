package com.dream.game.access;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.instance.L2PcInstance;

public abstract class gmHandler
{
	public static Logger _log = Logger.getLogger(gmHandler.class.getName());

	public abstract String[] getCommandList();

	public abstract void runCommand(L2PcInstance admin, String... params);
}