package com.coocaa.smartscreen.data.videocall;

import java.io.Serializable;
import java.util.List;

/**
 * @author kangwen
 * @date 2020/8/6.
 */
public class ContactsMulti implements Serializable {
    private List<ContactsResp> contactsResps;

    public ContactsMulti(List<ContactsResp> contactsResps) {
        this.contactsResps = contactsResps;
    }

    public List<ContactsResp> getContactsResps() {
        return contactsResps;
    }

    public void setContactsResps(List<ContactsResp> contactsResps) {
        this.contactsResps = contactsResps;
    }
}
