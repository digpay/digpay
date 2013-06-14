package com.btcashier.domain;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;


@Entity
public class Merchant {
    
    @Id
    private Integer mid;
    
    @Column
    private String apiKey;
    
    @OneToMany(mappedBy="merchant")
    private Set<AuthToken> tokens;
    
    @OneToMany(mappedBy="merchant")
    private Set<Address> addresses;
    
    @OneToMany(mappedBy="merchant")
    private Set<Sale> sales;
    
    @Column(nullable = false)
    @Basic(optional = false)
    private Integer requiredConfirmations;
    
    @Column(nullable = false)
    @Basic(optional = false)
    private String url;
    
    public Integer getMid() {
        return mid;
    }
    
    public void setMid(Integer mid) {
        this.mid = mid;
    }

    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Set<AuthToken> getTokens() {
        return tokens;
    }

    public void setTokens(Set<AuthToken> tokens) {
        this.tokens = tokens;
    }
    
    public Set<Address> getAddresses() {
        return addresses;
    }
    
    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    public Set<Sale> getSales() {
        return sales;
    }
    
    public void setSales(Set<Sale> sales) {
        this.sales = sales;
    }
    
    public Integer getRequiredConfirmations() {
        return requiredConfirmations;
    }

    public void setRequiredConfirmations(Integer requiredConfirmations) {
        this.requiredConfirmations = requiredConfirmations;
    }

    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
}
