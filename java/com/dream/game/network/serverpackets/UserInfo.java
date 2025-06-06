package com.dream.game.network.serverpackets;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.DressMeHolder;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Weapon;

public class UserInfo extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int _runSpd, _walkSpd, _relation;
	private float _moveMultiplier;
	
	public UserInfo(L2PcInstance cha)
	{
		this(cha, false);
	}
	
	public UserInfo(L2PcInstance cha, boolean first)
	{
		
		_activeChar = cha;
		if (cha == null)
			return;
		
		_moveMultiplier = _activeChar.getStat().getMovementSpeedMultiplier();
		_runSpd = Math.round(_activeChar.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_activeChar.getStat().getWalkSpeed() / _moveMultiplier);
		_relation = _activeChar.isClanLeader() ? 0x40 : 0;
		if (_activeChar.getSiegeState() == 1)
		{
			_relation |= 0x180;
		}
		if (_activeChar.getSiegeState() == 2)
		{
			_relation |= 0x80;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		
		if (_activeChar == null)
			return;
		
		writeC(0x04);
		
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getHeading());
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		
		if (_activeChar.getClassIndex() == 0)
		{
			writeD(_activeChar.getClassId().getId());
		}
		else
		{
			writeD(_activeChar.getBaseClass());
		}
		
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getStat().getSTR());
		writeD(_activeChar.getStat().getDEX());
		writeD(_activeChar.getStat().getCON());
		writeD(_activeChar.getStat().getINT());
		writeD(_activeChar.getStat().getWIT());
		writeD(_activeChar.getStat().getMEN());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		
		writeD(0x28);
		
		DressMeHolder armorSkin = _activeChar.getArmorSkin();
		DressMeHolder weaponSkin = _activeChar.getWeaponSkin();
		
		int hairallObj = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL);
		writeD((armorSkin != null && hairallObj > 0 && armorSkin.getHelmetId() > 0) ? armorSkin.getHelmetId() : hairallObj);
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		
		int rhandObj = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		int lhandObj = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND);
		
		if (_activeChar.isDressMe() && weaponSkin != null)
		{
			String equippedWeaponType = "";
			L2ItemInstance weaponInstance = _activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			
			if (weaponInstance != null && weaponInstance.getItem() instanceof L2Weapon)
			{
				L2Weapon weapon = (L2Weapon) weaponInstance.getItem();
				equippedWeaponType = weapon.getItemType().toString().toLowerCase();
			}
			
			if (equippedWeaponType.equalsIgnoreCase(weaponSkin.getWeaponTypeVisual()))
			{
				
				if (weaponSkin.getTwoHandId() > 0)
				{
					rhandObj = weaponSkin.getTwoHandId();
					lhandObj = 0;
				}
				else
				{
					if (weaponSkin.getRightHandId() > 0 && rhandObj > 0)
						rhandObj = weaponSkin.getRightHandId();
					if (weaponSkin.getLeftHandId() > 0 && lhandObj > 0)
						lhandObj = weaponSkin.getLeftHandId();
				}
				
			}
		}
		
		// Armas
		writeD(rhandObj); // PaperdollItemId RHAND
		writeD(lhandObj); // PaperdollItemId LHAND
		
		int glovesOId = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES);
		int chestOId = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST);
		int legsOId = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS);
		int feetOId = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET);
		
		writeD((armorSkin != null && glovesOId > 0 && armorSkin.getGlovesId() > 0) ? armorSkin.getGlovesId() : glovesOId);
		writeD((armorSkin != null && chestOId > 0 && armorSkin.getChestId() > 0) ? armorSkin.getChestId() : chestOId);
		writeD((armorSkin != null && legsOId > 0 && armorSkin.getLegsId() > 0) ? armorSkin.getLegsId() : legsOId);
		writeD((armorSkin != null && feetOId > 0 && armorSkin.getFeetId() > 0) ? armorSkin.getFeetId() : feetOId);
		
		writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(rhandObj);
		int hairObj = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR);
		writeD((armorSkin != null && hairObj > 0 && armorSkin.getHelmetId() > 0) ? armorSkin.getHelmetId() : hairObj);
		
		int faceObj = _activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE);
		writeD((armorSkin != null && faceObj > 0 && armorSkin.getHelmetId() > 0) ? armorSkin.getHelmetId() : faceObj);
		
		int hairall = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL);
		writeD((armorSkin != null && hairall > 0 && armorSkin.getHelmetId() > 0) ? armorSkin.getHelmetId() : hairall);

		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		
		int rhand = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
		int lhand = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND);
		
		if (_activeChar.isDressMe() && weaponSkin != null)
		{
			String equippedWeaponType = "";
			L2ItemInstance weaponInstance = _activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			
			if (weaponInstance != null && weaponInstance.getItem() instanceof L2Weapon)
			{
				L2Weapon weapon = (L2Weapon) weaponInstance.getItem();
				equippedWeaponType = weapon.getItemType().toString().toLowerCase();
			}
			
			if (equippedWeaponType.equalsIgnoreCase(weaponSkin.getWeaponTypeVisual()))
			{
				
				if (weaponSkin.getTwoHandId() > 0)
				{
					rhand = weaponSkin.getTwoHandId();
					lhand = 0;
				}
				else
				{
					if (weaponSkin.getRightHandId() > 0 && rhand > 0)
						rhand = weaponSkin.getRightHandId();
					if (weaponSkin.getLeftHandId() > 0 && lhand > 0)
						lhand = weaponSkin.getLeftHandId();
				}
				
		
				
			}
		}
		
		writeD(rhand); // PaperdollItemId RHAND
		writeD(lhand); // PaperdollItemId LHAND
		int glovesId = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES);
		int chestId = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST);
		int legsId = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS);
		int feetId = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET);
		
		writeD((armorSkin != null && glovesId > 0 && armorSkin.getGlovesId() > 0) ? armorSkin.getGlovesId() : glovesId);
		writeD((armorSkin != null && chestId > 0 && armorSkin.getChestId() > 0) ? armorSkin.getChestId() : chestId);
		writeD((armorSkin != null && legsId > 0 && armorSkin.getLegsId() > 0) ? armorSkin.getLegsId() : legsId);
		writeD((armorSkin != null && feetId > 0 && armorSkin.getFeetId() > 0) ? armorSkin.getFeetId() : feetId);
		
		writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(rhand);
		
		int hair = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR);
		writeD((armorSkin != null && hair > 0 && armorSkin.getHelmetId() > 0) ? armorSkin.getHelmetId() : hair);
		int face = _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE);
		writeD((armorSkin != null && face > 0 && armorSkin.getHelmetId() > 0) ? armorSkin.getHelmetId() : face);
		
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
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
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
		writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
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
		writeD(_activeChar.isFlying() ? _runSpd : 0);
		writeD(_activeChar.isFlying() ? _walkSpd : 0);
		writeF(_moveMultiplier);
		writeF(_activeChar.getStat().getAttackSpeedMultiplier());
		
		L2Summon pet = _activeChar.getPet();
		if (_activeChar.getMountType() != 0 && pet != null)
		{
			writeF(pet.getTemplate().getCollisionRadius());
			writeF(pet.getTemplate().getCollisionHeight());
		}
		else
		{
			writeF(_activeChar.getBaseTemplate().getCollisionRadius());
			writeF(_activeChar.getBaseTemplate().getCollisionHeight());
		}
		
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 1 : 0);
		
		String title = _activeChar.getTitle();
		if (_activeChar.getAppearance().isInvisible() && _activeChar.isGM())
		{
			title = "Invisible";
		}
		if (_activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
			if (polyObj != null)
			{
				title += " - " + polyObj.getName();
			}
		}
		if (_activeChar.isInFunEvent())
		{
			writeS(_activeChar._event.getTitle(_activeChar, _activeChar));
		}
		else
		{
			writeS(title);
		}
		
		writeD(_activeChar.getClanId());
		writeD(_activeChar.getClanCrestId());
		writeD(_activeChar.getAllyId());
		writeD(_activeChar.getAllyCrestId());
		writeD(_relation);
		writeC(_activeChar.getMountType());
		writeC(_activeChar.getPrivateStoreType());
		writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());
		
		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
		{
			writeH(id);
		}
		
		writeC(0x00);
		
		writeD(_activeChar.getAbnormalEffect());
		writeC(0x00);
		
		writeD(_activeChar.getClanPrivileges());
		
		writeH(_activeChar.getRecomLeft());
		writeH(_activeChar.getRecomHave());
		writeD(0x00);
		writeH(_activeChar.getInventoryLimit());
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00);
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getCurrentCp());
		writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect(false));
		
		if (_activeChar.getTeam() == 1)
		{
			writeC(0x01);
		}
		else if (_activeChar.getTeam() == 2)
		{
			writeC(0x02);
		}
		else
		{
			writeC(0x00);
		}
		
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0);
		writeC(_activeChar.isHero() || _activeChar.getIsPVPHero() ? 1 : 0);
		
		writeC(_activeChar.isFishing() ? 1 : 0);
		writeD(_activeChar.getFishx());
		writeD(_activeChar.getFishy());
		writeD(_activeChar.getFishz());
		
		if (_activeChar.isInFunEvent())
		{
			writeD(_activeChar._event.getCharNameColor(_activeChar, _activeChar));
		}
		else
		{
			writeD(_activeChar.getNameColor());
		}
		
		writeC(_activeChar.isRunning() ? 0x01 : 0x00);
		
		writeD(_activeChar.getPledgeClass());
		writeD(_activeChar.getPledgeClass());
		if (_activeChar.isInFunEvent())
		{
			writeD(_activeChar._event.getCharTitleColor(_activeChar, _activeChar));
		}
		else
		{
			writeD(_activeChar.getTitleColor());
		}
		
		if (_activeChar.isCursedWeaponEquipped())
		{
			writeD(CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()));
		}
		else
		{
			writeD(0x00);
		}
		
	}
	
}