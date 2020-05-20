/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.util.SSHUtil;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * This is a remote command implementation of setup-ssh local command
 * @author Yamini K B
 */
@Service(name = "_setup-ssh")
@I18n("setup.ssh")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({RuntimeType.DAS})
public class SetupSshCommand implements AdminCommand {
    @Param(name = "sshuser", optional = true, defaultValue = "${user.name}")
    private String user;
    @Param(name = "sshpassword", optional = false, password = true)
    private String sshpassword;
    @Param(name = "sshkeypassphrase",  optional = true, password = true)
    private String sshkeypassphrase;
    @Param(name = "sshport", optional = true, defaultValue = "22")
    private int port;
    @Param(optional = true)
    private String sshkeyfile;
    @Param(optional = true)
    private String sshpublickeyfile;
    @Param(optional = true, defaultValue = "false")
    private boolean generatekey;
    @Param(optional = false, primary = true, multiple = true)
    private List<String> hosts;
    private Logger logger;
    private String realPass;
    TokenResolver resolver = new TokenResolver();
    
    @Inject
    SSHLauncher sshL;

    private void validate() throws CommandException {
        user = resolver.resolve(user);

        if (!StringUtils.ok(sshpassword)) {
            throw new CommandException(Strings.get("setup.ssh.null.sshpass"));
        } else {
            // obtain real password
            realPass = sshL.expandPasswordAlias(sshpassword);

            if (realPass == null) {
                throw new CommandException(Strings.get("setup.ssh.unalias.error", sshpassword));
            }
        }
        
        if (sshkeyfile == null) {
            //if user hasn't specified a key file and there is no key file at default
            //location, then generate one
            String existingKey = SSHUtil.getExistingKeyFile();
            if (existingKey == null) {
                sshkeyfile = SSHUtil.getDefaultKeyFile();
                if (!generatekey) {
                    throw new CommandException(Strings.get("setup.ssh.no.keyfile"));
                }
            }
            else {
                sshkeyfile = existingKey;
                if (SSHUtil.isEncryptedKey(sshkeyfile)) {
                    sshkeypassphrase = getSSHPassphrase();
                }
            }
        }
        else {
            if (!isAbsolutePath(sshkeyfile)) {
                throw new CommandException(Strings.get("setup.ssh.invalid.path", sshkeyfile));
            }

            SSHUtil.validateKeyFile(sshkeyfile);
            if (SSHUtil.isEncryptedKey(sshkeyfile)) {
                sshkeypassphrase = getSSHPassphrase();
            }
        }

        if (sshpublickeyfile != null) {
            if (!isAbsolutePath(sshpublickeyfile)) {
                throw new CommandException(Strings.get("setup.ssh.invalid.path", sshpublickeyfile));
            }
            SSHUtil.validateKeyFile(sshpublickeyfile);
        }
    }
    
    @Override
    public final void execute(AdminCommandContext context) {
        logger = context.getLogger();

        // initialize logger for SSHLauncher
        sshL.init(logger);
        
        ActionReport report = context.getActionReport();
        
        try {
            validate();
        } catch (CommandException ce) {
            report.setMessage(ce.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        
        for (String node : hosts) {
            sshL.init(user, node, port, realPass, sshkeyfile, sshkeypassphrase, logger);
            if (generatekey ) {
                if (sshkeyfile != null || SSHUtil.getExistingKeyFile() != null) {
                    if (sshL.checkConnection()) {
                        logger.info(Strings.get("setup.ssh.already.configured", user, node));
                        continue;
                    }
                }
            }
            try {
                sshL.setupKey(node, sshpublickeyfile, generatekey, realPass);
            }
            catch (IOException ce) {
                logger.log(Level.INFO, "SSH key setup failed: " + ce);
                report.setMessage(Strings.get("setup.ssh.failed", ce.getMessage()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            catch (Exception e) {
                //handle KeyStoreException
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Keystore error: ", e);
                }
            }

            if (!sshL.checkConnection()) {
                report.setMessage(Strings.get("setup.ssh.conn.failed"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } 
    } 

    private boolean isAbsolutePath(String path) {
        boolean ret = false;
        File f = new File(path);
        if (f.isAbsolute())
            ret = true;
        return ret;
    } 

    /**
     * Get SSH key passphrase. Obtain real passphrase in case it is an alias.
     */
    private String getSSHPassphrase() throws CommandException {
        // empty pass phrase
        String key = "";
        
        if (sshkeypassphrase != null && !sshkeypassphrase.isEmpty()) {
            key = sshL.expandPasswordAlias(sshkeypassphrase);

            if (key == null) {
                throw new CommandException("setup.ssh.null.keypassphrase");
            }
        }
        return key;
    }
}
