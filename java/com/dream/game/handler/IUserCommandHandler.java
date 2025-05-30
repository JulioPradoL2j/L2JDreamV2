package com.dream.game.handler;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.instance.L2PcInstance;

public interface IUserCommandHandler
{
	public static Logger _log = Logger.getLogger(IUserCommandHandler.class.getName());

	public int[] getUserCommandList();

	public boolean useUserCommand(int id, L2PcInstance activeChar);

}