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
package org.kevoree.framework;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.Bootstraper;
import org.kevoree.api.NodeType;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.api.service.core.handler.ContextModel;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.framework.context.HashMapContextModel;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;

import java.util.HashMap;

public abstract class
		AbstractNodeType extends AbstractTypeDefinition implements NodeType {

	public void startNode () {
	}

	public void stopNode () {
	}

	public void updateNode () {
	}

    @Override
    public void setNodeName(String pnodeName) {
        super.setNodeName(pnodeName);
        super.setName(pnodeName);
    }

    @Override
    public void setName(String pname) {
        super.setName(pname);
        super.setNodeName(pname);
    }

    public abstract AdaptationModel kompare (ContainerRoot actualModel, ContainerRoot targetModel);

	public abstract PrimitiveCommand getPrimitive (AdaptationPrimitive primitive);

	private HashMapContextModel contextModel = new HashMapContextModel();

	@Override
	public ContextModel getContextModel () {
		return contextModel;
	}

	/**
	 * Allow to find the corresponding element into the model
	 *
	 * @return the node corresponding to this
	 */
	public ContainerNode getModelElement () {
		return KevoreeElementHelper.getNodeElement(this.getNodeName(), this.getModelService().getLastModel()).get();
	}

}




