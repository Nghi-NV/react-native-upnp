/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.model.mediaserver;

import android.content.Context;
import android.util.Log;

import com.reactlibrary.R;

import org.droidupnp.model.cling.localContent.AudioContainer;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentDirectoryService extends AbstractContentDirectoryService {
    private final static String TAG = "ContentDirectoryService";

    public final static char SEPARATOR = '$';

    // Type
    public final static int ROOT_ID = 0;
    //	public final static int VIDEO_ID  = 1;
    public final static int AUDIO_ID = 2;
//	public final static int IMAGE_ID  = 3;

    // Test
    public final static String VIDEO_TXT = "Videos";
    public final static String AUDIO_TXT = "Music";
    public final static String IMAGE_TXT = "Images";

    // Type subfolder
    public final static int ALL_ID = 0;
    public final static int FOLDER_ID = 1;
    public final static int ARTIST_ID = 2;
    public final static int ALBUM_ID = 3;

    // Prefix item
    public final static String VIDEO_PREFIX = "v-";
    public final static String AUDIO_PREFIX = "a-";
    public final static String IMAGE_PREFIX = "i-";
    public final static String DIRECTORY_PREFIX = "d-";


    private static Context ctx;
    private static String baseURL;

    public ContentDirectoryService() {
        Log.v(TAG, "Call default constructor...");
    }

    public ContentDirectoryService(Context ctx, String baseURL) {
        this.ctx = ctx;
        this.baseURL = baseURL;
    }

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag,
                               String filter, long firstResult, long maxResults,
                               SortCriterion[] orderby) throws ContentDirectoryException {
        Log.d(TAG, "Will browse " + objectID);

        try {
            DIDLContent didl = new DIDLContent();
            Container container = new AudioContainer("" + ALL_ID, "" + AUDIO_ID,
                    "All", ctx.getString(R.string.app_name), baseURL, ctx, null, null);

            if (container != null) {
                Log.d(TAG, "List container...");

                // Get container first
                for (Container c : container.getContainers())
                    didl.addContainer(c);

                Log.d(TAG, "List item...");

                // Then get item
                for (Item i : container.getItems())
                    didl.addItem(i);

                Log.d(TAG, "Return result...");

                int count = container.getChildCount();
                Log.d(TAG, "Child count : " + count);
                String answer = "";
                try {
                    answer = new DIDLParser().generate(didl);
                } catch (Exception ex) {
                    throw new ContentDirectoryException(
                            ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
                }
                Log.d(TAG, "answer : " + answer);

                return new BrowseResult(answer, count, count);
            }
        } catch (Exception ex) {
            throw new ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
        }

        Log.e(TAG, "No container for this ID !!!");
        throw new ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT);
    }
}
