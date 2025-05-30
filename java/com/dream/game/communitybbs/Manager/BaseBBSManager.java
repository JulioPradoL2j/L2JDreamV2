/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.communitybbs.Manager;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.cache.HtmCache;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ShowBoard;

public abstract class BaseBBSManager
{
	public static void separateAndSend(String html, L2PcInstance acha)
	{
		if (html == null)
			return;

		if (html.length() < 4090)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));

		}
		else if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));

		}
		else if (html.length() < 12270)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));

		}
	}

	public abstract void parsecmd(String command, L2PcInstance activeChar);

	public abstract void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar);

	protected void send1001(String html, L2PcInstance acha)
	{
		if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "1001"));
		}
	}

	protected void send1002(L2PcInstance acha)
	{
		send1002(acha, " ", " ", "0");
	}

	protected void send1002(L2PcInstance activeChar, String string, String string2, String string3)
	{
		List<String> _arg = new ArrayList<>();
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add(activeChar.getName());
		_arg.add(Integer.toString(activeChar.getObjectId()));
		_arg.add(activeChar.getAccountName());
		_arg.add("9");
		_arg.add(string2);
		_arg.add(string2);
		_arg.add(string);
		_arg.add(string3);
		_arg.add(string3);
		_arg.add("0");
		_arg.add("0");
		activeChar.sendPacket(new ShowBoard(_arg));
	}

	protected void sendError404(L2PcInstance activeChar, String fileName)
	{
		String content = HtmCache.getInstance().getHtm("data/html/communityboard/error404.htm");
		if (content == null)
		{
			content = "<html><body><br><br><center>404 : Your 404 error page is missing!' </center></body></html>";
		}
		else
		{
			content = content.replace("%filename%", fileName);
		}
		separateAndSend(content, activeChar);
	}
}