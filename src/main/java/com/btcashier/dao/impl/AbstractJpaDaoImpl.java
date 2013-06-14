package com.btcashier.dao.impl;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import com.btcashier.dao.AbstractJpaDao;

public abstract class AbstractJpaDaoImpl<K, E> implements AbstractJpaDao<K, E> {

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
            return list.get(0);
        }
    }

    /*package scope*/void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }

}
