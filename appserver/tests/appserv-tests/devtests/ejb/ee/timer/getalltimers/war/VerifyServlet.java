/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import javax.naming.*;

@WebServlet(urlPatterns="/VerifyServlet", loadOnStartup=1)
public class VerifyServlet extends HttpServlet {

    @EJB private SgltTimerBean sgltTimerBean;
    @EJB private StlesTimerBean stlesTimerBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

        PrintWriter out = resp.getWriter();
	resp.setContentType("text/html");
        String param = req.getQueryString();

        out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet VerifyServlet</title>");
            out.println("</head>");
            out.println("<body>");
        try {
            stlesTimerBean.createProgrammaticTimer();
            sgltTimerBean.createProgrammaticTimer();
            out.println("RESULT:" + sgltTimerBean.countAllTimers(param));
        }catch(Throwable e){
            out.println("got exception");
            out.println(e);
            e.printStackTrace();
        } finally {
            out.println("</body>");
            out.println("</html>");

            out.close();
            out.flush();

        }

    }

}
