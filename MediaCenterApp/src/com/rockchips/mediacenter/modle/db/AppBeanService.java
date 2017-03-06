package com.rockchips.mediacenter.modle.db;

import android.util.Log;

import org.xutils.ex.DbException;

import com.rockchips.mediacenter.application.MediaCenterApplication;

import java.util.ArrayList;
import java.util.List;

import momo.cn.edu.fjnu.androidutils.base.BaseBeanService;

/**
 * 数据库的增删改查，这里使用泛型方法
 * Created by GaoFei on 2016/3/25.
 */
public abstract class AppBeanService<T> implements BaseBeanService<T>{
    private final String TAG = AppBeanService.class.getSimpleName();
    @Override
    public void save(T object) {
        try {
            MediaCenterApplication.mDBManager.save(object);
        } catch (DbException e) {
            Log.i(TAG, "存储对象发生异常：" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void delete(T object) {
        try {
            MediaCenterApplication.mDBManager.delete(object);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(T object) {
        try {
            MediaCenterApplication.mDBManager.update(object);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public  List<T> getAll(Class<T> tClass) {
       List<T> lists = new ArrayList<T>();
        try {
            lists = MediaCenterApplication.mDBManager.findAll(tClass);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return lists;
    }

    @Override
    public T getObjectById(Class<T> tClass, Object id) {
        T object = null;
        try {
            object = MediaCenterApplication.mDBManager.findById(tClass, id);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public abstract boolean isExist(T object) ;

    @Override
    public void saveAll(List<T> objects) {
        try {
        		MediaCenterApplication.mDBManager.save(objects);
			
        } catch (DbException e) {
        	Log.i(TAG, "saveAll->exception:" + e);
            e.printStackTrace();
        }

    }

    @Override
    public void updateAll(List<T> objects) {
        try {
            MediaCenterApplication.mDBManager.update(objects);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveOrUpdateAll(List<T> objects) {
        try {
            MediaCenterApplication.mDBManager.saveOrUpdate(objects);
        } catch (DbException e) {
        	Log.i(TAG, "saveOrUpdateAll->exception:" + e);
            e.printStackTrace();
        }
    }
    
    public void saveOrUpdate(T object){
    	  try {
              MediaCenterApplication.mDBManager.saveOrUpdate(object);
          } catch (DbException e) {
              e.printStackTrace();
          }
    }
}