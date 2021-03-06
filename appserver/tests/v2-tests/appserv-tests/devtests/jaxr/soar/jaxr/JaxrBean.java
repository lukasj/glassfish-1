/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package jaxr;

import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.security.*;
import java.net.*;
import java.util.*;
public class JaxrBean implements SessionBean {
    private SessionContext sc;
    //private com.sun.jaxr.ra.ebxml.JaxrConnectionFactory cnfct = null;
    private ConnectionFactory cnfct = null;
    private Connection con = null;
//     private String regurl = "http://www-3.ibm.com/services/uddi/v2beta/inquiryapi";
//     private String regurlp = "https://www-3.ibm.com/services/uddi/v2beta/protect/publishapi";
    private StringBuffer result = new StringBuffer();
    String username = "jaxrsqa3a";
    String password = "testpass";
    public JaxrBean(){}

    public void ejbCreate() throws RemoteException {
    try{
        System.out.println (" IN EJBCREATE ");
        InitialContext ic = new InitialContext();

         String username = "jaxrsqa1a";
            // (String)ic.lookup("java:comp/env/username");
         String password = "testpass";
            // (String)ic.lookup("java:comp/env/password");
        //String regurl =  "http://jes-registry.east.sun.com:9080/omar/registry/soap";
            //(String)ic.lookup("java:comp/env/JaxrQueryURL");
        //String regurlp = "https://uddi.ibm.com/testregistry/publishapi";
            //(String)ic.lookup("java:comp/env/JaxrQueryURL");
        String httpProxyHost = "webcache.sfbay.sun.com";
            //(String)ic.lookup("java:comp/env/JaxrProxyHost");
        String httpProxyPort = "8080";
            //(String)ic.lookup("java:comp/env/JaxrProxyPort");;

        Properties props = new Properties();
        /*props.setProperty("javax.xml.registry.queryManagerURL", regurl);
        props.setProperty("javax.xml.registry.lifeCycleManagerURL", regurl);
        props.setProperty("com.sun.xml.registry.https.proxyHost", httpProxyHost);
        props.setProperty("com.sun.xml.registry.http.proxyHost", httpProxyHost);
        props.setProperty("com.sun.xml.registry.https.proxyPort", httpProxyPort);
        props.setProperty("com.sun.xml.registry.http.proxyPort", httpProxyPort);
        */
        cnfct = (javax.xml.registry.ConnectionFactory)ic.lookup("eis/SOAR");
        //cnfct = (com.sun.jaxr.ra.ebxml.JaxrConnectionFactory)icnfct;
        System.out.println (" Connection Factory = "+cnfct);
        cnfct.setProperties(props);
    } catch(JAXRException e){
        e.printStackTrace();
        throw new RemoteException("Cannot instantiate the factory " ,e);
    } catch(Exception e){
        e.printStackTrace();
        throw new RemoteException("Error in ejbCreate !!!", e);
    }
    System.out.println("In ejbCreate !! - created ConnectionFactory ");
    }


    public String getCompanyInformation(String company) throws EJBException,
                                   RemoteException{
    if(cnfct == null){
        return "ConnectionFactory was not instantiated. Test Failed";
    }
    if(company == null)
        throw new EJBException("Company name not specified");
    // create a jaxr connection instance

        try {
        //        System.setProperty("org.apache.commons.logging.simplelog.log.com.sun.xml.registry", "trace");
        System.out.println (" Connection factory = "+ cnfct);

        System.out.println (" Getting connection");
        com.sun.jaxr.ra.ebxml.JaxrConnectionFactory jcf
            = (com.sun.jaxr.ra.ebxml.JaxrConnectionFactory) cnfct;
        con = jcf.getConnection();
//        con = cnfct.createConnection();
        if (con == null) {
        System.out.println (" Connection is null");
        throw new EJBException("Connection could not be created");
        }
        System.out.println("Got the connection = "+ con);
        RegistryService rs = con.getRegistryService();
        System.out.println("Got the registry service = "+ rs);
        BusinessQueryManager bqm = rs.getBusinessQueryManager();
        System.out.println (" Business Query Manager = "+bqm);
        ArrayList names = new ArrayList();
        names.add(company);

        Collection fqualifiers = new ArrayList();
        fqualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
        System.out.println (" Before findOrganizations ");
        BulkResponse br = bqm.findOrganizations(fqualifiers, names, null,
                            null, null, null);

        System.out.println (" Bulk Response = "+br);
        if(br.getStatus() == JAXRResponse.STATUS_SUCCESS){
        System.out.println (" Results found for ("+company+")");
        Collection orgs = br.getCollection();
        Iterator rit = orgs.iterator();
        while(rit.hasNext()){
            Organization org = (Organization)rit.next();
            result.append(org.getName().getValue()+"\n");
            System.out.println (" Name = "+org.getName().getValue());
            System.out.println ("Description = "+ org.getDescription().getValue());
            result.append(org.getDescription().getValue()+"\n\n");
        }

        } else{
        System.out.println (" Could not query the registry due to the following exceptions :");
        Collection ex = br.getExceptions();
        Iterator it = ex.iterator();
        while(it.hasNext()){
            Exception e = (Exception) it.next();
            System.out.println (e.toString());
            result.append(e.toString());
        }
        }

            // publish to the registry...
            System.out.println(" ---- Publishing Information to registry ---");
            System.out.println(" About to create an org with name = "+ "SJSAS");
            createOrg("SJSAS", rs);
        } catch (Throwable ex) {
            ex.printStackTrace();
        System.out.println (" Test Failed");
        result.append("Test Failed");
        throw new java.rmi.RemoteException(ex.toString());
        }
        return result.toString();
    }

    private void createOrg(String name, RegistryService rs) throws Exception{
        BusinessLifeCycleManager blcm = rs.getBusinessLifeCycleManager();
        Organization org = blcm.createOrganization(name);
        // Create primary contact, set name
        User primaryContact = blcm.createUser();
        PersonName pName = blcm.createPersonName("Jane Doe");
        primaryContact.setPersonName(pName);
        org.setPrimaryContact(primaryContact);
        Collection orgs = new ArrayList();
        orgs.add(org);
        BulkResponse response = blcm.saveOrganizations(orgs);
        Collection exceptions = response.getExceptions();
    Collection keys = null;

        if (exceptions == null) {
            System.out.println("Organization saved");

            keys = response.getCollection();
            Iterator keyIter = keys.iterator();
            if (keyIter.hasNext()) {
                javax.xml.registry.infomodel.Key orgKey =
                    (javax.xml.registry.infomodel.Key) keyIter.next();
                String id = orgKey.getId();
                System.out.println("---- Organization key is " + id);
            }
        } else {
        System.out.println("Organization Could not be saved!");
    }
    System.out.println("Deleting the ORG now");
    response = blcm.deleteOrganizations(keys);
    exceptions = response.getExceptions();
    if(exceptions == null){
        System.out.println("Organization Deleted Successfully");
    }else{
        System.out.println("Organization Could not be deleted");
    }
}


    public void setSessionContext(SessionContext sc) {

        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
