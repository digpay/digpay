package com.btcashier.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.btcashier.dao.KeyValueDao;
import com.btcashier.domain.KeyValue;
import com.btcashier.domain.enums.Keys;

@Repository("keyValueDao")
public class KeyValueImpl extends AbstractJpaDaoImpl<Integer, KeyValue> implements KeyValueDao {

    @Override
    public KeyValue getByKey(Keys key) {
        final Query q = em.createQuery("select kv from KeyValue kv where kv.key = :key", KeyValue.class);
        q.setParameter("key", key);
        final List<KeyValue> kvs = q.getResultList();
        return obtainFirst(kvs);
    }
    
    

}
