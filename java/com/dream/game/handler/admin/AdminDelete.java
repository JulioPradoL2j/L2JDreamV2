package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.manager.RaidBossSpawnManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;

public class AdminDelete extends gmHandler
{
	private static final String[] commands =
	{
		"delete",
	};

	private static void handleDelete(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj != null && obj instanceof L2Npc)
		{
			L2Npc target = (L2Npc) obj;
			target.deleteMe();

			L2Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.stopRespawn();

				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", target.getName() + " removed");
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}

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

		String command = params[0];

		if (command.equals("delete"))
		{
			handleDelete(admin);
			return;
		}
	}
}