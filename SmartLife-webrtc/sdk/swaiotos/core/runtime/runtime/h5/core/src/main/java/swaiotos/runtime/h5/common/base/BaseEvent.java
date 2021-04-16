package swaiotos.runtime.h5.common.base;

/**
 * EventBus事件基类
 */
public class BaseEvent {
    /**
     * 可能类型有很多种，数据也不一样
     */
    private int eventType;
    private String typeString;
    private Object data;//数据对象
    private String eventId;

    public BaseEvent(){
    }

    public BaseEvent(int eventType) {
        this.eventType = eventType;
    }

    public BaseEvent(String typeString) {
        this.typeString = typeString;
    }

    public BaseEvent(Object data) {
        this.data = data;
    }

    public BaseEvent(int eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    public BaseEvent(int eventType, String typeString, Object data) {
        this.eventType = eventType;
        this.typeString = typeString;
        this.data = data;
    }
    public BaseEvent( String typeString,String eventId) {
        this.typeString = typeString;
        this.eventId = eventId;
    }
    public int getEventType() {
        return eventType;
    }
    public String getEventId(){
        return eventId;
    }
    public void setEventId(String eventId1){
        this.eventId = eventId1;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }
}
