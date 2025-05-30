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
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2TownPetInstance extends L2Npc
{
	public class RandomWalkTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!isInActiveRegion())
				return;

			int randomX = _spawnX + Rnd.get(-1, 1) * 50;
			int randomY = _spawnY + Rnd.get(-1, 1) * 50;

			if (randomX != getX() || randomY != getY())
			{
				getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(randomX, randomY, _spawnZ, 0));
			}
		}
	}

	public int _spawnX;
	public int _spawnY;

	public int _spawnZ;

	public L2TownPetInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void firstSpawn()
	{
		super.firstSpawn();
		_spawnX = getX();
		_spawnY = getY();
		_spawnZ = getZ();
		if (Config.ALLOW_PET_WALKERS)
		{
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RandomWalkTask(), 2000, 4000);
		}
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
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}