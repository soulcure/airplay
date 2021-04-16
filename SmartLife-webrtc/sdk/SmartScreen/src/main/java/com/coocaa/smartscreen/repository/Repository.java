package com.coocaa.smartscreen.repository;

import com.coocaa.smartscreen.network.api.VoiceControlApiService;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.smartscreen.repository.service.BindCodeRepository;
import com.coocaa.smartscreen.repository.service.DeviceRepository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.smartscreen.repository.service.MallRepository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.smartscreen.repository.service.VideoCallRepository;
import com.coocaa.smartscreen.repository.service.VoiceControlRepository;
import com.coocaa.smartscreen.repository.service.impl.AppRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.BindCodeRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.DeviceRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.LoginRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.MallRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.MovieRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.VideoCallRepositoryImpl;
import com.coocaa.smartscreen.repository.service.impl.VoiceControlRepositoryImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过反射生成对应仓库实例
 * Created by songxing on 2020/6/5
 */
@SuppressWarnings("unchecked")
public class Repository {
    private static final Map<String, Object> cache = new HashMap<>();
    private static final Map<Class, Class> interfaceMap = new HashMap<>();


    static {
        interfaceMap.put(VoiceControlRepository.class, VoiceControlRepositoryImpl.class);
        interfaceMap.put(VideoCallRepository.class, VideoCallRepositoryImpl.class);
        interfaceMap.put(LoginRepository.class, LoginRepositoryImpl.class);
        interfaceMap.put(MovieRepository.class, MovieRepositoryImpl.class);
        interfaceMap.put(AppRepository.class, AppRepositoryImpl.class);
        interfaceMap.put(DeviceRepository.class, DeviceRepositoryImpl.class);
        interfaceMap.put(MallRepository.class, MallRepositoryImpl.class);
        interfaceMap.put(BindCodeRepository.class, BindCodeRepositoryImpl.class);
    }

//    private static final Map<Class<?>, Object> cache = new HashMap();


  /*  public static  <T> T get(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("only accept interface: ".concat(String.valueOf(service)));
        } else {
            synchronized(cache) {
                Object instance;
                if ((instance = cache.get(service)) == null) {
                    instance = Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object object, Method method, Object[] args) throws Throwable {
                            return ;
                        }
                    });
                    cache.put(service, instance);
                }
                return (T) instance;
            }
        }
    }*/


    public static <T> T get(Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("only accept interface: ".concat(String.valueOf(service)));
        } else
            synchronized(cache) {
                try {
                    String key = service.getSimpleName();
                    if (cache.containsKey(key)) {
                        return (T) cache.get(key);
                    } else {
                        T t = (T) interfaceMap.get(service).newInstance();
                        cache.put(key, t);
                        return t;
                    }
                } catch (InstantiationException e) {
                    throw new RuntimeException("Cannot create an instance of " + service, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot create an instance of " + service, e);
                }
            }
    }
}
