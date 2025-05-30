/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;

public class GMViewCharacterInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;

	public GMViewCharacterInfo(L2PcInstance character)
	{
		_activeChar = character;
	}

	@Override
	protected final void writeImpl()
	{
		float moveMultiplier = _activeChar.getStat().getMovementSpeedMultiplier();
		int _runSpd = (int) (_activeChar.getRunSpeed() / moveMultiplier);
		int _walkSpd = (int) (_activeChar.getStat().getWalkSpeed() / moveMultiplier);

		writeC(0x8f);

		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getHeading());
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(_activeChar.getClassId().getId());
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getStat().getSTR());
		writeD(_activeChar.getStat().getDEX());
		writeD(_activeChar.getStat().getCON());
		writeD(_activeChar.getStat().getINT());
		writeD(_activeChar.getStat().getWIT());
		writeD(_activeChar.getStat().getMEN());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getStatus().getCurrentHp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getStatus().getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		writeD(_activeChar.getPkKills());

		Inventory _inv = _activeChar.getInventory();

		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));

		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);

		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);

		writeD(_activeChar.getPAtk(null));
		writeD(_activeChar.getPAtkSpd());
		writeD(_activeChar.getPDef(null));
		writeD(_activeChar.getEvasionRate(null));
		writeD(_activeChar.getAccuracy());
		writeD(_activeChar.getCriticalHit(null, null));
		writeD(_activeChar.getMAtk(null, null));

		writeD(_activeChar.getMAtkSpd());
		writeD(_activeChar.getPAtkSpd());

		writeD(_activeChar.getMDef(null, null));

		writeD(_activeChar.getPvpFlag());
		writeD(_activeChar.getKarma());

		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(moveMultiplier);
		writeF(_activeChar.getStat().getAttackSpeedMultiplier());
		writeF(_activeChar.getTemplate().getCollisionRadius());
		writeF(_activeChar.getTemplate().getCollisionHeight());
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 0x01 : 0x00);

		writeS(_activeChar.getTitle());
		writeD(_activeChar.getClanId());
		writeD(_activeChar.getClanCrestId());
		writeD(_activeChar.getAllyId());
		writeC(_activeChar.getMountType());
		writeC(_activeChar.getPrivateStoreType());
		writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());

		writeH(_activeChar.getRecomLeft());
		writeH(_activeChar.getRecomHave());
		writeD(_activeChar.getClassId().getId());
		writeD(0x00);
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getStatus().getCurrentCp());

		writeC(_activeChar.isRunning() ? 0x01 : 0x00);

		writeC(321);

		writeD(_activeChar.getPledgeClass());

		writeC(_activeChar.isNoble() ? 0x01 : 0x00);
		writeC(_activeChar.isHero() ? 0x01 : 0x00);

		writeD(_activeChar.getAppearance().getNameColor());
		writeD(_activeChar.getAppearance().getTitleColor());
	}

}