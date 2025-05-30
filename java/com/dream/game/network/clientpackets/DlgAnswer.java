package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class DlgAnswer extends L2GameClientPacket
{
	private int _messageId;
	private int _answer;
	private int _requesterId;

	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requesterId = readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();
		if (cha == null)
			return;

		Long answerTime = getClient().getActiveChar().getConfirmDlgRequestTime(_requesterId);
		if (_answer == 1 && answerTime != null && System.currentTimeMillis() > answerTime)
		{
			_answer = 0;
		}
		getClient().getActiveChar().removeConfirmDlgRequestTime(_requesterId);

		if (_messageId == SystemMessageId.S1_MAKING_RESSURECTION_REQUEST.getId())
		{
			cha.reviveAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		{
			cha.teleportAnswer(_answer, _requesterId);
		}
		else if (_messageId == SystemMessageId.S1.getId() && Config.ALLOW_WEDDING && cha.isEngageRequest())
		{
			cha.engageAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			cha.gatesAnswer(_answer, 1);
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			cha.gatesAnswer(_answer, 0);
		}
	}

}