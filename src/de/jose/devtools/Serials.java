package de.jose.devtools;

import java.util.Random;

/**
 */
public class Serials
{
    public static void main(String[] args)
    {
        int count=1;
        if (args.length >= 1) count = Integer.parseInt(args[0]);

        Random rand = new Random();
        while (count-- > 0)
        {
            long serial = rand.nextLong();
            System.out.println("static final long serialVersionUID = 0x"+Long.toHexString(serial)+"L;");
        }
    }
}
