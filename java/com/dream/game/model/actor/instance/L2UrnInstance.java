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

import java.util.StringTokenizer;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public final class L2UrnInstance extends L2NpcInstance
{
	protected static int _ingredient1;

	protected static int _ingCount1;

	protected static int _ingredient2;

	protected static int _ingCount2;

	protected static int _product;

	protected static int _prodCount;
	protected static int _mixTemp;
	protected static int mixChance;
	protected static int tempChance;

	protected static boolean checkUrnSuccess(int urnTemperature)
	{
		int mixFail = Config.ALT_URN_TEMP_FAIL;
		int mixTemperature = urnTemperature - 1;
		if (mixFail < 10)
		{
			mixFail = 10;
		}
		else if (mixFail > 40)
		{
			mixFail = 40;
		}
		mixChance = Rnd.get(100);
		tempChance = 100 - mixTemperature * mixFail;

		return mixChance < tempChance;
	}

	public static int getUrnItemsCount(L2PcInstance player, int itemId)
	{
		int count = 0;

		for (L2ItemInstance item : player.getInventory().getItems())
			if (item.getItemId() == itemId)
			{
				count += item.getCount();
			}
		return count;
	}

	public static void giveUrnItems(L2PcInstance player, int itemId, int count, int enchantlevel)
	{
		if (count <= 0)
			return;

		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());

		if (item == null)
			return;
		if (enchantlevel > 0)
		{
			item.setEnchantLevel(enchantlevel);
		}

		if (itemId == 57)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));
		}
		else if (count > 1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(count));
		}
		else
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
		}
	}

	public static void playSound(L2PcInstance player, String sound)
	{
		player.sendPacket(new PlaySound(sound));
	}

	public static void takeUrnItems(L2PcInstance player, int itemId, int count)
	{
		L2ItemInstance item = player.getInventory().getItemByItemId(itemId);

		if (item == null)
			return;

		if (count < 0 || count > item.getCount())
		{
			count = item.getCount();
		}

		if (itemId == 57)
		{
			player.reduceAdena("Quest", count, player, true);
		}
		else
		{
			player.destroyItemByItemId("Quest", itemId, count, player, true);
		}
	}

	public L2UrnInstance(int objectId, L2NpcTemplate template)
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
		else
		{
			showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();

		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}

		if (actualCommand.equalsIgnoreCase("make_low"))
		{
			if (!val.isEmpty())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				String filename = "data/html/urn/low" + val + ".htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
			String filename = "data/html/urn/low.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("make_high"))
		{
			if (!val.isEmpty())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				String filename = "data/html/urn/high" + val + ".htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}

			player.sendPacket(ActionFailed.STATIC_PACKET);
			String filename = "data/html/urn/high.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("urn_stone"))
		{
			if (getUrnItemsCount(player, 5904) >= 1)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				String filename = "data/html/urn/insertstone.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);

				return;
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			String filename = "data/html/urn/nostone.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("urn_mix"))
		{
			if (!val.isEmpty())
			{
				String w1 = val;
				int ingId1 = Integer.parseInt(w1);
				val = st.nextToken();
				String x1 = val;
				int ingCount1 = Integer.parseInt(x1);
				val = st.nextToken();
				String y1 = val;
				int ingId2 = Integer.parseInt(y1);
				val = st.nextToken();
				String z1 = val;
				int ingCount2 = Integer.parseInt(z1);
				val = st.nextToken();
				String go1 = val;
				int prodId = Integer.parseInt(go1);
				val = st.nextToken();
				String go2 = val;
				int tempSet = Integer.parseInt(go2);
				val = st.nextToken();
				String go3 = val;
				boolean mixLvl = Boolean.parseBoolean(go3);
				val = st.nextToken();
				String prodName1 = val;
				runUrnMix(player, ingId1, ingCount1, ingId2, ingCount2, prodId, tempSet, mixLvl, prodName1);

			}
		}
		else if (actualCommand.equalsIgnoreCase("urn_main"))
		{
			showMessageWindow(player);
		}
	}

	protected void runUrnMix(L2PcInstance player, int ingId1, int ingCount1, int ingId2, int ingCount2, int prodId, int tempSet, boolean mixLvl, String prodName2)
	{
		boolean correctMix1 = false;
		boolean correctMix2 = false;
		int ingHas1 = getUrnItemsCount(player, ingId1);
		int ingHas2 = getUrnItemsCount(player, ingId2);

		if (ingHas1 >= ingCount1)
		{
			takeUrnItems(player, ingId1, ingCount1);
			correctMix1 = true;
		}
		else
		{
			takeUrnItems(player, ingId1, ingHas1);
		}
		if (ingHas2 >= ingCount2)
		{
			takeUrnItems(player, ingId2, ingCount2);
			correctMix2 = true;
		}
		else
		{
			takeUrnItems(player, ingId2, ingHas2);
		}

		if (checkUrnSuccess(tempSet) && correctMix1 && correctMix2)
		{
			giveUrnItems(player, prodId, tempSet, 0);
			showSuccessWindow(player, mixLvl, prodName2, tempSet);
		}
		else
		{
			showFailureWindow(player);
		}
	}

	private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	public void showFailureWindow(L2PcInstance player)
	{

		StringBuilder msg = new StringBuilder("<html><body>");
		msg.append("%npcname%:<br><br>");
		msg.append("The contents burble and boil, smoke and steam rise from the urn.<BR>");
		msg.append("You peer into the urn to see nothing remains, the temperature was too hot!<br>");
		msg.append("<font color=\"LEVEL\">You have failed!</font><br>");
		msg.append("<table width=200>");
		msg.append("<tr><td><button value=\"Back\" action=\"bypass -h npc_%objectId%_urn_main\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		msg.append("</table>");
		msg.append("</body></html>");

		sendHtmlMessage(player, msg.toString());
	}

	public void showMessageWindow(L2PcInstance player)
	{
		StringBuilder msg = new StringBuilder("<html><body>");
		msg.append("%npcname%:<br><br>");
		msg.append("If you have brought the <font color=\"LEVEL\">Mixing Stone</font> then you must insert it to continue!<BR>");
		msg.append("<table width=200>");
		msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_urn_stone\">Insert Stone</a></td></tr>");
		msg.append("</table>");
		msg.append("</body></html>");

		sendHtmlMessage(player, msg.toString());
	}

	public void showSuccessWindow(L2PcInstance player, boolean urnLvl, String prodName3, int prodNum3)
	{
		String rankName = "";
		String urnEffect = "";
		int randomEffect = Rnd.get(3);
		if (randomEffect == 3)
		{
			urnEffect = "The contents burble and boil, smoke and steam rise from the urn.";
		}
		else if (randomEffect == 2)
		{
			urnEffect = "A swirl of red and yellow steam rises from the urn.";
		}
		else if (randomEffect == 1)
		{
			urnEffect = "Flame bursts from the urn with a cloud of black smoke.";
		}
		else if (randomEffect == 0)
		{
			urnEffect = "Black smoke billows forth from the urn and with a loud bang you are knocked to the floor.";
		}

		if (urnLvl)
		{
			rankName = "Apprentice Alchemist";
		}
		else
		{
			rankName = "Master Alchemist";
		}

		StringBuilder msg = new StringBuilder("<html><body>");
		msg.append("%npcname%:<br><br>");
		msg.append(urnEffect + "<BR>");
		msg.append("You peer into the urn to see " + prodName3 + " (" + prodNum3 + ") !<br>");
		msg.append("<font color=\"LEVEL\">Success!</font><br>");
		msg.append("You may yet become a " + rankName + " !<br>");
		msg.append("<table width=200>");
		msg.append("<tr><td><button value=\"Back\" action=\"bypass -h npc_%objectId%_urn_main\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		msg.append("</table>");
		msg.append("</body></html>");

		sendHtmlMessage(player, msg.toString());
	}
}