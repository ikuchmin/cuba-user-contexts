package ru.udya.usercontexts.core.builder;

import com.haulmont.cuba.core.entity.StandardEntity;

import java.util.List;

public class B extends StandardEntity {
    protected A a;
    protected List<C> cs;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public List<C> getCs() {
        return cs;
    }

    public void setCs(List<C> cs) {
        this.cs = cs;
    }

}
