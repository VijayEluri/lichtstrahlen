package com.escoand.android.lichtstrahlen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.escoand.android.lichtstrahlen_2014.R;

import de.escoand.android.library.CalendarAdapter;
import de.escoand.android.library.CalendarEvent;
import de.escoand.android.library.CalendarFragment;

public class CalendarDialog extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.calendar, null));
		return builder.create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		CalendarFragment calendar = (CalendarFragment) getFragmentManager()
				.findFragmentById(R.id.calendar);
		CalendarAdapter adapter = (CalendarAdapter) calendar.getListAdapter();

		ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
		SimpleDateFormat frmt = new SimpleDateFormat("yyyyMMdd",
				Locale.getDefault());
		String text;
		String text_short;

		Cursor cursor = new TextDatabase(getActivity()).getCalendarList();
		while (cursor.moveToNext()) {
			GregorianCalendar begin = new GregorianCalendar();
			GregorianCalendar end = new GregorianCalendar();

			try {
				begin.setTime(frmt.parse(cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_DATE))));
				end.setTime(frmt.parse(cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_DATE_UNTIL))));

				text = cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_VERSE));
				text_short = cursor.getString(cursor
						.getColumnIndex(TextDatabase.COLUMN_VERSE_SHORT));

				events.add(new CalendarEvent(begin, end, text, text_short));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		cursor.close();

		calendar.setEvents(events.toArray(new CalendarEvent[events.size()]));
		adapter.zoomToEvents();
		adapter.WEEK_NUMBERS = false;
		adapter.EVENT_BACKGROUND = R.color.calendar_background;
		adapter.EVENT_FOREGROUND = R.color.calendar_foreground;

		super.onActivityCreated(savedInstanceState);
	}
}
