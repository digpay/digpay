package com.btcashier.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.btcashier.domain.enums.Keys;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name="unique_key1", columnNames = { "key1" }))
public class KeyValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "key1")
    private Keys key;

    @Column
    private String value;

    public KeyValue() {
    }

    public KeyValue(Keys key) {
        this.key = key;
    }

    public Keys getKey() {
        return key;
    }

    public void setKey(Keys key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }
    
    public int getIntValue() {
        return Integer.parseInt(value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
