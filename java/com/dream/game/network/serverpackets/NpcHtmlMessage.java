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

import org.apache.log4j.Logger;

import com.dream.game.cache.HtmCache;
import com.dream.game.model.actor.instance.L2PcInstance;

public final class NpcHtmlMessage extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(NpcHtmlMessage.class.getName());

	private final int _npcObjId;
	private String _html;
	private int _itemId = 0;
	private boolean _validate = true;

	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}

	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}

	public void disableValidation()
	{
		_validate = false;
	}

	public void replace(String pattern, double value)
	{
		_html = _html.replaceAll(pattern, Double.toString(value));
	}

	public void replace(String pattern, int value)
	{
		_html = _html.replaceAll(pattern, Integer.toString(value));
	}

	public void replace(String pattern, long value)
	{
		_html = _html.replaceAll(pattern, Long.toString(value));
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}

	@Override
	public void runImpl()
	{
		if (!_validate)
			return;

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		activeChar.clearBypass();
		for (int i = 0; i < _html.length(); i++)
		{
			int start = _html.indexOf("\"bypass ", i);
			int finish = _html.indexOf("\"", start + 1);
			if (start < 0 || finish < 0)
			{
				break;
			}

			if (_html.substring(start + 8, start + 10).equals("-h"))
			{
				start += 11;
			}
			else
			{
				start += 8;
			}

			i = finish;
			int finish2 = _html.indexOf("$", start);
			if (finish2 < finish && finish2 > 0)
			{
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			}
			else
			{
				activeChar.addBypass(_html.substring(start, finish).trim());
			}
		}
	}

	public void setFile(String filename)
	{
		setHtml(HtmCache.getInstance().getHtmForce(filename));
	}

	public void setHtml(String text)
	{
		if (text.length() > 8192)
		{
			_html = "<html><body>Html was too long.</body></html>";
			_log.warn("NpcHtmlMessage: html is too long");
			return;
		}
		_html = text;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x0f);

		writeD(_npcObjId);
		writeS(_html);
		writeD(_itemId);
	}

}