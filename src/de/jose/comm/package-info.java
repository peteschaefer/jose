package de.jose.comm;

/**
 * There are two mechanisms for decoupled communication. RPC-like.
 * Both use messages and producer-consumer schemes.
 *
 * (1)  MessageProducer
 *      MessageListener
 *
 *      * 1:1 connections from a producer to registered listeners
 *      * quick
 *      * message delivery can be deferred to the AWT event thread
 *          which is important for all actions that modify the GUI
 *      * message types need to be hard-coded
 *
 * (2)  Command
 *      CommandListener
 *      CommandAction
 *      CommandDispatcher
 *
 *      * more flexible. there is a broadcast meachanisms: interested parties just register their listener code
 *      * receiving end is handled through a map of message key -> lambda (see setupActionMap)
 *      * messages can be defined on-the-fly by just introducing a string key
 *      * deferred execution can be done through flag INVOKE_LATER
 *          (but it's not yet used :( )
 *      * there is a singleton CommandDispatcher instance that can be used to send messages
 *          (either to a specific destination, or broadcast)
 *      * hierarchy of listeners
 *
 *      * was ... meant to be used for undo/redo. But not implemented properly.
 *
 * todo many places use SwingUtilities.invokeLater(). Maybe, this could be done more elegantly using one of the above mechanisms.
 *
 */