package com.btcashier.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.btcashier.dao.MerchantDao;
import com.btcashier.domain.Merchant;
import com.btcashier.domain.Sale;

@Repository("merchantDao")
public class MerchantDaoImpl extends AbstractJpaDaoImpl<Integer, Merchant> implements MerchantDao {

    @Override
    public Merchant getBySale(Sale sale) {
        final Query q = em.createQuery("select s.merchant from Sale s where s = :sale", Merchant.class);
        q.setParameter("sale", sale);
        final List<Merchant> merchants = q.getResultList();
        return obtainFirst(merchants);
    }

}
