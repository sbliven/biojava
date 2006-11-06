/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that can support multiple named timers.
 */
public class NamedTimer {

    Map startTimes = new HashMap();

    Map stopTimes = new HashMap();

    public NamedTimer() {

    }

    //---------------------------------------------------------------------
    /**
     * Start the timer.
     */
    public void start(String name) {

        if (!startTimes.containsKey(name)) {

            startTimes.put(name, new Long(System.currentTimeMillis()));

        } else {

            throw new RuntimeException("Timer named " + name + " already started!");

        }
    }

    //---------------------------------------------------------------------
    /**
     * Stop a timer.
     * 
     * @return The number of milliseconds since the timer was started.
     */
    public long stop(String name) {

        long result = 0;

        if (!startTimes.containsKey(name)) {

            throw new RuntimeException("Timer named " + name + " was not started!");

        } else {

            result = System.currentTimeMillis() - ((Long) startTimes.get(name)).longValue();
            stopTimes.put(name, new Long(System.currentTimeMillis()));

        }

        return result;

    }

    //---------------------------------------------------------------------

    /**
     * Retrieve the duration of a previously-stopped timer.
     */
    public long getDuration(String name) {

        long result = 0;

        if (!startTimes.containsKey(name)) {
          return result;
        }
        
        else if (!stopTimes.containsKey(name)) {

            throw new RuntimeException("Timer named " + name + " was not stopped!");

        } else {

            result = ((Long) stopTimes.get(name)).longValue() - ((Long) startTimes.get(name)).longValue();

        }

        return result;

    }

    //---------------------------------------------------------------------
    /**
     * Convert time in ms to hours, minutes, seconds.
     */
    public String format(long ms) {

        StringBuffer result = new StringBuffer();

        if (ms > 3600000) {

            result.append(ms / 3600000 + "hr ");
            ms %= 3600000;

        }
        if (ms > 60000) {

            result.append(ms / 60000 + "m ");
            ms %= 60000;

        }

        result.append(new DecimalFormat("##.#").format((double) ms / 1000.0) + "s");

        return result.toString();

    }

    //---------------------------------------------------------------------

}