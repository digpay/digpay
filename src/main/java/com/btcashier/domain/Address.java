package com.btcashier.domain;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(name="unique_btc_address", columnNames = { "btcAddress" }))
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    
    @OneToMany(mappedBy="address")
    private Set<Deposit> deposits;
    
    @Column(name = "btcAddress")
    private String btcAddress;
    
    @OneToOne
    @JoinColumn(name="sale_id")
    private Sale sale;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="MERCHANT_ID")
    private Merchant merchant;
    
    public Address(String btcAddress) {
        this.btcAddress = btcAddress;
    }
    
    public Address() {
        
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public String getBtcAddress() {
        return btcAddress;
    }
    
    public void setBtcAddress(String btcAddress) {
        this.btcAddress = btcAddress;
    }

    
    public Sale getSale() {
        return sale;
    }

    
    public void setSale(Sale sale) {
        this.sale = sale;
    }

    
    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Set<Deposit> getDeposits() {
        return deposits;
    }

    public void setDeposits(Set<Deposit> deposits) {
        this.deposits = deposits;
    }
    
}
