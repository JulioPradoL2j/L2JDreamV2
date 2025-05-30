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

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SiegeInfo;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

import javolution.text.TextBuilder;

public class L2SiegeRegisterInstance extends L2NpcInstance
{
	public L2SiegeRegisterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
		else
		{
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				showHtmlWindow(player);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("gludio_castle"))
			showSiegeInfoWindow(player, 1);
		else if (command.startsWith("dion_castle"))
			showSiegeInfoWindow(player, 2);
		else if (command.startsWith("giran_castle"))
			showSiegeInfoWindow(player, 3);
		else if (command.startsWith("oren_castle"))
			showSiegeInfoWindow(player, 4);
		else if (command.startsWith("aden_castle"))
			showSiegeInfoWindow(player, 5);
		else if (command.startsWith("innadril_castle"))
			showSiegeInfoWindow(player, 6);
		else if (command.startsWith("goddard_castle"))
			showSiegeInfoWindow(player, 7);
		else if (command.startsWith("rune_castle"))
			showSiegeInfoWindow(player, 8);
		else if (command.startsWith("schuttgart_castle"))
			showSiegeInfoWindow(player, 9);
		else
			super.onBypassFeedback(player, command);
	}
	
	public void showHtmlWindow(L2PcInstance activeChar)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("");
		
		replyMSG.append("<html><title>Siege Register Npc</title><body><center>");
		replyMSG.append("<img src=L2UI_CH3.herotower_deco width=256 height=32><br>");
		replyMSG.append("<font color=LEVEL>NPC Register Siege</font><br>");
		replyMSG.append("<img src=L2UI.SquareGray width=300 height=1><br><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_giran_castle\">Giran Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_aden_castle\">Aden Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_rune_castle\">Rune Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_goddard_castle\">Goddard Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_dion_castle\">Dion Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_schuttgart_castle\">Schuttgart Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_innadril_castle\">Innadril Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_oren_castle\">Oren Castle</a><br>");
		replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_gludio_castle\">Gludio Castle</a><br><br>");
		replyMSG.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
		replyMSG.append("<img src=l2ui.bbs_lineage2 height=16 width=80>");
		replyMSG.append("</center></body></html>");
		nhm.setHtml(replyMSG.toString());
		activeChar.sendPacket(nhm);
		return;
	}

	public void showSiegeInfoWindow(L2PcInstance player, int castleId)
	{
		Castle c = CastleManager.getInstance().getCastleById(castleId);
		if (c != null)
			player.sendPacket(new SiegeInfo(c, null, null));
	}
}