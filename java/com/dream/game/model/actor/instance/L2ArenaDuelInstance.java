package com.dream.game.model.actor.instance;
/*
* Copyright (C) 2004-2013 L2J Server
* 
* This file is part of L2J Server.
* 
* L2J Server is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* L2J Server is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.dream.game.model.L2Party;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.entity.events.arenaduel.ArenaDuel;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.util.Map;
import java.util.StringTokenizer;

public class L2ArenaDuelInstance extends L2NpcInstance
{
	public L2ArenaDuelInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";

		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;

		return "data/html/mods/ArenaDuel/" + filename + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");

		if (command.startsWith("1x1_register"))
		{
			if (player.isCursedWeaponEquipped() || player.isInStoreMode() || !player.isNoble() || player.isAio() || player.getKarma() > 0)
			{
				player.sendMessage("You does not have the necessary requirements.");
				return;
			}

			if (player.getClassId() == ClassId.shillienElder || player.getClassId() == ClassId.shillienSaint || player.getClassId() == ClassId.bishop || player.getClassId() == ClassId.cardinal || player.getClassId() == ClassId.elder || player.getClassId() == ClassId.evaSaint)
			{
				player.sendMessage("Yor class is not allowed in 1x1 game event.");
				return;
			}

			if (ArenaDuel.getInstance().register(player))
			{
				player.setArena1x1(true);
				player.setArenaProtection(true);

				NpcTalk1x1(player, this);
			}
		}

		if (command.startsWith("1x1_remove"))
			ArenaDuel.getInstance().remove(player);

		if (command.startsWith("1x1_observe"))
			observe1x1List(player);

		// Observe Commands
		String currentCommand = st.nextToken();

		if (currentCommand.equalsIgnoreCase("1x1_watch"))
		{
			if (player.isArenaProtection() || player.isArenaProtection())
			{
				player.sendMessage("You can't watch when you are registered.");
				return;
			}

			int arenaId = 0;

			if (st.countTokens() == 1)
				arenaId = Integer.valueOf(st.nextToken());

			ArenaDuel.getInstance().addSpectator(player, arenaId);
		}

		else
			super.onBypassFeedback(player, command);
	}

	public void observe1x1List(L2PcInstance player)
	{
		final StringBuilder sb = new StringBuilder();

		final Map<Integer, String> fights = ArenaDuel.getInstance().getFights();

		if (fights == null || fights.isEmpty())
			sb.append("<font color=\"LEVEL\">There are no 1x1 fights occurring.</font>");
		else
		{
			for (int id : fights.keySet())
				sb.append("<font color=\"LEVEL\"><a action=\"bypass -h npc_%objectId%_1x1_watch " + id + "\">" + fights.get(id) + "</a></font><br1>");
		}

		NpcHtmlMessage html = new NpcHtmlMessage(3);
		html.setFile(getHtmlPath(getNpcId(), 3));
		html.replace("%fighter%", sb.toString());
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}

	public void ClasseCheck(L2PcInstance activeChar)
	{
		L2Party plparty = activeChar.getParty();
		for (L2PcInstance player : plparty.getPartyMembers())
		{
			if (player != null)
			{
				if (player.getParty() != null)
				{
					if (player.getClassId() == ClassId.gladiator || player.getClassId() == ClassId.duelist)
						activeChar.duelist_cont = activeChar.duelist_cont + 1;

					if (player.getClassId() == ClassId.warlord || player.getClassId() == ClassId.dreadnought)
						activeChar.dreadnought_cont = activeChar.dreadnought_cont + 1;

					if (player.getClassId() == ClassId.paladin || player.getClassId() == ClassId.phoenixKnight || player.getClassId() == ClassId.darkAvenger || player.getClassId() == ClassId.hellKnight || player.getClassId() == ClassId.evaTemplar || player.getClassId() == ClassId.templeKnight || player.getClassId() == ClassId.shillienKnight || player.getClassId() == ClassId.shillienTemplar)
						activeChar.tanker_cont = activeChar.tanker_cont + 1;

					if (player.getClassId() == ClassId.adventurer || player.getClassId() == ClassId.treasureHunter || player.getClassId() == ClassId.windRider || player.getClassId() == ClassId.plainsWalker || player.getClassId() == ClassId.ghostHunter || player.getClassId() == ClassId.abyssWalker)
						activeChar.dagger_cont = activeChar.dagger_cont + 1;

					if (player.getClassId() == ClassId.hawkeye || player.getClassId() == ClassId.sagittarius || player.getClassId() == ClassId.silverRanger || player.getClassId() == ClassId.moonlightSentinel || player.getClassId() == ClassId.phantomRanger || player.getClassId() == ClassId.ghostSentinel)
						activeChar.archer_cont = activeChar.archer_cont + 1;

					if (player.getClassId() == ClassId.shillienElder || player.getClassId() == ClassId.shillienSaint || player.getClassId() == ClassId.bishop || player.getClassId() == ClassId.cardinal || player.getClassId() == ClassId.elder || player.getClassId() == ClassId.evaSaint)
						activeChar.bs_cont = activeChar.bs_cont + 1;

					if (player.getClassId() == ClassId.archmage || player.getClassId() == ClassId.sorceror)
						activeChar.archmage_cont = activeChar.archmage_cont + 1;

					if (player.getClassId() == ClassId.soultaker || player.getClassId() == ClassId.necromancer)
						activeChar.soultaker_cont = activeChar.soultaker_cont + 1;

					if (player.getClassId() == ClassId.mysticMuse || player.getClassId() == ClassId.spellsinger)
						activeChar.mysticMuse_cont = activeChar.mysticMuse_cont + 1;

					if (player.getClassId() == ClassId.stormScreamer || player.getClassId() == ClassId.spellhowler)
						activeChar.stormScreamer_cont = activeChar.stormScreamer_cont + 1;

					if (player.getClassId() == ClassId.titan || player.getClassId() == ClassId.destroyer)
						activeChar.titan_cont = activeChar.titan_cont + 1;

					if (player.getClassId() == ClassId.tyrant || player.getClassId() == ClassId.grandKhauatari)
						activeChar.grandKhauatari_cont = activeChar.grandKhauatari_cont + 1;

					if (player.getClassId() == ClassId.orcShaman || player.getClassId() == ClassId.overlord)
						activeChar.dominator_cont = activeChar.dominator_cont + 1;

					if (player.getClassId() == ClassId.doomcryer || player.getClassId() == ClassId.warcryer)
						activeChar.doomcryer_cont = activeChar.doomcryer_cont + 1;

				}
			}
		}
	}

	public void clean(L2PcInstance player)
	{
		player.duelist_cont = 0;
		player.dreadnought_cont = 0;
		player.tanker_cont = 0;
		player.dagger_cont = 0;
		player.archer_cont = 0;
		player.bs_cont = 0;
		player.archmage_cont = 0;
		player.soultaker_cont = 0;
		player.mysticMuse_cont = 0;
		player.stormScreamer_cont = 0;
		player.titan_cont = 0;
		player.grandKhauatari_cont = 0;
		player.dominator_cont = 0;
		player.doomcryer_cont = 0;
	}

	public void NpcTalk1x1(L2PcInstance player, L2Npc npc)
	{
		npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, "Tournament", player.getName() + " is registered on 1x1 game waiting list."));
		return;
	}

	public void NpcTalk2x2(L2PcInstance player, L2Npc npc)
	{
		npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, "Tournament", player.getName() + " is registered on 2x2 game waiting list."));
		return;
	}

	public void NpcTalk4x4(L2PcInstance player, L2Npc npc)
	{
		npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, "Tournament", player.getName() + " is registered on 4x4 game waiting list."));
		return;
	}

	public void NpcTalk9x9(L2PcInstance player, L2Npc npc)
	{
		npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, "Tournament", player.getName() + " is registered on 9x9 game waiting list."));
		return;
	}
}