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

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.btcashier.domain.enums.Currency;
import com.btcashier.domain.enums.SaleStatus;
import com.btcashier.utils.json.DepositsJsonView;
import com.fasterxml.jackson.annotation.JsonView;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(name="unique_merchant_id_and_merch_sale_id", columnNames = { "MERCHANT_ID", "merchantSaleId" }))
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    
    @OneToOne(fetch=FetchType.LAZY, mappedBy="sale", cascade=CascadeType.ALL)
    private Address address;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="MERCHANT_ID")
    private Merchant merchant;
    
    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    private Currency currency;
    
    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    private SaleStatus status;
    
    @Column(precision  = 12, scale = 0)
    private BigInteger price;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    @Basic(optional = false)
    private String merchantSaleId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    private Date lastSentDate;

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

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    @JsonView(DepositsJsonView.class)
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @JsonView(DepositsJsonView.class)
    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    @JsonView(DepositsJsonView.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonView(DepositsJsonView.class)
    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }
    
    @JsonView(DepositsJsonView.class)
    /*package scope*/ String getBtcAddress() {
        return address.getBtcAddress();
    }

    @JsonView(DepositsJsonView.class)
    public String getMerchantSaleId() {
        return merchantSaleId;
    }
    
    public void setMerchantSaleId(String merchantSaleId) {
        this.merchantSaleId = merchantSaleId;
    }
    
    public Date getLastSentDate() {
        return lastSentDate;
    }

    public void setLastSentDate(Date lastSentDate) {
        this.lastSentDate = lastSentDate;
    }
    
}
