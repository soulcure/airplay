package com.coocaa.tvpi.base.mvvm;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.coocaa.tvpi.base.BaseFragment;


public class BaseViewModelProvideFragment extends BaseFragment {
    private ViewModelProvider.Factory factory;
    protected AppCompatActivity activity;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }


    protected ViewModelProvider getAppViewModelProvider() {
        return new ViewModelProvider((ViewModelStoreOwner) activity.getApplicationContext(), getAppFactory(activity));
    }

    private ViewModelProvider.Factory getAppFactory(Activity activity) {
        checkActivity(this);
        Application application = checkApplication(activity);
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return factory;
    }

    private void checkActivity(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
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
