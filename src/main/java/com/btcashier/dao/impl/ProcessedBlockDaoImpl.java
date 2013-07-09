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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.btcashier.dao.ProcessedBlockDao;
import com.btcashier.domain.ProcessedBlock;

@Repository("processedBlockDao")
@Transactional(readOnly = true)
public class ProcessedBlockDaoImpl extends AbstractJpaDaoImpl<Integer, ProcessedBlock> implements ProcessedBlockDao {

    private final static Logger log = Logger.getLogger(ProcessedBlockDaoImpl.class);

    @Override
    public ProcessedBlock getLastProcessedBlock() {
        final Query q = em.createQuery("select pb from ProcessedBlock pb where pb.fork = 0 order by pb.height desc");
        q.setMaxResults(1);
        final List<ProcessedBlock> pbs = q.getResultList();
        return obtainFirst(pbs);
    }

    @Override
    public ProcessedBlock getByHash(String hash) {
        final Query q = em.createQuery("select pb from ProcessedBlock pb where pb.hash = :hash and pb.fork = 0");
        q.setParameter("hash", hash);
        final List<ProcessedBlock> pbs = q.getResultList();
        return obtainFirst(pbs);
    }

    @Override
    public ProcessedBlock getNextProcessedBlock(ProcessedBlock previous) {
        final Query q = em.createQuery("select pb from ProcessedBlock pb left join fetch pb.deposits where pb.previous = :previous and pb.fork = 0");
        q.setParameter("previous", previous);
        final List<ProcessedBlock> pbs = q.getResultList();
        return obtainFirst(pbs);
    }

}
