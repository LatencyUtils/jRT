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
//    private final IOHiccup ioHiccup;
    private final String iohiccup_field_name;
    private final String checkString;
    private final String iohic_field_name;
    private final String debugPre;
    private final String debugPost;

    IOHiccupTransformer(IOHiccup ioHiccup) {
//        this.ioHiccup = ioHiccup;
        this.configuration = ioHiccup.configuration;
        this.iohiccup_field_name = "ioHiccup_" + configuration.uuid.replaceAll("\\W", "_");
        this.iohic_field_name = "ioHic_" + configuration.uuid.replaceAll("\\W", "_");
        this.checkString = " if (null != this."+ iohic_field_name + " && null != " + iohiccup_field_name + ") ";
        if (configuration.printExceptions) {
            debugPre = " try { ";
            debugPost = " } catch (Exception e) { e.printStackTrace(); System.exit(1); } ";
        } else {
            debugPre = "";
            debugPost = "";
        }
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
            CtClass iohicClass = pool.get(accumulatorImplementationPackage + "IOHic");
            CtClass iohiccupClass = pool.get(accumulatorImplementationPackage + "IOHiccup");
            
            cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
            if (cl.isInterface() == false) {
                
                CtField field = new CtField(iohicClass, iohic_field_name, cl);
                cl.addField(field);

                CtField hiccup_field = new CtField(iohiccupClass, iohiccup_field_name, cl);
                cl.addField(hiccup_field);

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

    static final String accumulatorImplementationPackage = "org.iohiccup.";
    static final String accumulatorImplementationClass = accumulatorImplementationPackage + "IOHiccupAccumulator";

    private void doIOMethods(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
        if (method.getName().startsWith("read")) {
            if (configuration.i2oEnabled) {
                method.insertAfter(debugPre + checkString + accumulatorImplementationClass + ".putTimestampReadAfter("+ iohiccup_field_name +", "+ iohic_field_name + ");" + debugPost);
            }
            if (configuration.o2iEnabled) {
                method.insertAfter(debugPre + checkString + accumulatorImplementationClass + ".putTimestampReadBefore("+ iohiccup_field_name +", "+ iohic_field_name + ");" + debugPost);
            }
        }

        if (method.getName().startsWith("write")) {
            if (configuration.i2oEnabled) {
                method.insertBefore(debugPre + checkString + accumulatorImplementationClass + ".putTimestampWriteBefore("+ iohiccup_field_name +", "+ iohic_field_name + ");" + debugPost);
            }
            if (configuration.o2iEnabled) {
                method.insertBefore(debugPre + checkString + accumulatorImplementationClass + ".putTimestampWriteAfter("+ iohiccup_field_name +", "+ iohic_field_name + ");" + debugPost);
            }
        }
    }
    
    private void doIOStreamsConstructor(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
        if (method.getName().startsWith("SocketOutputStream") || method.getName().startsWith("SocketInputStream") ) {
            method.insertAfter(
                    debugPre +
                    iohiccup_field_name + " = " + accumulatorImplementationClass + ".getIOHiccup(\"" + configuration.uuid + "\");" +
                    "\n" +
                    iohic_field_name + "= " + accumulatorImplementationClass + 
                    ".initializeIOHic("+ iohiccup_field_name +", impl, impl.getInetAddress(), impl.getPort(), impl.getLocalPort());" +
                    debugPost);
        }
    }
    
}
