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
package org.codehaus.jremoting.test;

import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;

/**
 * Class MemoryLeakClientTst
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 * @version $Revision: 1.2 $
 * @todo - turn into a TestCase
 */
public class MemoryLeakClientTst {

    /**
     * Constructor MemoryLeakClientTst
     *
     * @param ml
     */
    public MemoryLeakClientTst(MemoryLeak ml) {

        // if you enable the vector, then serverside
        // memory usage ramps as MemoryLeak instances
        // are not garbage collected.
        //Vector v = new Vector();
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100000000; i++) {

            //v.add(ml);
            ml = ml.getNewMemoryLeak();

            // this seems to halve the speed.
            // but also is proven to be GC'd well for
            // custom stream.  For ObjectStream it is
            // a different measure.
            //ml.getHugeString();
            System.gc();    // pointless ?

            if (i % 100 == 0) {
                System.out.println("Iter " + i + ", " + (System.currentTimeMillis() - start) / 1000 + " seconds, Free Mem :" + Runtime.getRuntime().freeMemory());

                start = System.currentTimeMillis();
            }
        }
    }

    /**
     * Method main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("Memory Leak Client");

        Factory af = new ClientSideStubFactory(new SocketCustomStreamHostContext("127.0.0.1", 1277), false);

        MemoryLeak ml = (MemoryLeak) af.lookup("MemLeak");

        new MemoryLeakClientTst(ml);
        af.close();
    }
}
