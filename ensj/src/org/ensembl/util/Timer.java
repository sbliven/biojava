/*
  Copyright (C) 2003 EBI, GRL

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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Event timer.
 */
public class Timer implements Serializable {
  
	private static final long serialVersionUID = 1L;
	
	private boolean running = false;
  private long duration = 0;
  private long startTime = 0;
  private boolean paranoid;

  /**
   * @param paranoid Determines whether an exceptions is thrown if an already
   * running timer is started or a one not running is stopped.
   */
  public Timer(boolean paranoid) {
    this.paranoid = paranoid;
  }


  /**
   * Creates timer in non-paranoid mode. 
   * 
   * start() and stop() can be called multiple times without 
   * an exception being thrown.  */
  public Timer() {
    this(false);
  }

  public Timer start() {
    if ( paranoid && running )  throw new RuntimeException("Timer already started!");
    startTime = System.currentTimeMillis();
    running = true;
    return this;
  }


  public Timer stop() throws RuntimeException {
    if ( paranoid && !running ) throw new RuntimeException("Timer not started!");
    duration = System.currentTimeMillis() - startTime;
    running = false;
    return this;
  }

  public long getDuration() {
    if ( running ) duration = System.currentTimeMillis() - startTime;
    return duration;
  }
  
  public double getDurationInSecs() {
    return getDuration()/1000.0;
  }


  /**
   * Returns a time + date fromated as string.
   */
  public static String getTimeStamp() {
    SimpleDateFormat formatter
      = new SimpleDateFormat ("hh:mm:ss a dd.MM.yyyy");
    Date currentTime_1 = new Date();
    return  formatter.format(currentTime_1);
  }

  public static void main(String[] args) {
    Timer sTimer = new Timer();
    sTimer.start();
    try { Thread.sleep(50); } catch (Exception e) {}
    sTimer.stop();
    System.out.println(sTimer.getDuration());
    
  }
  
  
  /**
   * Returns duration in seconds.
   * @return duration in seconds.
   */
  public String toString() {
  	return Double.toString(getDurationInSecs());
  }
  
} // SimpleTimer
