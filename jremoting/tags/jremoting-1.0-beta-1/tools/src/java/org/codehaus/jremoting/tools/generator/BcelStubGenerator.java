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
package org.codehaus.jremoting.tools.generator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.codehaus.jremoting.server.PublicationItem;
import org.codehaus.jremoting.util.MethodNameHelper;
import org.codehaus.jremoting.util.StaticStubHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * Class BCElProxyGeneratorImpl
 * This class generates JRemoting stubs using Jakarta BCEL library.
 * <p/>
 * HOWTO: Use 'javap' to read the bytecodes of the stubs generated
 * by using the original stub-generator(which generates pure java code).
 *
 * @author <a href="mailto:vinayc77@yahoo.com">Vinay Chandran</a>
 */
public class BcelStubGenerator extends AbstractStubGenerator {

    //bcel    
    private InstructionFactory factory;
    private ConstantPoolGen constantsPool;
    private ClassGen classGen;
    private ArrayList<String> internalFieldRepresentingClasses;

    /**
     * Generate the class.
     *
     * @param classLoader the classloader to use during generation.
     * @see org.codehaus.jremoting.server.StubGenerator#generateClass(ClassLoader)
     */
    public void generateClass(ClassLoader classLoader) {

        //create the Main Stubs:
        generateStubClass(StaticStubHelper.formatProxyClassName(getGenName()), new PublicationItem[] { getPrimaryFacade() });

        //Create the Additional Facades
        if (getAdditionalFacades() != null) {
            for (int i = 0; i < getAdditionalFacades().length; i++) {
                String encodedClassName = MethodNameHelper.encodeClassName(getAdditionalFacades()[i].getFacadeClass());
                generateStubClass(StaticStubHelper.formatProxyClassName(getGenName(), encodedClassName), new PublicationItem[]{getAdditionalFacades()[i]});

            }
        }

    }

    /**
     * Method generateStubClass.
     * Create Stub Implementation with all interface methods
     * Generating name of form: [STUB_PREFIX][genName]_[STUB_POSTFIX].class
     *
     * @param generatedClassName the name of the class to generate.
     * @param facadeToStubify
     */
    protected void generateStubClass(String generatedClassName, PublicationItem[] facadesToStubify) {
        //Start creating class
        createNewClassDeclaration(generatedClassName, facadesToStubify);
        //create constructor that takes StubHelper
        createConstructor(generatedClassName);
        //create fields
        createFields();
        //create fields
        createGetReferenceIDMethod(generatedClassName);
        createHelperMethodForDotClassCalls(generatedClassName);
        createInterfaceMethods(generatedClassName, facadesToStubify);

        FileOutputStream fos = null;
        try {
            String cd = new File(getClassGenDir()).getCanonicalPath();
            fos = new FileOutputStream(cd + File.separator + generatedClassName + ".class");
            classGen.getJavaClass().dump(fos);
            fos.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    //BCEL
    /**
     * Method createAndInitializeClass.
     * This method starts creating the class.
     *
     * @param generatedClassName the stub class name
     * @param facadeToStubify
     */
    protected void createNewClassDeclaration(String generatedClassName, PublicationItem[] facadesToStubify) {

        String[] facades = new String[facadesToStubify.length + 1];
        for (int i = 0; i < facadesToStubify.length; i++) {
            PublicationItem publicationDescriptionItem = facadesToStubify[i];
            facades[i] = publicationDescriptionItem.getFacadeClass().getName();
        }
        facades[facadesToStubify.length] = "org.codehaus.jremoting.client.Stub";


        classGen = new ClassGen(generatedClassName, "java.lang.Object", generatedClassName + ".java", Constants.ACC_PUBLIC | Constants.ACC_SUPER | Constants.ACC_FINAL, facades);
        constantsPool = classGen.getConstantPool();
        factory = new InstructionFactory(classGen, constantsPool);
        internalFieldRepresentingClasses = new ArrayList<String>();

    }

    /**
     * Method createConstructor.
     * This method adds a constructor that takes in a StubHelper Instance
     *
     * @param generatedClassName the stub class name
     */
    protected void createConstructor(String generatedClassName) {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, new Type[]{new ObjectType("org.codehaus.jremoting.client.StubHelper")}, new String[]{"arg0"}, "<init>", generatedClassName, il, constantsPool);
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createFieldAccess(generatedClassName, "stubHelper", new ObjectType("org.codehaus.jremoting.client.StubHelper"), Constants.PUTFIELD));
        il.append(InstructionFactory.createReturn(Type.VOID));
        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        il.dispose();
    }

