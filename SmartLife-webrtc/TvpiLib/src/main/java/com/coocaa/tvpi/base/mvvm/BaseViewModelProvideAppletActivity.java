package com.coocaa.tvpi.base.mvvm;

import android.app.Activity;
import android.app.Application;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.BaseAppletActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

public class BaseViewModelProvideAppletActivity extends BaseAppletActivity {
    private ViewModelProvider.Factory factory;

    protected ViewModelProvider getAppViewModelProvider() {
        return new ViewModelProvider((ViewModelStoreOwner) getApplicationContext(),
                getAppFactory(this));
    }

    private ViewModelProvider.Factory getAppFactory(Activity activity) {
        Application application = checkApplication(activity);
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return factory;
    }

    private Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }
}
