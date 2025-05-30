package com.dream.game.model.zone;

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.Race;
import com.dream.game.network.SystemMessageId;

public class L2MothertreeZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;

			if (player.getRace() != Race.Elf)
				return;
			if (player.isInParty())
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
					if (member.getRace() != Race.Elf)
						return;
			}
			player.setInsideZone(this, FLAG_MOTHERTREE, true);
			player.sendPacket(SystemMessageId.ENTER_SHADOW_MOTHER_TREE);
		}
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance && character.isInsideZone(L2Zone.FLAG_MOTHERTREE))
		{
			character.setInsideZone(this, FLAG_MOTHERTREE, false);
			character.sendPacket(SystemMessageId.EXIT_SHADOW_MOTHER_TREE);
		}
		super.onExit(character);
	}
}