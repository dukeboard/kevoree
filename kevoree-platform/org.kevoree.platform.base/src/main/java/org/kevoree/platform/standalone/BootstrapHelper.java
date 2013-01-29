/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.platform.standalone;

import org.kevoree.*;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/27/13
 * Time: 2:02 PM
 */
public class BootstrapHelper {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapHelper.class);

    public void initModelInstance(ContainerRoot model, String defType, String groupType) {

        String nodeName = System.getProperty("node.name");

        ContainerNode rootModel = model.findNodesByID(nodeName);
        if (rootModel == null) {
            //CREATE DEFAULT
            ContainerNode node = null;
            TypeDefinition typeDefFound = model.findTypeDefinitionsByID(defType);
            if (typeDefFound != null) {
                logger.warn("Init default node instance for name " + nodeName);
                node = KevoreeFactory.$instance.createContainerNode();
                node.setName(nodeName);
                node.setTypeDefinition(typeDefFound);
                model.addNodes(node);
            } else {
                logger.error("Default type not found for name " + defType);
            }
            if (groupType != null) {
                TypeDefinition grouptypeDefFound = model.findTypeDefinitionsByID(groupType);
                if (grouptypeDefFound != null) {
                    Group g = KevoreeFactory.$instance.createGroup();
                    g.setName("sync");
                    g.setTypeDefinition(grouptypeDefFound);
                    g.addSubNodes(node);
                    model.addGroups(g);
                } else {
                    logger.error("Default type not found for name " + defType);
                }
            }
        }
    }


    public ContainerRoot generateFromKevS(File scriptFile, KevScriptEngine kevEngine) throws IOException, KevScriptEngineException {
        kevEngine.addVariable("kevoree.version", KevoreeFactory.$instance.getVersion());
        Enumeration props = System.getProperties().propertyNames();
        while (props.hasMoreElements()) {
            String p = props.nextElement().toString();
            kevEngine.addVariable(p, System.getProperty(p));
        }
        FileInputStream ins = new FileInputStream(scriptFile);
        StringBuffer buffer = new StringBuffer();
        while (ins.available() > 0) {
            buffer.append((char) ins.read());
        }
        kevEngine.append(buffer.toString());
        return kevEngine.interpret();
    }


}
