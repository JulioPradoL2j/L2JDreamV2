package com.dream.game.model.actor.instance;

import java.util.StringTokenizer;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;
import com.dream.util.StringUtil;

public final class L2BufferEventInstance extends L2NpcInstance
{

	public L2BufferEventInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			broadcastPacket(new SocialAction(this, Rnd.get(8)));
			player.setLastFolkNPC(this);
			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		int buffid = 0;
		int bufflevel = 1;
		if (st.countTokens() == 2)
		{
			buffid = Integer.valueOf(st.nextToken());
			bufflevel = Integer.valueOf(st.nextToken());
		}
		else if (st.countTokens() == 1)
		{
			buffid = Integer.valueOf(st.nextToken());
		}
		if (actualCommand.equalsIgnoreCase("getbuff"))
		{
			if (buffid != 0)
			{
				SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(this, player);
				showMessageWindow(player);
				player.broadcastPacket(new MagicSkillUse(this, player, buffid, bufflevel, 5, 0, false));
			}
		}
		else if (actualCommand.equalsIgnoreCase("restore"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			showMessageWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("cancel"))
		{
			player.stopAllEffects();
			showMessageWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		final StringBuilder strBuffer = StringUtil.startAppend(3500, "<html><body><center>");
		if (player.isSitting())
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You can't use buffer while you're sitting.");
			strBuffer.append("Stand up, <font color=\"LEVEL\">%charname%</font>!<br>");
			strBuffer.append("How dare you to talk with me while you're sitting?!<br>");
		}
		else if (player.isAlikeDead())
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You can't use buffer while you're dead or using fake death.");
			strBuffer.append("Sadly, <font color=\"LEVEL\">%charname%</font>, you're dead.<br>");
			strBuffer.append("I can't offer any support effect for dead people...<br>");
		}
		else if (player.isInCombat())
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You can't use buffer while you're in combat.");
			strBuffer.append("Sadly, <font color=\"LEVEL\">%charname%</font>, I can't serve you.<br>");
			strBuffer.append("Came back when you will not be in a combat.<br>");
		}
		else
		{
			strBuffer.append("<table width=300>");
			strBuffer.append("<tr><td><font color=\"ff9900\">Buffs:</font></td> <td><font color=\"ff9900\">Dances:</font></td> <td><font color=\"ff9900\">Special Buffs:</font></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1204 2\">Wind Walk</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 275 1\"><font color=\"7D053F\">Fury</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1389 3\"><font color=\"9172EC\">Greater Shield</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1040 3\">Shield</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 274 1\"><font color=\"7D053F\">Fire</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1388 3\"><font color=\"9172EC\">Greater Might</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1243 6\">Bless Shield</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 271 1\"><font color=\"7D053F\">Warrior</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1357 1\"><font color=\"9172EC\">Prophecy of Wind</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1068 3\">Might</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 272 1\"><font color=\"7D053F\">Inspiration</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1355 1\"><font color=\"9172EC\">Prophecy of Water</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1036 2\">Magic Barrier</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 310 1\"><font color=\"7D053F\">Vampire</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1356 1\"><font color=\"9172EC\">Prophecy of Fire</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1259 4\">Resist Shock</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 273 1\"><font color=\"7D053F\">Mystic</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1363 1\"><font color=\"9172EC\">Chant of Victory</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1035 4\">Mental Shield</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 276 1\"><font color=\"7D053F\">Concentration</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1413 1\"><font color=\"9172EC\">Magnu's Chant</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1045 6\">Blessed Body</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 277 1\"><font color=\"7D053F\">Light</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1362 1\"><font color=\"9172EC\">Chant of Spirit</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1048 6\">Blessed Soul</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 365 1\"><font color=\"7D053F\">Siren's Dance</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1062 2\">Berserker Spirit</a></td> <td><font color=\"ff9900\">Songs:</font></td> <td><font color=\"ff9900\">Special Buffs II:</font></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1240 3\">Guidance</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 264 1\"><font color=\"307D7E\">Earth</font></a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 4703 13\"><font color=\"9E7BFF\">Gift of Seraphim</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1242 3\">Death Whisper</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 267 1\"><font color=\"307D7E\">Warding</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 4699 13\"><font color=\"9E7BFF\">Blessing of Queen</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1077 3\">Focus</a></td>  <td><a action=\"bypass -h npc_%objectId%_getbuff 304 1\"><font color=\"307D7E\">Vitality</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 4700 13\"><font color=\"9E7BFF\">Gift of Queen</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1086 2\">Haste</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 269 1\"><font color=\"307D7E\">Hunter</font></a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 4702 13\"><font color=\"9E7BFF\">Blessing of Seraphim</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1087 3\">Agility</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 268 1\"><font color=\"307D7E\">Wind</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1085 3\">Acumen</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 266 1\"><font color=\"307D7E\">Water</font></a></td> <td><font color=\"ff9900\">Outros:</font></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1059 3\">Empower</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 349 1\"><font color=\"307D7E\">Renewal</font></a></td> <td><a action=\"bypass -h npc_%objectId%_restore\"><font color=\"ffffff\">Restore</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1303 2\">Wild Magic</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 364 1\"><font color=\"307D7E\">Champion</font></a></td> <td><a action=\"bypass -h npc_%objectId%_cancel\"><font color=\"ffffff\">Cancel</font></a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1078 6\">Concentration</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1304 3\">Advanced Block</a></td></tr>");
		}
		strBuffer.append("</center></body></html>");
		html.setHtml(strBuffer.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%charname%", player.getName());
		player.sendPacket(html);
	}
}