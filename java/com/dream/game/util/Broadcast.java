package com.dream.game.util;

import com.dream.Config;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.L2GameServerPacket;

public final class Broadcast
{
	public static void announceToOnlinePlayers(String text)
	{
		if (Config.ANNOUNCE_MODE.equals("l2j"))
		{
			CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Announce, "", text);
			toAllOnlinePlayers(cs);
		}
		else if (Config.ANNOUNCE_MODE.equals("l2off"))
	{
			CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, "", text);
			toAllOnlinePlayers(cs);
		}

	}

	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}

			player.sendPacket(mov);
		}
	}

	public static void toAllPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}

		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player.isInsideRadius(character, radius, false, true))
			{
				player.sendPacket(mov);
			}

	}

	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player == null || player.isOfflineTrade())
			{
				continue;
			}

			player.sendPacket(mov);
		}
	}

	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}

		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}

			if (character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}

	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}

		toKnownPlayers(character, mov);
	}

	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radius)
	{
		if (radius < 0)
		{
			radius = 600;
		}

		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}

		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			if (player != null && character.getDistanceSq(player) <= radius)
			{
				player.sendPacket(mov);
			}
	}

	private Broadcast()
	{

	}
}