package com.dream.game.model.actor.instance;

import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2GrandBossInstance extends L2Boss
{
	private static class GrandBossReturnHomeManager extends AbstractIterativePeriodicTaskManager<L2GrandBossInstance>
	{
		private static GrandBossReturnHomeManager _instance;

		public static GrandBossReturnHomeManager getInstance()
		{
			if (_instance == null)
			{
				_instance = new GrandBossReturnHomeManager();
			}
			return _instance;
		}

		private GrandBossReturnHomeManager()
		{
			super(5000);
		}

		@Override
		protected void callTask(L2GrandBossInstance task)
		{
			task.returnHome();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "returnHome()";
		}
	}

	protected boolean _isInSocialAction = false;

	private byte _zoneId = 0;

	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void deleteMe()
	{
		if (GrandBossReturnHomeManager.getInstance().hasTask(this))
		{
			GrandBossReturnHomeManager.getInstance().stopTask(this);
		}
		super.deleteMe();
	}

	@Override
	public void doAttack(L2Character target)
	{
		if (_isInSocialAction)
			return;
		super.doAttack(target);
	}

	@Override
	public void doCast(L2Skill skill)
	{
		if (_isInSocialAction)
			return;
		super.doCast(skill);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (_lair != null)
		{
			_lair.setRespawn();
		}
		return true;
	}

	public boolean isInSocialAction()
	{
		return _isInSocialAction;
	}

	@Override
	public void onSpawn()
	{
		setIsRaid(true);
		setIsGrandBoss(true);
		setupZone();
		super.onSpawn();
		GrandBossReturnHomeManager.getInstance().startTask(this);
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isInSocialAction() || isInvul())
			return;
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	public void returnHome()
	{
		if (getSpawn() != null)
		{
			if (_zoneId > 0)
			{
				if (!isDead())
					if (!isInsideZone(_zoneId))
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
			else if (GrandBossReturnHomeManager.getInstance().hasTask(this))
			{
				GrandBossReturnHomeManager.getInstance().stopTask(this);
			}
		}
		else if (GrandBossReturnHomeManager.getInstance().hasTask(this))
		{
			GrandBossReturnHomeManager.getInstance().stopTask(this);
		}
	}

	public void setIsInSocialAction(boolean value)
	{
		_isInSocialAction = value;
	}

	private void setupZone()
	{
		int npcId = getNpcId();
		if (npcId == 29001)
		{
			_zoneId = L2Zone.FLAG_QUEEN;
		}
		else if (npcId == 29020)
		{
			_zoneId = 21;
		}
		else if (npcId == 29022 || npcId == 13099)
		{
			_zoneId = 22;
		}
	}
}