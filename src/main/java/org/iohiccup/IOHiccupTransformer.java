/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.lang.instrument.ClassFileTransformer;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 *
 * @author fijiol
 */
public class IOHiccupTransformer implements ClassFileTransformer {
    private final IOHiccupConfiguration configuration;

    IOHiccupTransformer(IOHiccupConfiguration configuration) {
        this.configuration = configuration;
    }
    
    
    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class clazz, java.security.ProtectionDomain domain,
            byte[] bytes) {

        if (!className.startsWith("java/net/SocketInputStream")
                && !className.startsWith("java/net/SocketOutputStream")) {
            return bytes;
        }
        return doClass(className, clazz, bytes);
    }  
    
    private byte[] doClass(String name, Class clazz, byte[] b) {
        ClassPool pool = ClassPool.getDefault();

        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));

        //FIXME: DO NOT NEED actual to iterate over all methods, we can instrument only that we want
        CtClass cl = null;

        try {
            cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
            if (cl.isInterface() == false) {

                CtBehavior[] methods = cl.getDeclaredBehaviors();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].isEmpty() == false) {

                        if (methods[i].getLongName().endsWith("read(byte[],int,int,int)")
                                || methods[i].getLongName().endsWith("write(byte[],int,int)")) {
                            doMethod(name, methods[i]);
                        }
                    }
                }
                b = cl.toBytecode();
            }
        } catch (Exception e) {
            System.err.println("Could not instrument  " + name
                    + ",  exception : " + e.getMessage());
            System.err.flush();
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return b;
    }

    private void doMethod(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
        if (method.getName().startsWith("read")) {
            if (configuration.i2oEnabled) {
                method.insertAfter("org.iohiccup.IOHiccupAccumulator.putTimestampReadAfter(impl);");
            }
            if (configuration.o2iEnabled) {
                method.insertAfter("org.iohiccup.IOHiccupAccumulator.putTimestampReadBefore(impl);");
            }
        }
        if (method.getName().startsWith("write")) {
            if (configuration.i2oEnabled) {
                method.insertBefore("org.iohiccup.IOHiccupAccumulator.putTimestampWriteBefore(impl);");
            }
            if (configuration.o2iEnabled) {
                method.insertBefore("org.iohiccup.IOHiccupAccumulator.putTimestampWriteAfter(impl);");
            }
        }
    }
    
}
