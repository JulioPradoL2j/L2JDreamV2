package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.access.gmHandler;
import com.dream.game.manager.PetitionManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;

public class AdminPetition extends gmHandler
{
	private static final String[] commands =
	{
		"view_petitions",
		"view_petition",
		"accept_petition",
		"reject_petition",
		"reset_petitions",
		"force_peti",
		"change_peti_state"
	};

	@Override
	public String[] getCommandList()
	{
		return commands;
	}

	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null)
			return;

		final String command = params[0];
		L2Object targetChar = admin.getTarget();
		int petitionId = -1;

		try
		{
			petitionId = Integer.parseInt(params[1]);
		}
		catch (Exception e)
		{
		}

		if (command.equals("view_petitions"))
		{
			PetitionManager.getInstance().sendPendingPetitionList(admin);
			return;
		}
		else if (command.equals("view_petition"))
		{
			PetitionManager.getInstance().viewPetition(admin, petitionId);
			return;
		}
		else if (command.equals("change_peti_state"))
		{
			if (Config.PETITIONING_ALLOWED)
			{
				Config.PETITIONING_ALLOWED = false;
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Petitions are disabled");
			}
			else
			{
				Config.PETITIONING_ALLOWED = true;
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Petitions are disabled");
			}
			return;
		}
		else if (command.startsWith("accept_petition"))
		{
			if (PetitionManager.getInstance().isPlayerInConsultation(admin))
			{
				admin.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
				return;
			}
			if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
			{
				admin.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
				return;
			}
			if (!PetitionManager.getInstance().acceptPetition(admin, petitionId))
			{
				admin.sendPacket(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION);
			}
		}
		else if (command.startsWith("reject_petition"))
		{
			if (!PetitionManager.getInstance().rejectPetition(admin, petitionId))
			{
				admin.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER);
			}
		}
		else if (command.equals("reset_petitions"))
		{
			if (PetitionManager.getInstance().isPetitionInProcess())
			{
				admin.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
				return;
			}
			PetitionManager.getInstance().clearPendingPetitions();
		}
		else if (command.startsWith("force_peti"))
		{
			try
			{
				if (targetChar == null || !(targetChar instanceof L2PcInstance))
				{
					admin.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return;
				}
				L2PcInstance targetPlayer = (L2PcInstance) targetChar;

				String val = "";
				if (params.length > 1)
				{
					for (int x = 1; x < params.length; x++)
					{
						val += " " + params[x];
					}
				}

				petitionId = PetitionManager.getInstance().submitPetition(targetPlayer, val, 9);
				PetitionManager.getInstance().acceptPetition(admin, petitionId);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //force_peti [text]");
				return;
			}
		}
	}
}