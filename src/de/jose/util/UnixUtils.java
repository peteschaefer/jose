/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util;

import de.jose.Application;
import de.jose.Version;

/**
 * @author Peter Schäfer
 */

public class UnixUtils
{
    /** load library    */
    static  {
        System.load(Application.theWorkingDirectory+"/lib/"+Version.osDir+"/libunixutils.so");
    }
/*
       SIGHUP        1       Term    Hangup detected on controlling terminal
                                     or death of controlling process
       SIGINT        2       Term    Interrupt from keyboard
       SIGQUIT       3       Core    Quit from keyboard
       SIGILL        4       Core    Illegal Instruction
       SIGABRT       6       Core    Abort signal from abort(3)
       SIGFPE        8       Core    Floating point exception
       SIGKILL       9       Term    Kill signal
       SIGSEGV      11       Core    Invalid memory reference
       SIGPIPE      13       Term    Broken pipe: write to pipe with no readers
       SIGALRM      14       Term    Timer signal from alarm(2)
       SIGTERM      15       Term    Termination signal
       SIGUSR1   30,10,16    Term    User-defined signal 1
       SIGUSR2   31,12,17    Term    User-defined signal 2
       SIGCHLD   20,17,18    Ign     Child stopped or terminated
       SIGCONT   19,18,25            Continue if stopped
       SIGSTOP   17,19,23    Stop    Stop process
       SIGTSTP   18,20,24    Stop    Stop typed at tty
       SIGTTIN   21,21,26    Stop    tty input for background process
       SIGTTOU   22,22,27    Stop    tty output for background process

       The signals SIGKILL and SIGSTOP cannot be caught, blocked, or ignored.

       Next the signals not in the POSIX.1 standard but described in SUSv2 and SUSv3 / POSIX 1003.1-2001.

       Signal       Value     Action   Comment
       --------------------------------------------------------------------
       SIGIOT         6        Core    IOT trap. A synonym for SIGABRT
       SIGEMT       7,-,7      Term
       SIGSTKFLT    -,16,-     Term    Stack fault on coprocessor (unused)
       SIGIO       23,29,22    Term    I/O now possible (4.2 BSD)
       SIGCLD       -,-,18     Ign     A synonym for SIGCHLD
       SIGPWR      29,30,19    Term    Power failure (System V)
       SIGINFO      29,-,-             A synonym for SIGPWR
       SIGLOST      -,-,-      Term    File lock lost
       SIGWINCH    28,28,20    Ign     Window resize signal (4.3 BSD, Sun)
       SIGUNUSED    -,31,-     Term    Unused signal (will be SIGSYS)

       (Signal 29 is SIGINFO / SIGPWR on an alpha but SIGLOST on a sparc.)

*/
    public static int getPid(Process process) throws NoSuchFieldException, IllegalAccessException
    {
        // assumes process instanceof java.lang.UNIXProcess
        Object result = ReflectionUtil.getValue(process,"pid");
        return ((Number)result).intValue();
    }

    public static void kill(Process process, int sig)
    {
        try {
            kill(getPid(process),sig);
        } catch (NoSuchFieldException e) {
            Application.error(e);
        } catch (IllegalAccessException e) {
            Application.error(e);
        }
    }

    public static native int kill(int pid, int signal);
}
