/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup.socket.api;

import org.iohiccup.impl.IOHiccup;
import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.Modifier;

/*
TODO: split to CodeWriter and InstrumentationWalker (Transformer itself)

list of transformer actions/properties:
    - accumulator package/class parametrization
    - parametrize caching name/mechanizm 
        (by concurrent map, by adding new fields to java socket presentation)
        NIO?
    - socket filtering
    - conditions (f.e. initialized hiccup metainformation for proper socket?)
    - debugging helpers (asserts, and catching silent exceptions)

    Transformer should be parametrized with CodeWriter
*/
public class Transformer implements ClassFileTransformer {
    private final IOHiccup iOHiccup;
    
    private final CodeWriter codeWriter;


    public Transformer(IOHiccup iOHiccup, CodeWriter codeWriter) {
        this.iOHiccup = iOHiccup;
        this.codeWriter = codeWriter;
        
        codeWriter.init(iOHiccup);
    }
    
    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class clazz, java.security.ProtectionDomain domain,
            byte[] bytes) {

        
        if (!codeWriter.needInstrument(className)) {
            return bytes;
        }
        return doClass(className, clazz, bytes);
    }  
    
    public byte[] doClass(String className, Class clazz, byte[] b) {
        try {
            ClassPool pool = ClassPool.getDefault();

            CtClass cl = null;

            String code = null;
            String methodDescription = null;

            try {

                cl = pool.makeClass(new java.io.ByteArrayInputStream(b));

                if (cl.isInterface() == false) {

                    for (String varDeclaration : codeWriter.classNewFields(className)) { 
                        String[] var = varDeclaration.split(" ");
                        if (var.length != 2) {
                            code = null;
                            throw new Exception("Check your codeWriter implementation: var declaration array size != 2, ==" + var.length);
                        }
                        CtClass typeClass = pool.get(var[0]);

                        code = "adding field " + Arrays.deepToString(var);

                        CtField field = new CtField(typeClass, var[1], cl);
                        cl.addField(field);
                    }

                    CtBehavior[] methods = cl.getDeclaredBehaviors();

                    for (CtBehavior method : methods) {
                        System.err.println("there3: " + codeWriter.getClass().getName());

                        if ( method.isEmpty() == false && !Modifier.isNative(method.getModifiers()) ) {
                            String pre = codeWriter.preCode(method.getLongName());
                            String post = codeWriter.postCode(method.getLongName());

                            if (pre != null && pre.length() > 0) {
                                code = pre;
                                methodDescription = "insert before method " + method.getLongName();
                                method.insertBefore(pre);
                            }
                            if (post != null && post.length() > 0) {
                                code = post;
                                methodDescription = "insert before method " + method.getLongName();
                                method.insertAfter(post);
                            }
                        }
                    }

                    code = null;
                    methodDescription = null;
                    b = cl.toBytecode();
                }
            } catch (Exception e) {
                System.err.println("Could not instrument class=" + className
                        + " (" + methodDescription + ")"
                        + "\n, code = " + code + "\n, exception : " + 
                        e.getMessage() + "\n:" + e);
                System.err.flush();

                e.printStackTrace();
            } finally {
                if (cl != null) {
                    cl.detach();
                }
            }
            return b;
        } catch (RuntimeException e) {
            System.err.println("Transformation failed with : " + e);
            throw e;
        } catch (Throwable t) {
            System.err.println("Transformation failed with : " + t);
            System.exit(1);
            return null;
        }
    }

}
