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
package com.dream.game.ai;

import java.util.List;

import com.dream.Config;
import com.dream.game.datatables.xml.NpcWalkerRoutesTable;
import com.dream.game.model.L2NpcWalkerNode;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2NpcWalkerInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	private static final class NpcWalkerAiTaskManager extends AbstractIterativePeriodicTaskManager<L2NpcWalkerAI>
	{
		public static NpcWalkerAiTaskManager getInstance()
		{
			return _instance;
		}

		private static final NpcWalkerAiTaskManager _instance = new NpcWalkerAiTaskManager();

		private NpcWalkerAiTaskManager()
		{
			super(1000);
		}

		@Override
		protected void callTask(L2NpcWalkerAI task)
		{
			task.run();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "run()";
		}
	}

	private static final int DEFAULT_MOVE_DELAY = 0;

	private long _nextMoveTime;

	private boolean _walkingToNextPoint = false;

	private int _homeX, _homeY, _homeZ;

	private final L2NpcWalkerNode[] _route;

	private int _currentPos;

	public L2NpcWalkerAI(L2Character.AIAccessor accessor)
	{
		super(accessor);

		if (!Config.ALLOW_NPC_WALKERS)
		{
			_route = null;
			return;
		}

		List<L2NpcWalkerNode> route = NpcWalkerRoutesTable.getInstance().getRouteForNpc(getActor().getNpcId());

		_route = route.toArray(new L2NpcWalkerNode[route.size()]);

		if (_route.length == 0)
		{
			_log.warn("L2NpcWalker(ID: " + getActor().getNpcId() + ") without defined route!");
			return;
		}
		NpcWalkerAiTaskManager.getInstance().startTask(this);
	}

	private void checkArrived()
	{
		L2NpcWalkerNode node = getCurrentNode();

		int destX = node.getMoveX();
		int destY = node.getMoveY();
		int destZ = node.getMoveZ();

		if (getActor().getX() == destX && getActor().getY() == destY && getActor().getZ() == destZ)
		{
			String chat = node.getChatText();
			if (chat != null && !chat.isEmpty())
				getActor().broadcastChat(chat);

			long delay = node.getDelay() * 1000;

			if (delay < 0)
			{
				_log.info("L2NpcWalkerAI: negative delay(" + delay + "), using default instead.");
				delay = DEFAULT_MOVE_DELAY;
			}

			_nextMoveTime = System.currentTimeMillis() + delay;

			setWalkingToNextPoint(false);
		}
	}

	@Override
	public L2NpcWalkerInstance getActor()
	{
		return (L2NpcWalkerInstance) _actor;
	}

	private L2NpcWalkerNode getCurrentNode()
	{
		return _route[_currentPos];
	}

	public int getHomeX()
	{
		return _homeX;
	}

	public int getHomeY()
	{
		return _homeY;
	}

	public int getHomeZ()
	{
		return _homeZ;
	}

	public boolean isWalkingToNextPoint()
	{
		return _walkingToNextPoint;
	}

	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		_log.info("NpcWalker ID: " + getActor().getNpcId() + ": Blocked at rote position [" + _currentPos + "], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z + ". Teleporting to next point");

		L2NpcWalkerNode node = getCurrentNode();

		int destinationX = node.getMoveX();
		int destinationY = node.getMoveY();
		int destinationZ = node.getMoveZ();

		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}

	@Override
	protected void onEvtThink()
	{
		if (!Config.ALLOW_NPC_WALKERS || _route.length == 0 || getActor().getKnownList().getKnownPlayers().isEmpty())
			return;

		if (isWalkingToNextPoint())
		{
			checkArrived();
			return;
		}

		if (_nextMoveTime < System.currentTimeMillis())
			walkToLocation();
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	public void setHomeX(int homeX)
	{
		_homeX = homeX;
	}

	public void setHomeY(int homeY)
	{
		_homeY = homeY;
	}

	public void setHomeZ(int homeZ)
	{
		_homeZ = homeZ;
	}

	public void setWalkingToNextPoint(boolean value)
	{
		_walkingToNextPoint = value;
	}

	private void walkToLocation()
	{
		_currentPos = (_currentPos + 1) % _route.length;

		L2NpcWalkerNode node = getCurrentNode();

		if (node.getRunning())
			getActor().setRunning();
		else
			getActor().setWalking();

		int destX = node.getMoveX();
		int destY = node.getMoveY();
		int destZ = node.getMoveZ();

		setWalkingToNextPoint(true);

		setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(destX, destY, destZ, 0));
	}
}