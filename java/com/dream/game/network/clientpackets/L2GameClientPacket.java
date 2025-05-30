package com.dream.game.network.clientpackets;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import org.apache.log4j.Logger;

import com.dream.game.network.L2GameClient;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.mmocore.ReceivablePacket;

public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	public static Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());

	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}

	@Override
	protected boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (BufferOverflowException e)
		{
			if (getClient() != null)
			{
				getClient().closeNow();
			}

			_log.warn("Client: " + getClient().toString() + " - Buffer overflow and has been kicked");
		}
		catch (BufferUnderflowException e)
		{
			getClient().onBufferUnderflow();
		}
		catch (Throwable t)
		{
			_log.warn("Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + t.getMessage(), t);
		}

		return false;
	}

	protected abstract void readImpl();

	@Override
	public void run()
	{
		try
		{
			runImpl();

			if (this instanceof MoveBackwardToLocation || this instanceof AttackRequest || this instanceof RequestMagicSkillUse)
				if (getClient().getActiveChar() != null)
				{
					getClient().getActiveChar().onActionRequest();
				}
		}
		catch (Throwable t)
		{
			_log.warn("Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + t.getMessage(), t);

			if (this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}

	protected abstract void runImpl();

	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}

}