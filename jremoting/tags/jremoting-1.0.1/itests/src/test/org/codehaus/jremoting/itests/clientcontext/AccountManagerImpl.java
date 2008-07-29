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

import org.codehaus.jremoting.server.ServerContextFactory;

import java.util.HashMap;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class AccountManagerImpl implements AccountManager {

    private HashMap<String, Account> accounts = new HashMap<String, Account>();

    public AccountManagerImpl(ServerContextFactory contextFactory, Account one, Account two) {
        accounts.put(one.getID(), one);
        accounts.put(two.getID(), two);
    }


    public void transferAmount(String from, String to, int amt) throws TransferBarfed {

        Account fromAccount = accounts.get(from);
        Account toAccount = accounts.get(to);

        try {
            fromAccount.debit(amt);
            toAccount.credit(amt);
        } catch (DebitBarfed debitBarfed) {
            throw new TransferBarfed();
        } catch (CreditBarfed creditBarfed) {
            throw new TransferBarfed();
        } finally {
            // ?
        }

    }
}
