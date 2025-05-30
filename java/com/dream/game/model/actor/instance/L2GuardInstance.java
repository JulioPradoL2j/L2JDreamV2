package com.dream.game.model.actor.instance;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Guard;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.knownlist.GuardKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public final class L2GuardInstance extends L2Guard
{
	private static final class GuardReturnHomeManager extends AbstractIterativePeriodicTaskManager<L2GuardInstance>
	{
		private static final GuardReturnHomeManager _instance = new GuardReturnHomeManager();

		public static GuardReturnHomeManager getInstance()
		{
			return _instance;
		}

		private GuardReturnHomeManager()
		{
			super(RETURN_INTERVAL);
		}

		@Override
		protected void callTask(L2GuardInstance task)
		{
			if (task.getAI().getIntention() == CtrlIntention.IDLE)
			{
				task.returnHome();
			}
		}

		@Override
		protected String getCalledMethodName()
		{
			return "returnHome()";
		}
	}

	private static final int RETURN_INTERVAL = 60000;

	public L2GuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();

		GuardReturnHomeManager.getInstance().startTask(this);
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

		return "data/html/guard/" + filename + ".htm";
	}

	@Override
	public final GuardKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new GuardKnownList(this);
		}

		return (GuardKnownList) _knownList;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (getObjectId() != player.getTargetId())
		{
			player.setTarget(this);

			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new ValidateLocation(this));
		}
		else if (containsTarget(player))
		{
			player.getAI().setIntention(CtrlIntention.ATTACK, this);
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
		}
		else
		{
			broadcastPacket(new SocialAction(this, Rnd.nextInt(8)));

			Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
			if (qlsa != null && qlsa.length > 0)
			{
				player.setLastQuestNpcObject(getObjectId());
			}
			Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
			if (qlst != null && qlst.length == 1)
			{
				qlst[0].notifyFirstTalk(this, player);
			}
			else
			{
				showChatWindow(player, 0);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if (region != null && !region.isActive())
		{
			getAI().stopAITask();
		}
	}

	@Override
	public void returnHome()
	{
		if (!isDead())
			if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 150, false))
			{
				clearAggroList();

				getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			}
	}
}