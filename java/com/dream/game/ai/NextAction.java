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
package com.dream.game.ai;

import java.util.ArrayList;
import java.util.List;

public class NextAction
{
	public interface NextActionCallback
	{
		public void doWork();
	}

	private List<CtrlEvent> _events;
	private List<CtrlIntention> _intentions;
	private NextActionCallback _callback;

	public NextAction(CtrlEvent event, CtrlIntention intention, NextActionCallback callback)
	{
		if (_events == null)
		{
			_events = new ArrayList<>();
		}

		if (_intentions == null)
		{
			_intentions = new ArrayList<>();
		}

		if (event != null)
		{
			_events.add(event);
		}

		if (intention != null)
		{
			_intentions.add(intention);
		}
		setCallback(callback);
	}

	public NextAction(List<CtrlEvent> events, List<CtrlIntention> intentions, NextActionCallback callback)
	{
		_events = events;
		_intentions = intentions;
		setCallback(callback);
	}

	public void addEvent(CtrlEvent event)
	{
		if (_events == null)
		{
			_events = new ArrayList<>();
		}

		if (event != null)
		{
			_events.add(event);
		}
	}

	public void addIntention(CtrlIntention intention)
	{
		if (_intentions == null)
		{
			_intentions = new ArrayList<>();
		}

		if (intention != null)
		{
			_intentions.add(intention);
		}
	}

	public void doAction()
	{
		if (_callback != null)
		{
			_callback.doWork();
		}
	}

	public NextActionCallback getCallback()
	{
		return _callback;
	}

	public List<CtrlEvent> getEvents()
	{
		if (_events == null)
		{
			_events = new ArrayList<>();
		}
		return _events;
	}

	public List<CtrlIntention> getIntentions()
	{
		if (_intentions == null)
		{
			_intentions = new ArrayList<>();
		}
		return _intentions;
	}

	public void removeEvent(CtrlEvent event)
	{
		if (_events == null)
			return;
		_events.remove(event);
	}

	public void removeIntention(CtrlIntention intention)
	{
		if (_intentions == null)
			return;
		_intentions.remove(intention);
	}

	public void setCallback(NextActionCallback callback)
	{
		_callback = callback;
	}

	public void setEvents(List<CtrlEvent> event)
	{
		_events = event;
	}

	public void setIntentions(List<CtrlIntention> intentions)
	{
		_intentions = intentions;
	}
}