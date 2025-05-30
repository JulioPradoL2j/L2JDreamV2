package com.dream.game.network.serverpackets;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.actor.L2Decoy;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.skills.AbnormalEffect;
import com.dream.game.templates.chars.L2NpcTemplate;

public class CharInfo extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(CharInfo.class.getName());
	private final L2PcInstance _activeChar;
	private final Inventory _inv;
	private int _x;
	private int _y;
	private int _z;
	private int _objectId;
	private int _heading;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final float _moveMultiplier;
	private final float _attackSpeedMultiplier;
	private boolean _isDecoy;
	private L2Decoy _decoy;
	
	public CharInfo(L2Decoy decoy)
	{
		this(decoy.getOwner());
		_objectId = decoy.getObjectId();
		_x = decoy.getX();
		_y = decoy.getY();
		_z = decoy.getZ();
		_heading = decoy.getHeading();
		_isDecoy = true;
		_decoy = decoy;
	}

	public CharInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		_isDecoy = false;
		_objectId = _activeChar.getObjectId();
		_inv = _activeChar.getInventory();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_moveMultiplier = _activeChar.getStat().getMovementSpeedMultiplier();
		_attackSpeedMultiplier = _activeChar.getStat().getAttackSpeedMultiplier();
		_runSpd = (int) (_activeChar.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_activeChar.getStat().getWalkSpeed() / _moveMultiplier);
		_isDecoy = false;
		_decoy = null;
	}

	@Override
	protected final void writeImpl()
	{
		{
			boolean gmSeeInvis = _isDecoy;
			if (_activeChar.getAppearance().isInvisible() && !_isDecoy)
			{
				L2PcInstance tmp = getClient().getActiveChar();
				if (tmp != null && tmp.isGM())
				{
					gmSeeInvis = true;
				}
				else
					return;
			}

			if (_activeChar.getPoly().isMorphed())
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());

				if (template != null)
				{
					writeC(0x16);
					writeD(_activeChar.getObjectId());
					writeD(_activeChar.getPoly().getPolyId() + 1000000);
					writeD(_activeChar.getKarma() > 0 ? 1 : 0);
					writeD(_x);
					writeD(_y);
					writeD(_z);
					writeD(_heading);
					writeD(0x00);
					writeD(_mAtkSpd);
					writeD(_pAtkSpd);
					writeD(_runSpd);
					writeD(_walkSpd);
					writeD(_runSpd);
					writeD(_walkSpd);
					writeD(_runSpd);
					writeD(_walkSpd);
					writeD(_runSpd);
					writeD(_walkSpd);
					writeF(_moveMultiplier);
					writeF(_attackSpeedMultiplier);
					writeF(template.getCollisionRadius());
					writeF(template.getCollisionHeight());
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
					writeD(0);
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
					writeC(1);
					writeC(_activeChar.isRunning() ? 1 : 0);
					writeC(_activeChar.isInCombat() ? 1 : 0);
					writeC(_activeChar.isAlikeDead() ? 1 : 0);

					if (gmSeeInvis)
					{
						writeC(0);
						writeS("Invisible");
					}
					else
					{
						writeC(_activeChar.getAppearance().isInvisible() ? 1 : 0);
						if (_activeChar.isInFunEvent())
						{
							writeS(_activeChar._event.getTitle(_activeChar, _activeChar));
						}
						else
						{
							writeS(_activeChar.getTitle());
						}
					}

					if (_activeChar.isInFunEvent())
					{
						writeS(_activeChar._event.getName(_activeChar, _activeChar));
					}
					else
					{
						writeS(_activeChar.getName());
					}

					writeD(0);
					writeD(0);
					writeD(0000);

					if (gmSeeInvis)
					{
						writeD(_activeChar.getAbnormalEffect());
					}
					else
					{
						writeD(_activeChar.getAbnormalEffect());
					}

					writeD(0);
					writeD(0);
					writeD(0);
					writeD(0);
					writeC(0);
				}
				else
				{
					_log.warn("Character " + _activeChar.getName() + " (" + _activeChar.getObjectId() + ") morphed in a Npc (" + _activeChar.getPoly().getPolyId() + ") w/o template.");
				}
			}
			else
			{
				writeC(0x03);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(_heading);
				writeD(_objectId);

				if (_activeChar.isInFunEvent())
				{
					writeS(_activeChar._event.getName(_activeChar, _activeChar));
				}
				else
				{
					writeS(_activeChar.getName());
				}
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

				if (!_activeChar.isDressMeEnabled())
				{
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
				}
				else
				{
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
					writeD(_activeChar.getDressMeData() == null ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) : (_activeChar.getDressMeData().getGlovesId() == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) : _activeChar.getDressMeData().getGlovesId()));
					writeD(_activeChar.getDressMeData() == null ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) : (_activeChar.getDressMeData().getChestId() == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) : _activeChar.getDressMeData().getChestId()));
					writeD(_activeChar.getDressMeData() == null ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS) : (_activeChar.getDressMeData().getLegsId() == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS) : _activeChar.getDressMeData().getLegsId()));
					writeD(_activeChar.getDressMeData() == null ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET) : (_activeChar.getDressMeData().getBootsId() == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET) : _activeChar.getDressMeData().getBootsId()));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
					writeD(_activeChar.getDressMeData() == null ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : (_activeChar.getDressMeData().getHairId() == 0 ? _activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) : _activeChar.getDressMeData().getHairId()));
					writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
				}
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

				writeD(_activeChar.getPvpFlag());
				writeD(_activeChar.getKarma());

				writeD(_mAtkSpd);
				writeD(_pAtkSpd);

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
				writeF(_activeChar.getStat().getMovementSpeedMultiplier());
				writeF(_activeChar.getStat().getAttackSpeedMultiplier());
				writeF(_activeChar.getBaseTemplate().getCollisionRadius());
				writeF(_activeChar.getBaseTemplate().getCollisionHeight());

				writeD(_activeChar.getAppearance().getHairStyle());
				writeD(_activeChar.getAppearance().getHairColor());
				writeD(_activeChar.getAppearance().getFace());

				if (gmSeeInvis && !_isDecoy)
				{
					writeS("Invisible");
				}
				else
				{
					if (_activeChar.isInFunEvent())
					{
						writeS(_activeChar._event.getTitle(_activeChar, _activeChar));
					}
					else
					{
						writeS(_activeChar.getTitle());
					}
				}

				writeD(_activeChar.getClanId());
				writeD(_activeChar.getClanCrestId());
				writeD(_activeChar.getAllyId());
				writeD(_activeChar.getAllyCrestId());

				writeD(0);
				if (_isDecoy)
				{
					writeC(_decoy.isSitting() ? 0 : 1);
					writeC(0);
					writeC(0);
					writeC(0);
					writeC(0);
				}
				else
				{
					writeC(_activeChar.isSitting() ? 0 : 1);
					writeC(_activeChar.isRunning() ? 1 : 0);
					writeC(_activeChar.isInCombat() ? 1 : 0);
					writeC(_activeChar.isAlikeDead() ? 1 : 0);
					if (gmSeeInvis)
						writeC(0);
					else
						writeC(_activeChar.getAppearance().isInvisible() ? 1 : 0); // invisible = 1 visible =0
				}
				writeC(_activeChar.getMountType());
				writeC(_activeChar.getPrivateStoreType());

				writeH(_activeChar.getCubics().size());
				for (int id : _activeChar.getCubics().keySet())
				{
					writeH(id);
				}

				writeC(0x00);

				if (gmSeeInvis)
					writeD((_activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask()));
				else
					writeD(_activeChar.getAbnormalEffect());

				writeC(_activeChar.getRecomLeft());
				writeH(_activeChar.getRecomHave());
				writeD(_activeChar.getClassId().getId());

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
				writeC(_activeChar.isHero() ? 0x01 : 0x00);

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

				writeD(0x00);

				writeD(_activeChar.getPledgeClass());
				writeD(_activeChar.getPledgeType());

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
	}

}