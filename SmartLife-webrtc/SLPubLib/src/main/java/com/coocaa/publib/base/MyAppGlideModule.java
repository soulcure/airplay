package com.coocaa.publib.base;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * @ClassName MyAppGlideModule
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-18
 * @Version TODO (write something)
 */
@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
        /*
        用途：是否检测AndroidManifest里面的GlideModule
        该方法，默认返回true。
        但是如果我们通过上面的注解和继承AppGlideModule生成自己的module时，官方要求我们实现这个方法，返回并且false，这样避免AndroidManifest加载两次
        */
    }
}
