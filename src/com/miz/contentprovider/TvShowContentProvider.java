package com.miz.contentprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.miz.db.DbAdapterTvShow;
import com.miz.functions.MizLib;
import com.miz.mizuu.MizuuApplication;
import com.miz.mizuu.TvShow;

public class TvShowContentProvider extends SearchRecentSuggestionsProvider {

	static final String TAG = TvShowContentProvider.class.getSimpleName();
	public static final String AUTHORITY = TvShowContentProvider.class.getName();
	public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	private static final String[] COLUMNS = {
		BaseColumns._ID, // must include this column
		SearchManager.SUGGEST_COLUMN_TEXT_1, // First line (title)
		SearchManager.SUGGEST_COLUMN_TEXT_2, // Second line (smaller text)
		SearchManager.SUGGEST_COLUMN_INTENT_DATA, // Icon
		SearchManager.SUGGEST_COLUMN_ICON_1, // Icon
		SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, // TV show ID
		SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
		SearchManager.SUGGEST_COLUMN_SHORTCUT_ID };

	public TvShowContentProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String query = selectionArgs[0];
		if (query == null || query.length() == 0) {
			return null;
		}

		MatrixCursor cursor = new MatrixCursor(COLUMNS);

		try {
			List<TvShow> list = getSearchResults(query);
			int n = 0;
			for (TvShow show : list) {
				cursor.addRow(createRow(Integer.valueOf(n), show.getTitle(), show.getFirstAirdateYear(), show.getThumbnail(), show.getId()));
				n++;
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to lookup " + query, e);
		}
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private Object[] createRow(Integer id, String text1, String text2, String icon, String rowId) {
		return new Object[] {
				id, // _id
				text1, // text1
				text2, // text2
				icon,
				icon,
				rowId,
				"android.intent.action.SEARCH", // action
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}

	private List<TvShow> getSearchResults(String query) {
		List<TvShow> shows = new ArrayList<TvShow>();
		if (!MizLib.isEmpty(query)) {

			DbAdapterTvShow db = MizuuApplication.getTvDbAdapter();

			query = query.toLowerCase(Locale.ENGLISH);

			Cursor c = db.getAllShows();
			String title = "";
			while (c.moveToNext()) {
				title = c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_TITLE));

				if (title.toLowerCase(Locale.ENGLISH).startsWith(query)) {
					shows.add(new TvShow(
							getContext(),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_ID)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_TITLE)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_PLOT)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_RATING)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_GENRES)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_ACTORS)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_CERTIFICATION)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_FIRST_AIRDATE)),
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_RUNTIME)),
							false,
							c.getString(c.getColumnIndex(DbAdapterTvShow.KEY_SHOW_EXTRA1))
							));
				}
			}
		}
		return shows;
	}
}