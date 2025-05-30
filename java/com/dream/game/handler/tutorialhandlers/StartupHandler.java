package com.dream.game.handler.tutorialhandlers;

import com.dream.game.handler.ITutorialHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.StartupSystem;

public class StartupHandler implements ITutorialHandler
{
	private static final String[] LINK_COMMANDS =
	{
		"start"
	};

	@Override
	public boolean useLink(String _command, L2PcInstance activeChar, String params)
	{
		if (_command.startsWith("start"))
		{
			StartupSystem.handleCommands(activeChar, params);
		}
		return true;
	}

	@Override
	public String[] getLinkList()
	{
		return LINK_COMMANDS;
	}
}