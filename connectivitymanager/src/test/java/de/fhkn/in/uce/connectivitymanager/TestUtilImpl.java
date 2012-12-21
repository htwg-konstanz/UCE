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
package de.fhkn.in.uce.connectivitymanager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import de.fhkn.in.uce.connectivitymanager.selector.decisiontree.NATTraversalRule;
import de.fhkn.in.uce.plugininterface.NATFeatureRealization;
import de.fhkn.in.uce.plugininterface.NATSituation;
import de.fhkn.in.uce.plugininterface.NATTraversalTechnique;

public final class TestUtilImpl {

    private enum Implementation implements TestUtil {
        INSTANCE;

        private static final String NAT_BEHAVIOR_AND_TRAVERSAL_FILE_NAME = "natBeahviorCases.txt";

        @Override
        public NATTraversalTechnique getNATTraversalTechniqueForName(final String name) {
            return new NATTraversalTechniqueMock(name, 0, true);
        }

        @Override
        public Set<NATTraversalRule> getRulesForDecisionTreeLearning() {
            final Set<NATTraversalRule> result = new HashSet<NATTraversalRule>();
            final InputStream is = this.getStreamWithTestLearningData();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";

            try {
                while ((line = reader.readLine()) != null) {
                    final String[] content = line.split(",");

                    final NATSituation natSituation = new NATSituation(NATFeatureRealization.valueOf(content[0]),
                            NATFeatureRealization.valueOf(content[1]), NATFeatureRealization.valueOf(content[2]),
                            NATFeatureRealization.valueOf(content[3]));

                    final List<NATTraversalTechnique> natTraversalTechniques = new ArrayList<NATTraversalTechnique>();
                    for (int i = 4; i < content.length; i++) {
                        natTraversalTechniques.add(this.getNATTraversalTechniqueForName(content[i]));
                    }

                    result.add(new NATTraversalRule(natSituation, natTraversalTechniques));
                }
            } catch (final IOException e) {
                e.printStackTrace();
                try {
                    reader.close();
                    is.close();
                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
            }

            return result;
        }

        private InputStream getStreamWithTestLearningData() {
            return this.getClass().getClassLoader().getResourceAsStream(NAT_BEHAVIOR_AND_TRAVERSAL_FILE_NAME);
        }

        @Override
        public void setRegistryDirectory(final String directory) throws Exception {
            String proFileName = System.getProperty("user.dir")
                    + "/src/main/resources/de/fhkn/in/uce/connectivitymanager/registry/nattraversalregistry.properties";
            Properties prop = new Properties();
            prop.load(new FileInputStream(proFileName));
            prop.put("nattraversalregistry.directory", directory);
            prop.store(new FileOutputStream(proFileName), null);
        }

        @Override
        public InetSocketAddress getEndpointfromProperty(final String ipProperty, final String portProperty) {
            final ResourceBundle bundle = ResourceBundle
                    .getBundle("de.fhkn.in.uce.connectivitymanager.mediatorconnection.mediator");
            final String mediatorIP = bundle.getString(ipProperty);
            final int mediatorPort = Integer.valueOf(bundle.getString(portProperty));
            return new InetSocketAddress(mediatorIP, mediatorPort);
        }

        @Override
        public boolean compareLists(final List<NATTraversalTechnique> list1, final List<NATTraversalTechnique> list2) {
            boolean result = true;
            if (list1.size() == list2.size()) {
                for (NATTraversalTechnique elem1 : list1) {
                    if (!list2.contains(elem1)) {
                        result = false;
                    }
                }
            } else {
                result = false;
            }
            return result;
        }
    }

    public static TestUtil getInstance() {
        return Implementation.INSTANCE;
    }

    private TestUtilImpl() {
        throw new AssertionError();
    }
}
