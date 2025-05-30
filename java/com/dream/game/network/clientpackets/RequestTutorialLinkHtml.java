package com.dream.game.network.clientpackets;

import com.dream.game.handler.ITutorialHandler;
import com.dream.game.handler.TutorialHandler;
import com.dream.game.model.actor.instance.L2ClassMasterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.serverpackets.TutorialCloseHtml;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	// private String _link;
	String _bypass;

	@Override
	protected void readImpl()
	{
		// _link = readS();
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_bypass.equalsIgnoreCase("close"))
		{
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			return;
		}

		if (_bypass.startsWith("-h"))
		{
			_bypass = _bypass.substring(2);

			if (_bypass.startsWith("_"))
				_bypass = _bypass.substring(1);
		}

		final ITutorialHandler handler = TutorialHandler.getInstance().getHandler(_bypass);

		if (handler != null)
		{
			String command = _bypass;
			String params = "";
			if (_bypass.indexOf("_") != -1)
			{
				command = _bypass.substring(0, _bypass.indexOf("_"));
				params = _bypass.substring(_bypass.indexOf("_") + 1, _bypass.length());
			}
			handler.useLink(command, player, params);
		}
		else
		{
			// _log.warn("Startup System Warning: " + getClient() + " sent not handled RequestTutorialLinkHtml: [" + _bypass + "]");

		}
		L2ClassMasterInstance.onTutorialLink(player, _bypass);
		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}

}