/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.s1peqe.selfmanagement.tnative.action;

import javax.management.*;
import java.io.*;

public class CpuUtil implements NotificationListener,
    com.sun.s1peqe.selfmanagement.tnative.action.CpuUtilMBean {

    private final String JMX_MONITOR_GAUGE_LOW = "jmx.monitor.gauge.low";
    private final String JMX_MONITOR_GAUGE_HIGH = "jmx.monitor.gauge.high";
    public CpuUtil() {
    }
    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(notification != null) {
            if(notification.getType().equals(JMX_MONITOR_GAUGE_LOW) ||
                        notification.getType().equals(JMX_MONITOR_GAUGE_HIGH)) {
                    System.out.println("TEST PASSED");
                    out.write("Cpu util Monitor Test - PASSED\n");
                }
            } else {
                System.out.println("TEST FAILED");
            out.write("Cpu util Monitor Test - FAILED\n");
            }
            out.flush();
            out.close();
        } catch (Exception ex) { }
    }
}
