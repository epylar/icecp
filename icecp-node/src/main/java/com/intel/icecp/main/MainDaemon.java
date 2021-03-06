/*
 * Copyright (c) 2017 Intel Corporation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.icecp.main;

import com.intel.icecp.core.Node;
import com.intel.icecp.node.NodeFactory;
import com.intel.icecp.node.utils.ConfigurationUtils;
import com.intel.icecp.node.utils.NetworkUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.security.Policy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configure and run the main application thread.
 *
 */
public class MainDaemon {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Start application and leave a thread running in the background
     *
     * @param arguments the list of command-line parameters passed to the application
     */
    public static void main(String[] arguments) throws UnknownHostException {
        final Node node = NodeFactory.buildDefaultNode(generateNodeName(), ConfigurationUtils.getConfigurationPath(), ConfigurationUtils.getPermissionsPath());
        node.start();

        loadBuiltInModules(node);
        setupSecurityManager();
        applyCommandLineArguments(node, arguments);
        loopForever();
    }

    /**
     * @return the host name of this node with prefix appended
     * @throws UnknownHostException if the
     */
    protected static String generateNodeName() throws UnknownHostException {
        return "/intel/node/" + NetworkUtils.getHostName();
    }

    /**
     * Load built-in modules; use this to load trusted modules that should have all permissions (e.g. system-level)
     *
     * @param node the currently running {@link Node}
     */
    private static void loadBuiltInModules(final Node node) {
        logger.debug("Loading builtin modules.");
        try {
            //((NodeImpl) node).loadAndStartModule(Commands.class);
            //((NodeImpl) node).loadAndStartModule(NodeMgmtAPI.class);
        } catch (Exception e) {
            logger.log(Level.ERROR, "Failed to load built-in modules.", e);
        }
    }

    /**
     * Setup the security manager.
     * <p>
     * There are two methods to do this, one uses a policy file, and the other uses a Policy class. PolicyFile: Use the
     * setProperty() line below, specifying the policy file. The policy file would look like this to grant "all
     * permissions" grant { permission java.security.AllPermission; };
     * <p>
     * Policy class: Create a class that extends Policy, and override the getPermissions() method. See the
     * AllPermissionPolicy class below.
     * <p>
     * This is the NDN policy file/class, not the module permissions
     */
    private static void setupSecurityManager() {
        logger.debug("Configuring security manager");
        if (System.getProperty("java.security.policy") == null) {
            Policy.setPolicy(new AllPermissionPolicy());
        }

        if (ConfigurationUtils.isSandboxEnabled()) {
            logger.info("Security sandbox enabled");
            System.setSecurityManager(new SecurityManager());
        } else {
            logger.warn("Security sandbox disabled");
        }
    }

    /**
     * Apply command line arguments.
     *
     * @param node the instantiate node instance
     * @param arguments the list of command-line parameters passed to the application
     */
    protected static void applyCommandLineArguments(Node node, String[] arguments) {
        logger.debug("Applying command-line arguments: {}", Arrays.toString(arguments));
        if (hasHelpOption(arguments)) {
            System.out.print(showHelpOptions());
            System.exit(0);
        } else {
            for (ModuleParameter module : parseModules(arguments)) {
                logger.debug("Found command-line module argument: {}", module);
                node.loadAndStartModules(module.modulePath, module.configurationPath);
            }
        }
    }

    /**
     * @param arguments the list of command-line parameters passed to the application
     * @return true if the list of arguments contains "-h" or "--help"
     */
    protected static boolean hasHelpOption(String[] arguments) {
        return Arrays.stream(arguments).anyMatch((String s) -> "-h".equals(s) || "--help".equals(s));
    }

    /**
     * @return the help text for this application
     */
    protected static String showHelpOptions() {
        return "Usage: icecp [module[:path-to-configuration]]..." + System.lineSeparator() +
                "  -h\tprints this message" + System.lineSeparator() +
                "  example: icecp my-module/my-module-1.0.jar:my-module/configuration.json";
    }

    /**
     * Expects space-separated JAR files
     *
     * @param arguments the list of command-line parameters passed to the application
     * @return the module paths with their associated configuration paths
     */
    protected static List<ModuleParameter> parseModules(String[] arguments) {
        return Arrays.stream(arguments)
                .filter((s) -> !s.startsWith("-D")) // ignore any appended Java properties
                .map(ModuleParameter::build).collect(Collectors.toList());
    }

    /**
     * Keep main thread running forever
     */
    private static void loopForever() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("Main thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
