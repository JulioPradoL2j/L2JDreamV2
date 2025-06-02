package com.dream.game.handler.voiced;

import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;

public class AutoFarm implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"autofarm"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (command.startsWith("autofarm"))
		{
			if (player.isAutoFarm())
			{
				player.setAutoFarm(false);
				player.sendMessage("Autofarm stop.");
			}
			else
			{
				player.setAutoFarm(true);
				player.sendMessage("Autofarm active.");
			}
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
