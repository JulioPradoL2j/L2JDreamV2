package com.dream.game.skills;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.dream.game.model.L2Skill;
import com.dream.game.skills.conditions.Condition;
import com.dream.game.skills.conditions.ConditionParser;
import com.dream.game.skills.effects.EffectTemplate;
import com.dream.game.skills.funcs.FuncTemplate;
import com.dream.game.templates.item.L2Equip;

abstract class DocumentBase
{
	static final Logger _log = Logger.getLogger(DocumentBase.class);

	final File _file;

	private final ConditionParser _conditionParser = new ConditionParser()
	{
		@Override
		protected String getNodeValue(String nodeValue, Object template)
		{
			return getValue(nodeValue, template);
		}
	};

	DocumentBase(File pFile)
	{
		_file = pFile;
	}

	final void attachFunc(Node n, Object template, String name, Condition attachCond)
	{
		final NamedNodeMap attrs = n.getAttributes();

		final Stats stat = Stats.valueOfXml(attrs.getNamedItem("stat").getNodeValue());
		final int ord = Integer.decode(attrs.getNamedItem("order").getNodeValue());
		final String value = getLambda(n, template);

		final Condition applayCond = parseConditionIfExists(n.getFirstChild(), template);

		final FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, value);

		if (template instanceof L2Equip)
		{
			((L2Equip) template).attach(ft);
		}
		else if (template instanceof L2Skill)
		{
			((L2Skill) template).attach(ft);
		}
		else if (template instanceof EffectTemplate)
		{
			((EffectTemplate) template).attach(ft);
		}
		else
			throw new IllegalStateException("Invalid template for a Func");
	}

	abstract String getDefaultNodeName();

	final String getLambda(Node n, Object template)
	{
		return getValue(n.getAttributes().getNamedItem("val").getNodeValue(), template);
	}

	String getTableValue(String value, Object template)
	{
		throw new IllegalStateException();
	}

	final String getValue(String value, Object template)
	{
		if (value != null && value.length() >= 1 && value.charAt(0) == '#')
			return getTableValue(value, template);

		return value;
	}

	final void parse()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			parseDocument(factory.newDocumentBuilder().parse(_file));
		}
		catch (Exception e)
		{
			_log.fatal("Error in file: " + _file, e);
		}
	}

	final Condition parseConditionIfExists(Node n, Object template)
	{
		return _conditionParser.parseConditionIfExists(n, template);
	}

	final Condition parseConditionWithMessage(Node n, Object template)
	{
		return _conditionParser.parseConditionWithMessage(n, template);
	}

	abstract void parseDefaultNode(Node n);

	final void parseDocument(Document doc)
	{
		final String defaultNodeName = getDefaultNodeName();

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if (defaultNodeName.equalsIgnoreCase(d.getNodeName()))
					{
						parseDefaultNode(d);
					}
					else if (d.getNodeType() == Node.ELEMENT_NODE)
						throw new IllegalStateException("Invalid tag <" + d.getNodeName() + ">");
			}
			else if (defaultNodeName.equalsIgnoreCase(n.getNodeName()))
			{
				parseDefaultNode(n);
			}
			else if (n.getNodeType() == Node.ELEMENT_NODE)
				throw new IllegalStateException("Invalid tag <" + n.getNodeName() + ">");
	}

	final void parseTemplate(Node n, Object template)
	{

		n = n.getFirstChild();

		Condition condition = null;

		if (n != null)
			if ("cond".equalsIgnoreCase(n.getNodeName()))
			{
				condition = parseConditionWithMessage(n, template);
				n = n.getNextSibling();
			}

		for (; n != null; n = n.getNextSibling())
		{
			parseTemplateNode(n, template, condition);
		}
	}

	void parseTemplateNode(Node n, Object template, Condition condition)
	{
		if ("add".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "Add", condition);
		}
		else if ("sub".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "Sub", condition);
		}
		else if ("mul".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "Mul", condition);
		}
		else if ("script".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "Script", condition);
		}
		else if ("basemul".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "BaseMul", condition);
		}
		else if ("div".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "Div", condition);
		}
		else if ("set".equalsIgnoreCase(n.getNodeName()))
		{
			attachFunc(n, template, "Set", condition);
		}
		else if (n.getNodeType() == Node.ELEMENT_NODE)
			throw new IllegalStateException("Invalid tag <" + n.getNodeName() + "> in template");
	}
}
