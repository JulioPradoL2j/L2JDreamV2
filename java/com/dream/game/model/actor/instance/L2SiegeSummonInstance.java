package com.dream.game.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.dream.Message;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SiegeSummonInstance extends L2SummonInstance
{
	public class changeSiegeMode implements Runnable
	{
		@Override
		public void run()
		{
			getOwner().sendMessage(Message.getMessage(getOwner(), Message.MessageId.MSG_CHANGE_MODE_END));
			if (isOnSiegeMode())
			{
				onSiegeMode = false;
				setFollowStatus(true);
			}
			else
			{
				onSiegeMode = true;
			}
		}
	}

	public static final int SIEGE_GOLEM_ID = 14737;
	public static final int HOG_CANNON_ID = 14768;

	public static final int SWOOP_CANNON_ID = 14839;
	public boolean onSiegeMode = false;

	public ScheduledFuture<?> changeModeThread = null;

	public L2SiegeSummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner, skill);
	}

	public void changeSiegeMode()
	{
		if (changeModeThread != null && !changeModeThread.isDone())
		{
			getOwner().sendMessage(Message.getMessage(getOwner(), Message.MessageId.MSG_WAIT_CHANGE_MODE_END));
			return;
		}
		getOwner().sendMessage(Message.getMessage(getOwner(), Message.MessageId.MSG_CHANGE_MODE));
		changeModeThread = ThreadPoolManager.getInstance().scheduleGeneral(new changeSiegeMode(), 30000);
		setFollowStatus(false);
	}

	public boolean isOnSiegeMode()
	{
		return onSiegeMode;
	}

	public boolean isSiegeModeChanging()
	{
		if (changeModeThread != null && changeModeThread.isDone())
			return true;
		return false;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		Siege siege = SiegeManager.getSiege(this);
		if (!getOwner().isGM() && (siege == null || !siege.getIsInProgress()) && !isInsideZone(L2Zone.FLAG_SIEGE))
		{
			unSummon(getOwner());
			getOwner().sendMessage(Message.getMessage(getOwner(), Message.MessageId.MSG_NOT_IN_SIEGE_ZONE));
		}
	}

	public void resetSiegeModeChange()
	{
		if (changeModeThread != null && !changeModeThread.isDone())
		{
			getOwner().sendMessage(Message.getMessage(getOwner(), Message.MessageId.MSG_CHANGE_MODE_CANCEL));
			changeModeThread.cancel(true);
		}
	}
}