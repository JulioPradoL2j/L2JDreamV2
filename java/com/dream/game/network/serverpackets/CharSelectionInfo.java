package com.dream.game.network.serverpackets;

import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.CharSelectInfoPackage;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.world.L2World;
import com.dream.game.network.L2GameClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CharSelectionInfo extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(CharSelectionInfo.class.getName());

	
	private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE charId=? && class_id=? ORDER BY charId");
			statement.setInt(1, ObjectId);
			statement.setInt(2, activeClassId);
			ResultSet charList = statement.executeQuery();

			if (charList.next())
			{
				charInfopackage.setExp(charList.getLong("exp"));
				charInfopackage.setSp(charList.getInt("sp"));
				charInfopackage.setLevel(charList.getInt("level"));
			}
			charList.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not restore char subclass info: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
	{
		int objectId = chardata.getInt("charId");

		L2PcInstance.disconnectIfOnline(objectId);

		String name = chardata.getString("char_name");

		L2Object object = null;
		object = L2World.getInstance().getPlayer(name);
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.setOfflineTrade(false);
			player.deleteMe();
		}
		long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0)
			if (System.currentTimeMillis() > deletetime)
			{
				L2Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
				if (clan != null)
				{
					clan.removeClanMember(objectId, 0);
				}

				L2GameClient.deleteCharByObjId(objectId);
				return null;
			}
		CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getInt("sp"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		charInfopackage.setRace(chardata.getInt("race"));
		charInfopackage.setBanned(chardata.getBoolean("isBanned"));
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");

		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}

		charInfopackage.setClassId(activeClassId);

		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		}

		int weaponId = charInfopackage.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND);
		if (weaponId < 1)
		{
			weaponId = charInfopackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
		}

		if (weaponObjId > 0)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
				statement.setInt(1, weaponObjId);
				ResultSet result = statement.executeQuery();
				if (result.next())
				{
					int augment = result.getInt("augAttributes");
					charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
				}
				result.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not restore augmentation info: " + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		if (baseClassId == 0 && activeClassId > 0)
		{
			charInfopackage.setBaseClassId(activeClassId);
		}
		else
		{
			charInfopackage.setBaseClassId(baseClassId);
		}

		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		return charInfopackage;
	}

	private final String _loginName;
	private final int _sessionId;

	private int _activeId;

	private final CharSelectInfoPackage[] _characterPackages;

	public CharSelectionInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo();
		_activeId = -1;
	}

	public CharSelectionInfo(String loginName, int sessionId, int activeId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo();
		_activeId = activeId;
	}

	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}

	
	private CharSelectInfoPackage[] loadCharacterSelectInfo()
	{
		CharSelectInfoPackage charInfopackage;
		List<CharSelectInfoPackage> characterList = new ArrayList<>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT account_name, charId, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, rec_have, rec_left, online, char_slot, lastAccess, base_class, isBanned FROM characters WHERE account_name=?");
			statement.setString(1, _loginName);
			ResultSet charList = statement.executeQuery();
			while (charList.next())
			{
				try
				{
					charInfopackage = restoreChar(charList);
					if (charInfopackage != null)
					{
						characterList.add(charInfopackage);
					}
				}
				catch (Exception e)
				{
					_log.error("Error restore character", e);
					e.printStackTrace();
				}
			}
			charList.close();
			statement.close();
			return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
		}
		catch (Exception e)
		{
			_log.warn("Could not restore char info", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return new CharSelectInfoPackage[0];
	}

	@Override
	protected final void writeImpl()
	{
		int size = _characterPackages.length;

		writeC(0x13);
		writeD(size);

		long lastAccess = 0L;

		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
				if (lastAccess < _characterPackages[i].getLastAccess())
				{
					lastAccess = _characterPackages[i].getLastAccess();
					_activeId = i;
				}
		}

		for (int i = 0; i < size; i++)
		{
			CharSelectInfoPackage charInfoPackage = _characterPackages[i];

			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00);

			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());

			if (charInfoPackage.getClassId() == charInfoPackage.getBaseClassId())
			{
				writeD(charInfoPackage.getClassId());
			}
			else
			{
				writeD(charInfoPackage.getBaseClassId());
			}

			writeD(0x01);

			writeD(0x00);
			writeD(0x00);
			writeD(0x00);

			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());

			writeD(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeD(charInfoPackage.getLevel());

			writeD(charInfoPackage.getKarma());
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);

			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FACE));

			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());

			writeF(charInfoPackage.getMaxHp());
			writeF(charInfoPackage.getMaxMp());

			long deleteTime = charInfoPackage.getDeleteTimer();
			int deletedays = 0;

			if (deleteTime > 0)
			{
				deletedays = (int) ((deleteTime - System.currentTimeMillis()) / 1000);
			}

			writeD(deletedays);

			writeD(charInfoPackage.getClassId());

			if (i == _activeId)
			{
				writeD(0x01);
			}
			else
			{
				writeD(0x00);
			}

			writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());

			writeD(charInfoPackage.getAugmentationId());
		}
	}

}