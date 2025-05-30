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
import com.dream.game.model.L2Clan;
import com.dream.game.model.itemcontainer.PcFreight;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EnchantResult;
import com.dream.game.network.serverpackets.PackageToList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.WareHouseDepositList;
import com.dream.game.network.serverpackets.WareHouseWithdrawalList;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Ventic - Anarchy Fixed for L2JFrozen by xadresz
 */
public class L2ProtectedWarehouseInstance extends L2NpcInstance
{
	// private static Logger _log = Logger.getLogger(L2WarehouseInstance.class.getName());

	/**
	 * Instantiates a new l2 warehouse instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2ProtectedWarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance#getHtmlPath(int, int) */
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/mods/protectedwarehouse/" + pom + ".htm";
	}

	/**
	 * Show retrieve window.
	 * @param player the player
	 */
	private static void showRetrieveWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			return;
		}

		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}

	/**
	 * Show deposit window.
	 * @param player the player
	 */
	private static void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInvetoryDisable();

		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}

	/**
	 * Show deposit window clan.
	 * @param player the player
	 */
	private static void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			}
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInvetoryDisable();

				WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
				player.sendPacket(dl);
				dl = null;
			}
		}
	}

	/**
	 * Show withdraw window clan.
	 * @param player the player
	 */
	private static void showWithdrawWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
			return;
		}

		if (player.getClan().getLevel() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());

			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
	}

	/**
	 * Show withdraw window freight.
	 * @param player the player
	 */
	private void showWithdrawWindowFreight(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		PcFreight freight = player.getFreight();

		if (freight != null)
		{
			if (freight.getSize() > 0)
			{
				if (Config.ALT_GAME_FREIGHTS)
				{
					freight.setActiveLocation(0);
				}
				else
				{
					freight.setActiveLocation(getWorldRegion().hashCode());
				}
				player.setActiveWarehouse(freight);
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT));
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			}
		}
		else
		{

		}
		freight = null;
	}

	/**
	 * Show deposit window freight.
	 * @param player the player
	 */
	private static void showDepositWindowFreight(L2PcInstance player)
	{
		// No other chars in the account of this player
		if (player.getAccountChars().size() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
		}
		// One or more chars other than this player for this account
		else
		{

			Map<Integer, String> chars = player.getAccountChars();

			if (chars.size() < 1)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			player.sendPacket(new PackageToList(chars));
			chars = null;

		}
	}

	/**
	 * Show deposit window freight.
	 * @param player the player
	 * @param obj_Id the obj_ id
	 */
	private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		L2PcInstance destChar = L2PcInstance.load(obj_Id);
		if (destChar == null)
		{
			// Something went wrong!
			if (Config.DEBUG)
			{
				_log.warn("Error retrieving a target object for char " + player.getName() + " - using freight.");
			}
			return;
		}

		PcFreight freight = destChar.getFreight();
		if (Config.ALT_GAME_FREIGHTS)
		{
			freight.setActiveLocation(0);
		}
		else
		{
			freight.setActiveLocation(getWorldRegion().hashCode());
		}
		player.setActiveWarehouse(freight);
		player.tempInvetoryDisable();
		destChar.deleteMe();
		destChar = null;
		freight = null;

		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.FREIGHT));
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();

		if (actualCommand.equalsIgnoreCase("login"))
		{
			if (!player.hasWarehouseAccount())
			{
				player.sendMessage("You don't have a warehouse account.");
				return;
			}

			if (st.countTokens() == 2)
			{
				String id = st.nextToken();
				String pwd = st.nextToken();

				if (id.equals(player.getWarehouseAccountId()) && pwd.equals(player.getWarehouseAccountPwd()))
				{
					showChatWindow(player, 4);
				}
				else
				{
					player.sendMessage("Wrong account inserted.");
				}
			}
			else
			{
				player.sendMessage("Wrong values inserted.");
			}
		}

		if (actualCommand.equalsIgnoreCase("register"))
		{
			if (player.hasWarehouseAccount())
			{
				player.sendMessage("You already have a warehouse account.");
				return;
			}

			if (st.countTokens() == 2)
			{
				String id, pwd;
				id = st.nextToken();
				pwd = st.nextToken();

				player.setWarehouseAccountId(id);
				player.setWarehouseAccountPwd(pwd);
				player.setHasWarehouseAccount(true);
				player.sendMessage("Register complete. Id: " + player.getWarehouseAccountId() + " Password: " + player.getWarehouseAccountPwd());
				showChatWindow(player, 0);
			}
		}

		// Like L2OFF if you have enchant window opened it will be closed
		if (player.getActiveEnchantItem() != null)
			player.sendPacket(new EnchantResult(0));

		if (command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if (command.startsWith("WithdrawF"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				showWithdrawWindowFreight(player);
			}
		}
		else if (command.startsWith("DepositF"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				showDepositWindowFreight(player);
			}
		}
		else if (command.startsWith("FreightChar"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				int startOfId = command.lastIndexOf("_") + 1;
				String id = command.substring(startOfId);
				showDepositWindowFreight(player, Integer.parseInt(id));
			}
		}
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class

			super.onBypassFeedback(player, command);
		}
	}
}