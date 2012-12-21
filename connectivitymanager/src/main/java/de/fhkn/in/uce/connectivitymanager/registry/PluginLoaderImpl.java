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
package de.fhkn.in.uce.connectivitymanager.registry;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

final class PluginLoaderImpl implements PluginLoader {
    private static final PluginLoaderImpl INSTANCE = new PluginLoaderImpl();
    private final Logger logger = LoggerFactory.getLogger(PluginLoaderImpl.class);
    private ServiceLoader<NATTraversalTechnique> serviceLoader = ServiceLoader.load(NATTraversalTechnique.class);

    @Override
    public void loadPlugins() throws Exception {
        final File[] plugins = this.getPluginFiles();
        final URL[] pluginUrls = this.getUrlsFromFiles(plugins);
        this.addUrlsToClasspath(pluginUrls);
        this.serviceLoader = ServiceLoader.load(NATTraversalTechnique.class);
        this.checkPluginsLoaded();
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

    private void checkPluginsLoaded() {
        final Iterator<NATTraversalTechnique> iterator = this.serviceLoader.iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException("No NAT Traversal Techniques found.");
        }
        while (iterator.hasNext()) {
            final NATTraversalTechnique plugin = iterator.next();
            this.logger.info("Found plugin: {}", plugin.getMetaData().getTraversalTechniqueName()); //$NON-NLS-1$
        }
    }

    @Override
    public Iterator<NATTraversalTechnique> getPluginIterator() {
        return this.serviceLoader.iterator();
    }

    private String getPluginFolderPath() {
        return System.getProperty("user.dir") + this.getFolderNameFromBundle(); //$NON-NLS-1$
    }

    private String getFolderNameFromBundle() {
        final ResourceBundle bundle = ResourceBundle
                .getBundle("de.fhkn.in.uce.connectivitymanager.registry.nattraversalregistry"); //$NON-NLS-1$
        return bundle.getString("nattraversalregistry.directory");
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
