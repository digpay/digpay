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
package com.btcashier.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_hash", columnNames = { "hash" }))
public class ProcessedBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "DATETIME NOT NULL ON UPDATE CURRENT_TIMESTAMP", nullable = true)
    private Date updated;

    @OneToMany(mappedBy = "block")
    private Set<Deposit> deposits;

    @Basic(optional = false)
    @Column(name = "hash", nullable = false)
    private String hash;
    
    @Basic(optional = false)
    @Column(name = "fork", nullable = false)
    private boolean fork;
    
    @Basic(optional = false)
    @Column(name = "height", nullable = false)
    private Integer height;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="previous_processedblock_id")
    private ProcessedBlock previous;
    
    @PrePersist
    protected void onCreate() {
        updated = created = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Set<Deposit> getDeposits() {
        return deposits;
    }

    public void setDeposits(Set<Deposit> deposits) {
        this.deposits = deposits;
    }
    
    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public ProcessedBlock getPrevious() {
        return previous;
    }
    
    public void setPrevious(ProcessedBlock previous) {
        this.previous = previous;
    }

    private void setCreated(Date created) {
        this.created = created;
    }

    private void setUpdated(Date updated) {
        this.updated = updated;
    }
    
    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }
    
}
