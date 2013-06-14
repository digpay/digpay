package com.btcashier.domain;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
public class AuthToken {
    
    @Id
    private String token;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Basic(optional = false)
    private Date createdAt;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="MERCHANT_ID")
    private Merchant merchant;
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Merchant getMerchant() {
        return merchant;
    }
    
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
}
