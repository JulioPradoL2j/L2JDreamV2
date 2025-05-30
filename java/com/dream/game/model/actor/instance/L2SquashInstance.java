package com.dream.game.model.actor.instance;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.actor.L2Character;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2SquashInstance extends L2MonsterInstance
{
	private final L2PcInstance _owner;
	private int _level;
	private final List<Integer> _hitterId = new ArrayList<>();

	public L2SquashInstance(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		_owner = owner;
		_hitterId.clear();
	}

	@Override
	public boolean canReduceHp(double damage, L2Character attacker)
	{
		if (attacker.getActiveWeaponItem() == null)
		{
			monSay();
			return false;
		}
		if (attacker.getActiveWeaponItem().getItemId() != 7058)
		{
			monSay();
			return false;
		}
		if (!_hitterId.contains(attacker.getObjectId()))
		{
			_hitterId.add(attacker.getObjectId());
		}
		boolean hitRnd = false;
		if (getNpcId() == 13016 || getNpcId() == 13017)
		{
			if (_hitterId.size() > 1)
				if (attacker == getOwner())
				{
					hitRnd = Rnd.nextBoolean();
				}
		}
		else if (attacker == getOwner())
		{
			hitRnd = Rnd.nextBoolean();
		}
		return hitRnd;
	}

	@Override
	public final int getLevel()
	{
		return _level;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public void monSay()
	{
		if (Rnd.get(100) > 30)
			return;

		String text = "You can't kill me.";
		if (!getKnownList().getKnownPlayers().isEmpty())
		{
			broadcastPacket(new CreatureSay(0, SystemChatChannelId.Chat_Normal, "Pumpkin", text));
		}
	}

	public void setLevel(int par)
	{
		_level = par;
	}
}