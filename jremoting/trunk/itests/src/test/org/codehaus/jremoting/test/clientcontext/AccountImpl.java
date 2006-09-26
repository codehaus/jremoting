/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.test.clientcontext;

import org.codehaus.jremoting.ClientContext;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class AccountImpl implements Account {

    private ServerSideClientContextFactory clientContextFactory;
    String symbolicKey;
    private int balance = 123;
    private AccountListener accountListener;

    public AccountImpl(ServerSideClientContextFactory clientContextFactory, String symbolicKey, AccountListener accountListener) {
        this.clientContextFactory = clientContextFactory;
        this.symbolicKey = symbolicKey;
        this.accountListener = accountListener;
    }

    public String getSymbolicKey() {
        return symbolicKey;
    }

    public int getBalance() {
        return balance;
    }

    public void debit(int amt) throws DebitBarfed {
        ClientContext cc = clientContextFactory.get();
        balance = balance - amt;
        accountListener.record(getSymbolicKey() + ":debit:" + amt, cc);
    }

    public void credit(int amt) throws CreditBarfed {
        ClientContext cc = clientContextFactory.get();
        balance = balance + amt;
        accountListener.record(getSymbolicKey() + ":credit:" + amt, cc);
    }
}
