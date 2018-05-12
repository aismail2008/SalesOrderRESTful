package com.code.dal;

import java.io.Serializable;

import javax.persistence.Transient;

public abstract class BaseEntity implements Serializable {
    private boolean selected;
//    protected final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:sss";
    
    @Transient    
    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }     
}