/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.escoand.android.lichtstrahlen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.escoand.android.library.AbstractDatabase;
import com.escoand.android.lichtstrahlen_2014.R;

@SuppressLint("SimpleDateFormat")
public class TextDatabase extends AbstractDatabase {
	public static final String DATABASE_NAME = "verses";
	public static final int DATABASE_VERSION = 105;

	protected static final String COLUMN_DATE = "date";
	protected static final String COLUMN_DATE_UNTIL = "date_until";
	protected static final String COLUMN_AUTHOR = "author";
	protected static final String COLUMN_TITLE = "title";
	protected static final String COLUMN_VERSE = "verse";
	protected static final String COLUMN_VERSE_UNTIL = "verse_until";
	protected static final String COLUMN_TEXT = "text";
	protected static final String COLUMN_ORDERID = "orderid";
	protected static final String COLUMN_COUNT = "count";

	private final Context context;

	public TextDatabase(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		this.context = context;

		TABLE_NAME = DATABASE_NAME;
		COLUMNS = new String[] { COLUMN_DATE, COLUMN_AUTHOR, COLUMN_TITLE,
				COLUMN_VERSE, COLUMN_TEXT, COLUMN_ORDERID };
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);

		/* load data */
		InputStream stream = context.getResources().openRawResource(R.raw.data);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String line;
		ContentValues values = new ContentValues();

		try {
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				if (cols.length < 6)
					continue;
				values.clear();
				values.put(COLUMN_ORDERID, Integer.parseInt(cols[0]));
				values.put(COLUMN_DATE, cols[1].trim());
				values.put(COLUMN_AUTHOR, cols[2].trim());
				values.put(COLUMN_VERSE, cols[3].trim());
				values.put(COLUMN_TITLE, cols[4].trim());
				values.put(COLUMN_TEXT, cols[5].trim());
				db.insert(TABLE_NAME, null, values);
				if (cols.length >= 8 && !cols[6].equals("")
						&& !cols[7].equals("")) {
					values.remove(COLUMN_AUTHOR);
					values.put(COLUMN_ORDERID, -1);
					values.put(COLUMN_TITLE,
							context.getString(R.string.mainWeek));
					values.put(COLUMN_TEXT, cols[6].trim());
					values.put(COLUMN_VERSE, cols[7].trim());
					db.insert(TABLE_NAME, null, values);
				}
				if (cols.length >= 10 && !cols[8].equals("")
						&& !cols[9].equals("")) {
					values.remove(COLUMN_AUTHOR);
					values.put(COLUMN_ORDERID, -2);
					values.put(COLUMN_TITLE,
							context.getString(R.string.mainMonth));
					values.put(COLUMN_TEXT, cols[8].trim());
					values.put(COLUMN_VERSE, cols[9].trim());
					db.insert(TABLE_NAME, null, values);
				}
			}
			reader.close();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* get list */
	public final Cursor getList() {
		// multiple lines returned in some cases
		Cursor cursor = getReadableDatabase().rawQuery(
				"select distinct b." + COLUMN_VERSE + " as " + COLUMN_VERSE
						+ ", c." + COLUMN_VERSE + " as " + COLUMN_VERSE_UNTIL
						+ ", a.* from (select min(" + COLUMN_DATE + ") as "
						+ COLUMN_DATE + ", max(" + COLUMN_DATE + ") as "
						+ COLUMN_DATE_UNTIL + ", count(*) as " + COLUMN_COUNT
						+ ", " + COLUMN_ORDERID + ", rowid as _id from "
						+ TABLE_NAME + " where " + COLUMN_ORDERID
						+ ">0 group by " + COLUMN_ORDERID + ") a join "
						+ TABLE_NAME + " b on a." + COLUMN_DATE + "=b."
						+ COLUMN_DATE + " left join " + TABLE_NAME + " c on a."
						+ COLUMN_DATE_UNTIL + "=c." + COLUMN_DATE + " and a."
						+ COLUMN_DATE + "!=a." + COLUMN_DATE_UNTIL
						+ " order by " + COLUMN_ORDERID, new String[] {});
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	/* get full list */
	public final Cursor getFullList(final String searchIn,
			final String[] searchFor) {
		return getItems(new String[] { COLUMN_DATE, COLUMN_TITLE, COLUMN_VERSE,
				COLUMN_TEXT, COLUMN_AUTHOR }, searchIn, searchFor, COLUMN_DATE
				+ ", " + COLUMN_ORDERID);
	}

	/* get data for specific date */
	public final Cursor getDate(final Date date) {
		return getFullList(COLUMN_DATE + "=?",
				new String[] { new SimpleDateFormat("yyyyMMdd").format(date) });
	}

	/* get search result */
	public final Cursor getSearch(final String searchFor) {
		return getFullList(TABLE_NAME + " MATCH ?", new String[] { searchFor });
	}
}