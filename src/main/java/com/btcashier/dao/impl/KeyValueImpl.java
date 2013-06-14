/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
