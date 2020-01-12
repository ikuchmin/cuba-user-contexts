package ru.udya.usercontexts.core.builder;

import com.haulmont.cuba.core.entity.StandardEntity;

import java.util.List;

public class C extends StandardEntity {
    protected B b;
    protected List<D> ds;

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public List<D> getDs() {
        return ds;
    }

    public void setDs(List<D> ds) {
        this.ds = ds;
    }
}
