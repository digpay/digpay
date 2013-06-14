package com.btcashier.dao;

import org.springframework.transaction.annotation.Transactional;

import com.btcashier.domain.KeyValue;
import com.btcashier.domain.enums.Keys;

@Transactional(readOnly = true)
public interface KeyValueDao extends AbstractJpaDao<Integer, KeyValue> {

    KeyValue getByKey(Keys key);

}