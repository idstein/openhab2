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

package org.bubblecloud.zigbee.api.cluster.impl.general.scenes;

import java.util.Enumeration;
import java.util.Hashtable;

import org.bubblecloud.zigbee.api.cluster.impl.api.core.Attribute;
import org.bubblecloud.zigbee.api.cluster.impl.api.core.ZBDeserializer;
import org.bubblecloud.zigbee.api.cluster.impl.api.general.scenes.ExtensionFieldSetAddScene;

/**
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @version $LastChangedRevision: 799 $ ($LastChangedDate: 2013-08-06 19:00:05 +0300 (Tue, 06 Aug 2013) $)
 */
public class ExtensionFieldSetAddSceneImpl implements ExtensionFieldSetAddScene {


    private int clusterId;
    private Attribute[] attributes;
    Hashtable<Attribute, Object> set;
    Attribute attribute;
    ZBDeserializer deserializer;
    boolean endSet;


    public boolean endSet() {
        return endSet;
    }

    public ExtensionFieldSetAddSceneImpl(int clusterId) {
        this.clusterId = clusterId;
    }

    public void setAttribute(Attribute attribute, Object value) {
        set.put(attribute, value);
    }


    public Attribute[] getAttributes(int clusterId) {
        Enumeration<Attribute> attribute = set.keys();
        int i = 0;
        while (attribute.hasMoreElements()) {
            attributes[i] = attribute.nextElement();
            i++;
        }
        return attributes;
    }

    public int getClusterId() {
        return clusterId;
    }

    public Object getValue(Attribute attributeId) {
        return set.get(attributeId);
    }


    public int getLength() {
        int length = 5; // 4 for ClusterId e 1 for length?
        for (int i = 0; i < attributes.length; i++) {
            length = length + 4 + attributes[i].getZigBeeType().getLength();
        }
        return length;
    }

}
