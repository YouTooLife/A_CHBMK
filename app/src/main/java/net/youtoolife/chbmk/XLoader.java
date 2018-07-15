package net.youtoolife.chbmk;

/**
 * Created by youtoolife on 5/5/18.
 */

import java.util.HashMap;

class XLoader extends ClassLoader

{
    HashMap mappings;

    XLoader(HashMap mappings)
    {
        this.mappings = mappings;
    }

    public synchronized Class loadClass(String name, byte[] buf) throws ClassNotFoundException
    {
        try
        {
            System.out.println("loadClass (" + name + ")");

            if (!mappings.containsKey(name))
            {

                return super.findSystemClass(name);

            }

            //String fileName = (String) mappings.get(name);

            return defineClass(name, buf, 0, buf.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ClassNotFoundException(e.getMessage(), e);
        }
    }
}
