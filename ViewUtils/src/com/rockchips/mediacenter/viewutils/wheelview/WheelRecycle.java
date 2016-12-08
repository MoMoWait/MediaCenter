/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rockchips.mediacenter.viewutils.wheelview;

import java.util.LinkedList;
import java.util.List;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Recycle stores wheel items to reuse.
 */
public class WheelRecycle
{
    // Cached items
    private List<View> mItems;

    // Cached empty items
    private List<View> mEmptyItems;

    // Wheel view
    private WheelView mWheel;

    /**
     * Constructor
     * @param wheel the wheel view
     */
    public WheelRecycle(WheelView wheel)
    {
        this.mWheel = wheel;
    }

    /**
     * Recycles items from specified layout. There are saved only items not
     * included to specified range. All the cached items are removed from
     * original layout.
     * 
     * @param layout the layout containing items to be cached
     * @param firstItem the number of first item in layout
     * @param range the range of current wheel items
     * @return the new value of first item number
     */
    public int recycleItems(LinearLayout layout, int firstItem, ItemsRange range)
    {
        int index = firstItem;
        for (int i = 0; i < layout.getChildCount(); )
        {
            if (!range.contains(index))
            {
                recycleView(layout.getChildAt(i), index);
                layout.removeViewAt(i);
                if (i == 0)
                { 
                    // first item
                    firstItem++;
                }
            }
            else
            {
                i++; // go to next item
            }
            index++;
        }
        return firstItem;
    }

    /**
     * Gets item view
     * @return the cached view
     */
    public View getItem()
    {
        return getCachedView(mItems);
    }

    /**
     * Gets empty item view
     * @return the cached empty view
     */
    public View getEmptyItem()
    {
        return getCachedView(mEmptyItems);
    }

    /**
     * Clears all views
     */
    public void clearAll()
    {
        if (mItems != null)
        {
            mItems.clear();
        }
        if (mEmptyItems != null)
        {
            mEmptyItems.clear();
        }
    }

    /**
     * Adds view to specified cache. Creates a cache list if it is null.
     * @param view the view to be cached
     * @param cache the cache list
     * @return the cache list
     */
    private List<View> addView(View view, List<View> cache)
    {
        if (cache == null)
        {
            cache = new LinkedList<View>();
        }

        cache.add(view);
        return cache;
    }

    /**
     * Adds view to cache. Determines view type (item view or empty one) by
     * index.
     * @param view the view to be cached
     * @param index the index of view
     */
    private void recycleView(View view, int index)
    {
        int count = mWheel.getViewAdapter().getItemsCount();

        if ((index < 0 || index >= count) && !mWheel.isCyclic())
        {
            // empty view
            mEmptyItems = addView(view, mEmptyItems);
        }
        else
        {
            mItems = addView(view, mItems);
        }
    }

    /**
     * Gets view from specified cache.
     * @param cache the cache
     * @return the first view from cache.
     */
    private View getCachedView(List<View> cache)
    {
        if (cache != null && cache.size() > 0)
        {
            View view = cache.get(0);
            cache.remove(0);
            return view;
        }
        return null;
    }

}
