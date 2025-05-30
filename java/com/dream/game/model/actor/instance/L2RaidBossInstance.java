package com.dream.game.model.actor.instance;

import com.dream.game.manager.RaidBossSpawnManager;
import com.dream.game.model.L2Boss;
import com.dream.game.model.actor.L2Character;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2RaidBossInstance extends L2Boss
{
	private static final class RaidBossReturnHomeManager extends AbstractIterativePeriodicTaskManager<L2RaidBossInstance>
	{
		private static final RaidBossReturnHomeManager _instance = new RaidBossReturnHomeManager();

		public static RaidBossReturnHomeManager getInstance()
		{
			return _instance;
		}

		private RaidBossReturnHomeManager()
		{
			super(5000);
		}

		@Override
		protected void callTask(L2RaidBossInstance task)
		{
			task.returnHome();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "returnHome()";
		}
	}

	private boolean _canReturnHome = true;

	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		RaidBossReturnHomeManager.getInstance().startTask(this);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		RaidBossSpawnManager.getInstance().updateStatus(this, true);
		return true;
	}

	@Override
	public void onSpawn()
	{
		setIsRaid(true);
		setIsBoss(true);
		super.onSpawn();
	}

	@Override
	public void returnHome()
	{
		if (getNpcId() == 29095)
			return;

		if (_canReturnHome && getSpawn() != null)
		{
			int zoneSize = getSpawn().getSpawnZoneSize();
			if (zoneSize > 0)
			{
				if (!isDead())
					if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), zoneSize, false))
					{
						clearAggroList();
						healFull();
						teleToLocation(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz());
						if (hasMinions())
						{
							callMinions(true);
						}
					}
			}
			else if (RaidBossReturnHomeManager.getInstance().hasTask(this))
			{
				RaidBossReturnHomeManager.getInstance().stopTask(this);
			}
		}
		else if (RaidBossReturnHomeManager.getInstance().hasTask(this))
		{
			RaidBossReturnHomeManager.getInstance().stopTask(this);
		}
	}

	public void setCanReturnHome(boolean val)
	{
		_canReturnHome = val;
	}
}