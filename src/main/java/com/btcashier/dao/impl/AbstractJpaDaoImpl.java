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

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.btcashier.dao.AbstractJpaDao;

public abstract class AbstractJpaDaoImpl<K, E> implements AbstractJpaDao<K, E> {

    private final static Logger log = Logger.getLogger(AbstractJpaDaoImpl.class);

    private Class<E> entityClass;

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    protected EntityManager em;

    @SuppressWarnings("unchecked")
    public AbstractJpaDaoImpl() {
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[1];
    }

    @Override
    public void create(E entity) {
        em.persist(entity);
    }

    @Override
    public void remove(E entity) {
        em.remove(entity);
    }

    @Override
    public E saveOrUpdate(E entity) {
        return em.merge(entity);
    }

    @Override
    public void refresh(E entity) {
        em.refresh(entity);
    }

    @Override
    public E findById(K id) {
        return em.find(entityClass, id);
    }

    @Override
    public E flush(E entity) {
        em.flush();
        return entity;

    }

    @Override
    public List<E> findAll() {
        return em.createQuery("SELECT h FROM " + entityClass.getName() + " h").getResultList();
    }

    @Override
    public void removeById(final K id) {
        final Query q = em.createQuery("DELETE FROM " + entityClass.getName() + " h WHERE h.id = :id");
        q.setParameter("id", id);
        q.executeUpdate();
    }

    protected E obtainFirst(List<E> list) {
        if (null == list || list.size() == 0) {
            return null;
        } else {
            if (list.size() > 1) {
                log.warn("More than one element in obtainFirst(): " + list.size() + " [" + list + "]");
            }
            return list.get(0);
        }
    }

    /*package scope*/void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }

}
