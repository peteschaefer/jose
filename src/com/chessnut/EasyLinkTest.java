package com.chessnut;

public class EasyLinkTest
{
    public static void main(String[] args)
    {
        final int[] isConnected = {0};
        final String[] currentFen = {""};

        isConnected[0] = EasyLink.connect();
        if (isConnected[0] != 0)
            System.out.println("Connected.");
        else {
            System.out.println("Not Connected." + isConnected[0]);
            return;
        }

        EasyLink.setRealtimeCallback(new EasyLink.IRealTimeCallback() {
            @Override
            public void realTimeCallback(String fen) {
                if (isConnected[0] == 0) {
                    isConnected[0] = 1;
                    System.out.println("Connected.");
                }
                if (!fen.equals(currentFen[0]))
                    System.out.println("current fen: "+(currentFen[0] = fen));
            }
        });

        EasyLink.switchRealTimeMode();
/*
        while(!isConnected[0]) {
            System.out.println("connecting...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
*/
        System.out.println("dll version: "+EasyLink.version());
        System.out.println("MCU version: "+EasyLink.getMcuVersion());
        System.out.println("BLE version: "+EasyLink.getBleVersion());

        System.out.println("Battery: "+EasyLink.getBattery());
        System.out.println("Stored Files: "+EasyLink.getFileCount());

        StringBuilder leds = new StringBuilder("00000000/00000000/00000000/00000000/00000000/00000000/00000000/00000000");

        for(int i=0; i < 65; ++i) {
            int j1 = (i-1)*9/8;
            int j2 = i*9/8;
            if (j1>=0) leds.setCharAt(j1,'0');
            if (j2<leds.length()) leds.setCharAt(j2,'1');
            EasyLink.led(leds.toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
           // if (i>=5 && i <=10)
           //     EasyLink.beep(100+20*i,250);
        }


        EasyLink.disconnect();
    }
}
