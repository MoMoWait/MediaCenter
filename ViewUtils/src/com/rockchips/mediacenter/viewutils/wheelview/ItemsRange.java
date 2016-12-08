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

/**
 * Range for visible items.
 */
public class ItemsRange
{
    private int mFirst;

    private int mCount;

    public ItemsRange()
    {
        this(0, 0);
    }

    /**
     * 
     * @param first
     * @param count
     */
    public ItemsRange(int first, int count)
    {
        this.mFirst = first;
        this.mCount = count;
    }

    /**
     * 
     * @return the number of the first item
     */
    public int getFirst()
    {
        return mFirst;
    }

    /**
     * 
     * @return the number of last item
     */
    public int getLast()
    {
        return getFirst() + getCount() - 1;
    }

    /**
     * 
     * @return the count of items
     */
    public int getCount()
    {
        return mCount;
    }

    /**
     * 
     * @param index the item number
     * @return true if item is contained
     */
    public boolean contains(int index)
    {
        return index >= getFirst() && index <= getLast();
    }
}