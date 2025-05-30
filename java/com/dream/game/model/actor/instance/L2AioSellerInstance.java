/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EtcStatusUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.util.CloseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javolution.text.TextBuilder;

/**
 * @author RedHoT
 */
public class L2AioSellerInstance extends L2NpcInstance
{
	public L2AioSellerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean canInteract(L2PcInstance player)
	{
		// Can't interact while casting a spell.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			return false;

		// Can't interact while died.
		if (player.isDead() || player.isFakeDeath())
			return false;

		// Can't interact sitted.
		if (player.isSitting())
			return false;

		// Can't interact in shop mode, or during a transaction or a request.
		if (player.isInStoreMode() || player.isProcessingTransaction())
			return false;

		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
			return false;

		return true;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		player.setLastFolkNPC(this);
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				showHtmlWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int itemId = Config.AIO_ITEM_ID;
		int itemCount = Config.AIO_ITEM_CNT;
		int itemCount1 = Config.AIO_ITEM_CNT1;
		int itemCount2 = Config.AIO_ITEM_CNT2;
		
		L2ItemInstance itemInstance = player.getInventory().getItemByItemId(itemId);
		
		if (command.startsWith("add_aio_1"))
		{
			if (itemInstance == null || !itemInstance.isStackable() && player.getInventory().getInventoryItemCount(itemId, -1) < itemCount)
			{
				player.sendMessage("You do not have enough items!");
				return;
			}
			if (itemInstance.isStackable())
			{
				if (!player.destroyItemByItemId("Aio Seller", itemId, itemCount, player.getTarget(), true))
				{
					player.sendMessage("You do not have enough items!");
					return;
				}
			}
			else
				for (int i = 0; i < itemCount; i++)
					player.destroyItemByItemId("Aio Seller", itemId, 1, player.getTarget(), true);
			
			doAio(player, 30);
		}
		else if (command.startsWith("add_aio_2"))
		{
			if (itemInstance == null || !itemInstance.isStackable() && player.getInventory().getInventoryItemCount(itemId, -1) < (itemCount1))
			{
				player.sendMessage("You do not have enough items!");
				return;
			}
			if (itemInstance.isStackable())
			{
				if (!player.destroyItemByItemId("Aio Seller", itemId, (itemCount1), player.getTarget(), true))
				{
					player.sendMessage("You do not have enough items!");
					return;
				}
			}
			else
				for (int i = 0; i < (itemCount1); i++)
					player.destroyItemByItemId("Aio Seller", itemId, 1, player.getTarget(), true);
			
			doAio(player, 60);
		}
		else if (command.startsWith("add_aio_3"))
		{
			if (itemInstance == null || !itemInstance.isStackable() && player.getInventory().getInventoryItemCount(itemId, -1) < (itemCount2))
			{
				player.sendMessage("You do not have enough items!");
				return;
			}
			if (itemInstance.isStackable())
			{
				if (!player.destroyItemByItemId("Aio Seller", itemId, (itemCount2), player.getTarget(), true))
				{
					player.sendMessage("You do not have enough items!");
					return;
				}
			}
			else
				for (int i = 0; i < (itemCount2); i++)
					player.destroyItemByItemId("Aio Seller", itemId, 1, player.getTarget(), true);
			
			doAio(player, 90);
		}
		else if (command.startsWith("remove_aio"))
		{
			removeAio(player);
		}
		showHtmlWindow(player);
	}
	
	private void showHtmlWindow(L2PcInstance activeChar)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("");
		
