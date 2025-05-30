package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.model.actor.instance.L2PcInstance;

public class AdminBuffShop extends gmHandler
{
	private static final String[] commands =
	{
		"admin_setbuffshopslot"
	};
	
	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		final String command = params[0];
		if (command.startsWith("admin_setbuffshopslot"))
		{
			if ((admin.getTarget() == null) || !(admin.getTarget() instanceof L2PcInstance))
			{
				admin.sendMessage("Incorrect target.");
			}
			else
			{
				int value = 0;
				try
				{
					value = Integer.valueOf(command.substring(21).trim()).intValue();
				}
				catch (IndexOutOfBoundsException e)
				{
					
				}
				catch (NumberFormatException e)
				{
					
				}
				if (value >= 0)
				{
					((L2PcInstance) admin.getTarget()).setPrivateBuffShopLimit(value);
					admin.sendMessage((new StringBuilder()).append("Changed ").append(admin.getTarget().getName()).append("'s buffshop limit to ").append(value).append(".").toString());
					((L2PcInstance) admin.getTarget()).sendMessage((new StringBuilder()).append("GM Changed your buffshop limit to ").append(value).append(".").toString());
				}
				else
				{
					admin.sendMessage("Usage: //setbuffshopslot <int>");
				}
			}
		}
		return;
	}
	
	@Override
	public String[] getCommandList()
	{
		return commands;
	}
	
}