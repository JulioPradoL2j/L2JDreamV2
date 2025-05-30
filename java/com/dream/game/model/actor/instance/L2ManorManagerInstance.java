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

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.BuyListSeed;
import com.dream.game.network.serverpackets.ExShowCropInfo;
import com.dream.game.network.serverpackets.ExShowManorDefaultInfo;
import com.dream.game.network.serverpackets.ExShowProcureCropDetail;
import com.dream.game.network.serverpackets.ExShowSeedInfo;
import com.dream.game.network.serverpackets.ExShowSellCropList;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2ManorManagerInstance extends L2MerchantInstance
{
	public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public String getHtmlPath()
	{
		return "data/html/manormanager/";
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/manormanager/manager.htm";
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		player.setLastFolkNPC(this);

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
		else if (CastleManorManager.getInstance().isDisabled())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcdefault.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
		}
		else if (!player.isGM() && getCastle() != null && getCastle().getCastleId() > 0 && player.getClan() != null && getCastle().getOwnerId() == player.getClanId() && player.isClanLeader())
		{
			showMessageWindow(player, "manager-lord.htm");
		}
		else
		{
			showMessageWindow(player, "manager.htm");
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getLastFolkNPC() == null || player.getLastFolkNPC().getObjectId() != getObjectId())
			return;

		final L2Npc manager = player.getLastFolkNPC();
		final boolean isCastle = manager instanceof L2CastleChamberlainInstance;
		if (!(manager instanceof L2ManorManagerInstance || isCastle))
			return;

		if (command.startsWith("manor_menu_select"))
		{
			final Castle castle = manager.getCastle();

			if (CastleManorManager.getInstance().isUnderMaintenance())
			{
				player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			String params = command.substring(command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			int state = Integer.parseInt(st.nextToken().split("=")[1]);
			int time = Integer.parseInt(st.nextToken().split("=")[1]);

			int castleId;
			if (state == -1)
			{
				castleId = getCastle().getCastleId();
			}
			else
			{
				castleId = state;
			}

			switch (ask)
			{
				case 1:
					if (castleId != getCastle().getCastleId())
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR).addString(manager.getCastle().getName()));
					}
					else
					{
						player.sendPacket(new BuyListSeed(player.getAdena(), castleId, castle.getSeedProduction(CastleManorManager.PERIOD_CURRENT)));
					}
					break;
				case 2:
					player.sendPacket(new ExShowSellCropList(player, castleId, getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT)));
					break;
				case 3:
					if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowSeedInfo(castleId, null));
					}
					else
					{
						player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
					}
					break;
				case 4:
					if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowCropInfo(castleId, null));
					}
					else
					{
						player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
					}
					break;
				case 5:
					player.sendPacket(new ExShowManorDefaultInfo());
					break;
				case 6:
					showBuyWindow(player, Integer.parseInt("3" + getNpcId()));
					break;
				case 9:
					player.sendPacket(new ExShowProcureCropDetail(state));
					break;
			}
		}
		else if (command.startsWith("help"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			String filename = "manor_client_help00" + st.nextToken() + ".htm";
			showMessageWindow(player, filename);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showMessageWindow(L2PcInstance player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(getHtmlPath() + filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}