		if (Config.LIST_CLASSES_AIO.contains(activeChar.getTemplate().getClassId().getId()) && !activeChar.isSubClassActive())
		{
			replyMSG.append("<html><title>AIOx Manager</title><body>");
			replyMSG.append("<center><font color=696969>Hello " + activeChar.getName() + ", Here you can buy AIOx Status!</font><br></center>");
			replyMSG.append("<center><font color=LEVEL>Cost " + Config.AIO_COIN_TEXT + " !</font></center>");
			replyMSG.append("<img src=L2UI.SquareGray width=300 height=1><br>");
			replyMSG.append("<center>");
			replyMSG.append("<center><button value=\"30 Day's\" action=\"bypass -h npc_" + getObjectId() + "_add_aio_1\" width=139 height=21 back=\"L2UI_CH3.msnbutton_over\" fore=\"L2UI_CH3.msnbutton\"></center>");
			replyMSG.append("<img src=L2UI.SquareGray width=300 height=1><br>");
			replyMSG.append("<center><button value=\"60 Day's \" action=\"bypass -h npc_" + getObjectId() + "_add_aio_2\" width=139 height=21 back=\"L2UI_CH3.msnbutton_over\" fore=\"L2UI_CH3.msnbutton\"></center>");
			replyMSG.append("<img src=L2UI.SquareGray width=300 height=1><br>");
			replyMSG.append("<center><button value=\"90 Day's \" action=\"bypass -h npc_" + getObjectId() + "_add_aio_3\" width=139 height=21 back=\"L2UI_CH3.msnbutton_over\" fore=\"L2UI_CH3.msnbutton\"></center>");
			replyMSG.append("<img src=L2UI.SquareGray width=300 height=1><br>");

			if (activeChar.isAio())
			{
				replyMSG.append("<center><font color=696969>Hey " + activeChar.getName() + ", if you remove, you will lose all your remaining credits!</font><br></center>");
				replyMSG.append("<center><button value=\"Remove AIO\" action=\"bypass -h npc_" + getObjectId() + "_remove_aio\" width=139 height=21 back=\"L2UI_CH3.msnbutton_over\" fore=\"L2UI_CH3.msnbutton\"></center>");
			}
			replyMSG.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
			replyMSG.append("</center></body></html>");

			nhm.setHtml(replyMSG.toString());
			activeChar.sendPacket(nhm);
		}
		else
		{
			replyMSG.append("<html><title>AIOx Manager</title><body>");
			replyMSG.append("<img src=L2UI.SquareGray width=300 height=1><br>");
			replyMSG.append("<center><font color=696969>Hello " + activeChar.getName() + ", you are not in the correct class!</font><br></center>");
			replyMSG.append("<center><font color=LEVEL>Allowed Classes : " + Config.AIO_CLASSES_NAME + " !</font></center>");
			replyMSG.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
			replyMSG.append("</center></body></html>");

			nhm.setHtml(replyMSG.toString());
			activeChar.sendPacket(nhm);
		}
		
		activeChar.sendPacket(new ActionFailed());
	}
	
	
	public void doAio(L2PcInstance player, int days)
	{
		if (player == null)
			return;

		player.setAio(true);
		player.setEndTime("aio", days);
		player.getStat().addExp(player.getStat().getExpForLevel(81));
		
		Connection connection = null;
		try
		{
			connection = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=1, aio_end=? WHERE charId=?");
			statement.setLong(1, player.getAioEndTime());
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
			connection.close();
			
			if (Config.ALLOW_AIO_NCOLOR && player.isAio())
				player.getAppearance().setNameColor(Config.AIO_NCOLOR);
			
			if (Config.ALLOW_AIO_TCOLOR && player.isAio())
				player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
			
			if (Config.ALLOW_AIO_DUAL && player.isAio())
				player.getInventory().addItem("AIOx", Config.AIO_DUAL_ID, 1, player, null);

			player.rewardAioSkills();
			player.sendPacket(new EtcStatusUpdate(player));
			player.sendPacket(new ItemList(player, true));
			player.sendSkillList();
			player.sendMessage("You are now an Aio, Congratulations!");
			player.broadcastUserInfo();
		}
		catch (Exception e)
		{
				e.printStackTrace();
			
			_log.warn("could not set Aio stats to char:", e);
		}
		finally
		{
			CloseUtil.close(connection);
		}
	}
	
	
	public void removeAio(L2PcInstance player)
	{
		long xpcur = 0;
		long xpres = 0;
		player.setAio(false);
		player.setAioEndTime(0);
		player.lostAioSkills();
		if (Config.ENABLE_AIO_DELEVEL)
		{
			xpcur = player.getStat().getExp();
			xpres = player.getStat().getExpForLevel(Config.AIO_SET_DELEVEL);

			if (xpcur > xpres)
			{
				player.getStat().removeExp(xpcur - xpres);
			}
		}
		
		Connection connection = null;
		try
		{
			connection = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = connection.prepareStatement("UPDATE characters SET aio=0, aio_end=0 WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			statement.close();
			connection.close();
			
			player.lostAioSkills();
			player.getAppearance().setNameColor(0xFFFFFF);
			player.getAppearance().setTitleColor(0xFFFF77);

			if (Config.ALLOW_AIO_DUAL)
				player.getInventory().destroyItemByItemId("AIOx", Config.AIO_DUAL_ID, 1, player, null);

			player.sendPacket(new ItemList(player, true));
			player.broadcastUserInfo();
			player.sendPacket(new EtcStatusUpdate(player));
			player.sendSkillList();
			player.sendMessage("Now You are not an Aio.");
			player.broadcastUserInfo();
		}
		catch (Exception e)
		{
				e.printStackTrace();
			
			_log.warn("could not remove Aio stats of char:", e);
		}
		finally
		{
			CloseUtil.close(connection);
		}
	}
}