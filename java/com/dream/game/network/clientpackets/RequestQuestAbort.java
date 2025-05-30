package com.dream.game.network.clientpackets;

import com.dream.game.manager.QuestManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.serverpackets.QuestList;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _questId;

	@Override
	protected void readImpl()
	{
		_questId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		Quest qe = QuestManager.getInstance().getQuest(_questId);
		if (qe != null)
		{
			QuestState qs = activeChar.getQuestState(qe.getName());
			if (qs != null)
			{
				qs.exitQuest(true);
				activeChar.sendPacket(new QuestList(activeChar));
			}
		}
	}

}