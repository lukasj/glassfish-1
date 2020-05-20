/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.examples.jersey_cdi.resources;

import jakarta.annotation.ManagedBean;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;

/**
 * Shows injection of context objects and path parameters into the fields of a managed bean.
 *
 * @author robc
 */
@ManagedBean
@Path("/other/{c}/{d}")
public class MyOtherResource {

    @GET
    @Produces("text/plain")
    public String get() {
        return "OTHER " + c + " " + d;
    }

    @Context UriInfo uriInfo;
    @Context Request request;

    @PathParam("c") String c;
    @PathParam("d") String d;

}
