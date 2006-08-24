package org.codehaus.jremoting.commands;

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
public interface ResponseConstants {

        // 'good' responses after 100
    /**
     * A response of type class (generated proxy)
     */
    int CLASSRESPONSE = 1;
    /**
     * A response of type method
     */
    int METHODRESPONSE = 2;
    /**
     * A response of type exception
     */
    int EXCEPTIONRESPONSE = 3;
    /**
     * A response of type lookup
     */
    int LOOKUPRESPONSE = 4;
    /**
     * A response of facade as a result of a method request
     */
    int METHODFACADERESPONSE = 5;
    /**
     * An ack on open of connection
     */
    int OPENCONNECTIONRESPONSE = 6;
    /**
     * An ack on simple ping
     */
    int PINGRESPONSE = 7;
    /**
     * A list of published services
     */
    int LISTRESPONSE = 8;
    /**
     * A response of and array of facades
     */
    int METHODFACADEARRAYRESPONSE = 9;
    /**
     * A grabage collection response
     */
    int GCRESPONSE = 10;

    /**
     * An instruction try again in local modes.  Used instread of an OpenConnectionReply.
     */
    int SAMEVMRESPONSE = 11;

	/**
     * The list of remote methods within the published Object
     */
    int LISTMETHODSRESPONSE=12;

    // 'bad' replies after 100

    /**
     * A some type of problem occured.
     */
    int PROBLEMRESPONSE = 100;
    /**
     * The service requested was not published
     */
    int NOTPUBLISHEDRESPONSE = 102;
    /**
     * The request failed
     */
    int REQUESTFAILEDRESPONSE = 103;
    /**
     * The service is suspended
     */
    int SUSPENDEDRESPONSE = 104;
    /**
     * The connection has been ended
     */
    int ENDCONNECTIONRESPONSE = 105;
    /**
     * There is no such reference
     */
    int NOSUCHREFERENCERESPONSE = 106;
    /**
     * The proxy class could not be retrieved.
     */
    int CLASSRETRIEVALFAILEDRESPONSE = 107;

    int CLIENTABEND = 108;

    int INVOCATIONEXCEPTIONRESPONSE = 109;

    int NOSUCHSESSIONRESPONSE = 110;
}