    /**
     * Add method
     * <pre>
     * private transient org.codehaus.jremoting.client.StubHelper stubHelper;
     * </pre>
     */
    protected void createFields() {
        FieldGen field;
        field = new FieldGen(Constants.ACC_PRIVATE | Constants.ACC_TRANSIENT, new ObjectType("org.codehaus.jremoting.client.StubHelper"), "stubHelper", constantsPool);
        classGen.addField(field.getField());
    }

    /**
     * Add method
     * <pre>
     * public Long jRemotingGetReferenceID(Object factoryThatIsAsking) {
     * return stubHelper.getReference(factoryThatIsAsking);
     * }
     * </pre>
     *
     * @param generatedClassName the generated class name
     */
    protected void createGetReferenceIDMethod(String generatedClassName) {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, new ObjectType("java.lang.Long"), new Type[]{Type.OBJECT}, new String[]{"arg0"}, "jRemotingGetReferenceID", generatedClassName, il, constantsPool);
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(generatedClassName, "stubHelper", new ObjectType("org.codehaus.jremoting.client.StubHelper"), Constants.GETFIELD));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "getReference", new ObjectType("java.lang.Long"), new Type[]{Type.OBJECT}, Constants.INVOKEINTERFACE));
        il.append(InstructionFactory.createReturn(Type.OBJECT));
        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        il.dispose();
    }

    /**
     * Creates a method class$(String) which is used
     * during SomeClass.class instruction
     *
     * @param generatedClassName the instance class name
     */
    protected void createHelperMethodForDotClassCalls(String generatedClassName) {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_STATIC, new ObjectType("java.lang.Class"), new Type[]{Type.STRING}, new String[]{"arg0"}, "class$", generatedClassName, il, constantsPool);
        InstructionHandle ih0 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke("java.lang.Class", "forName", new ObjectType("java.lang.Class"), new Type[]{Type.STRING}, Constants.INVOKESTATIC));
        InstructionHandle ih4 = il.append(InstructionFactory.createReturn(Type.OBJECT));
        InstructionHandle ih5 = il.append(InstructionFactory.createStore(Type.OBJECT, 1));
        il.append(factory.createNew("java.lang.NoClassDefFoundError"));
        il.append(InstructionConstants.DUP);
        il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke("java.lang.Throwable", "getMessage", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        il.append(factory.createInvoke("java.lang.NoClassDefFoundError", "<init>", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKESPECIAL));
        il.append(InstructionConstants.ATHROW);
        method.addExceptionHandler(ih0, ih4, ih5, new ObjectType("java.lang.ClassNotFoundException"));
        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        il.dispose();
    }

    /**
     * This methods shall iterate through the set of methods
     * of the interface creating equivalent methods for the
     * stubs in the process.
     *
     * @param generatedClassName the generated class name
     * @param facadesToStubify   the facades to make stubs for.
     */
    protected void createInterfaceMethods(String generatedClassName, PublicationItem[] facadesToStubify) {
        for (int x = 0; x < facadesToStubify.length; x++) {
            Class clazz = facadesToStubify[x].getFacadeClass();

            Method[] methods = getGeneratableMethods(clazz);
            generateEqualsMethod(generatedClassName);
            for (int i = 0; i < methods.length; i++) {
                createInterfaceMethod(generatedClassName, methods[i], facadesToStubify[x]);
            }

        }

    }

    /**
     * Add the java.lang.reflect.Method wrapper into the stub
     *
     * @param generatedClassName the instance class name
     * @param mth                the method
     * @param facadesToStubify
     */
    protected void createInterfaceMethod(String generatedClassName, Method mth, PublicationItem facadesToStubify) {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, getReturnType(mth), getArguments(mth), getArgumentNames(mth), mth.getName(), generatedClassName, il, constantsPool);

        //debug(getArguments(m));

        // **** TO Insert TEST Bytecode Inside the stub ,uncomment the subsequent lines
        //if (verbose)
        //    createTestMethod(il, "calling " + mth.getName());

        /*
         *  Declaration of Arrays
         * =======================
         *  Object[] args = new Object[__number__of__arguments];
         *  Class[] argClasses = new Class[__number__of__arguments];
         */

        int variableIndex, numberOfArguments;
        Class[] paramTypes = mth.getParameterTypes();
        numberOfArguments = paramTypes.length;
        variableIndex = getFreeIndexToStart(paramTypes);
        il.append(new PUSH(constantsPool, numberOfArguments));
        il.append(factory.createNewArray(Type.OBJECT, (short) 1));
        il.append(InstructionFactory.createStore(Type.OBJECT, ++variableIndex));
        il.append(new PUSH(constantsPool, numberOfArguments));
        il.append(factory.createNewArray(new ObjectType("java.lang.Class"), (short) 1));
        il.append(InstructionFactory.createStore(Type.OBJECT, ++variableIndex));

        /*
         *  Assigning parameter into Object[] and Class[] Array
         * ====================================================
         *   args[0] = v0;
         *   argClasses[0]=v0Class.class
         */
        //Used for adjustment of double/long datatype:
        createInterfaceMethodArgs(numberOfArguments, il, variableIndex, paramTypes, generatedClassName);

        //check if its a rollback  method
        InstructionHandle ih_rollback = null;
        InstructionHandle catchHandler = null;
        BranchInstruction gotoCall = null;
        InstructionHandle ih_tryEnd = null;
        if (facadesToStubify.isRollback(mth)) {
            ih_rollback = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
            il.append(factory.createFieldAccess(generatedClassName, "stubHelper", new ObjectType("org.codehaus.jremoting.client.StubHelper"), Constants.GETFIELD));
            il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "rollbackAsyncRequests", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
            gotoCall = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
            ih_tryEnd = il.append(gotoCall);

            catchHandler = il.append(InstructionFactory.createStore(Type.OBJECT, ++variableIndex));
            il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
            injectCommonExceptionCatchBlock(il, method, variableIndex);
            --variableIndex;
            //createTestMethod(il,"after rollback");
        }

        /* Within the stub put the
        * Call processObjectRequest on the instance StubHelper held within the stub
        * Thus,
        * Injecting the following
        * ================================================
        * try
        * {
        *      Object retVal = stubHelper.processObjectRequest("foo1(int,
        *        float, java.lang.String, java.lang.Integer)",args,argClasses);
        *      return (java.lang.String) retVal;
        * }
        *  catch (Throwable t)
        *  {
        *         if (t instanceof RuntimeException)
        *         {
        *           throw (RuntimeException) t;
        *         }
        *         else if (t instanceof Error)
        *         {
        *                throw (Error) t;
        *         }
        *         else
        *         {
        *                t.printStackTrace();
        *                 throw new org.codehaus.jremoting.client.
        *                      InvocationException("Should never get here:" +t.getMessage());
        *         }
        *  }
        * ================================================
        */
        InstructionHandle ihe1 = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));

        if (facadesToStubify.isRollback(mth)) {
            gotoCall.setTarget(ihe1);
            method.addExceptionHandler(ih_rollback, ih_tryEnd, catchHandler, new ObjectType("java.lang.Throwable"));
        }

        il.append(factory.createFieldAccess(generatedClassName, "stubHelper", new ObjectType("org.codehaus.jremoting.client.StubHelper"), Constants.GETFIELD));
        // **** Check if the return type is facade ***
        Class returnClass = mth.getReturnType();
        if (returnClass.isArray()) {
            returnClass = returnClass.getComponentType();
        }

        if (isAdditionalFacade(mth.getReturnType())) {
            String encodedReturnClassName = "class$" + MethodNameHelper.encodeClassName(returnClass);
            addField(encodedReturnClassName);
            il.append(factory.createFieldAccess(generatedClassName, encodedReturnClassName, new ObjectType("java.lang.Class"), Constants.GETSTATIC));
            BranchInstruction ifnullReturnClass = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
            il.append(ifnullReturnClass);
            il.append(factory.createFieldAccess(generatedClassName, encodedReturnClassName, new ObjectType("java.lang.Class"), Constants.GETSTATIC));
            BranchInstruction gotoReturnClass = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
            il.append(gotoReturnClass);

            InstructionHandle ihPushMethodName = il.append(new PUSH(constantsPool, returnClass.getName()));
            ifnullReturnClass.setTarget(ihPushMethodName);
            il.append(factory.createInvoke(generatedClassName, "class$", new ObjectType("java.lang.Class"), new Type[]{Type.STRING}, Constants.INVOKESTATIC));
            il.append(InstructionConstants.DUP);
            il.append(factory.createFieldAccess(generatedClassName, encodedReturnClassName, new ObjectType("java.lang.Class"), Constants.PUTSTATIC));
            InstructionHandle ihPushSignature = il.append(new PUSH(constantsPool, MethodNameHelper.getMethodSignature(mth)));
            gotoReturnClass.setTarget(ihPushSignature);
            il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex - 1));
            il.append(new PUSH(constantsPool, MethodNameHelper.encodeClassName(getClassType(returnClass))));
            il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "processObjectRequestGettingFacade", Type.OBJECT, new Type[]{new ObjectType("java.lang.Class"), Type.STRING, new ArrayType(Type.OBJECT, 1), Type.STRING}, Constants.INVOKEINTERFACE));
        } else {
            //method signature = METHODNAME(arguments....)
            il.append(new PUSH(constantsPool, MethodNameHelper.getMethodSignature(mth)));
            variableIndex -= 2;
            il.append(InstructionFactory.createLoad(Type.OBJECT, ++variableIndex));
            il.append(InstructionFactory.createLoad(Type.OBJECT, ++variableIndex));
            //Check for async methods
            if (facadesToStubify.isAsync(mth)) {
                il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "queueAsyncRequest", Type.VOID, new Type[]{Type.STRING, new ArrayType(Type.OBJECT, 1), new ArrayType(new ObjectType("java.lang.Class"), 1)}, Constants.INVOKEINTERFACE));


            } else {
                if (getBCELPrimitiveType(mth.getReturnType().getName()) == Type.VOID) {
                    il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "processVoidRequest", Type.VOID, new Type[]{Type.STRING, new ArrayType(Type.OBJECT, 1), new ArrayType(new ObjectType("java.lang.Class"), 1)}, Constants.INVOKEINTERFACE));
                } else {
                    il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "processObjectRequest", Type.OBJECT, new Type[]{Type.STRING, new ArrayType(Type.OBJECT, 1), new ArrayType(new ObjectType("java.lang.Class"), 1)}, Constants.INVOKEINTERFACE));
                    il.append(InstructionFactory.createStore(Type.OBJECT, ++variableIndex));
                    il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));


                }
            }
        }

        //createTestMethod(il,"after remote call");

        InstructionHandle ihe2;
        if (facadesToStubify.isCommit(mth)) {

            gotoCall = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
            ihe2 = il.append(gotoCall);
            variableIndex++;

        } else {
            if (mth.getReturnType().isPrimitive()) {
                if (getBCELPrimitiveType(mth.getReturnType().getName()) == Type.VOID) {
                    ihe2 = il.append(InstructionFactory.createReturn(Type.VOID));
                } else {
                    il.append(factory.createCheckCast(new ObjectType(getJavaWrapperClass(mth.getReturnType().getName()))));
                    il.append(factory.createInvoke(getJavaWrapperClass(mth.getReturnType().getName()), mth.getReturnType().getName() + "Value", getBCELPrimitiveType(mth.getReturnType().getName()), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
                    ihe2 = il.append(InstructionFactory.createReturn(getBCELPrimitiveType(mth.getReturnType().getName())));
                }
            } else {
                il.append(factory.createCheckCast(new ObjectType(mth.getReturnType().getName())));
                ihe2 = il.append(InstructionFactory.createReturn(Type.OBJECT));
            }
        }

        InstructionHandle ihe3 = il.append(InstructionFactory.createStore(Type.OBJECT, variableIndex));

        //add custom exceptionHandling here
        Class[] exceptionClasses = mth.getExceptionTypes();
        InstructionHandle customHandler = null;
        BranchInstruction ifCustomExceptionBranch = null;
        for (int i = 0; i < exceptionClasses.length; i++) {

            customHandler = il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
            //create the series of custom exception handlers for the classes
            if (ifCustomExceptionBranch != null) {
                ifCustomExceptionBranch.setTarget(customHandler);
            }
            il.append(new INSTANCEOF(constantsPool.addClass(new ObjectType(exceptionClasses[i].getName()))));
            ifCustomExceptionBranch = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
            il.append(ifCustomExceptionBranch);
            il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
            il.append(factory.createCheckCast(new ObjectType(exceptionClasses[i].getName())));
            il.append(InstructionConstants.ATHROW);
        }

        InstructionHandle defaultExceptionHandler = il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
        if (customHandler != null) {
            ifCustomExceptionBranch.setTarget(defaultExceptionHandler);
        }

        //add standard exception handling routine which handles any
        //other exception generated during the remote call
        injectCommonExceptionCatchBlock(il, method, variableIndex);

        method.addExceptionHandler(ihe1, ihe2, ihe3, new ObjectType("java.lang.Throwable"));

        //check if its a commit method
        if (facadesToStubify.isCommit(mth)) {
            InstructionHandle ih_commit = il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
            gotoCall.setTarget(ih_commit);
            il.append(factory.createFieldAccess(generatedClassName, "stubHelper", new ObjectType("org.codehaus.jremoting.client.StubHelper"), Constants.GETFIELD));

            il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "commitAsyncRequests", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
            InstructionHandle ih_return = il.append(InstructionFactory.createReturn(Type.VOID));
            catchHandler = il.append(InstructionFactory.createStore(Type.OBJECT, variableIndex));
            il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
            injectCommonExceptionCatchBlock(il, method, variableIndex);
            method.addExceptionHandler(ih_commit, ih_return, catchHandler, new ObjectType("java.lang.Throwable"));
        }


        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        il.dispose();
    }

    private void generateEqualsMethod(String generatedClassName) {

        /* public boolean equals(Object o) {
         *   return stubHelper.isEquals(this,o);
         * }
         */

        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, Type.BOOLEAN, new Type[]{Type.OBJECT}, new String[]{"arg0"}, "equals", generatedClassName, il, constantsPool);

        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));

        il.append(factory.createFieldAccess(generatedClassName, "stubHelper", new ObjectType("org.codehaus.jremoting.client.StubHelper"), Constants.GETFIELD));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        il.append(InstructionFactory.createLoad(Type.OBJECT, 1));

        il.append(factory.createInvoke("org.codehaus.jremoting.client.StubHelper", "isEquals", Type.BOOLEAN, new Type[]{Type.OBJECT, Type.OBJECT}, Constants.INVOKEINTERFACE));
        il.append(InstructionFactory.createReturn(Type.INT));
        method.setMaxStack();
        method.setMaxLocals();
        classGen.addMethod(method.getMethod());
        il.dispose();
    }

    /**
     * Create interface method's args.
     *
     * @param numberOfArguments  the number of arguments.
     * @param il                 an instruction list
     * @param variableIndex      a varible index.
     * @param paramTypes         parameter types
     * @param generatedClassName the generated class name.
     */
    private void createInterfaceMethodArgs(int numberOfArguments, InstructionList il, int variableIndex, Class[] paramTypes, String generatedClassName) {
        Type previousType = null;
        int loadIndex = 0;
        for (int i = 0; i < numberOfArguments; i++) {
            // assigning the obj ref's
            il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex - 1));
            il.append(new PUSH(constantsPool, i));
            String className = paramTypes[i].getName();
            //adjust for any previous wider datatype (double/long)
            if (previousType != null && (previousType == Type.DOUBLE || previousType == Type.LONG)) {
                ++loadIndex;
            }
            if (paramTypes[i].isPrimitive()) {
                il.append(factory.createNew(getJavaWrapperClass(className)));
                il.append(InstructionConstants.DUP);
                il.append(InstructionFactory.createLoad(getBCELPrimitiveType(className), ++loadIndex));
                il.append(factory.createInvoke(getJavaWrapperClass(className), "<init>", Type.VOID, new Type[]{getBCELPrimitiveType(className)}, Constants.INVOKESPECIAL));
                il.append(InstructionConstants.AASTORE);
            } else {

                //create the static fields for enabling .class calls
                String encodedFieldName;
                if (paramTypes[i].isArray()) {
                    int index = className.lastIndexOf('[');
                    if (className.charAt(index + 1) == 'L') {
                        encodedFieldName = "array$" + className.substring(1 + index, className.length() - 1).replace('.', '$');
                    } else {
                        encodedFieldName = "array$" + className.substring(1 + index, className.length());
                    }
                } else {
                    encodedFieldName = "class$" + className.replace('.', '$');
                }

                addField(encodedFieldName);
                // ******** TODO assign the obj reference
                il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex - 1));
                il.append(new PUSH(constantsPool, i));
                il.append(InstructionFactory.createLoad(Type.OBJECT, ++loadIndex));
                il.append(InstructionConstants.AASTORE);

                // *********TODO assign the class ref's
                il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
                il.append(new PUSH(constantsPool, i));
                il.append(factory.createFieldAccess(generatedClassName, encodedFieldName, new ObjectType("java.lang.Class"), Constants.GETSTATIC));
                BranchInstruction ifnull = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
                il.append(ifnull);
                il.append(factory.createFieldAccess(generatedClassName, encodedFieldName, new ObjectType("java.lang.Class"), Constants.GETSTATIC));
                BranchInstruction goHeadToStoreRef = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
                il.append(goHeadToStoreRef);
                InstructionHandle ifnullStartHere = il.append(new PUSH(constantsPool, className));

                ifnull.setTarget(ifnullStartHere);

                il.append(factory.createInvoke(generatedClassName, "class$", new ObjectType("java.lang.Class"), new Type[]{Type.STRING}, Constants.INVOKESTATIC));
                il.append(InstructionConstants.DUP);
                il.append(factory.createFieldAccess(generatedClassName, encodedFieldName, new ObjectType("java.lang.Class"), Constants.PUTSTATIC));
                InstructionHandle storeClassRef = il.append(InstructionConstants.AASTORE);
                goHeadToStoreRef.setTarget(storeClassRef);

            }
            previousType = getBCELPrimitiveType(className);
        }
    }

    /**
     * Inject common exception catch blocks
     */
    public void injectCommonExceptionCatchBlock(InstructionList il, MethodGen method, int variableIndex) {
        il.append(new INSTANCEOF(constantsPool.addClass(new ObjectType("java.lang.RuntimeException"))));
        BranchInstruction b1 = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(b1);
        il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
        il.append(factory.createCheckCast(new ObjectType("java.lang.RuntimeException")));
        il.append(InstructionConstants.ATHROW);
        InstructionHandle ih1 = il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
        il.append(new INSTANCEOF(constantsPool.addClass(new ObjectType("java.lang.Error"))));
        BranchInstruction b2 = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
        il.append(b2);
        il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
        il.append(factory.createCheckCast(new ObjectType("java.lang.Error")));
        il.append(InstructionConstants.ATHROW);
        InstructionHandle ih2 = il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
        il.append(factory.createInvoke("java.lang.Throwable", "printStackTrace", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        il.append(factory.createNew("org.codehaus.jremoting.client.InvocationException"));
        il.append(InstructionConstants.DUP);
        il.append(factory.createNew("java.lang.StringBuffer"));
        il.append(InstructionConstants.DUP);
        il.append(new PUSH(constantsPool, "Should never get here: "));
        il.append(factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKESPECIAL));
        il.append(InstructionFactory.createLoad(Type.OBJECT, variableIndex));
        il.append(factory.createInvoke("java.lang.Throwable", "getMessage", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        il.append(factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
        il.append(factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        il.append(factory.createInvoke("org.codehaus.jremoting.client.InvocationException", "<init>", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKESPECIAL));
        il.append(InstructionConstants.ATHROW);

        b1.setTarget(ih1);
        b2.setTarget(ih2);

    }

    /**
     * Method getFreeIndexToStart.
     * Returns the index to openConnection allocating the subsequent stack variables
     *
     * @param classes the classes
     * @return int the index
     */
    protected int getFreeIndexToStart(Class[] classes) {
        int index = 0;
        for (int i = 0; i < classes.length; i++) {
            if (getBCELType(classes[i]) == Type.DOUBLE || getBCELType(classes[i]) == Type.LONG) {
                index += 2;
            }
            index += 1;
        }

        return index;
    }

    /**
     * Method getArguments.
     * Convert the arguments of the method
     * into equivalent BCEL datatypes
     *
     * @param method the method for which arguments are needed.
     * @return Type[] an array of types
     */
    protected Type[] getArguments(Method method) {
        Class[] classes = method.getParameterTypes();
        if (classes.length == 0) {
            return Type.NO_ARGS;
        }

        Type[] types = new Type[classes.length];
        for (int i = 0; i < classes.length; i++) {
            types[i] = getBCELType(classes[i]);
        }
        return types;
    }

    /**
     * Method getReturnType.
     * Convert the returnType of the method into BCEL datatype
     *
     * @param method the method
     * @return Type the type
     */
    protected Type getReturnType(Method method) {
        return getBCELType(method.getReturnType());
    }

    /**
     * Method getArgumentNames.
     * The arguments are arg0,arg1,.....
     *
     * @param method the method
     * @return String[]
     */
    protected String[] getArgumentNames(Method method) {
        Class[] classes = method.getParameterTypes();
        String[] args = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            args[i] = "arg" + i;
        }
        return args;
    }

    /**
     * Method getBCELType.
     * Maps the java datatype and the BCEL datatype
     *
     * @param clazz the class
     * @return Type the type
     */
    protected Type getBCELType(Class clazz) {

        if (clazz.isPrimitive()) {
            return getBCELPrimitiveType(clazz.getName());
        } else if (!clazz.isArray()) {
            return new ObjectType(clazz.getName());
        } else {
            String className = clazz.getName();
            int index = className.lastIndexOf('[');
            int arrayDepth = className.indexOf('[') - className.lastIndexOf('[') + 1;
            if (className.charAt(index + 1) == 'L') {
                return new ArrayType(new ObjectType(clazz.getComponentType().getName()), arrayDepth);
            }

            return new ArrayType(getBCELPrimitiveType(className.substring(arrayDepth)), arrayDepth);
        }

    }

    /**
     * Method getBCELPrimitiveType.
     * Returns the BCEL Type given the Class Name
     *
     * @param javaDataType the java data type
     * @return Type the BCEL type
     */
    protected Type getBCELPrimitiveType(String javaDataType) {
        switch (javaDataType.charAt(0)) {

            case 'b':
                if (javaDataType.charAt(1) == 'o') {
                    return Type.BOOLEAN;
                } else {
                    return Type.BYTE;
                }
            case 'c':
            case 'C':
                return Type.CHAR;
            case 's':
            case 'S':
                return Type.SHORT;
            case 'i':
            case 'I':
                return Type.INT;
            case 'l':
            case 'L':
                return Type.LONG;
            case 'f':
            case 'F':
                return Type.FLOAT;
            case 'd':
            case 'D':
                return Type.DOUBLE;
                //boolean array appears in this format
            case 'Z':
                return Type.BOOLEAN;
            case 'B':
                return Type.BYTE;
            case 'v':
            case 'V':
                return Type.VOID;
        }
        return null;
    }

    /**
     * Method getJavaWrapperClass.
     * Returns the String representing the Wrapper class given the
     * primitive datatype
     *
     * @param javaDataType the java data type
     * @return String the JavaSource type.
     */
    protected String getJavaWrapperClass(String javaDataType) {
        switch (javaDataType.charAt(0)) {

            case 'b':
                if (javaDataType.charAt(1) == 'o') {
                    return "java.lang.Boolean";
                } else {
                    return "java.lang.Byte";
                }
            case 'c':
            case 'C':
                return "java.lang.Character";
            case 's':
            case 'S':
                return "java.lang.Short";
            case 'i':
            case 'I':
                return "java.lang.Integer";
            case 'l':
            case 'L':
                return "java.lang.Long";
            case 'f':
            case 'F':
                return "java.lang.Float";
            case 'd':
            case 'D':
                return "java.lang.Double";
            case 'B':
                return "java.lang.Byte";
            case 'Z':
                return "java.lang.Boolean";
            case 'v':
            case 'V':
                return "java.lang.Void";
            case '[':
                return getJavaWrapperClass(javaDataType.substring(1));

        }
        return null; //never occurs;
    }

    /**
     * @param encodedFieldName the encoded field name
     */
    protected void addField(String encodedFieldName) {
        if (!internalFieldRepresentingClasses.contains(encodedFieldName)) {
            //System.out.println("method."+method.getName()+".addingfield["
            //  + _encodedFieldName + "]");
            FieldGen field = new FieldGen(Constants.ACC_STATIC, new ObjectType("java.lang.Class"), encodedFieldName, constantsPool);
            classGen.addField(field.getField());
            internalFieldRepresentingClasses.add(encodedFieldName);
        }

    }

    /**
     * @param il  the instruction list
     * @param msg the message
     */
    protected void createTestMethod(InstructionList il, String msg) {
        il.append(factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
        il.append(new PUSH(constantsPool, msg));
        il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
    }

    /**
     * A debugging method
     *
     * @param prefix   the prefix to print
     * @param objArray the object array to print.
     */
    protected void debug(String prefix, Object[] objArray) {
        System.out.print(prefix);
        for (int i = 0; i < objArray.length; i++) {
            System.out.print(objArray[i] + ":");
        }
        System.out.println();
    }

    /**
     * Get a class for a generated class name.
     *
     * @param generatedClassName the generated class name
     * @return the class
     */
    public Class getGeneratedClass(String generatedClassName) {
        return getStubClass(generatedClassName);
    }

    /**
     * Method getStubClass.
     * This get the Class definition from the bytes
     *
     * @param className the clas name
     * @return Class the class
     */
    protected Class getStubClass(String className) {

        /*
        FromJavaClassClassLoader fromJavaClassClassLoader =
            new FromJavaClassClassLoader();
        Class clazz =
            fromJavaClassClassLoader.getClassFromJavaClass(classGen.getJavaClass());
        */
        Class clazz = null;

        try {
            URLClassLoader urlCL = new URLClassLoader(new URL[]{new File(getClassGenDir()).toURL()},
                    this.getClass().getClassLoader());
            clazz = urlCL.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // this is OK, as null will be passed back.
        }

        return clazz;
    }

    //++++++++++++++++++testing
    //</BCEL>

}