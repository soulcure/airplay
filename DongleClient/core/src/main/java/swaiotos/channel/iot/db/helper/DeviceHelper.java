package swaiotos.channel.iot.db.helper;

import android.content.Context;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import swaiotos.channel.iot.db.bean.Device;
import swaiotos.channel.iot.db.dao.DeviceDao;
import swaiotos.channel.iot.db.manager.DaoManager;


/**
 * Author:  colin
 * Date:    2020-12-21 20:06
 * Description:
 */
public class DeviceHelper {

    private static DeviceHelper instance;


    public static DeviceHelper instance() {
        if (instance == null) {
            instance = new DeviceHelper();
        }
        return instance;
    }

    private DeviceHelper() {

    }


    /**
     * 查询所有设备记录
     *
     * @param context
     * @return
     */
    public List<Device> toQueryDeviceList(Context context) {
        DeviceDao dao = DaoManager.instance(context).getDaoSession().getDeviceDao();
        QueryBuilder<Device> qb = dao.queryBuilder();
        return qb.list();
    }


    /**
     * 删除所有设备记录
     *
     * @return
     */
    public void delAllDevice(Context context) {
        DeviceDao dao = DaoManager.instance(context).getDaoSession().getDeviceDao();
        dao.deleteAll();
    }


    public void delDeviceBySid(Context context, String sid) {
        DeviceDao dao = DaoManager.instance(context).getDaoSession().getDeviceDao();
        QueryBuilder<Device> qb = dao.queryBuilder();
        DeleteQuery<Device> dq = qb.where(DeviceDao.Properties.ZpLsid.eq(sid)).buildDelete();
        dq.executeDeleteWithoutDetachingEntities();
    }


    /**
     * 添加或者更新
     *
     * @param context
     * @param list
     */
    public void insertOrUpdate(Context context, List<Device> list) {
        if (list != null && list.size() > 0) {
            DeviceDao dao = DaoManager.instance(context).getDaoSession().getDeviceDao();
            dao.insertOrReplaceInTx(list);
        }
    }


    /**
     * 更新设备
     *
     * @param context
     * @param bean
     */
    public void insertOrUpdate(Context context, Device bean) {
        DeviceDao dao = DaoManager.instance(context).getDaoSession().getDeviceDao();
        dao.insertOrReplace(bean);
    }


    /**
     * 通过sid查询设备
     *
     * @param context
     * @return
     */
    public Device toQueryDeviceBySid(Context context, String sid) {
        Device bean = null;
        DeviceDao dao = DaoManager.instance(context).getDaoSession().getDeviceDao();
        QueryBuilder<Device> qb = dao.queryBuilder();
        List<Device> list = qb.where(DeviceDao.Properties.ZpLsid.eq(sid))
                .orderDesc(DeviceDao.Properties.Id).list();
        if (list != null && list.size() > 0) {
            bean = list.get(0);
        }
        return bean;
    }


}
