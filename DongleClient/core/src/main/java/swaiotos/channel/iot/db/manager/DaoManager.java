package swaiotos.channel.iot.db.manager;

import android.content.Context;

import org.greenrobot.greendao.query.QueryBuilder;

import swaiotos.channel.iot.db.dao.DaoMaster;
import swaiotos.channel.iot.db.dao.DaoSession;

public class DaoManager {
    private static final String DB_NAME = "device.db";//数据库名称

    private volatile static DaoManager instance;//多线程访问
    private static DaoMaster.DevOpenHelper mHelper;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;
    private Context mContext;

    /**
     * 使用单例模式获得操作数据库的对象
     */
    public static DaoManager instance(Context context) {
        if (instance == null) {
            synchronized (DaoManager.class) {
                instance = new DaoManager(context);
            }
        }
        return instance;
    }

    public DaoManager(Context context) {
        mContext = context;
    }


    /**
     * 判断数据库是否存在，如果不存在则创建
     */
    public DaoMaster getDaoMaster() {
        if (null == mDaoMaster) {
            mHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            mDaoMaster = new DaoMaster(mHelper.getWritableDatabase());
        }
        return mDaoMaster;
    }

    /**
     * 完成对数据库的增删查找
     */
    public DaoSession getDaoSession() {
        if (null == mDaoSession) {
            if (null == mDaoMaster) {
                mDaoMaster = getDaoMaster();
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }

    /**
     * 设置debug模式开启或关闭，默认关闭
     */
    public void setDebug(boolean flag) {
        QueryBuilder.LOG_SQL = flag;
        QueryBuilder.LOG_VALUES = flag;
    }

    /**
     * 关闭数据库
     */
    public void closeDataBase() {
        closeHelper();
        closeDaoSession();
    }

    public void closeDaoSession() {
        if (null != mDaoSession) {
            mDaoSession.clear();
            mDaoSession = null;
        }
    }

    public void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }
    }
}