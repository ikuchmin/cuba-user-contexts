package ru.udya.usercontexts.core.builder;

import com.haulmont.cuba.core.entity.StandardEntity;

import java.util.List;

public class A extends StandardEntity {
    protected List<B> bs;

    public List<B> getBs() {
        return bs;
    }

    public void setBs(List<B> bs) {
        this.bs = bs;
    }
}
