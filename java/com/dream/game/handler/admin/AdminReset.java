package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.xml.ResetData;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.ResetHolder;
import com.dream.game.model.holders.ResetPrize;
import com.dream.game.model.holders.ResetType;
import com.dream.game.taskmanager.ResetTask;

public class AdminReset extends gmHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"reset_end_daily",
		"reset_end_monthly"
	};
	
	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null)
			return;
		
		String command = params[0];
		
		if (command.equals("reset_end_daily"))
		{
			endResetByType(ResetType.DAILY, admin);
		}
		else if (command.equals("reset_end_monthly"))
		{
			endResetByType(ResetType.MONTH, admin);
		}
	}
	
	private static void endResetByType(ResetType type, L2PcInstance admin)
	{
		for (ResetHolder holder : ResetData.getInstance().getResets())
		{
			for (ResetPrize prize : holder.getPrizes())
			{
				if (prize.getType() != type || !prize.isEnabled())
					continue;
				ResetTask.distributeRewards(prize, type);
			}
		}
		
		if (admin != null)
			admin.sendMessage("Ranking " + type + " finalizado manualmente.");
	}
	
	@Override
	public String[] getCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
