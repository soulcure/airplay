package com.coocaa.swaiotos.virtualinput.statemachine;

import android.os.Parcel;
import android.os.Parcelable;

import com.coocaa.swaiotos.virtualinput.VirtualInputTypeDefine;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class SceneMachine implements Serializable, Parcelable {
    public int sceneType = VirtualInputTypeDefine.TYPE_DEFAULT;
    public String typeName;
    public String sceneName = VirtualInputTypeDefine.NAME_DEFAULT;
    public String clienId;

    public SceneMachine() {

    }

    protected SceneMachine(Parcel in) {
        sceneType = in.readInt();
        typeName = in.readString();
        sceneName = in.readString();
        clienId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sceneType);
        dest.writeString(typeName);
        dest.writeString(sceneName);
        dest.writeString(clienId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SceneMachine> CREATOR = new Creator<SceneMachine>() {
        @Override
        public SceneMachine createFromParcel(Parcel in) {
            return new SceneMachine(in);
        }

        @Override
        public SceneMachine[] newArray(int size) {
            return new SceneMachine[size];
        }
    };

    public void refresh(SceneMachine stateMachine) {
        this.sceneType = stateMachine.sceneType;
        this.typeName = stateMachine.typeName;
        this.sceneName = stateMachine.sceneName;
        this.clienId = stateMachine.clienId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SceneMachine{");
        sb.append("sceneType=").append(sceneType);
        sb.append(", typeName='").append(typeName).append('\'');
        sb.append(", sceneName='").append(sceneName).append('\'');
        sb.append(", clienId='").append(clienId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
