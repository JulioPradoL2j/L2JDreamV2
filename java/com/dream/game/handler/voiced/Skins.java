package com.dream.game.handler.voiced;

import com.dream.Message;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.DressMeData;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class Skins implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
		"skins",
		"skin1",
		"skin2",
		"skin3",
		"skin4",
		"skin5",
		"skin6",
		"skin7",
		"skin8",
		"skin9",
		"skin10",
		"skin11",
		"skin12",
		"skin13",
		"skin14",
		"skin15",
		"skin16",
		"skin17",
		"skin18",
		"skin19",
		"skin20",
		"skin21",
		"skin22",
		"skin23",
		"skin24",
		"skin25",
		"skin26",
		"skin27",
		"skin28",
		"skin29",
		"skin30",
		"skin31",
		"hairoff",
		"skinoff",
		"skinspage1",
		"skinspage2"
	};

	private static void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
		html.setFile("data/html/mods/voiced_skins.htm");
		html.replace("%player%", activeChar.getName());
		activeChar.sendPacket(html);
	}

	private static void showPage1(L2PcInstance activeChar)
	{
		NpcHtmlMessage html1 = new NpcHtmlMessage(activeChar.getObjectId());
		html1.setFile("data/html/mods/voiced_skins-1.htm");
		html1.replace("%player%", activeChar.getName());
		activeChar.sendPacket(html1);
	}

	private static void showPage2(L2PcInstance activeChar)
	{
		NpcHtmlMessage html2 = new NpcHtmlMessage(activeChar.getObjectId());
		html2.setFile("data/html/mods/voiced_skins-2.htm");
		html2.replace("%player%", activeChar.getName());
		activeChar.sendPacket(html2);
	}

	@Override
	public String getDescription(String command)
	{
		return "Skins System.";
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar.isInOlympiadMode() || activeChar.isInCombat())
		{
			activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return true;
		}

		if (command.equals("skinspage2"))
		{
			showPage2(activeChar);
			return true;
		}

		if (command.equals("skinspage1"))
		{
			showPage1(activeChar);
			return true;
		}

		if (command.equals("skins"))
		{
			showMainPage(activeChar);
			return true;
		}
		else if (command.equals("skin1"))
		{
			if (activeChar.getInventory().getItemByItemId(30000) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15000);
				activeChar.getDressMeData().setHairId(15001);
				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);

				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin2"))
		{
			if (activeChar.getInventory().getItemByItemId(30001) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15002);
				activeChar.getDressMeData().setHairId(15003);
				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);

				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin3"))
		{
			if (activeChar.getInventory().getItemByItemId(30002) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15004);
				activeChar.getDressMeData().setHairId(15005);
				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);

				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin4"))
		{
			if (activeChar.getInventory().getItemByItemId(30003) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15006);
				activeChar.getDressMeData().setHairId(15007);
				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin5"))
		{
			if (activeChar.getInventory().getItemByItemId(30004) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15008);
				activeChar.getDressMeData().setHairId(15009);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin6"))
		{
			if (activeChar.getInventory().getItemByItemId(30005) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15010);
				activeChar.getDressMeData().setHairId(15011);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin7"))
		{
			if (activeChar.getInventory().getItemByItemId(30006) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15012);
				activeChar.getDressMeData().setHairId(15013);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin8"))
		{
			if (activeChar.getInventory().getItemByItemId(30007) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15014);
				activeChar.getDressMeData().setHairId(15015);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin9"))
		{
			if (activeChar.getInventory().getItemByItemId(30008) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15016);
				activeChar.getDressMeData().setHairId(15017);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin10"))
		{
			if (activeChar.getInventory().getItemByItemId(30009) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15018);
				activeChar.getDressMeData().setHairId(15019);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin11"))
		{
			if (activeChar.getInventory().getItemByItemId(30010) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15020);
				activeChar.getDressMeData().setHairId(15021);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin12"))
		{
			if (activeChar.getInventory().getItemByItemId(30011) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15022);
				activeChar.getDressMeData().setHairId(15023);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin13"))
		{
			if (activeChar.getInventory().getItemByItemId(30012) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15024);
				activeChar.getDressMeData().setHairId(15025);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin14"))
		{
			if (activeChar.getInventory().getItemByItemId(30013) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15026);
				activeChar.getDressMeData().setHairId(15027);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);

				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin15"))
		{
			if (activeChar.getInventory().getItemByItemId(30014) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15028);
				activeChar.getDressMeData().setHairId(15029);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin16"))
		{
			if (activeChar.getInventory().getItemByItemId(30015) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15030);
				activeChar.getDressMeData().setHairId(15031);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin17"))
		{
			if (activeChar.getInventory().getItemByItemId(30016) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15032);
				activeChar.getDressMeData().setHairId(15033);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin18"))
		{
			if (activeChar.getInventory().getItemByItemId(30017) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15034);
				activeChar.getDressMeData().setHairId(15035);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin19"))
		{
			if (activeChar.getInventory().getItemByItemId(30018) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15036);
				activeChar.getDressMeData().setHairId(15037);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin20"))
		{
			if (activeChar.getInventory().getItemByItemId(30019) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15038);
				activeChar.getDressMeData().setHairId(15039);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin21"))
		{
			if (activeChar.getInventory().getItemByItemId(30020) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15040);
				activeChar.getDressMeData().setHairId(15041);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin22"))
		{
			if (activeChar.getInventory().getItemByItemId(30021) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15042);
				activeChar.getDressMeData().setHairId(15043);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin23"))
		{
			if (activeChar.getInventory().getItemByItemId(30022) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15044);
				activeChar.getDressMeData().setHairId(15045);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin24"))
		{
			if (activeChar.getInventory().getItemByItemId(30023) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15046);
				activeChar.getDressMeData().setHairId(15047);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin25"))
		{
			if (activeChar.getInventory().getItemByItemId(30024) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15048);
				activeChar.getDressMeData().setHairId(15049);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin26"))
		{
			if (activeChar.getInventory().getItemByItemId(30025) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15050);
				activeChar.getDressMeData().setHairId(15051);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin27"))
		{
			if (activeChar.getInventory().getItemByItemId(30026) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15052);
				activeChar.getDressMeData().setHairId(15053);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin28"))
		{
			if (activeChar.getInventory().getItemByItemId(30027) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15054);
				activeChar.getDressMeData().setHairId(15055);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin29"))
		{
			if (activeChar.getInventory().getItemByItemId(30028) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15056);
				activeChar.getDressMeData().setHairId(15057);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin30"))
		{
			if (activeChar.getInventory().getItemByItemId(30029) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(15058);
				activeChar.getDressMeData().setHairId(15059);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skin31"))
		{
			if (activeChar.getInventory().getItemByItemId(30030) != null)
			{
				if (activeChar.getDressMeData() == null)
				{
					DressMeData dmd = new DressMeData();
					activeChar.setDressMeData(dmd);
				}
				activeChar.setDressMeEnabled(true);

				activeChar.getDressMeData().setChestId(6408);
				activeChar.getDressMeData().setHairId(0);

				activeChar.getDressMeData().setGlovesId(0);
				activeChar.getDressMeData().setLegsId(0);
				activeChar.getDressMeData().setBootsId(0);
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Conditions aren't met.");
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("skinoff"))
		{
			if (activeChar.getDressMeData() != null)
			{
				activeChar.setDressMeEnabled(false);
				activeChar.broadcastUserInfo();
			}
			else
			{
				showMainPage(activeChar);
				return true;
			}
		}
		else if (command.equals("hairoff"))
		{
			if (activeChar.getDressMeData() != null)
			{
				activeChar.getDressMeData().setHairId(0);

				activeChar.broadcastUserInfo();
			}
			else
			{
				showMainPage(activeChar);
				return true;
			}
		}
		else
		{
			showMainPage(activeChar);
			return true;
		}
		return false;
	}
}