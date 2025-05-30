package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.game.model.L2Party;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FestivalMonsterInstance extends L2MonsterInstance
{
	private final boolean _isArcher;
	private final boolean _isChest;
	protected int _bonusMultiplier = 1;

	public L2FestivalMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isArcher = SevenSignsFestival.isFestivalArcher(getTemplate().getNpcId());
		_isChest = SevenSignsFestival.isFestivalChest(getTemplate().getNpcId());
	}

	@Override
	public void doItemDrop(L2Character lastAttacker)
	{
		L2PcInstance killingChar = null;

		if (!(lastAttacker instanceof L2PcInstance))
			return;

		killingChar = (L2PcInstance) lastAttacker;
		L2Party associatedParty = killingChar.getParty();

		if (associatedParty == null)
			return;

		L2PcInstance partyLeader = associatedParty.getPartyMembers().get(0);
		L2ItemInstance addedOfferings = partyLeader.getInventory().addItem("Sign", SevenSignsFestival.FESTIVAL_OFFERING_ID, _bonusMultiplier, partyLeader, this);

		InventoryUpdate iu = new InventoryUpdate();

		if (addedOfferings.getCount() != _bonusMultiplier)
		{
			iu.addModifiedItem(addedOfferings);
		}
		else
		{
			iu.addNewItem(addedOfferings);
		}

		partyLeader.sendPacket(iu);

		super.doItemDrop(lastAttacker);
	}

	@Override
	public int getAggroRange()
	{
		if (_isArcher)
			return Config.ALT_FESTIVAL_ARCHER_AGGRO;
		if (_isChest)
			return Config.ALT_FESTIVAL_CHEST_AGGRO;

		return Config.ALT_FESTIVAL_MONSTER_AGGRO;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !(attacker instanceof L2FestivalMonsterInstance);
	}

	public void setOfferingBonus(int bonusMultiplier)
	{
		_bonusMultiplier = bonusMultiplier;
	}
}