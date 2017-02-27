/*
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

package com.rockchips.mediacenter.view;

import java.util.LinkedList;
import java.util.List;


import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * Abstract Wheel adapter.
 */
public abstract class AbstractWheelAdapter implements WheelViewAdapter
{
    // Observers
    private List<DataSetObserver> mDatasetObservers;

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public View getEmptyItem(View convertView, ViewGroup parent)
    {
        return null;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        if (mDatasetObservers == null)
        {
            mDatasetObservers = new LinkedList<DataSetObserver>();
        }
        mDatasetObservers.add(observer);
        // mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        if (mDatasetObservers != null)
        {
            mDatasetObservers.remove(observer);
        }
        // mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged()
    {
        mDataSetObservable.notifyChanged();
    }

    /**
     * Notifies observers about data changing
     */
    public void notifyDataChangedEvent()
    {
        if (mDatasetObservers != null)
        {
            for (DataSetObserver observer : mDatasetObservers)
            {
                observer.onChanged();
            }
        }
    }

    /**
     * Notifies observers about invalidating data
     */
    protected void notifyDataInvalidatedEvent()
    {
        if (mDatasetObservers != null)
        {
            for (DataSetObserver observer : mDatasetObservers)
            {
                observer.onInvalidated();
            }
        }
    }
}
