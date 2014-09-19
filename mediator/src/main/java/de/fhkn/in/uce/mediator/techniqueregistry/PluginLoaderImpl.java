/*
 * Copyright (c) 2012 Alexander Diener,
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhkn.in.uce.mediator.techniqueregistry;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.mediator.HandleMessage;

/**
 * Singleton implementation of {@link PluginLoader} which uses the
 * {@link ServiceLoader}. The directory which is defined by a property file is
 * used to extend the classpath with the plugins. The plugins which implement
 * {@link HandleMessage} are accessible with this class.
 * 
 * @author Alexander Diener (aldiener@htwg-konstanz.de)
 * 
 */
final class PluginLoaderImpl implements PluginLoader {
    private static final PluginLoaderImpl INSTANCE = new PluginLoaderImpl();
    private static final String RESOURCE_PLUGIN_DIRECTORY = "de.fhkn.in.uce.mediator.techniqueregistry.nattraversalregistry"; //$NON-NLS-1$
    private static final String DEFAULT_PLUGIN_DIRECTORY = "/plugins/"; //$NON-NLS-1$
    private final Logger logger = LoggerFactory.getLogger(PluginLoaderImpl.class);
    private ServiceLoader<HandleMessage> serviceLoader = ServiceLoader.load(HandleMessage.class);

    @Override
    public void loadPlugins() throws Exception {
        final File[] plugins = this.getPluginFiles();
        final URL[] pluginUrls = this.getUrlsFromFiles(plugins);
        this.addUrlsToClasspath(pluginUrls);
        this.serviceLoader = ServiceLoader.load(HandleMessage.class);
        this.checkLoadedPlugins();
    }

    private void checkLoadedPlugins() {
    	logger.debug("Checking loaded plugins.");
        Iterator<HandleMessage> iterator = this.serviceLoader.iterator();
        if (!iterator.hasNext()) {
        	throw new RuntimeException("No message handler plugins found.");
        }
        while (iterator.hasNext()) {
            HandleMessage handleMessage = (HandleMessage) iterator.next();
            logger.debug("Found plugin for handling connection requests for encoding {}", handleMessage //$NON-NLS-1$
                    .getAttributeForTraversalTechnique().getEncoded());
        }
    }

    private File[] getPluginFiles() {
        final String pluginFolderPath = this.getPluginFolderPath();
        final File pluginFolder = new File(pluginFolderPath);
        this.logger.info("Plugin directory = {}", pluginFolder.getAbsolutePath()); //$NON-NLS-1$
        File[] plugins = new File[0];
        plugins = pluginFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File arg0, final String arg1) {
                return arg1.endsWith(".jar"); //$NON-NLS-1$
            }
        });

        return plugins;
    }

    private URL[] getUrlsFromFiles(final File[] files) throws Exception {
        final URL[] result = new URL[files.length];
        for (int i = 0; i < result.length; i++) {
            final File pluginFile = files[i];
            result[i] = pluginFile.toURI().toURL();
        }

        return result;
    }

    private void addUrlsToClasspath(final URL[] urls) {
        final ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
        final ClassLoader newClassLoader = new URLClassLoader(urls, currentClassloader);
        Thread.currentThread().setContextClassLoader(newClassLoader);
    }

    @Override
    public Iterator<HandleMessage> getPluginIterator() {
        return this.serviceLoader.iterator();
    }

    private String getPluginFolderPath() {
        return System.getProperty("user.dir") + this.getFolderNameFromBundle(); //$NON-NLS-1$
    }

    private String getFolderNameFromBundle() {
        String result = DEFAULT_PLUGIN_DIRECTORY;
        final InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream(RESOURCE_PLUGIN_DIRECTORY);
        final Properties props = new Properties();
        if (resourceAsStream != null) {
            try {
                props.load(resourceAsStream);
            } catch (final IOException e) {
                logger.error(
                        "Resource for plugin directory could not be loaded. Default location will be used. {}", e.getMessage()); //$NON-NLS-1$
            }
            result = props.getProperty("nattraversalregistry.directory"); //$NON-NLS-1$
        }
        // final ResourceBundle bundle =
        // ResourceBundle.getBundle(RESOURCE_PLUGIN_DIRECTORY);
        //        return bundle.getString("nattraversalregistry.directory"); //$NON-NLS-1$
        return result;
    }

    private PluginLoaderImpl() {
        try {
            this.loadPlugins();
        } catch (final Exception e) {
            throw new RuntimeException("Exception while loading NAT Traversal Technique plugins.", e); //$NON-NLS-1$
        }
    }

    public static PluginLoaderImpl getInstance() {
        return INSTANCE;
    }
}
