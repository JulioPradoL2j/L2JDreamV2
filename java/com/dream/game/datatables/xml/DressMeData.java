package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.data.xml.StatSet;
import com.dream.game.model.holders.DressMeEffectHolder;
import com.dream.game.model.holders.DressMeHolder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

public class DressMeData implements IXmlReader
{
	private final List<DressMeHolder> _entries = new ArrayList<>();
	
	public DressMeData()
	{
		load();
	}
	
	public void reload()
	{
		_entries.clear();
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/custom/mods/DressMeData.xml");
		LOGGER.info("Loaded {" + _entries.size() + "} DressMe entries.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "dressMeList", listNode -> forEach(listNode, "dress", dressNode -> {
			final StatSet attrs = parseAttributes(dressNode);
			
			final DressMeHolder holder = new DressMeHolder(attrs);
			
			forEach(dressNode, "visualSet", setNode -> holder.setVisualSet(parseAttributes(setNode)));
			forEach(dressNode, "visualWep", wepNode -> holder.setWeaponSet(parseAttributes(wepNode)));
			forEach(dressNode, "visualEffect", fxNode -> holder.setEffect(new DressMeEffectHolder(parseAttributes(fxNode))));
			
			_entries.add(holder);
		}));
	}
	
	public List<DressMeHolder> getEntries()
	{
		return _entries;
	}
	
	public DressMeHolder getBySkillId(int skillId)
	{
		return _entries.stream().filter(d -> d.getSkillId() == skillId).findFirst().orElse(null);
	}
	
	public static DressMeData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DressMeData INSTANCE = new DressMeData();
	}
}