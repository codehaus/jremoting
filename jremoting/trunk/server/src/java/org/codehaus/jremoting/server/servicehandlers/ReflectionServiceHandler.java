package org.codehaus.jremoting.server.servicehandlers;

import org.codehaus.jremoting.server.Publisher;
import org.codehaus.jremoting.server.Publication;
import org.codehaus.jremoting.util.MethodNameHelper;
import org.codehaus.jremoting.requests.InvokeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;

public class ReflectionServiceHandler extends ServiceHandler {

    private Map<String, Method> methodMap = new HashMap<String, Method>();

    public ReflectionServiceHandler(Publisher publisher, String publishedThing, Publication publicationDescription, Class facadeClass) {
        super(publisher, publishedThing, publicationDescription, facadeClass);
        populateMethods();
    }

    private void populateMethods() {
        Method[] methods = null;
        try {
            Method ts = Object.class.getMethod("toString", new Class[0]);
            Method hc = Object.class.getMethod("hashCode", new Class[0]);
            Method eq = Object.class.getMethod("equals", new Class[]{Object.class});
            Method[] interfaceMethods = facadeClass.getMethods();
            methods = new Method[interfaceMethods.length + 3];
            System.arraycopy(interfaceMethods, 0, methods, 0, interfaceMethods.length);
            methods[interfaceMethods.length] = ts;
            methods[interfaceMethods.length + 1] = hc;
            methods[interfaceMethods.length + 2] = eq;
        } catch (NoSuchMethodException e) {
            // never!
        }
        for (Method method : methods) {
            String methodSignature = MethodNameHelper.getMethodSignature(method);
            if (!methodMap.containsKey(methodSignature)) {
                methodMap.put(methodSignature, method);
            }
        }
    }

    protected boolean isFacadeMethodSignature(String methodSignature) {
        return methodMap.containsKey(methodSignature);
    }

    protected Object invokeFacadeMethod(InvokeMethod request, String methodSignature, Object instance) 
            throws IllegalAccessException, InvocationTargetException {
        Method method = methodMap.get(methodSignature);
        Object[] args = request.getArgs();
        return invokeMethod(instance, method, args);
    }

    protected Object invokeMethod(Object instance, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(instance, args);
    }

    public String[] getListOfMethods() {
        String[] methodNames = (String[]) methodMap.keySet().toArray(new String[0]);
        return methodNames;
    }
}
