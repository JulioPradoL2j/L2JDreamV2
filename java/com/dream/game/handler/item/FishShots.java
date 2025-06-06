package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.util.Broadcast;

/**
 * @author -Nemesiss-
 */
public class FishShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		6535,
		6536,
		6537,
		6538,
		6539,
		6540,
		6540,
		6540
	};
	
	private static final int[] SKILL_IDS =
	{
		2181,
		2182,
		2183,
		2184,
		2185,
		2186,
		2186,
		2186
	};
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		
		if (weaponInst == null || weaponItem.getItemType() != L2WeaponType.ROD)
			return;
		
		if (weaponInst.getChargedFishshot())
			// spiritshot is already active
			return;
		
		int FishshotId = item.getItemId();
		int grade = weaponItem.getCrystalType();
		int count = item.getCount();
		
		if (grade == L2Item.CRYSTAL_NONE && FishshotId != 6535 || grade == L2Item.CRYSTAL_D && FishshotId != 6536 || grade == L2Item.CRYSTAL_C && FishshotId != 6537 || grade == L2Item.CRYSTAL_B && FishshotId != 6538 || grade == L2Item.CRYSTAL_A && FishshotId != 6539 || grade == L2Item.CRYSTAL_S && FishshotId != 6540 || grade == L2Item.CRYSTAL_R && FishshotId != 6540 || grade == L2Item.CRYSTAL_S80 && FishshotId != 6540 || grade == L2Item.CRYSTAL_S84 && FishshotId != 6540)
		{
			// 1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
			return;
		}
		
		if (count < 1)
			return;
		
		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		L2Object oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, activeChar, SKILL_IDS[grade], 1, 0, 0, false));
		activeChar.setTarget(oldTarget);
	}
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}