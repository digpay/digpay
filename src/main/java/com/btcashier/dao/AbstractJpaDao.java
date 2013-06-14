package com.btcashier.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AbstractJpaDao<K, E> {

	@Transactional(readOnly = false)
	public void create(E entity);

	@Transactional(readOnly = false)
	public void remove(E entity);
	
	@Transactional(readOnly = false)
	public void removeById(K id);

	@Transactional(readOnly = false)
	public E saveOrUpdate(E entity);

	public void refresh(E entity);

	public E findById(K id);

	public E flush(E entity);

	public List<E> findAll();

}