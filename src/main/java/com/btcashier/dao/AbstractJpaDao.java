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