package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.sql.CharNameTable;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.QuestManager;
import com.dream.game.model.L2ShortCut;
import com.dream.game.model.L2SkillLearn;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.world.L2World;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.serverpackets.CharCreateFail;
import com.dream.game.network.serverpackets.CharCreateOk;
import com.dream.game.network.serverpackets.CharSelectionInfo;
import com.dream.game.templates.chars.L2PcTemplate;
import com.dream.game.templates.chars.L2PcTemplate.PcTemplateItem;
import com.dream.game.util.Util;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Object _lock = new Object();
	private String _name;
	@SuppressWarnings("unused")
	private int _race, _classId, _int, _str, _con, _men, _dex, _wit;

	private byte _sex, _hairStyle, _hairColor, _face;

	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		L2World.getInstance().storeObject(newChar);

		L2PcTemplate template = newChar.getTemplate();

		if (Config.STARTING_ADENA > 0)
		{
			newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		}
		if (Config.STARTING_AA > 0)
		{
			newChar.addAncientAdena("Init", Config.STARTING_AA, null, false);
		}

		if (Config.CUSTOM_STARTER_ITEMS_ENABLED)
		{
			if (newChar.isMageClass())
			{
				for (final int[] reward : Config.STARTING_CUSTOM_ITEMS_M)
				{
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
						newChar.getInventory().addItem("Starter Items Mage", reward[0], reward[1], newChar, null);
					else
						for (int i = 0; i < reward[1]; ++i)
							newChar.getInventory().addItem("Starter Items Mage", reward[0], 1, newChar, null);
				}
			}
			else
			{
				for (final int[] reward : Config.STARTING_CUSTOM_ITEMS_F)
				{
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
						newChar.getInventory().addItem("Starter Items Fighter", reward[0], reward[1], newChar, null);
					else
						for (int i = 0; i < reward[1]; ++i)
							newChar.getInventory().addItem("Starter Items Fighter", reward[0], 1, newChar, null);
				}
			}
		}

		if (Config.ALLOW_NEW_CHAR_CUSTOM_POSITION)
		{
			newChar.getPosition().setXYZInvisible(Config.NEW_CHAR_POSITION_X, Config.NEW_CHAR_POSITION_Y, Config.NEW_CHAR_POSITION_Z);
		}
		else
		{
			newChar.getPosition().setXYZInvisible(template.getSpawnX(), template.getSpawnY(), template.getSpawnZ());
		}

		if (Config.CUSTOM_CHAR_TITLE)
		{
			newChar.setTitle(Config.CUSTOM_CHAR_TITLE_TEXT);
			newChar.getAppearance().setTitleColor(Config.TITLE_COLOR);
		}
		else
		{
			newChar.setTitle("");
		}

		if (Config.ENABLE_STARTUP_LVL)
		{
			long EXp = Experience.LEVEL[Config.ADD_LVL_NEWBIE];
			newChar.addExpAndSp(EXp, 0);
		}

		newChar.addSkill(SkillTable.getInstance().getInfo(194, 1), true);
		newChar.registerShortCut(new L2ShortCut(0, 0, 3, 2, 0, 1));
		newChar.registerShortCut(new L2ShortCut(3, 0, 3, 5, 0, 1));
		newChar.registerShortCut(new L2ShortCut(10, 0, 3, 0, 0, 1));

		for (PcTemplateItem ia : template.getItems())
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", ia.getItemId(), ia.getAmount(), newChar, null);

			if (item.getItemId() == 5588)
			{
				newChar.registerShortCut(new L2ShortCut(11, 0, 1, item.getObjectId(), 0, 1));
			}
			if (item.isEquipable() && ia.isEquipped())
			{
				newChar.getInventory().equipItemAndRecord(item);
			}
		}

		for (L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId()))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
			if (skill.getId() == 1001 || skill.getId() == 1177)
			{
				newChar.registerShortCut(new L2ShortCut(1, 0, 2, skill.getId(), skill.getLevel(), 1));
			}
			if (skill.getId() == 1216)
			{
				newChar.registerShortCut(new L2ShortCut(10, 0, 2, skill.getId(), skill.getLevel(), 1));
			}
		}

		startTutorialQuest(newChar);
		newChar.store();
		newChar.deleteMe();

		CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		synchronized (_lock)
		{
			if (_name.length() < 3 || _name.length() > 16)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS));
				return;
			}

			if (!Util.isValidPlayerName(_name))
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
				return;
			}

			if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}
			if (_face > 2 || _face < 0)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}

			if (_hairStyle < 0 || _sex == 0 && _hairStyle > 4 || _sex != 0 && _hairStyle > 6)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}

			if (_hairColor > 3 || _hairColor < 0)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}

			if (CharNameTable.accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}

			if (Config.FORBIDDEN_NAMES.length > 1)
			{
				for (String st : Config.FORBIDDEN_NAMES)
					if (_name.toLowerCase().contains(st.toLowerCase()))
					{
						sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
						return;
					}
			}

			L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
			if (template == null || template.getClassBaseLevel() > 1)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}

			int objectId = IdFactory.getInstance().getNextId();
			L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);

			newChar.setCurrentCp(0);
			newChar.setCurrentHp(newChar.getMaxHp());
			newChar.setCurrentMp(newChar.getMaxMp());

			sendPacket(CharCreateOk.STATIC_PACKET);

			initNewChar(getClient(), newChar);
		}
	}

	public void startTutorialQuest(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		Quest q = null;
		if (qs == null)
		{
			q = QuestManager.getInstance().getQuest("255_Tutorial");
		}
		if (q != null)
		{
			q.newQuestState(player);
		}
	}

}