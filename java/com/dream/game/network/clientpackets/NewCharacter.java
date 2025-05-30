package com.dream.game.network.clientpackets;

import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.model.base.ClassId;
import com.dream.game.network.serverpackets.NewCharacterSuccess;
import com.dream.game.templates.chars.L2PcTemplate;

public class NewCharacter extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		NewCharacterSuccess ct = new NewCharacterSuccess();

		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(0);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.fighter);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.mage);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkMage);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcMage);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter);
		ct.addChar(template);

		sendPacket(ct);
	}

}