package com.dream.game.handler;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.instance.L2PcInstance;

public interface IVoicedCommandHandler
{
	public static Logger _log = Logger.getLogger(IVoicedCommandHandler.class.getName());

	public String getDescription(String command);

	public String[] getVoicedCommandList();

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target);

}