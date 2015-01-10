package com.stericson.RootShellTests;

import com.stericson.RootShell.containers.RootClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RootClass.Candidate
public class NativeJavaClass
{

    public NativeJavaClass(RootClass.RootArgs args)
    {
        System.out.println("NativeJavaClass says: oh hi there.");
        String p = "/data/data/com.android.browser/cache";
        File f = new File(p);
        String[] fl = f.list();
        if (fl != null)
        {
            System.out.println("Look at all the stuff in your browser's cache:");
            for (String af : fl)
            {
                System.out.println("-" + af);
            }
            System.out.println("Leaving my mark for posterity...");
            File f2 = new File(p + "/rootshell_was_here");
            try
            {
                FileWriter filewriter = new FileWriter(f2);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write("This is just a file created using RootShell's Sanity check tools..\n");
                out.close();
                System.out.println("Done!");
            }
            catch (IOException e)
            {
                System.out.println("...and I failed miserably.");
                e.printStackTrace();
            }

        }
    }

}
