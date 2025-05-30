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
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.world.L2World;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2CastleTeleporterInstance extends L2NpcInstance
{
	protected class oustAllPlayers implements Runnable
	{
		@Override
		public void run()
		{
			if (getCastle().getSiege().getIsInProgress())
			{
				final L2MapRegion region = MapRegionTable.getInstance().getRegion(getX(), getY());

				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					if (region == MapRegionTable.getInstance().getRegion(player.getX(), player.getY()))
					{
						player.sendPacket(new NpcSay(getObjectId(), 1, getNpcId(), "The defenders of " + getCastle().getName() + " will be teleported to the inner castle."));
					}
			}
			getCastle().oustAllPlayers();
			_currentTask = false;
		}
	}

	protected boolean _currentTask;

	private int _delay;

	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private final int getDelayInSeconds()
	{
		return _delay > 0 ? _delay / 1000 : 0;
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
			showChatWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.equalsIgnoreCase("tele"))
		{
			if (!_currentTask)
			{
				if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
				{
				_delay = 480000;
				}
				else
				{
				_delay = 30000;
				}

				_currentTask = true;
				ThreadPoolManager.getInstance().scheduleGeneral(new oustAllPlayers(), _delay);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/castleteleporter/MassGK-1.htm");
			html.replace("%delay%", getDelayInSeconds());
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename;
		if (!_currentTask)
		{
			if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
			{
				filename = "data/html/castleteleporter/MassGK-2.htm";
			}
			else
			{
				filename = "data/html/castleteleporter/MassGK.htm";
			}
		}
		else
		{
			filename = "data/html/castleteleporter/MassGK-1.htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		html.replace("%delay%", getDelayInSeconds());
		player.sendPacket(html);
	}
}