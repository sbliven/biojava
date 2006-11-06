/*
 Copyright (C) 2004 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.util;

/**
 * Simple class to print & update a console percentage progress meter.
 */

public class ProgressPrinter {

    private String postFix = "%";
    private int lastMsgLength = 0; 
    private int min, max, current;
    
    /**
     * Create a new ProgressPrinter.
     * @param min Minimum value.
     * @param max Maximum value.
     * @param postFix What to print after each percentage; will usually be "%"
     */
    public ProgressPrinter(int min, int max, String postFix) {
        
        this.min = min;
        this.max = max;
        this.postFix = postFix;
        
    }
    
    /**
     * Update the current value.
     * @param current The current value (must be between min and max)
     * @return A string that when printed will erase the previous output and print the new percentage.
     */
    public String update(int current) {
        
        int denominator = max-min;
        int percent = (denominator==0) ? 100 : (100 * (current-min)) / denominator;
        String bs = "";
        for (int i = 0; i < lastMsgLength; i++) {
            bs += "\b";
        }
        String msg = percent + postFix;
        lastMsgLength = msg.length();
        
        return(bs + msg);
        
    }
    
    public void printUpdate(int current) {
        
        System.out.print(update(current));
        System.out.flush();
        
    }
    
    
    /**
     * @return Returns the current.
     */
    public int getCurrent() {

        return current;
    }
    /**
     * @return Returns the max.
     */
    public int getMax() {

        return max;
    }
    /**
     * @return Returns the min.
     */
    public int getMin() {

        return min;
    }
    /**
     * @param current The current to set.
     */
    public void setCurrent(int current) {

        this.current = current;
    }
    /**
     * @param max The max to set.
     */
    public void setMax(int max) {

        this.max = max;
    }
    /**
     * @param min The min to set.
     */
    public void setMin(int min) {

        this.min = min;
    }
    
    /**
     * Set the postfix to be printed.
     * @param postfix The new postfix.
     */
    public void setPostfix(String postfix) {
        
        this.postFix = postfix;
        
    }
    
    /**
     * Get the postfix to be printed.
     * @return The postfix.
     */
    public String getPostfix() {
        
        return postFix;
        
    }
    
} // ProgressPrinter
