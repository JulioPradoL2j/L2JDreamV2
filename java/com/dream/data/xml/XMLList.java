package com.dream.data.xml;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.dream.config.PropertyTransformer;
import com.dream.config.TransformFactory;
import com.dream.config.TransformationException;

public abstract class XMLList extends XMLObject
{
	public static class XMLListTransformer implements PropertyTransformer<XMLList>
	{

		@Override
		public XMLList transform(String value, Field field, Object... data) throws TransformationException
		{
			try
			{
				XMLList obj = (XMLList) field.get(data[1]);
				if (obj == null)
					throw new TransformationException("Field " + field.getName() + " not initialized");
				obj.load((Node) data[0]);
				return obj;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new TransformationException(e.getMessage());
			}

		}

	}

	protected static Logger _log = Logger.getLogger("DATABASE");

	static
	{
		TransformFactory.registerTransformer(XMLList.class, new XMLListTransformer());
	}

	public abstract void addObject(XMLObject obj);

	protected abstract XMLObject createNew(String nodeName, NamedNodeMap attr);

	@Override
	public void load(Node node)
	{
		super.load(node);
		for (Node d = node.getFirstChild(); d != null; d = d.getNextSibling())
		{
			XMLObject obj = createNew(d.getNodeName(), d.getAttributes());
			if (obj != null)
			{
				obj.load(d);
				addObject(obj);
			}
		}

	}

}