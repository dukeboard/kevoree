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

package org.kevoree.adaptation.deploy.osgi

import org.kevoree.adaptation.deploy.osgi.command._
import org.kevoree._
import adaptation.deploy.jcl._
import framework.context.KevoreeDeployManager
import framework.PrimitiveCommand
import kompare.JavaSePrimitive
import org.osgi.framework.Bundle
import org.slf4j.LoggerFactory

class BaseDeployOSGi(bundle: Bundle) {

  private val ctx = KevoreeDeployManager
  /*
  ctx.setBundle(bundle)
  ctx.setBundleContext(bundle.getBundleContext)
  val sr = bundle.getBundleContext.getServiceReference(classOf[PackageAdmin].getName)
  ctx.setServicePackageAdmin(bundle.getBundleContext.getService(sr).asInstanceOf[PackageAdmin])
              */

  private val logger = LoggerFactory.getLogger(this.getClass);

  def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive, nodeName: String): PrimitiveCommand = {
    p.getPrimitiveType.getName match {
      // case Some(primitiveType) => {
      //   primitiveType.getName match {

      case JavaSePrimitive.AddDeployUnit if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[DeployUnit])) => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.AddDeployUnit => AddDeployUnitAetherCommand(p.getRef.asInstanceOf[DeployUnit])

      case JavaSePrimitive.UpdateDeployUnit if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[DeployUnit])) => UpdateDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.UpdateDeployUnit => UpdateDeployUnitAetherCommand(p.getRef.asInstanceOf[DeployUnit])

      case JavaSePrimitive.RemoveDeployUnit if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[DeployUnit])) => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.RemoveDeployUnit => RemoveDeployUnitCommand(p.getRef.asInstanceOf[DeployUnit])

      case JavaSePrimitive.AddThirdParty if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[DeployUnit])) => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.AddThirdParty => AddThirdPartyAetherCommand(p.getRef.asInstanceOf[DeployUnit])

      case JavaSePrimitive.RemoveThirdParty if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[DeployUnit])) => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.RemoveThirdParty => RemoveThirdPartyCommand(p.getRef.asInstanceOf[DeployUnit])


      case JavaSePrimitive.AddType if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[TypeDefinition],nodeName)) => NoopCommand()
      case JavaSePrimitive.AddType => AddTypeCommand(p.getRef.asInstanceOf[TypeDefinition], nodeName)

       case JavaSePrimitive.RemoveType if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[TypeDefinition],nodeName)) => NoopCommand()
      case JavaSePrimitive.RemoveType => RemoveTypeCommand(p.getRef.asInstanceOf[TypeDefinition], nodeName)

      case JavaSePrimitive.AddInstance if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[Instance].getTypeDefinition,nodeName)) => AddInstance(p.getRef.asInstanceOf[Instance],nodeName)
      case JavaSePrimitive.AddInstance => AddInstanceCommand(p.getRef.asInstanceOf[Instance], nodeName)

      case JavaSePrimitive.UpdateDictionaryInstance if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[Instance].getTypeDefinition,nodeName)) => UpdateDictionary(p.getRef.asInstanceOf[Instance], nodeName)
      case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], nodeName)

      case JavaSePrimitive.RemoveInstance if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[Instance].getTypeDefinition,nodeName)) => RemoveInstance(p.getRef.asInstanceOf[Instance], nodeName)
      case JavaSePrimitive.RemoveInstance => RemoveInstanceCommand(p.getRef.asInstanceOf[Instance], nodeName)

      case JavaSePrimitive.StopInstance if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[Instance].getTypeDefinition,nodeName)) => StartStopInstance(p.getRef.asInstanceOf[Instance], nodeName,false)
      case JavaSePrimitive.StopInstance => StopInstanceCommand(p.getRef.asInstanceOf[Instance], nodeName)

      case JavaSePrimitive.StartInstance if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[Instance].getTypeDefinition,nodeName)) => StartStopInstance(p.getRef.asInstanceOf[Instance], nodeName,true)
      case JavaSePrimitive.StartInstance => StartInstanceCommand(p.getRef.asInstanceOf[Instance], nodeName)



      //  case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
      case JavaSePrimitive.AddBinding => AddBindingCommand(p.getRef.asInstanceOf[MBinding], nodeName)
      case JavaSePrimitive.RemoveBinding => RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], nodeName)
      case JavaSePrimitive.AddFragmentBinding => AddFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, nodeName)
      case JavaSePrimitive.RemoveFragmentBinding => RemoveFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, nodeName)


      case JavaSePrimitive.StartThirdParty if (JCLHelper.isJCLManaged(p.getRef.asInstanceOf[DeployUnit])) => NoopCommand()
      case JavaSePrimitive.StartThirdParty => StartThirdPartyCommand(p.getRef.asInstanceOf[DeployUnit], nodeName)
      case _@name => {
        logger.error("Unknown Kevoree adaptation primitive " + name);
        null
      }
    }
    //  }
    // case None => logger.error("Unknown Kevoree adaptation primitive type "); null
    // }


  }


}
