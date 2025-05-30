package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;

public class AdminDoorControl extends gmHandler
{
	private static DoorTable _doorTable;
	private static final String[] commands =
	{
		"open",
		"close",
		"openall",
		"closeall"
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
		_doorTable = DoorTable.getInstance();

		try
		{
			if (command.startsWith("open") && params.length > 1)
			{
				try
				{
					int doorId = Integer.parseInt(params[1]);
					if (_doorTable.getDoor(doorId) != null)
					{
						_doorTable.getDoor(doorId).openMe();
					}
					else
					{
						for (Castle castle : CastleManager.getInstance().getCastles().values())
							if (castle.getDoor(doorId) != null)
							{
								castle.getDoor(doorId).openMe();
							}
					}
				}
				catch (Exception e)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "ID is invalid");
				}
				return;
			}
			else if (command.startsWith("close") && params.length > 1)
			{
				try
				{
					int doorId = Integer.parseInt(params[1]);
					if (_doorTable.getDoor(doorId) != null)
					{
						_doorTable.getDoor(doorId).closeMe();
					}
					else
					{
						for (Castle castle : CastleManager.getInstance().getCastles().values())
							if (castle.getDoor(doorId) != null)
							{
								castle.getDoor(doorId).closeMe();
							}
					}
				}
				catch (Exception e)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "ID is invalid");
				}
				return;
			}
			if (command.equals("closeall"))
			{
				for (L2DoorInstance door : _doorTable.getDoors())
				{
					door.closeMe();
				}
				for (Castle castle : CastleManager.getInstance().getCastles().values())
				{
					for (L2DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
				return;
			}
			if (command.equals("openall"))
			{
				for (L2DoorInstance door : _doorTable.getDoors())
				{
					door.openMe();
				}
				for (Castle castle : CastleManager.getInstance().getCastles().values())
				{
					for (L2DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
				return;
			}
			if (command.equals("open"))
			{
				L2Object target = admin.getTarget();
				if (target != null && target instanceof L2DoorInstance)
				{
					((L2DoorInstance) target).openMe();
				}
				else
				{
					admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				return;
			}

			if (command.equals("close"))
			{
				L2Object target = admin.getTarget();
				if (target != null && target instanceof L2DoorInstance)
				{
					((L2DoorInstance) target).closeMe();
				}
				else
				{
					admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				return;
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
		return;
	}
}