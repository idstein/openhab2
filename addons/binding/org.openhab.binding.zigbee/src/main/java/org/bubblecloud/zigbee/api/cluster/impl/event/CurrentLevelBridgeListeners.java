/*
   Copyright 2008-2013 CNR-ISTI, http://isti.cnr.it
   Institute of Information Science and Technologies 
   of the Italian National Research Council 


   See the NOTICE file distributed with this work for additional 
   information regarding copyright ownership

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.bubblecloud.zigbee.api.cluster.impl.event;

import org.bubblecloud.zigbee.api.cluster.Cluster;
import org.bubblecloud.zigbee.api.cluster.general.event.CurrentLevelEvent;
import org.bubblecloud.zigbee.api.cluster.general.event.CurrentLevelListener;
import org.bubblecloud.zigbee.api.ReportingConfiguration;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.AnalogReporter;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.Attribute;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.ReportListener;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.Reporter;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Default implementation of the delegator class that handles the eventing of the {@link CurrentLevelListener}
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @version $LastChangedRevision: 799 $ ($LastChangedDate: 2013-08-06 19:00:05 +0300 (Tue, 06 Aug 2013) $)
 * @since 0.6.0
 */
public class CurrentLevelBridgeListeners implements ReportListener {

    private class CurrentLevelEventImpl implements CurrentLevelEvent {

        private final Cluster source;
        private final int event;

        public CurrentLevelEventImpl(Cluster cluster, Integer value) {
            source = cluster;
            event = value.intValue();
        }

        public int getEvent() {
            return event;
        }

        public Cluster getSource() {
            return source;
        }
    }

    private final Attribute bridged;
    private final ArrayList<CurrentLevelListener> listeners = new ArrayList<CurrentLevelListener>();
    private final Cluster cluster;
    private final ReportingConfiguration configuration;

    public CurrentLevelBridgeListeners(final ReportingConfiguration conf, final Attribute attribute, final Cluster c) {
        bridged = attribute;
        cluster = c;
        configuration = conf;
    }

    public void receivedReport(Dictionary<Attribute, Object> reports) {
        if (reports.get(bridged) == null) {
            return;
        }
        synchronized (listeners) {
            for (CurrentLevelListener listener : listeners) {
                listener.changedCurrentLevel(new CurrentLevelEventImpl(cluster, (Integer) reports.get(bridged)));
            }
        }
    }

    public List<CurrentLevelListener> getListeners() {
        return listeners;
    }

    public boolean subscribe(CurrentLevelListener listener) {
        synchronized (listeners) {
            if (listeners.size() == 0) {
                AnalogReporter subscription = (AnalogReporter) bridged.getReporter();
                if (configuration.getReportingOverwrite() || subscription.isActive() == false) {
                    subscription.setMaximumReportingInterval(configuration.getReportingMaximum());
                    subscription.setMinimumReportingInterval(configuration.getReportingMinimum());
                    subscription.setReportableChange(configuration.getReportingChange());
                    subscription.updateConfiguration();
                }
                if (subscription.addReportListener(this) == false) {
                    return false;
                }
            }
            return listeners.add(listener);
        }
    }

    public boolean unsubscribe(CurrentLevelListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.size() == 0) {
                Reporter reporter = bridged.getReporter();
                if (reporter.getReportListenersCount() == 1) {
                    reporter.clear();
                }
            }
        }
        return true;
    }
}