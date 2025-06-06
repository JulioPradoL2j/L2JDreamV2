/*
 * L2Guardian - MrFreedomFights 
 * 
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
package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SetupGauge;
import com.dream.game.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands: polymorph
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2007/07/31 10:05:56 $
 */

public class AdminPolymorph extends gmHandler
{
	private static final String[] commands =
	{
		"polymorph",
		"unpolymorph",
		"polymorph_menu",
		"unpolymorph_menu"
	};
	
	@Override
	public String[] getCommandList()
	{
		return commands;
	}
	
	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null)
			return;
		L2Object targetChar = admin.getTarget();
		
		final String command = params[0];
		
		if (command.startsWith("polymorph"))
		{
			StringTokenizer st = new StringTokenizer(command);
			
			try
			{
				st.nextToken();
				String p1 = st.nextToken();
				
				if (st.hasMoreTokens())
				{
					String p2 = st.nextToken();
					doPolymorph(admin, targetChar, p2, p1);
					p2 = null;
				}
				else
				{
					doPolymorph(admin, targetChar, p1, "npc");
				}
				
				p1 = null;
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				
				admin.sendMessage("Usage: //polymorph [type] <id>");
			}
			
			targetChar = null;
			st = null;
		}
		else if (command.equals("unpolymorph"))
		{
			doUnpoly(admin, admin.getTarget());
		}
		
		if (command.contains("menu"))
		{
			showMainPage(admin);
		}
		
		return;
	}
	
	private static void doPolymorph(final L2PcInstance activeChar, final L2Object obj, final String id, final String type)
	{
		if (obj != null)
		{
			obj.getPoly().setPolyInfo(type, id);
			
			// animation
			if (obj instanceof L2Character)
			{
				L2Character Char = (L2Character) obj;
				MagicSkillUse msk = new MagicSkillUse(Char, Char, 1008, 1, 4000, 0, false);
				Char.broadcastPacket(msk);
				SetupGauge sg = new SetupGauge(0, 4000);
				Char.sendPacket(sg);
				Char = null;
				sg = null;
				msk = null;
			}
			
			// end of animation
			obj.decayMe();
			obj.spawnMe(obj.getX(), obj.getY(), obj.getZ());
			activeChar.sendMessage("Polymorph succeed");
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
	
	/**
	 * @param activeChar
	 * @param target
	 */
	private static void doUnpoly(final L2PcInstance activeChar, final L2Object target)
	{
		if (target != null)
		{
			target.getPoly().setPolyInfo(null, "1");
			target.decayMe();
			target.spawnMe(target.getX(), target.getY(), target.getZ());
			activeChar.sendMessage("Unpolymorph succeed");
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
	
	private static void showMainPage(final L2PcInstance admin)
	{
		sendHtml(admin, "effects");
	}
	
	private static void sendHtml(L2PcInstance admin, String patch)
	{
		String name = patch + ".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(admin.getObjectId());
		html.setFile("data/html/admin/menus/" + name);
		admin.sendPacket(html);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.dream.game.access.gmHandler#runCommand(com.dream.game.model.actor.instance.L2PcInstance, java.lang.String[])
	 */
	
}
