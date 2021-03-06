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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

import com.btcashier.utils.json.DepositsJsonView;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_transactionIdAndNAndBlockTime", columnNames = { "transactionIdAndNAndBlockTime" }))
public class Deposit {

    public static final int BTC_FRACTION_LENGTH = 8;
    public static final long BTC_MULTIPLIER = new BigInteger("10").pow(BTC_FRACTION_LENGTH).longValue();
    public static final BigInteger BTC_MULTIPLIER_BI = BigInteger.valueOf(BTC_MULTIPLIER);//new BigDecimal(BTC_MULTIPLIER);
    public static final BigDecimal BTC_MULTIPLIER_BD = BigDecimal.valueOf(BTC_MULTIPLIER);//new BigDecimal(BTC_MULTIPLIER);

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private ProcessedBlock block;

    @Column(precision = 12, scale = 0)
    private BigInteger amount;

    @Basic(optional = false)
    @Column(nullable = false)
    private Long time;

    /**
     * Will have this form: "transactionId:N:BlockTime", eg:
     * 0c58341ad9b9846771c0be43bea903911d7e7bd3c5dea38ca4e684ba172821ab:1:1373019090
     * Where N is the index number of vout's,
     * BlockTime is the unix timestamp of the corresponding block creation.
     */
    @Column(name = "transactionIdAndNAndBlockTime")
    @Index(name = "idx_transactionIdAndNAndBlockTime")
    private String transactionIdAndNAndBlockTime;

    @Column(nullable = false)
    @Basic(optional = false)
    private Long numberConfirmations;

    @PrePersist
    protected void onCreate() {
        updated = created = new Date();
    }

    @JsonView(DepositsJsonView.class)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonView(DepositsJsonView.class)
    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getTransactionIdAndNAndBlockTime() {
        return transactionIdAndNAndBlockTime;
    }

    public void setTransactionIdAndNAndBlockTime(String transactionIdAndNAndBlockTime) {
        this.transactionIdAndNAndBlockTime = transactionIdAndNAndBlockTime;
    }

    @JsonView(DepositsJsonView.class)
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @JsonView(DepositsJsonView.class)
    public Long getNumberConfirmations() {
        return numberConfirmations;
    }

    public void setNumberConfirmations(Long numberConfirmations) {
        this.numberConfirmations = numberConfirmations;
    }

    public String getPrettyPrintAmount() {
        final BigInteger btcAmount = amount./*setScale(BTC_FRACTION_LENGTH).*/divide(BTC_MULTIPLIER_BI);
        return btcAmount + " BTC";
    }

    public String getPrettyPrintTime() {
        return new Date(1000L * time).toString();
    }

    @JsonView(DepositsJsonView.class)
    /*package scope*/String getTx() {
        return transactionIdAndNAndBlockTime.split(":")[0];
    }
    
    @JsonView(DepositsJsonView.class)
    public boolean isCanceled() {
        return block.isFork();
    }

    public Date getCreated() {
        return created;
    }
    
    public Date getUpdated() {
        return updated;
    }

    
    public ProcessedBlock getBlock() {
        return block;
    }

    
    public void setBlock(ProcessedBlock block) {
        this.block = block;
    }

    
    private void setCreated(Date created) {
        this.created = created;
    }

    
    private void setUpdated(Date updated) {
        this.updated = updated;
    }
    
}
