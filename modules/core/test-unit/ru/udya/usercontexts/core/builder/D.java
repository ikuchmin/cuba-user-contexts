package ru.udya.usercontexts.core.builder;

import com.haulmont.cuba.core.entity.StandardEntity;

import java.util.List;

public class D extends StandardEntity {
    protected C parentC;
    protected List<B> bs;

    public C getParentC() {
        return parentC;
    }

    public void setParentC(C parentC) {
        this.parentC = parentC;
    }

    public List<B> getBs() {
        return bs;
    }

    public void setBs(List<B> bs) {
        this.bs = bs;
    }
}
