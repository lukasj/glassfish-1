/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.util;

/**
The API that should be used to output from a Cmd running within the framework.
 */
public interface Output extends DebugSink
{
    /**
    Output a message without a newline.

    @param o        the Object to output
     */
    public void print(Object o);

    /**
    Output a message with a newline.

    @param o        the Object to output
     */
    public void println(Object o);

    /**
    Output a message to error output

    @param o        the Object to output
     */
    public void printError(Object o);

    /**
    Output a debug error message if getDebug() is currently true.

    @param o        the Object to output
     */
    public void printDebug(Object o);

    /**
    Done with it, can be destroyed.
     */
    public void close();

};


