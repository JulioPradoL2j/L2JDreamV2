/*
 * This program is free software; you can redistribute it and/or modify
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

import java.util.StringTokenizer;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public final class L2BufferInstance extends L2NpcInstance
{
	public L2BufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();

		int buffid = 0;
		int bufflevel = 1;
		String nextWindow = null;
		if (st.countTokens() == 3)
		{
			buffid = Integer.valueOf(st.nextToken());
			bufflevel = Integer.valueOf(st.nextToken());
			nextWindow = st.nextToken();
		}
		else if (st.countTokens() == 1)
			buffid = Integer.valueOf(st.nextToken());

		if (actualCommand.equalsIgnoreCase("getbuff"))
		{
			if (buffid != 0)
			{
				SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(this, player);
				player.broadcastPacket(new MagicSkillUse(this, player, buffid, bufflevel, 5, 0, false));
				showChatWindow(player, nextWindow);
			}
		}
		else if (actualCommand.equalsIgnoreCase("restore"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			showMessageWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("cancel"))
		{
			player.stopAllEffects();
			showMessageWindow(player);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			broadcastPacket(new SocialAction(this, Rnd.get(8)));
			player.setLastFolkNPC(this);

			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		String filename = "data/html/mods/buffer/" + getNpcId() + ".htm";
		filename = getHtmlPath(getNpcId(), 0);
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%player%", player.getName());
		player.sendPacket(html);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/mods/buffer/" + pom + ".htm";
	}
}