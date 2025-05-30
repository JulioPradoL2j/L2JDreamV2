package com.dream.game.util;

import org.apache.log4j.Logger;

import com.dream.Message;
import com.dream.game.datatables.GmListTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.Disconnection;

public final class IllegalPlayerAction implements Runnable
{
	private static Logger _logAudit = Logger.getLogger("audit");

	public static final int PUNISH_BROADCAST = 1;
	public static final int PUNISH_KICK = 2;
	public static final int PUNISH_JAIL = 4;

	protected String _message;
	protected int _punishment;
	protected L2PcInstance _actor;

	public IllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		_message = message;
		_punishment = punishment;
		_actor = actor;

		switch (punishment)
		{
			case PUNISH_KICK:
				_actor.sendMessage(Message.getMessage(_actor, Message.MessageId.MSG_ILLEGAL_ACTION_KICK));
				break;
			case PUNISH_JAIL:
				_actor.sendMessage(Message.getMessage(_actor, Message.MessageId.MSG_ILLEGAL_ACTION_JAIL));
				break;
		}
	}

	@Override
	public void run()
	{
		_logAudit.info("AUDIT:" + _message + "," + _actor + " " + _punishment);

		GmListTable.broadcastMessageToGMs(_message);

		switch (_punishment)
		{
			case PUNISH_BROADCAST:
				return;
			case PUNISH_KICK:
				new Disconnection(_actor).defaultSequence(false);
				break;
			case PUNISH_JAIL:
				_actor.setInJail(true, -1);
				break;
		}
	}
}
