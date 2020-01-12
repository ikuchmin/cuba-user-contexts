package ru.udya.usercontexts.core.builder.subpackage;

import com.haulmont.cuba.core.entity.StandardEntity;
import ru.udya.usercontexts.core.builder.A;

public class E extends StandardEntity {
    protected A a;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }
}
