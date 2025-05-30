package com.dream.game.handler.voiced;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class Configurator implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"menu",
		"autoloot",
		"enableTrade",
		"disableTrade",
		"enableOffKnow",
		"disableOffKnow",
		"enableAutoloot",
		"disableAutoloot",
		"enableMessage",
		"disableMessage",
		"enableshowSkillSuccess",
		"disableshowSkillSuccess",
		"enableBuffAnim",
		"disableBuffAnim",
		"enableGainExp",
		"disableGainExp",
		"ignorecolors",
		"enableblockbuff",
		"disableblockbuff",
		"blockparty",
		"unblockparty"
	};

	private static String getBlockMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"CC0000\">OFF</font>";
		if (activeChar.isPreventedFromReceivingBuffs())
		{
			result = "<font color=\"00FF00\">ON</font>";
		}
		return result;
	}

	private static String getBuffAnimMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"CC0000\">OFF</font>";
		if (activeChar.ShowBuffAnim())
		{
			result = "<font color=\"00FF00\">ON</font>";
		}
		return result;
	}

	private static String getGainExpMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"00FF00\">ON</font>";
		if (activeChar.canGainExp())
		{
			result = "<font color=\"CC0000\">OFF</font>";
		}
		return result;
	}

	private static String getKnowListMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"00FF00\">ON</font>";
		if (activeChar.showTraders())
		{
			result = "<font color=\"CC0000\">OFF</font>";
		}
		return result;
	}

	private static String getLootMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"CC0000\">OFF</font>";
		if (activeChar.isAutoLootEnabled())
		{
			result = "<font color=\"00FF00\">ON</font>";
		}
		return result;
	}

	private static String getMessageMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"CC0000\">OFF</font>";
		if (activeChar.getMessageRefusal())
		{
			result = "<font color=\"00FF00\">ON</font>";
		}
		return result;
	}

	private static String getPartyMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"CC0000\">OFF</font>";
		if (activeChar.isPartyInvProt())
		{
			result = "<font color=\"00FF00\">ON</font>";
		}
		return result;
	}

	private static String getTradeMode(L2PcInstance activeChar)
	{
		String result = "<font color=\"CC0000\">OFF</font>";
		if (activeChar.getTradeRefusal())
		{
			result = "<font color=\"00FF00\">ON</font>";
		}
		return result;
	}

	private static void showMainPage(L2PcInstance activeChar)
	{
		if (!Config.ALLOW_MENU)
			return;

		NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
		html.setFile("data/html/mods/menu.htm");
		html.replace("%notraders%", getKnowListMode(activeChar));
		html.replace("%notrade%", getTradeMode(activeChar));
		html.replace("%autoloot%", getLootMode(activeChar));
		html.replace("%nomsg%", getMessageMode(activeChar));
		html.replace("%buffanim%", getBuffAnimMode(activeChar));
		html.replace("%gainexp%", getGainExpMode(activeChar));
		html.replace("%skillchance%", activeChar.isShowSkillChance() ? "<font color=\"00FF00\">ON</font>" : "<font color=\"CC0000\">OFF</font>");
		html.replace("%blockbuff%", getBlockMode(activeChar));
		html.replace("%partyblock%", getPartyMode(activeChar));
		activeChar.sendPacket(html);
	}

	@Override
	public String getDescription(String command)
	{
		if (command.equals("menu"))
			return "Displays a menu of commands.";
		return "In detail in the menu.";
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar.isInOlympiadMode() || activeChar.isInCombat())
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return true;
		}

		if (command.startsWith("menu"))
		{
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableshowSkillSuccess"))
		{
			if (!Config.SHOW_SKILL_SUCCESS_CHANCE)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_FORBIDEN_BY_ADMIN));
			}
			else
			{
				activeChar.setShowSkillChance(true);
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Show skill change is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableshowSkillSuccess"))
		{
			if (!Config.SHOW_SKILL_SUCCESS_CHANCE)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_FORBIDEN_BY_ADMIN));
			}
			else
			{
				activeChar.setShowSkillChance(false);
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Show skill change is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableblockbuff"))
		{
			if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Not avaliable now");
				return true;
			}

			activeChar.setPreventedFromReceivingBuffs(true);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Buff blocking is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableblockbuff"))
		{
			if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Not avaliable now");
				return true;
			}

			activeChar.setPreventedFromReceivingBuffs(false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Buff blocking is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.equals("ignorecolors"))
		{
			boolean val = true;
			try
			{
				val = !activeChar.getCharacterData().getBool("ignorecolors");
				activeChar.getCharacterData().set("ignorecolors", val);
			}
			catch (Exception e)
			{
				activeChar.getCharacterData().set("ignorecolors", val);
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Ignoring system colors " + (val ? "endbled" : "disabled"));

		}
		else if (command.startsWith("autoloot"))
		{
			if (!Config.ALLOW_AUTO_LOOT)
			{
				activeChar.notWorking(false);
				return true;
			}

			if (activeChar.isAutoLootEnabled())
			{
				activeChar.enableAutoLoot(false);
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "AutoLoot is off");
			}
			else
			{
				activeChar.enableAutoLoot(true);
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "AutoLoot is on");
			}
		}
		else if (command.startsWith("enableTrade"))
		{
			activeChar.setTradeRefusal(false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade blocking is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableTrade"))
		{
			activeChar.setTradeRefusal(true);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trade blocking is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableOffKnow"))
		{
			if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_ONLY_IN_PEACE_ZONE));
				showMainPage(activeChar);
				return true;
			}
			activeChar.setKnowlistMode(true);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trader is Show.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableOffKnow"))
		{
			if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_ONLY_IN_PEACE_ZONE));
				showMainPage(activeChar);
				return true;
			}
			activeChar.setKnowlistMode(false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Trader is Hidden.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableAutoloot"))
		{
			if (Config.ALLOW_AUTO_LOOT)
			{
				activeChar.enableAutoLoot(true);
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "AutoLoot is Enabled.");

			}
			else
			{
				activeChar.notWorking(false);
			}
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableAutoloot"))
		{
			if (Config.ALLOW_AUTO_LOOT)
			{
				activeChar.enableAutoLoot(false);
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "AutoLoot is Disabled.");
			}
			else
			{
				activeChar.notWorking(false);
			}
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableGainExp"))
		{
			if (Config.ALLOW_USE_EXP_SET)
			{
				activeChar.canGainExp(true);
			}
			else
			{
				activeChar.notWorking(false);
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Experience is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableGainExp"))
		{
			if (Config.ALLOW_USE_EXP_SET)
			{
				activeChar.canGainExp(false);
			}
			else
			{
				activeChar.notWorking(false);
			}
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Experience is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableMessage"))
		{
			activeChar.setMessageRefusal(false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Message is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableMessage"))
		{
			activeChar.setMessageRefusal(true);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Message is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("enableBuffAnim"))
		{
			activeChar.setShowBuffAnim(true);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Buff Animation is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("disableBuffAnim"))
		{
			activeChar.setShowBuffAnim(false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Buff Animation is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("blockparty"))
		{
			activeChar.setIsPartyInvProt(true);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Party blocking is Enabled.");
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("unblockparty"))
		{
			activeChar.setIsPartyInvProt(false);
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Party blocking is Disabled.");
			showMainPage(activeChar);
			return true;
		}
		return false;
	}

}