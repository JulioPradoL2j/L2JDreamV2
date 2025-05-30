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

import java.util.List;

import com.dream.util.StringUtil;

public class ShowBoard extends L2GameServerPacket
{
	public static final ShowBoard STATIC_SHOWBOARD_102 = new ShowBoard(null, "102");
	public static final ShowBoard STATIC_SHOWBOARD_103 = new ShowBoard(null, "103");

	private final static String TOP = "bypass _bbshome";
	private final static String FAV = "bypass _bbsgetfav";
	private final static String REGION = "bypass _bbsloc";
	private final static String CLAN = "bypass _bbsclan";
	private final static String MEMO = "bypass _bbsmemo";
	private final static String MAIL = "bypass _maillist_0_1_0_";
	private final static String FRIENDS = "bypass _friendlist_0_";
	private final static String ADDFAV = "bypass bbs_add_fav";

	private final StringBuilder _htmlCode;

	public ShowBoard(List<String> arg)
	{
		_htmlCode = StringUtil.startAppend(500, "1002\u0008");
		for (String str : arg)
		{
			StringUtil.append(_htmlCode, str, " \u0008");
		}
	}

	public ShowBoard(String htmlCode, String id)
	{
		_htmlCode = StringUtil.startAppend(500, id, "\u0008", htmlCode);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x6e);
		writeC(0x01); // 1 to show, 0 to hide
		writeS(TOP);
		writeS(FAV);
		writeS(REGION);
		writeS(CLAN);
		writeS(MEMO);
		writeS(MAIL);
		writeS(FRIENDS);
		writeS(ADDFAV);
		writeS(_htmlCode.toString());
	}

}