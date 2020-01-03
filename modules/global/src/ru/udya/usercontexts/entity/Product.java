package ru.udya.usercontexts.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@NamePattern("%s|name")
@Table(name = "CUBAUSERCONTEXTS_PRODUCT")
@Entity(name = "cubausercontexts$Product")
public class Product extends StandardEntity {
    private static final long serialVersionUID = - 1646867684086017187L;

    @Column(name = "NAME")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}