package com.dream.game.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

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

public class ConfirmDlg extends L2GameServerPacket
{
	private static class CnfDlgData
	{
		protected final int type;
		protected final Object value;

		protected CnfDlgData(int t, Object val)
		{
			type = t;
			value = val;
		}
	}

	private static final int TYPE_ZONE_NAME = 7;

	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private final int _messageId;

	private int _skillLvL = 1;

	private final List<CnfDlgData> _info = new ArrayList<>();
	private int _time = 0;

	private int _requesterId = 0;

	public ConfirmDlg(int messageId)
	{
		_messageId = messageId;
	}

	public ConfirmDlg(SystemMessageId messageId)
	{
		_messageId = messageId.getId();
	}

	public ConfirmDlg addCharName(L2Character cha)
	{
		if (cha instanceof L2Npc)
			return addNpcName((L2Npc) cha);
		if (cha instanceof L2PcInstance)
			return addPcName((L2PcInstance) cha);
		if (cha instanceof L2Summon)
			return addNpcName((L2Summon) cha);
		return addString(cha.getName());
	}

	public ConfirmDlg addItemName(int id)
	{
		_info.add(new CnfDlgData(TYPE_ITEM_NAME, id));
		return this;
	}

	public ConfirmDlg addItemName(L2Item item)
	{
		return addItemName(item.getItemId());
	}

	public ConfirmDlg addItemName(L2ItemInstance item)
	{
		return addItemName(item.getItem().getItemId());
	}

	public ConfirmDlg addNpcName(int id)
	{
		_info.add(new CnfDlgData(TYPE_NPC_NAME, id));
		return this;
	}

	public ConfirmDlg addNpcName(L2Npc npc)
	{
		return addNpcName(npc.getTemplate());
	}

	public ConfirmDlg addNpcName(L2NpcTemplate tpl)
	{
		return addNpcName(tpl.getNpcId());
	}

	public ConfirmDlg addNpcName(L2Summon npc)
	{
		return addNpcName(npc.getNpcId());
	}

	public ConfirmDlg addNumber(int number)
	{
		_info.add(new CnfDlgData(TYPE_NUMBER, number));
		return this;
	}

	public ConfirmDlg addPcName(L2PcInstance pc)
	{
		return addString(pc.getName());
	}

	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}

	public ConfirmDlg addSkillName(int id)
	{
		return addSkillName(id, 1);
	}

	public ConfirmDlg addSkillName(int id, int lvl)
	{
		_info.add(new CnfDlgData(TYPE_SKILL_NAME, id));
		_skillLvL = lvl;
		return this;
	}

	public ConfirmDlg addSkillName(L2Effect effect)
	{
		return addSkillName(effect.getSkill());
	}

	public ConfirmDlg addSkillName(L2Skill skill)
	{
		if (skill.getId() != skill.getDisplayId())
			return addString(skill.getName());

		return addSkillName(skill.getId(), skill.getLevel());
	}

	public ConfirmDlg addString(String text)
	{
		_info.add(new CnfDlgData(TYPE_TEXT, text));
		return this;
	}

	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}

	public ConfirmDlg addZoneName(int x, int y, int z)
	{
		Integer[] coord =
		{
			x,
			y,
			z
		};
		_info.add(new CnfDlgData(TYPE_ZONE_NAME, coord));
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xed);
		writeD(_messageId);

		if (_info.isEmpty())
		{
			writeD(0x00);
			writeD(_time);
			writeD(_requesterId);
		}
		else
		{
			writeD(_info.size());

			for (CnfDlgData data : _info)
			{
				writeD(data.type);

				switch (data.type)
				{
					case TYPE_TEXT:
						writeS((String) data.value);
						break;
					case TYPE_NUMBER:
					case TYPE_NPC_NAME:
					case TYPE_ITEM_NAME:
						writeD((Integer) data.value);
						break;
					case TYPE_SKILL_NAME:
						writeD((Integer) data.value); // Skill Id
						writeD(_skillLvL); // Skill lvl
						break;
					case TYPE_ZONE_NAME:
						Integer[] array = (Integer[]) data.value;
						writeD(array[0]);
						writeD(array[1]);
						writeD(array[2]);
						break;
				}
			}
			if (_time != 0)
			{
				writeD(_time);
			}
			if (_requesterId != 0)
			{
				writeD(_requesterId);
			}
		}
	}

}