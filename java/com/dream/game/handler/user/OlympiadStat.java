package com.dream.game.handler.user;

import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.olympiad.Olympiad;

public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		109
	};

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;

		if (activeChar == null)
			return false;

		int _objId = activeChar.getObjectId();
		int _fights = Olympiad.getCompetitionDone(_objId);
		int _won = Olympiad.getCompetitionWon(_objId);
		int _lost = Olympiad.getCompetitionLost(_objId);
		int _points = Olympiad.getNoblePoints(_objId);
		activeChar.sendMessage("For the current Grand Olympiad you have participated in " + _fights + " match(es). " + _won + " win(s) and " + _lost + " defeat(s). You currently have " + _points + " Olympiad Point(s).");
		return true;
	}
}