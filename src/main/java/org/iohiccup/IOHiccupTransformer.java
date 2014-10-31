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
import javassist.CtField;
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
    
    static final String iohic_field_name = "iohic";
    
    private byte[] doClass(String name, Class clazz, byte[] b) {
        ClassPool pool = ClassPool.getDefault();

        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));

        //FIXME: DO NOT NEED actual to iterate over all methods, we can instrument only that we want
        CtClass cl = null;

        try {
            CtClass iohicClass = pool.get(accumulatorImplementationPackage + "IOHic");
            
            cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
            if (cl.isInterface() == false) {
                
                CtField field = new CtField(iohicClass, iohic_field_name, cl);
                cl.addField(field);

                CtBehavior[] methods = cl.getDeclaredBehaviors();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].isEmpty() == false) {

                        if (methods[i].getLongName().endsWith("read(byte[],int,int,int)")
                                || methods[i].getLongName().endsWith("write(byte[],int,int)")) {
                            doIOMethods(name, methods[i]);
                        }
                        
//                        if (false)
                        if (methods[i].getLongName().endsWith("SocketOutputStream(java.net.AbstractPlainSocketImpl)") ||
                                methods[i].getLongName().endsWith("SocketInputStream(java.net.AbstractPlainSocketImpl)")) {
                            doIOStreamsConstructor(name, methods[i]);
                        }
                    }
                }
                b = cl.toBytecode();
            }
        } catch (Exception e) {
            System.err.println("Could not instrument  " + name
                    + ",  exception : " + e + ":" + e.getMessage());
            System.err.flush();
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return b;
    }

    static final String checkString = " if (null != this."+ iohic_field_name + ") ";
    static final String accumulatorImplementationPackage = "org.iohiccup.";
    static final String accumulatorImplementationClass = accumulatorImplementationPackage + "IOHiccupAccumulator";

    private void doIOMethods(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
        if (method.getName().startsWith("read")) {
            if (configuration.i2oEnabled) {
                method.insertAfter(checkString + accumulatorImplementationClass + ".putTimestampReadAfter("+ iohic_field_name + ");");
            }
            if (configuration.o2iEnabled) {
                method.insertAfter(checkString + accumulatorImplementationClass + ".putTimestampReadBefore("+ iohic_field_name + ");");
            }
        }

        if (method.getName().startsWith("write")) {
            if (configuration.i2oEnabled) {
                method.insertBefore(checkString + accumulatorImplementationClass + ".putTimestampWriteBefore("+ iohic_field_name + ");");
            }
            if (configuration.o2iEnabled) {
                method.insertBefore(checkString + accumulatorImplementationClass + ".putTimestampWriteAfter("+ iohic_field_name + ");");
            }
        }
    }
    
    private void doIOStreamsConstructor(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
        if (method.getName().startsWith("SocketOutputStream") || method.getName().startsWith("SocketInputStream") ) {
            method.insertBefore(iohic_field_name + "= " + accumulatorImplementationClass + ".initializeIOHic(impl);");
        }
    }
    
}
