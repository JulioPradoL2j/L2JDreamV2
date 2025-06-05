package com.dream.game.handler.voiced;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

public class ClassMaster implements IVoicedCommandHandler
{

	private static final String NOT_ALLOWED = "<html><body><br><br><center>Not allowed now</center></body></html>";

	private static void changeClass(L2PcInstance player, int val)
	{
		player.setClassId(val);

		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		}
		else
		{
			player.setBaseClass(player.getActiveClass());
		}

		player.broadcastUserInfo();
		player.broadcastClassIcon();
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"change_class"
		};
	}

	@Override
	public String getDescription(String command)
	{
		return "Call the dialog box ClassMaster";
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]
		{
			"classmaster"
		};
	}

	public String handleCommand(L2PcInstance activeChar, String command, String args)
	{
		return process(activeChar, args, true);
	}

	public String process(L2PcInstance player, String args, boolean isBBS)
	{
		if ((player._event != null) || Olympiad.isRegistered(player) || Olympiad.isRegisteredInComp(player) || player.isAio())
			return NOT_ALLOWED;

		if (args == null || args.length() == 0)
		{
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body><center><br><br><br><font color=\"006600\">Class Master:</font><br>");
			ClassId classId = player.getClassId();
			int level = player.getLevel();
			int jobLevel = classId.level();
			int newJobLevel = jobLevel + 1;

			if ((level >= 20 && jobLevel == 0 || level >= 40 && jobLevel == 1 || level >= 76 && jobLevel == 2) && Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))
			{
				if ((level >= 20 && jobLevel == 0 || level >= 40 && jobLevel == 1 || level >= 76 && jobLevel == 2) && Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))
				{
					sb.append("Avaliable classes:<br>");

					for (ClassId child : ClassId.values())
						if (child.childOf(classId) && child.level() == newJobLevel)
						{
							sb.append("<br><a action=\"bypass " + "voice_classmaster " + child.getId() + "\"> " + CharTemplateTable.getClassNameById(child.getId()) + "</a>");
						}

					if (Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel) != null && !Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).isEmpty())
					{
						sb.append("<br><br>Required items:");
						sb.append("<table width=270>");
						for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
						{
							int _count = Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
							sb.append("<tr><td><font color=\"LEVEL\">" + _count + "</font></td><td>" + ItemTable.getInstance().getTemplate(_itemId).getName() + "</td></tr>");
						}
						sb.append("</table>");
					}
				}

				sb.append("<br>");
			}
			else
			{
				switch (jobLevel)
				{
					case 0:
						if (Config.CLASS_MASTER_SETTINGS.isAllowed(1))
						{
							sb.append("Call me after <font color=\"LEVEL\">20</font> level.<br>");
						}
						else if (Config.CLASS_MASTER_SETTINGS.isAllowed(2))
						{
							sb.append("Call me after 1st class transfer.<br>");
						}
						else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
						{
							sb.append("Call me after 2nd class transfer.<br>");
						}
						else
						{
							sb.append("You can't change your profession.<br>");
						}
						break;
					case 1:
						if (Config.CLASS_MASTER_SETTINGS.isAllowed(2))
						{
							sb.append("Call me after <font color=\"LEVEL\">40</font> level.<br>");
						}
						else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
						{
							sb.append("Call me after 2nd class transfer.<br>");
						}
						else
						{
							sb.append("You can't change your profession.<br>");
						}
						break;
					case 2:
						if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
						{
							sb.append("Call me after <font color=\"LEVEL\">76</font> level.<br>");
						}
						else
						{
							sb.append("You can't change your profession.<br>");
						}
						break;
					case 3:
						sb.append("You reach top class.<br>");
						break;
				}
				sb.append("<br>");
			}
			sb.append("</center></body></html>");
			return sb.toString();

		}
		int val = Integer.parseInt(args);

		ClassId classId = player.getClassId();
		ClassId newClassId = ClassId.values()[val];

		int level = player.getLevel();
		int jobLevel = classId.level();
		int newJobLevel = newClassId.level();

		if (!Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))
			return NOT_ALLOWED;
		if (!newClassId.childOf(classId))
			return NOT_ALLOWED;
		if (newJobLevel != jobLevel + 1)
			return NOT_ALLOWED;
		if (level < 20 && newJobLevel > 1)
			return NOT_ALLOWED;
		if (level < 40 && newJobLevel > 2)
			return NOT_ALLOWED;
		if (level < 76 && newJobLevel > 3)
			return NOT_ALLOWED;

		// Weight/Inventory check
		if (!Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).isEmpty())
			if (player.getWeightPenalty() >= 3 || player.getInventoryLimit() * 0.8 <= player.getInventory().getSize())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
				return NOT_ALLOWED;
			}

		for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
		{
			int _count = Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
			if (player.getInventory().getInventoryItemCount(_itemId, -1) < _count)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return NOT_ALLOWED;
			}
		}
		for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
		{
			int _count = Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
			player.destroyItemByItemId("ClassMaster", _itemId, _count, player, true);
		}
		for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).keySet())
		{
			int _count = Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).get(_itemId);
			player.addItem("ClassMaster", _itemId, _count, player, true);
		}

		changeClass(player, val);

		player.rewardSkills();

		if (newJobLevel == 3)
		{
			player.sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
		}
		else
		{
			player.sendPacket(SystemMessageId.CLASS_TRANSFER);
		}

		TextBuilder sb = new TextBuilder();
		sb.append("<html><body>");
		sb.append("<br><br><center>");
		sb.append("Congrats, you got class: <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getClassId().getId()) + "</font>.");
		sb.append("</center></body></html>");
		player.refreshOverloaded();
		player.refreshExpertisePenalty();
		player.broadcastFullInfo();
		return sb.toString();
	}


	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		try
		{
			Integer.parseInt(target);
		}
		catch (NumberFormatException e)
		{
			target = null;
		}
		msg.setHtml(process(activeChar, target, false));
		activeChar.sendPacket(msg);
		return false;
	}
}