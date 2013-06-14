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
package com.btcashier.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.btcashier.dao.MerchantDao;
import com.btcashier.domain.AuthToken;
import com.btcashier.domain.Merchant;
import com.btcashier.service.AuthTokenService;
import com.btcashier.utils.ErrorCodes;

@Controller
@RequestMapping("/rest/auth")
public class AuthController extends AbstractRestController {

    @Autowired
    private MerchantDao merchantDao;

    @Autowired
    private AuthTokenService authTokenService;
    
    @RequestMapping(value = "/{mid}/{apiKey}", method = RequestMethod.GET)
    @ResponseBody
    public String auth(@PathVariable Integer mid, @PathVariable String apiKey) {

        Merchant merchant = merchantDao.findById(mid);
        if (null != merchant && merchant.getApiKey().equals(apiKey)) {
            final AuthToken token = authTokenService.createNewToken(merchant);
            return token.getToken();
        } else {
            throw ErrorCodes.create(ErrorCodes.AUTH_MID_OR_API_KEY_WRONG);
        }
    }

}
