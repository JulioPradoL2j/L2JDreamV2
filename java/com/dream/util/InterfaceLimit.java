package com.dream.util;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public class InterfaceLimit implements DocumentListener
{
	private int _maximumLines;
	private final boolean _isRemoveFromStart;
	

	public InterfaceLimit(int maximumLines)
	{
		this(maximumLines, true);
	}
	

	public InterfaceLimit(int maximumLines, boolean isRemoveFromStart)
	{
		setLimitLines(maximumLines);
		_isRemoveFromStart = isRemoveFromStart;
	}
	

	public int getLimitLines()
	{
		return _maximumLines;
	}

	public void setLimitLines(int maximumLines)
	{
		if (maximumLines < 1)
		{
			final String message = "Maximum lines must be greater than 0";
			throw new IllegalArgumentException(message);
		}
		
		_maximumLines = maximumLines;
	}
	

	@Override
	public void insertUpdate(DocumentEvent e)
	{
		// Changes to the Document can not be done within the listener so we need to add the processing to the end of the EDT.
		SwingUtilities.invokeLater(() -> removeLines(e));
	}
	
	@Override
	public void removeUpdate(DocumentEvent e)
	{
		// Ignore.
	}
	
	@Override
	public void changedUpdate(DocumentEvent e)
	{
		// Ignore.
	}
	
	/*
	 * Remove lines from the Document when necessary.
	 */
	private void removeLines(DocumentEvent e)
	{
		// The root Element of the Document will tell us the total number of line in the Document.
		final Document document = e.getDocument();
		final Element root = document.getDefaultRootElement();
		
		while (root.getElementCount() > _maximumLines)
		{
			if (_isRemoveFromStart)
			{
				removeFromStart(document, root);
			}
			else
			{
				removeFromEnd(document, root);
			}
		}
	}
	
	
	private static void removeFromStart(Document document, Element root)
	{
		final Element line = root.getElement(0);
		final int end = line.getEndOffset();
		
		try
		{
			document.remove(0, end);
		}
		catch (BadLocationException ble)
		{
			System.out.println(ble);
		}
	}
	

	private static void removeFromEnd(Document document, Element root)
	{
		// We use start minus 1 to make sure we remove the newline character of the previous line.
		
		final Element line = root.getElement(root.getElementCount() - 1);
		final int start = line.getStartOffset();
		final int end = line.getEndOffset();
		
		try
		{
			document.remove(start - 1, end - start);
		}
		catch (BadLocationException ble)
		{
			System.out.println(ble);
		}
	}
}
