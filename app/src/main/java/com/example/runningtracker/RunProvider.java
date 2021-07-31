package com.example.runningtracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.FileNotFoundException;

// content provider class
public class RunProvider extends ContentProvider {

    // Defines a Data Access Object to perform the database operations
    private RunDao runDao;

    public static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(RunContract.AUTHORITY, "run", 1);
        uriMatcher.addURI(RunContract.AUTHORITY, "/*", 2);
    }

    @Override
    public boolean onCreate() {
        Log.d("g53mdp", "contentprovider oncreate");

        // Gets a Data Access Object to perform the database operations
        runDao = MyRoomDatabase.getDatabase(getContext()).getRunDao();

        return true;
    }

    // Retrieve cursor pointing at runs table
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Log.d("g53mdp", uri.toString() + " " + uriMatcher.match(uri));
        Cursor cursor;
        switch(uriMatcher.match(uri)) {
            case 1:
                cursor = runDao.getAllRuns();
                if (getContext() != null) {
                    cursor.setNotificationUri(getContext()
                            .getContentResolver(), uri);
                    return cursor;
                }
            default:
                return null;
        }
    }

    // get type of data returned
    @Nullable
    @Override
    public String getType(Uri uri) {

        String contentType;

        if (uri.getLastPathSegment()==null) {
            contentType = RunContract.CONTENT_TYPE_MULTIPLE;
        } else {
            contentType = RunContract.CONTENT_TYPE_SINGLE;
        }

        return contentType;
    }

    @Nullable
    @Override
    public Uri insert(@Nullable Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[]
            selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        throw new UnsupportedOperationException("not implemented");
    }

}
