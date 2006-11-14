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
package org.codehaus.jremoting.itests.clientcontext;

import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.server.ServerSideContextFactory;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class TalkativeAccountStartingWith123Dollars implements Account {

    private ServerSideContextFactory contextFactory;
    private String id;
    private int balance = 123;
    private AccountListener accountListener;

    public TalkativeAccountStartingWith123Dollars(ServerSideContextFactory contextFactory, String id, AccountListener accountListener) {
        this.contextFactory = contextFactory;
        this.id = id;
        this.accountListener = accountListener;
    }

    public String getID() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void debit(int amt) throws DebitBarfed {
        Context cc = contextFactory.get();
        balance = balance - amt;
        accountListener.record(getID() + ":debited:" + amt, cc);
    }

    public void credit(int amt) throws CreditBarfed {
        Context cc = contextFactory.get();
        balance = balance + amt;
        accountListener.record(getID() + ":credited:" + amt, cc);
    }
}
