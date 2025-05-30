package com.dream.data.xml;

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class XMLDocument
{
	public static String get(Node n, String item)
	{
		final Node d = n.getAttributes().getNamedItem(item);
		if (d == null)
			return "";
		final String val = d.getNodeValue();
		if (val == null)
			return "";
		return val;
	}

	public static boolean get(Node n, String item, boolean dflt)
	{
		final Node d = n.getAttributes().getNamedItem(item);
		if (d == null)
			return dflt;
		final String val = d.getNodeValue();
		if (val == null)
			return dflt;
		return Boolean.parseBoolean(val);
	}

	public static int get(Node n, String item, int dflt)
	{
		final Node d = n.getAttributes().getNamedItem(item);
		if (d == null)
			return dflt;
		final String val = d.getNodeValue();
		if (val == null)
			return dflt;
		return Integer.parseInt(val);
	}

	public static long get(Node n, String item, long dflt)
	{
		final Node d = n.getAttributes().getNamedItem(item);
		if (d == null)
			return dflt;
		final String val = d.getNodeValue();
		if (val == null)
			return dflt;
		return Long.parseLong(val);
	}

	public static LinkedList<Node> getNodes(Node node)
	{
		LinkedList<Node> list = new LinkedList<>();
		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
		{
			list.add(n);
		}
		return list;
	}

	public static LinkedList<Node> getNodes(Node node, String name)
	{
		LinkedList<Node> list = new LinkedList<>();
		for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling())
			if (isNodeName(n, name))
			{
				list.add(n);
			}
		return list;
	}

	public static boolean isNodeName(Node node, String name)
	{
		return node != null && node.getNodeName().equals(name);
	}

	public void load(File documentFile) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		parseDocument(factory.newDocumentBuilder().parse(documentFile));
	}

	abstract protected void parseDocument(Document doc);
}