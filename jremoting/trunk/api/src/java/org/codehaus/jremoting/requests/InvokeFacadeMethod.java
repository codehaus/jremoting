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
package org.codehaus.jremoting.requests;

import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.requests.InvokeMethod;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Class InvokeFacadeMethod
 *
 * @author Paul Hammant
 *
 */
public final class InvokeFacadeMethod extends InvokeMethod {

    private String baseReturnClassNameEncoded;
    private static final long serialVersionUID = 6412918341011729041L;

    /**
     * Constructor InvokeFacadeMethod
     *
     * @param publishedServiceName       the published service name
     * @param objectName                 the object Name
     * @param methodSignature            the method signature
     * @param args                       an array of args for the method invocation
     * @param reference                the reference ID
     * @param baseReturnClassNameEncoded the encoded name of the base class
     * @param session                    the session ID
     */
    public InvokeFacadeMethod(String publishedServiceName, String objectName, String methodSignature, Object[] args, Long reference, String baseReturnClassNameEncoded, long session) {

        super(publishedServiceName, objectName, methodSignature, args, reference, session);

        this.baseReturnClassNameEncoded = baseReturnClassNameEncoded;
    }

    /**
     * Constructor InvokeFacadeMethod
     */
    public InvokeFacadeMethod() {
    }

    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.requests.RequestConstants
     */
    public int getRequestCode() {
        return RequestConstants.METHODFACADEREQUEST;
    }

    /**
     * Get return class name in encoded form.
     *
     * @return the encoded name of the base class
     */
    public String getBaseReturnClassNameEncoded() {
        return baseReturnClassNameEncoded;
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(baseReturnClassNameEncoded);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        super.readExternal(in);

        baseReturnClassNameEncoded = (String) in.readObject();
    }
}
