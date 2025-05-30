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
package com.dream.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

public class ScriptPackage
{
	private final static Logger _log = Logger.getLogger(ScriptPackage.class);

	private final List<ScriptDocument> _scriptFiles;
	private final String _name;

	public ScriptPackage(ZipFile pack)
	{
		_scriptFiles = new ArrayList<>();
		_name = pack.getName();
		addFiles(pack);
	}

	private void addFiles(ZipFile pack)
	{
		for (Enumeration<? extends ZipEntry> e = pack.entries(); e.hasMoreElements();)
		{
			ZipEntry entry = e.nextElement();
			if (entry.getName().endsWith(".xml"))
			{
				try
				{
					ScriptDocument newScript = new ScriptDocument(entry.getName(), pack.getInputStream(entry));
					_scriptFiles.add(newScript);
				}
				catch (IOException e1)
				{
					_log.error(e1.getMessage(), e1);
				}
			}
			else if (!entry.isDirectory())
			{

			}
		}
	}

	public String getName()
	{
		return _name;
	}

	public List<ScriptDocument> getScriptFiles()
	{
		return _scriptFiles;
	}

	@Override
	public String toString()
	{
		if (getScriptFiles().isEmpty())
			return "Empty script Package.";

		String out = "Package Name: " + getName() + "\n";

		if (!getScriptFiles().isEmpty())
		{
			out += "Xml Script Files...\n";
			for (ScriptDocument script : getScriptFiles())
			{
				out += script.getName() + "\n";
			}
		}
		return out;
	}
}