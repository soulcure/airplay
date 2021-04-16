package swaiotos.runtime.h5.gameengine;

public class IotKeyCode {
    private String identify;
    private int keyCodeID;
    private int keyCode;
    private int keyAction;

    public enum KEYCODE_CODE_ENUM{
        KEYCODE_UP(101),
        KEYCODE_DOWN(102),
        KEYCODE_LEFT(103),
        KEYCODE_RIGHT(104),
        KEYCODE_MENU(105),
        KEYCODE_CENTER(106),
        KEYCODE_BACK(107),
        KEYCODE_A(108),
        KEYCODE_B(109),
        KEYCODE_X(110),
        KEYCODE_Y(111),
        KEYCODE_SHAKE(112),
        KEYCODE_BROADCAST_PAUSE(113),
        KEYCODE_BROADCAST_RESUME(114);

        private int value = 0;

        private KEYCODE_CODE_ENUM(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static KEYCODE_CODE_ENUM valueOf(int value) {    //    手写的从int到enum的转换函数
            switch (value) {
                case 101:
                    return KEYCODE_UP;
                case 102:
                    return KEYCODE_DOWN;
                case 103:
                    return KEYCODE_LEFT;
                case 104:
                    return KEYCODE_RIGHT;
                case 105:
                    return KEYCODE_MENU;
                case 106:
                    return KEYCODE_CENTER;
                case 107:
                    return KEYCODE_BACK;
                case 108:
                    return KEYCODE_A;
                case 109:
                    return KEYCODE_B;
                case 110:
                    return KEYCODE_X;
                case 111:
                    return KEYCODE_Y;
                case 112:
                    return KEYCODE_SHAKE;
                case 113:
                    return KEYCODE_BROADCAST_PAUSE;
                case 114:
                    return KEYCODE_BROADCAST_RESUME;
                default:
                    return null;
            }
        }

        public int value() {
            return this.value;
        }
    }

    public enum KEY_ACTION_ENUM{
        KEY_ACTION_PRESS_DOWN(201),
        KEY_ACTION_PRESS_UP(202),
        KEY_ACTION_TRIGGER(203);

        private int value = 0;

        private KEY_ACTION_ENUM(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static KEY_ACTION_ENUM valueOf(int value) {
            switch (value) {
                case 201:
                    return KEY_ACTION_PRESS_DOWN;
                case 202:
                    return KEY_ACTION_PRESS_UP;
                case 203:
                    return KEY_ACTION_TRIGGER;
                default:
                    return null;
            }
        }

        public int value() {
            return this.value;
        }
    }

    public IotKeyCode(String identify,int keyCodeID,KEY_ACTION_ENUM action,KEYCODE_CODE_ENUM keyCode){
        this.identify = identify;
        this.keyCodeID = keyCodeID;
        this.keyCode = keyCode.value();
        this.keyAction = action.value();
    }

    public int keycodeID(){
        return keyCodeID;
    }

    public String getIdentify(){
        return identify;
    }

    public int keyCode(){
        return keyCode;
    }

    public int keyAction(){
        return keyAction;
    }

    public static final class Builder {
        private String build_Identify;
        private int build_KeyCode = 0;
        private KEYCODE_CODE_ENUM KEYCODE;
        private KEY_ACTION_ENUM KEYACTION;

        public Builder identify(String id) {
            this.build_Identify =id;
            return this;
        }

        public Builder keyCodeID(int keyCodeID) {
            this.build_KeyCode =keyCodeID;
            return this;
        }

        public Builder keyCode(KEYCODE_CODE_ENUM keyCode) {
            this.KEYCODE = keyCode;
            return this;
        }

        public Builder keyAction(KEY_ACTION_ENUM keyAction) {
            this.KEYACTION = keyAction;
            return this;
        }

        public IotKeyCode build() {
            IotKeyCode keycode = new IotKeyCode(build_Identify,build_KeyCode, KEYACTION, KEYCODE);
            return keycode;
        }
    }
}
