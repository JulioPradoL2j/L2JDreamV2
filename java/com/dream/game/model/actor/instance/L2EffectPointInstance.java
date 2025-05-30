package com.dream.game.model.actor.instance;

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2EffectPointInstance extends L2Npc
{
	private final L2Character _owner;

	public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
	{
		super(objectId, template);
		_owner = owner;
	}

	public L2Character getOwner()
	{
		return _owner;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}