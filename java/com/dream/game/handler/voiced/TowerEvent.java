package com.dream.game.handler.voiced;

import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.towerwars.ListMath;

public class TowerEvent implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"towerevent"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (command.startsWith("towerevent"))
		{
			ListMath.getInstance().registerPlayer(player);
			player.sendMessage("You have been registered for the Tower Wars event.");
		}
		
		return false;
	}
	
	@Override
	public String getDescription(String command)
	{
		if (command.equals("autofarm"))
			return "Displays a autofarm of commands.";
		return "In detail in the autofarm.";
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
