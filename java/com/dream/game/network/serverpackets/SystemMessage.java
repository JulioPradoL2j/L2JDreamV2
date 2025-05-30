package com.dream.game.network.serverpackets;

import java.util.Vector;

import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Item;

public final class SystemMessage extends L2GameServerPacket
{
	private static final int TYPE_ZONE_NAME = 7;

	private static final byte TYPE_ITEM_NUMBER = 6;

	private static final byte TYPE_CASTLE_NAME = 5;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;

	public static SystemMessage getSystemMessage(SystemMessageId smId)
	{
		SystemMessage sm = new SystemMessage(smId);
		return sm;
	}

	public static SystemMessage sendString(String msg)
	{
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1).addString(msg);
		return sm;
	}

	private final int _messageId;
	private final Vector<Integer> _types = new Vector<>();

	private final Vector<Object> _values = new Vector<>();

	private int _skillLvL = 1;

	public SystemMessage(int messageId)
	{
		_messageId = messageId;
	}

	public SystemMessage(SystemMessageId messageId)
	{
		_messageId = messageId.getId();
	}

	public SystemMessage addCharName(L2Character cha)
	{
		if (cha instanceof L2Npc)
			return addNpcName((L2Npc) cha);

		if (cha instanceof L2PcInstance)
			return addPcName((L2PcInstance) cha);

		if (cha instanceof L2Summon)
			return addNpcName((L2Summon) cha);

		return addString(cha.getName());
	}

	public SystemMessage addFortId(int number)
	{
		_types.add(Integer.valueOf(TYPE_CASTLE_NAME));
		_values.add(Integer.valueOf(number));
		return this;
	}

	public SystemMessage addItemName(int id)
	{
		_types.add(Integer.valueOf(TYPE_ITEM_NAME));
		_values.add(Integer.valueOf(id));
		return this;
	}

	public SystemMessage addItemName(L2Item item)
	{
		if (item.getItemDisplayId() != item.getItemId())
			return addString(item.getName());
		return addItemName(item.getItemId());
	}

	public SystemMessage addItemName(L2ItemInstance item)
	{
		if (item == null)
			return this;
		return addItemName(item.getItem());
	}

	public final SystemMessage addItemNumber(final int number)
	{
		_types.add(Integer.valueOf(TYPE_ITEM_NUMBER));
		_values.add(Integer.valueOf(number));
		return this;
	}

	public SystemMessage addNpcName(int id)
	{
		_types.add(Integer.valueOf(TYPE_NPC_NAME));
		_values.add(Integer.valueOf(1000000 + id));

		return this;
	}

	public SystemMessage addNpcName(L2Npc npc)
	{
		return addNpcName(npc.getTemplate());
	}

	public SystemMessage addNpcName(L2NpcTemplate tpl)
	{
		if (tpl.isCustom())
			return addString(tpl.getName());

		return addNpcName(tpl.getNpcId());
	}

	public SystemMessage addNpcName(L2Summon npc)
	{
		return addNpcName(npc.getNpcId());
	}

	public SystemMessage addNumber(int number)
	{
		_types.add(Integer.valueOf(TYPE_NUMBER));
		_values.add(Integer.valueOf(number));
		return this;
	}

	public SystemMessage addPcName(L2PcInstance pc)
	{
		return addString(pc.getAppearance().getVisibleName());
	}

	public SystemMessage addSkillName(int id)
	{
		return addSkillName(id, 1);
	}

	public SystemMessage addSkillName(int id, int lvl)
	{
		_types.add(Integer.valueOf(TYPE_SKILL_NAME));
		_values.add(Integer.valueOf(id));
		_skillLvL = lvl;

		return this;
	}

	public SystemMessage addSkillName(L2Effect effect)
	{
		return addSkillName(effect.getSkill());
	}

	public SystemMessage addSkillName(L2Skill skill)
	{
		if (skill.getId() != skill.getDisplayId())
			return addString(skill.getName());

		return addSkillName(skill.getId(), skill.getLevel());
	}

	public SystemMessage addString(String text)
	{
		_types.add(Integer.valueOf(TYPE_TEXT));
		_values.add(text);

		return this;
	}

	public SystemMessage addZoneName(int x, int y, int z)
	{
		_types.add(Integer.valueOf(TYPE_ZONE_NAME));
		int[] coord =
		{
			x,
			y,
			z
		};
		_values.add(coord);

		return this;
	}

	public int getMessageID()
	{
		return _messageId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x64);

		writeD(_messageId);
		writeD(_types.size());

		for (int i = 0; i < _types.size(); i++)
		{
			int t = _types.get(i).intValue();

			writeD(t);

			switch (t)
			{
				case TYPE_TEXT:
				{
					writeS((String) _values.get(i));
					break;
				}
				case TYPE_ITEM_NUMBER:
				case TYPE_CASTLE_NAME:
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					int t1 = ((Integer) _values.get(i)).intValue();
					writeD(t1);
					break;
				}
				case TYPE_SKILL_NAME:
				{
					int t1 = ((Integer) _values.get(i)).intValue();
					writeD(t1); // Skill Id
					writeD(_skillLvL);
					break;
				}
				case TYPE_ZONE_NAME:
				{
					int t1 = ((int[]) _values.get(i))[0];
					int t2 = ((int[]) _values.get(i))[1];
					int t3 = ((int[]) _values.get(i))[2];
					writeD(t1);
					writeD(t2);
					writeD(t3);
					break;
				}
			}
		}
	}
}