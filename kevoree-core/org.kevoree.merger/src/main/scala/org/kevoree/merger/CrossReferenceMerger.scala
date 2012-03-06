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
package org.kevoree.merger

import org.kevoree.{ComponentInstance, ContainerRoot}
import resolver._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/10/11
 * Time: 22:18
 * To change this template use File | Settings | File Templates.
 */

trait CrossReferenceMerger {
  def breakCrossRef(actualModel: ContainerRoot, modelToMerge: ContainerRoot) {

    //BREAK TOPOLOGY MODEL
    (actualModel.getNodeNetworks ++ modelToMerge.getNodeNetworks).foreach {
      nn =>
        nn.getInitBy.map {
          initByNode =>
            nn.setInitBy(Some(UnresolvedNode(initByNode.getName)))
        }
        nn.setTarget(UnresolvedNode(nn.getTarget.getName))
    }

    //BREAK DEPLOY TARGET NODE TYPE
    (actualModel.getDeployUnits ++ modelToMerge.getDeployUnits).foreach {
      dp =>
        dp.getTargetNodeType.map {
          targetNodeType =>
            dp.setTargetNodeType(Some(UnresolvedNodeType(targetNodeType.getName)))
        }
    }
    //BREAK EVERY CROSS REFERENCE
    (actualModel.getLibraries ++ modelToMerge.getLibraries).foreach {
      library =>
        val subTypes = library.getSubTypes
        library.removeAllSubTypes()
        subTypes.foreach {
          libSubType =>
            library.addSubTypes(UnresolvedTypeDefinition(libSubType.getName))
        }
    }
    (actualModel.getTypeDefinitions ++ modelToMerge.getTypeDefinitions).foreach {
      typeDef =>
        typeDef.getSuperTypes.foreach {
          superType =>
            typeDef.removeSuperTypes(superType)
            typeDef.addSuperTypes(UnresolvedTypeDefinition(superType.getName))
        }
    }
    import org.kevoree.framework.aspects.KevoreeAspects._
    (actualModel.getAllInstances ++ modelToMerge.getAllInstances).foreach {
      instance =>
        instance.setTypeDefinition(UnresolvedTypeDefinition(instance.getTypeDefinition.getName))
        //BREAK PORT TYPE REF
        if (instance.isInstanceOf[ComponentInstance]) {
          val componentInstance: ComponentInstance = instance.asInstanceOf[ComponentInstance]
          componentInstance.getProvided.foreach {
            pport =>
              pport.setPortTypeRef(UnresolvedPortTypeRef(pport.getPortTypeRef.getName))
          }
          componentInstance.getRequired.foreach {
            rport =>
              rport.setPortTypeRef(UnresolvedPortTypeRef(rport.getPortTypeRef.getName))
          }
        }

        //BREAK DICTIONARY
        instance.getDictionary.map {
          instanceDictionary =>
            instanceDictionary.getValues.foreach {
              dictionaryValue =>
                dictionaryValue.setAttribute(UnresolvedDictionaryAttribute(dictionaryValue.getAttribute.getName))
                dictionaryValue.getTargetNode.map {
                  targetNode =>
                    dictionaryValue.setTargetNode(Some(UnresolvedNode(targetNode.getName)))
                }
            }
        }
    }
  }
}