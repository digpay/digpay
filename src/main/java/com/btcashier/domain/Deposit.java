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
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

import com.btcashier.utils.json.DepositsJsonView;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_transactionIdAndN", columnNames = { "transactionIdAndN" }))
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
    @JoinColumn(name = "ADDRESS_ID")
    private Address address;

    @Column(precision = 12, scale = 0)
    private BigInteger amount;

    @Basic(optional = false)
    @Column(nullable = false)
    private Long time;

    /**
     * Will have this form: "transactionId:N", eg:
     * 0c58341ad9b9846771c0be43bea903911d7e7bd3c5dea38ca4e684ba172821ab:1
     * Where N is the index number of vout's.
     */
    @Column(name = "transactionIdAndN")
    @Index(name = "transactionIdAndNIndex")
    private String transactionIdAndN;

    @Column(nullable = false)
    @Basic(optional = false)
    private Long numberConfirmations;

    @Column(nullable = false)
    @Basic(optional = false)
    private Integer seenInBlock;
    
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

    public String getTransactionIdAndN() {
        return transactionIdAndN;
    }

    public void setTransactionIdAndN(String transactionIdAndN) {
        this.transactionIdAndN = transactionIdAndN;
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
        return transactionIdAndN.split(":")[0];
    }

    public Integer getSeenInBlock() {
        return seenInBlock;
    }

    public void setSeenInBlock(Integer seenInBlock) {
        this.seenInBlock = seenInBlock;
    }

    public Date getCreated() {
        return created;
    }
    
    public Date getUpdated() {
        return updated;
    }

}
