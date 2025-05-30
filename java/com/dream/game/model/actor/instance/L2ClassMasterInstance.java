/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.manager.QuestManager;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.TutorialCloseHtml;
import com.dream.game.network.serverpackets.TutorialShowHtml;
import com.dream.game.network.serverpackets.TutorialShowQuestionMark;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.lang.L2TextBuilder;

public final class L2ClassMasterInstance extends L2NpcInstance
{
	private static void changeClass(L2PcInstance player, int val)
	{
		if (_log.isDebugEnabled())
		{
			_log.debug("Changing class to ClassId:" + val);
		}
		player.setClassId(val);

		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		}
		else
		{
			ClassId classId = ClassId.getClassIdByOrdinal(player.getActiveClass());

			if (classId.getParent() != null)
			{
				while (classId.level() == 0)
				{
					classId = classId.getParent();
				}
			}

			player.setBaseClass(classId);
		}

		player.broadcastUserInfo();
		player.broadcastClassIcon();
	}

	private static final boolean checkAndChangeClass(L2PcInstance player, int val)
	{
		final ClassId currentClassId = player.getClassId();
		int newJobLevel = currentClassId.level() + 1;
		if (getMinLevel(currentClassId.level()) > player.getLevel() && !Config.ALT_CLASS_MASTER_ENTIRE_TREE)
			return false;

		if (!validateClassId(currentClassId, val))
			return false;

		if (!checkDestroyAndRewardItems(player, newJobLevel))
			return false;

		player.setClassId(val);

		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		}
		else
		{
			player.setBaseClass(player.getActiveClass());
		}

		Quest q = QuestManager.getInstance().getQuest("SkillTransfer");
		if (q != null)
		{
			q.startQuestTimer("givePormanders", 1, null, player);
		}

		player.broadcastUserInfo();
		return true;
	}

	private static boolean checkDestroyAndRewardItems(L2PcInstance player, int newJobLevel)
	{
		// Check if player have all required items for class transfer
		for (Integer _itemId : Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
		{
			int _count = Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
			if (player.getInventory().getInventoryItemCount(_itemId, -1) < _count)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return false;
			}
		}

		// Get all required items for class transfer
		for (Integer _itemId : Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
		{
			int _count = Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
			player.destroyItemByItemId("ClassMaster", _itemId, _count, player, true);
		}

		// Reward player with items
		for (Integer _itemId : Config.ALT_CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).keySet())
		{
			int _count = Config.ALT_CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).get(_itemId);
			player.addItem("ClassMaster", _itemId, _count, player, true);
		}

		return true;
	}

	/**
	 * Returns minimum player level required for next class transfer
	 * @param level - current skillId level (0 - start, 1 - first, etc)
	 * @return
	 */
	private static final int getMinLevel(int level)
	{
		switch (level)
		{
			case 0:
				return 20;
			case 1:
				return 40;
			case 2:
				return 76;
			default:
				return Integer.MAX_VALUE;
		}
	}

	public static final void onTutorialLink(L2PcInstance player, String request)
	{
		if (!Config.ALT_CLASS_MASTER_TUTORIAL || request == null || !request.startsWith("CO"))
			return;

		if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
			return;

		try
		{
			int val = Integer.parseInt(request.substring(2));
			checkAndChangeClass(player, val);
		}
		catch (Exception e)
		{
		}
		player.sendPacket(new TutorialCloseHtml());
	}

	public static final void onTutorialQuestionMark(L2PcInstance player, int number)
	{
		if (!Config.ALT_CLASS_MASTER_TUTORIAL || number != 1001)
			return;

		showTutorialHtml(player);
	}

	// L2JServer CM methods below

	private static final void showHtmlMenu(L2PcInstance player, int objectId, int level)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(objectId);

		if (!Config.ALT_L2J_CLASS_MASTER)
		{
			html.setFile("data/html/classmaster/disabled.htm");
		}
		else
		{
			final ClassId currentClassId = player.getClassId();
			if (currentClassId.level() >= level)
			{
				html.setFile("data/html/classmaster/nomore.htm");
			}
			else
			{
				final int minLevel = getMinLevel(currentClassId.level());
				if (player.getLevel() >= minLevel || Config.ALT_CLASS_MASTER_ENTIRE_TREE)
				{
					final L2TextBuilder menu = L2TextBuilder.newInstance(100);
					for (ClassId cid : ClassId.values())
						if (validateClassId(currentClassId, cid) && cid.level() == level)
						{
							menu.append("<a action=\"bypass -h npc_%objectId%_change_class ");
							menu.append(cid.getId());
							menu.append("\">");
							menu.append(CharTemplateTable.getClassNameById(cid.getId()));
							menu.append("</a><br>");
						}

					if (menu.length() > 0)
					{
						html.setFile("data/html/classmaster/template.htm");
						html.replace("%name%", CharTemplateTable.getClassNameById(currentClassId.getId()));
						html.replace("%menu%", menu.moveToString());
					}
					else
					{
						html.setFile("data/html/classmaster/comebacklater.htm");
						html.replace("%level%", String.valueOf(getMinLevel(level - 1)));
					}
				}
				else if (minLevel < Integer.MAX_VALUE)
				{
					html.setFile("data/html/classmaster/comebacklater.htm");
					html.replace("%level%", String.valueOf(minLevel));
				}
				else
				{
					html.setFile("data/html/classmaster/nomore.htm");
				}
			}
		}

		html.replace("%objectId%", String.valueOf(objectId));
		player.sendPacket(html);
	}

	public static final void showQuestionMark(L2PcInstance player)
	{
		if (!Config.ALT_CLASS_MASTER_TUTORIAL)
			return;

		final ClassId classId = player.getClassId();
		if (getMinLevel(classId.level()) > player.getLevel())
			return;

		player.sendPacket(new TutorialShowQuestionMark(1001));
	}

	private static final void showTutorialHtml(L2PcInstance player)
	{
		final ClassId currentClassId = player.getClassId();
		int newJobLevel = currentClassId.level() + 1;
		if (getMinLevel(currentClassId.level()) > player.getLevel() && !Config.ALT_CLASS_MASTER_ENTIRE_TREE)
			return;

		String msg = HtmCache.getInstance().getHtm("data/html/classmaster/tutorialtemplate.htm");

		msg = msg.replaceAll("%name%", CharTemplateTable.getClassNameById(currentClassId.getId()));

		final L2TextBuilder menu = L2TextBuilder.newInstance(100);
		for (ClassId cid : ClassId.values())
			if (validateClassId(currentClassId, cid))
			{
				menu.append("<a action=\"link CO");
				menu.append(cid.getId());
				menu.append("\">");
				menu.append(CharTemplateTable.getClassNameById(cid.getId()));
				menu.append("</a><br>");
			}

		if (Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel) != null && !Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).isEmpty())
		{
			menu.append("<br><br>Item(s) required for class change:");
			menu.append("<table width=270>");
			for (Integer _itemId : Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
			{
				int _count = Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
				menu.append("<tr><td><font color=\"LEVEL\">" + _count + "</font></td><td>" + ItemTable.getInstance().getTemplate(_itemId).getName() + "</td></tr>");
			}
			menu.append("</table><br><br>");
		}

		msg = msg.replaceAll("%menu%", menu.moveToString());
		player.sendPacket(new TutorialShowHtml(msg));
	}

	/**
	 * Returns true if class change is possible
	 * @param oldCID current player ClassId
	 * @param newCID new ClassId
	 * @return true if class change is possible
	 */
	private static final boolean validateClassId(ClassId oldCID, ClassId newCID)
	{
		if (newCID == null || newCID.getRace() == null)
			return false;

		if (oldCID.equals(newCID.getParent()))
			return true;

		if (Config.ALT_CLASS_MASTER_ENTIRE_TREE && newCID.childOf(oldCID))
			return true;

		return false;
	}

	/**
	 * Returns true if class change is possible
	 * @param oldCID current player ClassId
	 * @param val new class index
	 * @return
	 */
	private static final boolean validateClassId(ClassId oldCID, int val)
	{
		try
		{
			return validateClassId(oldCID, ClassId.values()[val]);
		}
		catch (Exception e)
		{
			// possible ArrayOutOfBoundsException
		}
		return false;
	}

	public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";

		if (val == 0)
		{
			filename = "" + npcId;
		}
		else
		{
			filename = npcId + "-" + val;
		}

		return "data/html/classmaster/" + filename + ".htm";
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);

		if (this != player.getTarget())
		{
			player.setTarget(this);

			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
				return;
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			L2TextBuilder sb = L2TextBuilder.newInstance();
			sb.append("<html><body>");
			sb.append(getName() + ":<br>");
			sb.append("<br>");

			ClassId classId = player.getClassId();
			int level = player.getLevel();
			int jobLevel = classId.level();

			int newJobLevel = jobLevel + 1;

			if (player.isGM())
			{
				showChatWindowChooseClass(player);
			}

			if ((level >= 20 && jobLevel == 0 || level >= 40 && jobLevel == 1 || level >= 76 && jobLevel == 2) && Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(newJobLevel) || Config.ALT_CLASS_MASTER_STRIDER_UPDATE)
			{
				if ((level >= 20 && jobLevel == 0 || level >= 40 && jobLevel == 1 || level >= 76 && jobLevel == 2) && Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))
				{
					sb.append("You can change your occupation to following:<br>");

					for (ClassId child : ClassId.values())
						if (child.childOf(classId) && child.level() == newJobLevel)
						{
							sb.append("<br><a action=\"bypass -h npc_" + getObjectId() + "_change_class " + child.getId() + "\"> " + CharTemplateTable.getClassNameById(child.getId()) + "</a>");
						}

					if (Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel) != null && !Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).isEmpty())
					{
						sb.append("<br><br>Item(s) required for class change:");
						sb.append("<table width=270>");
						for (Integer _itemId : Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
						{
							int _count = Config.ALT_CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
							sb.append("<tr><td><font color=\"LEVEL\">" + _count + "</font></td><td>" + ItemTable.getInstance().getTemplate(_itemId).getName() + "</td></tr>");
						}
						sb.append("</table>");
					}
				}

				if (Config.ALT_CLASS_MASTER_STRIDER_UPDATE)
				{
					sb.append("<table width=270>");
					sb.append("<tr><td><br></td></tr>");
					sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_upgrade_hatchling\">Upgrade Hatchling to Strider</a></td></tr>");
					sb.append("</table>");
				}
				sb.append("<br>");
			}
			else
			{
				switch (jobLevel)
				{
					case 0:
						if (Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(1))
						{
							sb.append("Come back here when you reached level 20 to change your class.<br>");
						}
						else if (Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(2))
						{
							sb.append("Come back after your first occupation change.<br>");
						}
						else if (Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(3))
						{
							sb.append("Come back after your second occupation change.<br>");
						}
						else
						{
							sb.append("I can't change your occupation.<br>");
						}
						break;
					case 1:
						if (Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(2))
						{
							sb.append("Come back here when you reached level 40 to change your class.<br>");
						}
						else if (Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(3))
						{
							sb.append("Come back after your second occupation change.<br>");
						}
						else
						{
							sb.append("I can't change your occupation.<br>");
						}
						break;
					case 2:
						if (Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(3))
						{
							sb.append("Come back here when you reached level 76 to change your class.<br>");
						}
						else
						{
							sb.append("I can't change your occupation.<br>");
						}
						break;
					case 3:
						sb.append("There is no class change available for you anymore.<br>");
						break;
				}
				// If the player hasn't available class , he can change pet too...
				if (Config.ALT_CLASS_MASTER_STRIDER_UPDATE)
				{
					sb.append("<table width=270>");
					sb.append("<tr><td><br></td></tr>");
					sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_upgrade_hatchling\">Upgrade Hatchling to Strider</a></td></tr>");
					sb.append("</table>");
				}
				sb.append("<br>");
			}

			for (Quest q : Quest.findAllEvents())
			{
				sb.append("Event: <a action=\"bypass -h Quest " + q.getName() + "\">" + q.getDescr() + "</a><br>");
			}
			sb.append("</body></html>");
			html.setHtml(sb.moveToString());
			player.sendPacket(html);

		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (Config.ALT_L2J_CLASS_MASTER)
		{
			if (command.startsWith("1stClass"))
			{
				if (player.isGM())
				{
					showChatWindow1st(player);
				}
				showHtmlMenu(player, getObjectId(), 1);
			}
			else if (command.startsWith("2ndClass"))
			{
				if (player.isGM())
				{
					showChatWindow2nd(player);
				}
				showHtmlMenu(player, getObjectId(), 2);
			}
			else if (command.startsWith("3rdClass"))
			{
				if (player.isGM())
				{
					showChatWindow3rd(player);
				}
				showHtmlMenu(player, getObjectId(), 3);
			}
			else if (command.startsWith("baseClass"))
			{
				if (player.isGM())
				{
					showChatWindowBase(player);
				}
			}
			else if (command.startsWith("change_class"))
			{
				int val = Integer.parseInt(command.substring(13));

				if (checkAndChangeClass(player, val))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/classmaster/ok.htm");
					html.replace("%name%", CharTemplateTable.getClassNameById(val));
					player.sendPacket(html);
				}
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
			return;
		}

		if (command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));

			ClassId classId = player.getClassId();
			ClassId newClassId = ClassId.values()[val];

			int level = player.getLevel();
			int jobLevel = classId.level();
			int newJobLevel = newClassId.level();

			// -- Exploit prevention
			// Prevents changing if config option disabled
			if (!Config.ALT_CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))
				return;

			// Prevents changing to class not in same class tree
			if (!newClassId.childOf(classId))
				return;

			// Prevents changing between same level jobs
			if (newJobLevel != jobLevel + 1)
				return;

			// Check for player level
			if (level < 20 && newJobLevel > 1)
				return;
			if (level < 40 && newJobLevel > 2)
				return;
			if (level < 76 && newJobLevel > 3)
				return;
			// -- Prevention ends

			if (!checkDestroyAndRewardItems(player, newJobLevel))
				return;

			changeClass(player, val);

			player.rewardSkills();

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			L2TextBuilder sb = L2TextBuilder.newInstance();
			sb.append("<html><body>");
			sb.append(getName() + ":<br>");
			sb.append("<br>");
			sb.append("You have now become a <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getClassId().getId()) + "</font>.");
			sb.append("</body></html>");
			html.setHtml(sb.moveToString());
			player.sendPacket(html);

			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			// Update the expertise status of the L2PcInstance
			player.refreshExpertisePenalty();
		}
		else if (command.startsWith("upgrade_hatchling") && Config.ALT_CLASS_MASTER_STRIDER_UPDATE)
		{
			boolean canUpgrade = false;
			if (player.getPet() != null)
			{
				if (player.getPet().getNpcId() == 12311 || player.getPet().getNpcId() == 12312 || player.getPet().getNpcId() == 12313)
				{
					if (player.getPet().getLevel() >= 55)
					{
						canUpgrade = true;
					}
					else
					{
						player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The level of your hatchling is too low to be upgraded.");
					}
				}
				else
				{
					player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have to summon your hatchling.");
				}
			}
			else
			{
				player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You have to summon your hatchling if you want to upgrade him.");
			}

			if (!canUpgrade)
				return;

			int[] hatchCollar =
			{
				3500,
				3501,
				3502
			};
			int[] striderCollar =
			{
				4422,
				4423,
				4424
			};

			for (int i = 0; i < 3; i++)
			{
				L2ItemInstance collar = player.getInventory().getItemByItemId(hatchCollar[i]);

				if (collar != null)
				{
					// Unsummon the hatchling
					player.getPet().unSummon(player);
					player.destroyItem("ClassMaster", collar, player, true);
					player.addItem("ClassMaster", striderCollar[i], 1, player, true, true);

					return;
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow1st(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<table width=200>");
		sb.append("<tr><td><center>GM Class Master:</center></td></tr>");
		sb.append("<tr><td><br></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 1\">Advance to " + CharTemplateTable.getClassNameById(1) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 4\">Advance to " + CharTemplateTable.getClassNameById(4) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 7\">Advance to " + CharTemplateTable.getClassNameById(7) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 11\">Advance to " + CharTemplateTable.getClassNameById(11) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 15\">Advance to " + CharTemplateTable.getClassNameById(15) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 19\">Advance to " + CharTemplateTable.getClassNameById(19) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 22\">Advance to " + CharTemplateTable.getClassNameById(22) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 26\">Advance to " + CharTemplateTable.getClassNameById(26) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 29\">Advance to " + CharTemplateTable.getClassNameById(29) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 32\">Advance to " + CharTemplateTable.getClassNameById(32) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 35\">Advance to " + CharTemplateTable.getClassNameById(35) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 39\">Advance to " + CharTemplateTable.getClassNameById(39) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 42\">Advance to " + CharTemplateTable.getClassNameById(42) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 45\">Advance to " + CharTemplateTable.getClassNameById(45) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 47\">Advance to " + CharTemplateTable.getClassNameById(47) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 50\">Advance to " + CharTemplateTable.getClassNameById(50) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 54\">Advance to " + CharTemplateTable.getClassNameById(54) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 56\">Advance to " + CharTemplateTable.getClassNameById(56) + "</a></td></tr>");
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
		return;
	}

	private void showChatWindow2nd(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<table width=200>");
		sb.append("<tr><td><center>GM Class Master:</center></td></tr>");
		sb.append("<tr><td><br></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 2\">Advance to " + CharTemplateTable.getClassNameById(2) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 3\">Advance to " + CharTemplateTable.getClassNameById(3) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 5\">Advance to " + CharTemplateTable.getClassNameById(5) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 6\">Advance to " + CharTemplateTable.getClassNameById(6) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 8\">Advance to " + CharTemplateTable.getClassNameById(8) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 9\">Advance to " + CharTemplateTable.getClassNameById(9) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 12\">Advance to " + CharTemplateTable.getClassNameById(12) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 13\">Advance to " + CharTemplateTable.getClassNameById(13) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 14\">Advance to " + CharTemplateTable.getClassNameById(14) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 16\">Advance to " + CharTemplateTable.getClassNameById(16) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 17\">Advance to " + CharTemplateTable.getClassNameById(17) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 20\">Advance to " + CharTemplateTable.getClassNameById(20) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 21\">Advance to " + CharTemplateTable.getClassNameById(21) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 23\">Advance to " + CharTemplateTable.getClassNameById(23) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 24\">Advance to " + CharTemplateTable.getClassNameById(24) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 27\">Advance to " + CharTemplateTable.getClassNameById(27) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 28\">Advance to " + CharTemplateTable.getClassNameById(28) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 30\">Advance to " + CharTemplateTable.getClassNameById(30) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 33\">Advance to " + CharTemplateTable.getClassNameById(33) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 34\">Advance to " + CharTemplateTable.getClassNameById(34) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 36\">Advance to " + CharTemplateTable.getClassNameById(36) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 37\">Advance to " + CharTemplateTable.getClassNameById(37) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 40\">Advance to " + CharTemplateTable.getClassNameById(40) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 41\">Advance to " + CharTemplateTable.getClassNameById(41) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 43\">Advance to " + CharTemplateTable.getClassNameById(43) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 46\">Advance to " + CharTemplateTable.getClassNameById(46) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 48\">Advance to " + CharTemplateTable.getClassNameById(48) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 51\">Advance to " + CharTemplateTable.getClassNameById(51) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 52\">Advance to " + CharTemplateTable.getClassNameById(52) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 55\">Advance to " + CharTemplateTable.getClassNameById(55) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 57\">Advance to " + CharTemplateTable.getClassNameById(57) + "</a></td></tr>");
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
		return;
	}

	private void showChatWindow3rd(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<table width=200>");
		sb.append("<tr><td><center>GM Class Master:</center></td></tr>");
		sb.append("<tr><td><br></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 88\">Advance to " + CharTemplateTable.getClassNameById(88) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 89\">Advance to " + CharTemplateTable.getClassNameById(89) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 90\">Advance to " + CharTemplateTable.getClassNameById(90) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 91\">Advance to " + CharTemplateTable.getClassNameById(91) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 92\">Advance to " + CharTemplateTable.getClassNameById(92) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 93\">Advance to " + CharTemplateTable.getClassNameById(93) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 94\">Advance to " + CharTemplateTable.getClassNameById(94) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 95\">Advance to " + CharTemplateTable.getClassNameById(95) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 96\">Advance to " + CharTemplateTable.getClassNameById(96) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 97\">Advance to " + CharTemplateTable.getClassNameById(97) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 98\">Advance to " + CharTemplateTable.getClassNameById(98) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 99\">Advance to " + CharTemplateTable.getClassNameById(99) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 100\">Advance to " + CharTemplateTable.getClassNameById(100) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 101\">Advance to " + CharTemplateTable.getClassNameById(101) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 102\">Advance to " + CharTemplateTable.getClassNameById(102) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 103\">Advance to " + CharTemplateTable.getClassNameById(103) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 104\">Advance to " + CharTemplateTable.getClassNameById(104) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 105\">Advance to " + CharTemplateTable.getClassNameById(105) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 106\">Advance to " + CharTemplateTable.getClassNameById(106) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 107\">Advance to " + CharTemplateTable.getClassNameById(107) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 108\">Advance to " + CharTemplateTable.getClassNameById(108) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 109\">Advance to " + CharTemplateTable.getClassNameById(109) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 110\">Advance to " + CharTemplateTable.getClassNameById(110) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 111\">Advance to " + CharTemplateTable.getClassNameById(111) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 112\">Advance to " + CharTemplateTable.getClassNameById(112) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 113\">Advance to " + CharTemplateTable.getClassNameById(113) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 114\">Advance to " + CharTemplateTable.getClassNameById(114) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 115\">Advance to " + CharTemplateTable.getClassNameById(115) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 116\">Advance to " + CharTemplateTable.getClassNameById(116) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 117\">Advance to " + CharTemplateTable.getClassNameById(117) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 118\">Advance to " + CharTemplateTable.getClassNameById(118) + "</a></td></tr>");
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
		return;
	}

	private void showChatWindowBase(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<table width=200>");
		sb.append("<tr><td><center>GM Class Master:</center></td></tr>");
		sb.append("<tr><td><br></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 0\">Advance to " + CharTemplateTable.getClassNameById(0) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 10\">Advance to " + CharTemplateTable.getClassNameById(10) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 18\">Advance to " + CharTemplateTable.getClassNameById(18) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 25\">Advance to " + CharTemplateTable.getClassNameById(25) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 31\">Advance to " + CharTemplateTable.getClassNameById(31) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 38\">Advance to " + CharTemplateTable.getClassNameById(38) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 44\">Advance to " + CharTemplateTable.getClassNameById(44) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 49\">Advance to " + CharTemplateTable.getClassNameById(49) + "</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class 53\">Advance to " + CharTemplateTable.getClassNameById(53) + "</a></td></tr>");
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
		return;
	}

	private void showChatWindowChooseClass(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<table width=200>");
		sb.append("<tr><td><center>GM Class Master:</center></td></tr>");
		sb.append("<tr><td><br></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_baseClass\">Base Classes.</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_1stClass\">1st Classes.</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_2ndClass\">2nd Classes.</a></td></tr>");
		sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_3rdClass\">3rd Classes.</a></td></tr>");
		sb.append("<tr><td><br></td></tr>");
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
		return;
	}
}