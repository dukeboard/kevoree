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

package org.kevoree.framework.annotation.processor.visitor.sub

import org.kevoree.ContainerRoot
import org.kevoree.NodeType
import org.kevoree.TypeDefinition
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import org.kevoree.framework.annotation.processor.visitor.KevoreeAnnotationProcessor
import org.kevoree.framework.annotation.processor.LocalUtility


/* Common Sub process to deal with ThirdParty definition */

trait ThirdPartyProcessor {

  def processThirdParty(componentType: TypeDefinition, classdef: TypeElement, env: ProcessingEnvironment, rootVisitor: KevoreeAnnotationProcessor) = {
    val root: ContainerRoot = componentType.eContainer.asInstanceOf[ContainerRoot]

    var thirdPartyAnnotations: List[org.kevoree.annotation.ThirdParty] = Nil

    val annotationThirdParty = classdef.getAnnotation(classOf[org.kevoree.annotation.ThirdParty])
    if (annotationThirdParty != null) {
      thirdPartyAnnotations = thirdPartyAnnotations ++ List(annotationThirdParty)
    }

    val annotationThirdParties = classdef.getAnnotation(classOf[org.kevoree.annotation.ThirdParties])
    if (annotationThirdParties != null) {
      thirdPartyAnnotations = thirdPartyAnnotations ++ annotationThirdParties.value.toList
    }

    import scala.collection.JavaConversions._
    val thirdParties = rootVisitor.getOptions.get("thirdParties").toString
    val thirdPartiesList: List[String] = thirdParties.split(";").filter(r => r != null && r != "").toList

    val nodeTypeNames = rootVisitor.getOptions.get("nodeTypeNames").toString
    val nodeTypeNameList: List[String] = nodeTypeNames.split(",").filter(r => r != null && r != "").toList


    /* CHECK THIRDPARTIES */
    /*
    thirdPartyAnnotations.foreach{tp=>
      root.getDeployUnits.find({etp => etp. == tp.name}) match {
        case Some(e) => {
            componentType.getDeployUnits().get(0).addRequiredLibs(e)
          }
        case None => {
            val newThirdParty = kevoreeFactory.createDeployUnit
            newThirdParty.setName(tp.name)
            newThirdParty.setUrl(tp.url)
            root.addDeployUnits(newThirdParty)
            componentType.getDeployUnits().get(0).addRequiredLibs(newThirdParty)
          }
      }
    }    */

    /* CHECK TP from POM */
    thirdPartiesList.foreach {
      tp =>
        val splittedTP = tp.split(",")
        val name = splittedTP(1)
        val groupName = splittedTP(0)
        val version = splittedTP(2)
        val dutype = splittedTP(3)
        var url: String = null

        if (splittedTP.length == 5) {
          url = splittedTP(4)
        }

        root.getDeployUnits.find({
          etp => etp.getName == name && etp.getGroupName == groupName && etp.getVersion == version
        }) match {
          case Some(e) => {
            if (!componentType.getDeployUnits().get(0).getRequiredLibs.exists(etp => etp.getName == name && etp.getGroupName == groupName && etp.getVersion == version)) {
              componentType.getDeployUnits().get(0).addRequiredLibs(e)
            }
          }
          case None => {
            val newThirdParty = LocalUtility.kevoreeFactory.createDeployUnit
            newThirdParty.setName(name)
            newThirdParty.setGroupName(groupName)
            newThirdParty.setVersion(version)
            newThirdParty.setType(dutype)
            if (url != null && url != "") {
              newThirdParty.setUrl(url)
            }
            root.addDeployUnits(newThirdParty)
            componentType.getDeployUnits().get(0).addRequiredLibs(newThirdParty)
          }
        }
    }

    /* POST PROCESS ADD NODE TYPE TO ALL THIRDPARTY */
    componentType.getDeployUnits().get(0).getRequiredLibs.foreach {
      tp =>
        nodeTypeNameList.foreach {
          nodeTypeName =>
          /* ROOT ADD NODE TYPE IF NECESSARY */
            nodeTypeNameList.foreach {
              nodeTypeName =>
                componentType.eContainer.asInstanceOf[ContainerRoot].getTypeDefinitions.filter(p => p.isInstanceOf[NodeType]).find(nt => nt.getName == nodeTypeName) match {
                  case Some(existingNodeType) => tp.setTargetNodeType(existingNodeType.asInstanceOf[NodeType])
                  case None => {
                    val nodeType = LocalUtility.kevoreeFactory.createNodeType
                    nodeType.setName(nodeTypeName)
                    root.addTypeDefinitions(nodeType)
                    tp.setTargetNodeType(nodeType)
                  }
                }
            }

        }
    }
  }
}
