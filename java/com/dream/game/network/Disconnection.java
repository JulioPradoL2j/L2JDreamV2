package com.dream.game.network;

import com.dream.game.manager.DuelManager;
import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.Duel;
import com.dream.game.taskmanager.AttackStanceTaskManager;

public final class Disconnection
{
	public static L2PcInstance getActiveChar(L2GameClient client, L2PcInstance activeChar)
	{
		if (activeChar != null)
			return activeChar;

		if (client != null)
			return client.getActiveChar();

		return null;
	}

	public static L2GameClient getClient(L2GameClient client, L2PcInstance activeChar)
	{
		if (client != null)
			return client;

		if (activeChar != null)
			return activeChar.getClient();

		return null;
	}

	private final L2GameClient _client;
	private final L2PcInstance _activeChar;

	public Disconnection(L2GameClient client)
	{
		this(client, null);

	}

	public Disconnection(L2GameClient client, L2PcInstance activeChar)
	{
		_client = getClient(client, activeChar);
		_activeChar = getActiveChar(client, activeChar);
		if (_activeChar != null)
		{
			store();
		}
		if (_client != null)
		{
			_client.setActiveChar(null);
		}

		if (_activeChar != null)
		{
			_activeChar.setClient(null);
		}
	}

	public Disconnection(L2PcInstance activeChar)
	{
		this(null, activeChar);
	}

	public Disconnection close(boolean toLoginScreen)
	{
		if (_client != null)
		{
			_client.close(toLoginScreen);
		}

		return this;
	}

	public void defaultSequence(boolean toLoginScreen)
	{
		deleteMe();
		close(toLoginScreen);
	}

	public Disconnection deleteMe()
	{
		try
		{
			if (_activeChar != null)
			{
				_activeChar.deleteMe();
			}

		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}

		return this;
	}

	public void onDisconnection()
	{
		if (_activeChar != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					deleteMe();
				}
			}, _activeChar.canLogout() ? 0 : AttackStanceTaskManager.COMBAT_TIME);
		}
	}

	public Disconnection store()
	{
		try
		{
			if (_activeChar != null)
			{
				if (_activeChar.isMoving())
				{
					_activeChar.stopMove();
				}

				if (_activeChar.inObserverMode())
				{
					_activeChar.leaveObserverMode();
				}
				if (_activeChar._event != null)
				{
					_activeChar._event.onLogout(_activeChar);
				}
				if (RainbowSpringSiege.getInstance().isPlayerInArena(_activeChar))
				{
					RainbowSpringSiege.getInstance().removeFromArena(_activeChar);
				}
				if (_activeChar.getDuelState() != Duel.DUELSTATE_NODUEL)
				{
					Duel duel = DuelManager.getInstance().getDuel(_activeChar.getDuelId());
					if (!duel.isPartyDuel())
					{
						duel.doSurrender(_activeChar);
						duel.restorePlayerConditions(_activeChar);
					}
				}
				if (_activeChar.getPartner() != null)
				{
					_activeChar.getPartner().onPartnerDisconnect();
				}
				_activeChar.store(true);
			}
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
		return this;
	}
}