/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup.socket.regular;

import org.iohiccup.impl.Configuration;
import org.iohiccup.impl.IOHiccup;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class Transformer implements ClassFileTransformer {
    public Configuration configuration;
//    private final IOHiccup ioHiccup;
    public String iohiccup_field_name;
    public String checkString;
    public String iohic_field_name;
    public String debugPre;
    public String debugPost;
    public String accumulatorImplementationPackage = "org.iohiccup.";
    public String accumulatorImplementationClass = accumulatorImplementationPackage + "IOHiccupAccumulator";

    public Transformer(IOHiccup ioHiccup) {
//        this.ioHiccup = ioHiccup;
        this.accumulatorImplementationPackage = "org.iohiccup.";
        this.accumulatorImplementationClass = accumulatorImplementationPackage + "IOHiccupAccumulator";
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
    
    public void attachTo(Instrumentation instrumentation) {
        instrumentation.addTransformer(this);
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
    
    public byte[] doClass(String name, Class clazz, byte[] b) {
        ClassPool pool = ClassPool.getDefault();

        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));

        //FIXME: DO NOT NEED actual to iterate over all methods, we can instrument only that we want
        CtClass cl = null;

        try {
            CtClass iohicClass = pool.get(accumulatorImplementationPackage + "IOHic");
            CtClass iohiccupClass = pool.get(accumulatorImplementationPackage + "IOHiccup");
            
            cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
            if (cl.isInterface() == false) {
                
                if (this.getClass().equals(Transformer.class)) {
                    CtField field = new CtField(iohicClass, iohic_field_name, cl);
                    cl.addField(field);

                    CtField hiccup_field = new CtField(iohiccupClass, iohiccup_field_name, cl);
                    cl.addField(hiccup_field);
                }

                CtBehavior[] methods = cl.getDeclaredBehaviors();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].isEmpty() == false) {

                        if (methods[i].getLongName().endsWith("read(byte[],int,int,int)")
                                || methods[i].getLongName().endsWith("write(byte[],int,int)")) {
                            doIOMethods(name, methods[i]);
                        }
                        
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

    public void doIOMethods(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
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
    
    public void doIOStreamsConstructor(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
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
