package com.dream.data;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ScriptDocument
{
	private final static Logger _log = Logger.getLogger(ScriptDocument.class);
	private Document _document = null;
	private String _name = null;

	public ScriptDocument(String name, InputStream input)
	{
		if (input == null)
			return;
		_name = name;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			_document = builder.parse(input);
		}
		catch (SAXException sxe)
		{
			_log.error("Invalid document " + name + ". Error = " + sxe.getMessage());
		}
		catch (ParserConfigurationException pce)
		{
			_log.error(pce.getMessage(), pce);
		}
		catch (IOException ioe)
		{
			_log.error(ioe.getMessage(), ioe);
		}
	}

	public Document getDocument()
	{
		return _document;
	}

	public String getName()
	{
		return _name;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}