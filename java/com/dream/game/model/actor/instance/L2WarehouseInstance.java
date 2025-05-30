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

import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.cache.HtmCache;
import com.dream.game.manager.TownManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.entity.Town;
import com.dream.game.model.itemcontainer.PcFreight;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EnchantResult;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.PackageToList;
import com.dream.game.network.serverpackets.SortedWareHouseWithdrawalList;
import com.dream.game.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import com.dream.game.network.serverpackets.WareHouseDepositList;
import com.dream.game.network.serverpackets.WareHouseWithdrawalList;
import com.dream.game.templates.chars.L2NpcTemplate;

public final class L2WarehouseInstance extends L2NpcInstance
{
	private final static Logger _log = Logger.getLogger(L2WarehouseInstance.class.getName());

	private static void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInvetoryDisable();
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}

	private static void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			}
			else
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
				{
					player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				}

				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInvetoryDisable();
				WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
				player.sendPacket(dl);
			}
	}

	private static void showDepositWindowFreight(L2PcInstance player)
	{
		if (player.getAccountChars().size() == 0)
		{
			player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
		}
		else
		{
			Map<Integer, String> chars = player.getAccountChars();

			if (chars.size() < 1)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			player.sendPacket(new PackageToList(chars));
		}
	}

	private static void showRetrieveWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}

	private static void showRetrieveWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
	}

	private static void showWithdrawWindowClan(L2PcInstance player)
	{
		if (player.getClan() == null || player.getClan().getLevel() == 0)
		{
			player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
		}
		else if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private static void showWithdrawWindowClan(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		if (player.getClan() == null || player.getClan().getLevel() == 0)
		{
			player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
		}
		else if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private int _closestTownId = -1;

	public L2WarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private int getClosestTown()
	{
		if (_closestTownId < 0)
		{
			Town town = TownManager.getInstance().getClosestTown(this);
			if (town != null)
			{
				_closestTownId = town.getTownId();
			}
			else
			{
				_closestTownId = 0;
			}
		}

		return _closestTownId;
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

		return "data/html/warehouse/" + filename + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getActiveEnchantItem() != null)
		{
			player.sendPacket(new EnchantResult(0));
		}

		String param[] = command.split("_");

		if (command.startsWith("WithdrawP"))
		{
			if (Config.ENABLE_WAREHOUSESORTING_PRIVATE)
			{
				String htmFile = "data/html/mods/WhSortedP.htm";
				String htmContent = HtmCache.getInstance().getHtm(htmFile);
				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(npcHtmlMessage);
				}
				else
				{
					_log.warn("Missing htm: " + htmFile + " !");
				}
			}
			else
			{
				showRetrieveWindow(player);
			}
		}
		else if (command.startsWith("WithdrawSortedP"))
		{
			if (param.length > 2)
			{
				showRetrieveWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				showRetrieveWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				showRetrieveWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.startsWith("WithdrawC"))
		{
			if (Config.ENABLE_WAREHOUSESORTING_CLAN)
			{
				String htmFile = "data/html/mods/WhSortedC.htm";
				String htmContent = HtmCache.getInstance().getHtm(htmFile);
				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(npcHtmlMessage);
				}
				else
				{
					_log.warn("Missing htm: " + htmFile + " !");
				}
			}
			else
			{
				showWithdrawWindowClan(player);
			}
		}
		else if (command.startsWith("WithdrawSortedC"))
		{
			if (param.length > 2)
			{
				showWithdrawWindowClan(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				showWithdrawWindowClan(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				showWithdrawWindowClan(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if (command.startsWith("WithdrawF"))
		{
			if (Config.ALLOW_FREIGHT)
				if (Config.ENABLE_WAREHOUSESORTING_FREIGHT)
				{
					String htmFile = "data/html/mods/WhSortedF.htm";
					String htmContent = HtmCache.getInstance().getHtm(htmFile);
					if (htmContent != null)
					{
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(npcHtmlMessage);
					}
					else
					{
						_log.warn("Missing htm: " + htmFile + " !");
					}
				}
				else
				{
					showWithdrawWindowFreight(player);
				}
		}
		else if (command.startsWith("WithdrawSortedF"))
		{
			if (Config.ALLOW_FREIGHT)
				if (param.length > 2)
				{
					showWithdrawWindowFreight(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
				}
				else if (param.length > 1)
				{
					showWithdrawWindowFreight(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
				}
				else
				{
					showWithdrawWindowFreight(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
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
			if (Config.ALLOW_FREIGHT && param.length > 1)
			{
				showDepositWindowFreight(player, Integer.parseInt(param[1]));
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		PcFreight freight = new PcFreight(null);
		freight.doQuickRestore(obj_Id);

		if (Config.ALT_GAME_FREIGHTS)
		{
			freight.setActiveLocation(0);
		}
		else
		{
			freight.setActiveLocation(getClosestTown());
		}

		player.setActiveWarehouse(freight);
		player.tempInvetoryDisable();
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.FREIGHT));
	}

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
					freight.setActiveLocation(getClosestTown());
				}
				player.setActiveWarehouse(freight);
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT));
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			}
		}
		else
		{
		}
	}

	private void showWithdrawWindowFreight(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
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
					freight.setActiveLocation(getClosestTown());
				}
				player.setActiveWarehouse(freight);
				player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT, itemtype, sortorder));
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			}
		}
		else
		{
		}
	}
}