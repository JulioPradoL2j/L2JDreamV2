package com.dream.game.model.actor.instance;

import java.text.SimpleDateFormat;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.clanhallsiege.BanditStrongholdSiege;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortResistSiegeManager;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.manager.clanhallsiege.WildBeastFarmSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2ClanHallSiegeInfInstance extends L2Npc
{
	private static boolean validateCondition(int index)
	{
		if (index == 1)
			return !DevastatedCastleSiege.getInstance().getIsInProgress();
		return !FortressOfDeadSiege.getInstance().getIsInProgress();
	}

	private ClanHall clanhall;

	public L2ClanHallSiegeInfInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
		}
		else if (getNpcId() == 35420)
		{
			showSiegeInfoWindow(player, 1);
		}
		else if (getNpcId() == 35639)
		{
			showSiegeInfoWindow(player, 2);
		}
		else
		{
			showMessageWindow(player, 0);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			showMessageWindow(player, val);
		}
		else if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}

			if (quest.length() == 0)
			{
				showQuestWindow(player);
			}
			else
			{
				showQuestWindow(player, quest);
			}
		}
		else if (command.startsWith("Registration"))
		{
			L2Clan playerClan = player.getClan();
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			String str;
			str = "<html><body>Journal!<br>";

			switch (getTemplate().getNpcId())
			{
				case 35437:
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
					{
						showMessageWindow(player, 1);
						return;
					}
					if (BanditStrongholdSiege.getInstance().clanhall.getOwnerClan() == playerClan)
					{
						str += "Your clan has already registered on the siege, that you still want me?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add/Remove a member of the siege</a><br>";
					}
					else if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
					{
						str += "Your clan has already registered on the siege, that you still want me?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_UnRegister\">Unregister</a><br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add/Remove a member of the siege</a><br>";
					}
					else
					{
						int res = BanditStrongholdSiege.getInstance().registerClanOnSiege(player, playerClan);
						if (res == 0)
						{
							str += "Your clan : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully registered on the siege Clan Hall.<br>";
							str += "Now you need to select no more than 18 players, who will take part in the siege, Member of your clan.<br>";
							str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Select members of the siege</a><br>";
						}
						else if (res == 1)
						{
							str += "You did not pass the test and have not received the right to participate in the siege of the fortress of thieves<br>";
							str += "Come back when you're done.";
						}
						else if (res == 2)
						{
							str += "Unfortunately you are late. The five clan leaders have already applied for registration.<br>";
							str += "Next time be more luck.";
						}
					}
					break;
				case 35627:
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
					{
						showMessageWindow(player, 1);
						return;
					}
					if (WildBeastFarmSiege.getInstance().clanhall.getOwnerClan() == playerClan)
					{
						str += "Your clan has already registered on the siege, that you still want me?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add/Remove a member of the siege</a><br>";
					}
					else if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
					{
						str += "Your clan has already registered on the siege, that you still want me?<br>";
						str += "<a action=\"bypass -h npc_%objectId%_UnRegister\">unregister</a><br>";
						str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Add/Remove a member of the siege</a><br>";
					}
					else
					{
						int res = WildBeastFarmSiege.getInstance().registerClanOnSiege(player, playerClan);
						if (res == 0)
						{
							str += "Your clan: <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully registered for the siege of Clan Hall.<br>";
							str += "Now you need to select no more than 18 players who will take part in the siege of the members of your clan.<br>";
							str += "<a action=\"bypass -h npc_%objectId%_PlayerList\">Select members of the siege</a><br>";
						}
						else if (res == 1)
						{
							str += "You did not pass the test and have not received the right to participate in the siege of the fortress of thieves<br>";
							str += "Come back when you're done.";
						}
						else if (res == 2)
						{
							str += "Unfortunately you are too late. Five clan leaders have already filed an application for registration.<br>";
							str += "Next time be more agile.";
						}
					}
					break;
				case 35604:
					if (!RainbowSpringSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 6);
						return;
					}
					if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
					{
						showMessageWindow(player, 4);
						return;
					}
					if (RainbowSpringSiege.getInstance()._clanhall.getOwnerClan() == playerClan)
					{
						str += "Your clan has already registered for the siege that you still want me to do?<br>";
					}
					else if (RainbowSpringSiege.getInstance().isClanOnSiege(playerClan))
					{
						str += "Your clan has already filed an application for participation in the competition for the possession of the clan hall that you still want me to do?<br>";
					}
					else
					{
						int res = RainbowSpringSiege.getInstance().registerClanOnSiege(player, playerClan);
						if (res > 0)
						{
							str += "Your request to participate in the competition for the possession of the clan hall is accepted, you have <font color=\"LEVEL\">" + res + " Certificate of Participation in the War Clan Hall Hot Springs</font>.<br>";
						}
						else
						{
							str += "To apply for participation in the competition for the possession of the clan hall, you need to get as much as possible <font color=\"LEVEL\">Evidence of participation in the war Clan Hall Hot Springs</font>.<br>";
						}
					}
					break;
			}
			str += "</body></html>";
			html.setHtml(str);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (command.startsWith("UnRegister"))
		{
			L2Clan playerClan = player.getClan();
			NpcHtmlMessage html;
			String str;
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
			{
				_log.info("Player " + player.getName() + " use packet hack, try unregister clan.");
				return;
			}
			switch (getTemplate().getNpcId())
			{
				case 35437:
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					html = new NpcHtmlMessage(getObjectId());
					if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
					{
						if (BanditStrongholdSiege.getInstance().unRegisterClan(playerClan))
						{
							str = "<html><body>Newsletter!<br>";
							str += "Your clan: <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully unregistered the siege Clan Hall.<br>";
							str += "</body></html>";
							html.setHtml(str);
							html.replace("%objectId%", String.valueOf(getObjectId()));
							player.sendPacket(html);
						}
					}
					else
					{
						_log.info("Player " + player.getName() + " use packet hack, try unregister clan.");
					}
					break;
				case 35627:
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					html = new NpcHtmlMessage(getObjectId());
					if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
					{
						if (WildBeastFarmSiege.getInstance().unRegisterClan(playerClan))
						{
							str = "<html><body>Newsletter!<br>";
							str += "Your Clan : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully unregistered the siege Clan Hall.<br>";
							str += "</body></html>";
							html.setHtml(str);
							html.replace("%objectId%", String.valueOf(getObjectId()));
							player.sendPacket(html);
						}
					}
					else
					{
						_log.info("Player " + player.getName() + " use packet hack, try unregister clan.");
					}
					break;
				case 35604:
					if (!RainbowSpringSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 6);
						return;
					}
					html = new NpcHtmlMessage(getObjectId());
					if (RainbowSpringSiege.getInstance().isClanOnSiege(playerClan))
						if (RainbowSpringSiege.getInstance().unRegisterClan(player))
						{
							str = "<html><body>Newsletter!<br>";
							str += "Your Clan : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>, successfully deregistered the siege Clan Hall.<br>";
							str += "</body></html>";
							html.setHtml(str);
							html.replace("%objectId%", String.valueOf(getObjectId()));
							player.sendPacket(html);
						}
					break;
			}
		}
		else if (command.startsWith("PlayerList"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				return;
			switch (getTemplate().getNpcId())
			{
				case 35437:
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
					{
						showPlayersList(playerClan, player);
					}
					break;
				case 35627:
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
					{
						showPlayersList(playerClan, player);
					}
					break;
			}
		}
		else if (command.startsWith("addPlayer"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				return;
			String val = command.substring(10);
			if (playerClan.getClanMember(val) == null)
				return;

			switch (getTemplate().getNpcId())
			{
				case 35437:
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					BanditStrongholdSiege.getInstance().addPlayer(playerClan, val);
					if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
					{
						showPlayersList(playerClan, player);
					}
					break;
				case 35627:
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					WildBeastFarmSiege.getInstance().addPlayer(playerClan, val);
					if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
					{
						showPlayersList(playerClan, player);
					}
					break;
			}
		}
		else if (command.startsWith("removePlayer"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || playerClan.getLeaderName() != player.getName() || playerClan.getLevel() < 4)
				return;
			String val = command.substring(13);
			switch (getTemplate().getNpcId())
			{
				case 35437:
					if (!BanditStrongholdSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if (playerClan.getClanMember(val) != null)
					{
						BanditStrongholdSiege.getInstance().removePlayer(playerClan, val);
					}
					if (BanditStrongholdSiege.getInstance().isClanOnSiege(playerClan))
					{
						showPlayersList(playerClan, player);
					}
					break;
				case 35627:
					if (!WildBeastFarmSiege.getInstance().isRegistrationPeriod())
					{
						showMessageWindow(player, 3);
						return;
					}
					if (playerClan.getClanMember(val) != null)
					{
						WildBeastFarmSiege.getInstance().removePlayer(playerClan, val);
					}
					if (WildBeastFarmSiege.getInstance().isClanOnSiege(playerClan))
					{
						showPlayersList(playerClan, player);
					}
					break;
			}
		}
	}

	public void showMessageWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		long startSiege = 0;
		int npcId = getTemplate().getNpcId();
		String filename;
		if (val == 0)
		{
			filename = "data/html/default/" + npcId + ".htm";
		}
		else
		{
			filename = "data/html/default/" + npcId + "-" + val + ".htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		if (npcId == 35382)
		{
			startSiege = FortResistSiegeManager.getInstance().getSiegeDate().getTimeInMillis();
		}
		else if (npcId == 35437 || npcId == 35627 || npcId == 35604)
		{
			clanhall = null;
			String clans = "";
			clans += "<table width=280 border=0>";
			int clanCount = 0;

			switch (npcId)
			{
				case 35437:
					clanhall = ClanHallManager.getInstance().getClanHallById(35);
					startSiege = BanditStrongholdSiege.getInstance().getSiegeDate().getTimeInMillis();
					for (String a : BanditStrongholdSiege.getInstance().getRegisteredClans())
					{
						clanCount++;
						clans += "<tr><td><font color=\"LEVEL\">" + a + "</font>  (The Number Of :" + BanditStrongholdSiege.getInstance().getPlayersCount(a) + "people.)</td></tr>";
					}
					break;
				case 35627:
					clanhall = ClanHallManager.getInstance().getClanHallById(63);
					startSiege = WildBeastFarmSiege.getInstance().getSiegeDate().getTimeInMillis();
					for (String a : WildBeastFarmSiege.getInstance().getRegisteredClans())
					{
						clanCount++;
						clans += "<tr><td><font color=\"LEVEL\">" + a + "</font>  (The Number Of :" + BanditStrongholdSiege.getInstance().getPlayersCount(a) + "people.)</td></tr>";
					}
					break;
				case 35604:
					clanhall = ClanHallManager.getInstance().getClanHallById(62);
					startSiege = RainbowSpringSiege.getInstance().getSiegeDate().getTimeInMillis();
					break;
			}
			while (clanCount < 5)
			{
				clans += "<tr><td><font color=\"LEVEL\">**Not registered**</font>  (The Number Of : people.)</td></tr>";
				clanCount++;
			}
			clans += "</table>";
			html.replace("%clan%", String.valueOf(clans));
			L2Clan clan = clanhall.getOwnerClan();
			String clanName;
			if (clan == null)
			{
				clanName = "NPC";
			}
			else
			{
				clanName = clan.getName();
			}
			html.replace("%clanname%", String.valueOf(clanName));
		}

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		html.replace("%SiegeDate%", String.valueOf(format.format(startSiege)));
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	public void showPlayersList(L2Clan playerClan, L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		String str;
		str = "<html><body>Newsletter!<br>";
		str += "Your Clan : <font color=\"LEVEL\">" + player.getClan().getName() + "</font>. Select participants for the siege.<br><br>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=170 align=center>Registered</td><td width=110 align=center>action</td></tr></table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0>";
		if (getTemplate().getNpcId() == 35437)
		{
			for (String temp : BanditStrongholdSiege.getInstance().getRegisteredPlayers(playerClan))
			{
				str += "<tr><td width=170>" + temp + "</td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_removePlayer " + temp + "\"> Delete</a></td></tr>";
			}
		}
		else if (getTemplate().getNpcId() == 35627)
		{
			for (String temp : WildBeastFarmSiege.getInstance().getRegisteredPlayers(playerClan))
			{
				str += "<tr><td width=170>" + temp + "</td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_removePlayer " + temp + "\"> Delete</a></td></tr>";
			}
		}
		str += "</table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=170 align=center>Members Of The Clan</td><td width=110 align=center>action</td></tr></table>";
		str += "<img src=\"L2UI.SquareWhite\" width=280 height=1>";
		str += "<table width=280 border=0>";
		for (L2ClanMember temp : playerClan.getMembers())
		{
			if (getTemplate().getNpcId() == 35437 && !BanditStrongholdSiege.getInstance().getRegisteredPlayers(playerClan).contains(temp.getName()))
			{
				str += "<tr><td width=170>" + temp.getName() + "</td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_addPlayer " + temp.getName() + "\"> Add</a></td></tr>";
			}
			if (getTemplate().getNpcId() == 35627 && !WildBeastFarmSiege.getInstance().getRegisteredPlayers(playerClan).contains(temp.getName()))
			{
				str += "<tr><td width=170>" + temp.getName() + "</td><td width=110 align=center><a action=\"bypass -h npc_%objectId%_addPlayer " + temp.getName() + "\"> Add</a></td></tr>";
			}
		}
		str += "</table>";
		str += "</body></html>";
		html.setHtml(str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	public void showSiegeInfoWindow(L2PcInstance player, int index)
	{
		if (validateCondition(index))
		{
			if (index == 1)
			{
				DevastatedCastleSiege.getInstance().listRegisterClan(player);
			}
			else
			{
				FortressOfDeadSiege.getInstance().listRegisterClan(player);
			}
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/siege/" + getTemplate().getNpcId() + "-busy.htm");
			html.replace("%castlename%", getCastle().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}