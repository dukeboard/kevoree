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
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.framework;

import org.kevoree.Channel;
import org.kevoree.api.Bootstraper;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.framework.message.Message;

import java.util.HashMap;

/**
 * @author ffouquet
 */
public abstract class AbstractChannelFragment implements ChannelFragment {

    private ModelHandlerServiceProxy modelServiceProxy = new ModelHandlerServiceProxy();

    public void setModelService(KevoreeModelHandlerService ms) {
        modelServiceProxy.setProxy(ms);
    }

    public KevoreeModelHandlerService getModelService() {
        return modelServiceProxy;
    }

    public java.util.List<KevoreePort> getBindedPorts() {
        return null;
    } //OVERRIDE BY FACTORY

    public java.util.List<KevoreeChannelFragment> getOtherFragments() {
        return null;
    } //OVERRIDE BY FACTORY

    public Object forward(KevoreeActor delegate, Message msg) {
        return null;
    } //OVERRIDE BY FACTORY

    public HashMap<String, Object> getDictionary() {
        return null;
    } //OVERRIDE BY FACTORY

    public String getNodeName() {
        return null;
    }

    public String getName() {
        return null;
    }

    public Object remoteDispatch(Message msg) {
        return null;
    }

    private KevScriptEngineFactory kevScriptEngineFactory = null;

    public KevScriptEngineFactory getKevScriptEngineFactory() {
        return kevScriptEngineFactory;
    }

    public void setKevScriptEngineFactory(KevScriptEngineFactory kf) {
        kevScriptEngineFactory = kf;
    }


    /**
     * Allow to find the corresponding element into the model
     *
     * @return the channel corresponding to this
     */
    public Channel getModelElement() {
        return KevoreeElementHelper.getChannelElement(this.getName(), this.getModelService().getLastModel()).get();
    }

    private Bootstraper bootstrapService = null;

    public void setBootStrapperService(Bootstraper brs) {
        bootstrapService = brs;
    }

    public Bootstraper getBootStrapperService() {
        return bootstrapService;
    }


}
