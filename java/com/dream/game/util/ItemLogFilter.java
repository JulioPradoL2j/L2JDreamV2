package com.dream.game.util;

import java.util.EnumSet;
import java.util.logging.LogRecord;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.templates.item.AbstractL2ItemType;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.jdklog.L2LogFilter;

public final class ItemLogFilter extends L2LogFilter
{
	private static final String EXCLUDED_PROCESSES = "Consume";

	private static final EnumSet<L2EtcItemType> EXCLUDED_ITEM_TYPES = EnumSet.of(L2EtcItemType.SHOT, L2EtcItemType.ARROW, L2EtcItemType.HERB);

	@Override
	protected String getLoggerName()
	{
		return "item";
	}

	@Override
	public boolean isLoggable(LogRecord record)
	{
		if (!super.isLoggable(record))
			return false;

		if (record.getParameters() != null)
		{
			AbstractL2ItemType type = ((L2ItemInstance) record.getParameters()[0]).getItemType();
			if (EXCLUDED_ITEM_TYPES.contains(type))
				return false;
		}

		String[] messageList = record.getMessage().split(":");
		if (messageList.length >= 2 && EXCLUDED_PROCESSES.contains(messageList[1]))
			return false;

		return true;
	}

}