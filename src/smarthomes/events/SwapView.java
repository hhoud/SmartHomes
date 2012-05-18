/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.events;

/**
 *
 * @author root
 */
public class SwapView {
    // Create the listener list
    protected javax.swing.event.EventListenerList listenerList =
        new javax.swing.event.EventListenerList();

    // This methods allows classes to register for MyEvents
    public void addMyEventListener(SwapViewEventListener listener) {
        listenerList.add(SwapViewEventListener.class, listener);
    }

    // This methods allows classes to unregister for MyEvents
    public void removeMyEventListener(SwapViewEventListener listener) {
        listenerList.remove(SwapViewEventListener.class, listener);
    }

    // This private class is used to fire MyEvents
   public void fireMyEvent(SwapViewEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==SwapViewEventListener.class) {
                ((SwapViewEventListener)listeners[i+1]).swapViewEventOccured(evt);
            }
        }
    }
}
