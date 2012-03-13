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
package org.kevoree.tools.aether.framework.android

import org.kevoree.DeployUnit
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler
import actors.DaemonActor
import java.io.File
import org.kevoree.kcl.KevoreeJarClassLoader
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:50
 */

class AndroidJCLContextHandler(ctx: android.content.Context, parent: ClassLoader) extends DaemonActor with KevoreeClassLoaderHandler {
  private val kcl_cache = new java.util.HashMap[String, KevoreeJarClassLoader]()
  private val kcl_cache_file = new java.util.HashMap[String, File]()
  private var lockedDu: List[String] = List()
  private val logger = LoggerFactory.getLogger(this.getClass)

  start()

  case class DUMP()

  case class INSTALL_DEPLOYUNIT_FILE(du: DeployUnit, file: File)

  case class INSTALL_DEPLOYUNIT(du: DeployUnit)

  case class REMOVE_DEPLOYUNIT(du: DeployUnit)

  case class GET_KCL(du: DeployUnit)

  case class MANUALLY_ADD_TO_CACHE(du: DeployUnit, kcl: KevoreeJarClassLoader)

  case class GET_CACHE_FILE(du: DeployUnit)

  case class CLEAR()

  case class KILLActor()

  def stop() {
    this ! KILLActor()
  }

  def act() {
    loop {
      react {
        case GET_CACHE_FILE(du) => reply(getCacheFileInternals(du))
        case INSTALL_DEPLOYUNIT_FILE(du, file) => reply(installDeployUnitInternals(du, file))
        case INSTALL_DEPLOYUNIT(du) => reply(installDeployUnitNoFileInternals(du))
        case GET_KCL(du) => reply(getKCLInternals(du))
        case REMOVE_DEPLOYUNIT(du) => removeDeployUnitInternals(du)
        case MANUALLY_ADD_TO_CACHE(du, kcl) => manuallyAddToCacheInternals(du, kcl)
        case DUMP() => printDumpInternals()
        case CLEAR() => clearInternals(); reply()
        case KILLActor() => exit()
      }
    }
  }

  def clear() {
    this !? CLEAR()
  }

  def getCacheFile(du: DeployUnit): File = {
    (this !? GET_CACHE_FILE(du)).asInstanceOf[File]
  }

  def manuallyAddToCache(du: DeployUnit, kcl: KevoreeJarClassLoader) {
    this ! MANUALLY_ADD_TO_CACHE(du, kcl)
  }

  def printDump() {
    this ! DUMP()
  }


  private def clearInternals() {
    kcl_cache.keySet().toList.foreach {
      key =>
        if (!lockedDu.contains(key)) {
          if (kcl_cache.containsKey(key)) {
            logger.debug("Remove KCL for {}", key)
            kcl_cache.get(key).unload()
            kcl_cache.remove(key)
          }
          if (kcl_cache_file.containsKey(key)) {
            kcl_cache_file.remove(key)
          }
        }
    }
    if (logger.isDebugEnabled) {
      logger.debug("-----------------------------DUMP after clear-------------------------")
      printDumpInternals()
      logger.debug("-----------------------------END DUMP after clear-------------------------")
    }
  }

  private def getCacheFileInternals(du: DeployUnit): File = {
    kcl_cache_file.get(buildKEY(du))
  }

  private def manuallyAddToCacheInternals(du: DeployUnit, kcl: KevoreeJarClassLoader) {
    kcl_cache.put(buildKEY(du), kcl)
    lockedDu = lockedDu ++ List(buildKEY(du))
    // kcl_cache_file.put(buildKEY(du), f)
  }

  private def printDumpInternals() {
    logger.debug("------------------ KCL Dump -----------------------")
    kcl_cache.foreach {
      k =>
        logger.debug("Dump = {}", k._1)
        k._2.printDump()
    }
    logger.debug("================== End KCL Dump ===================")
  }

  private def installDeployUnitInternals(du: DeployUnit, file: File): KevoreeJarClassLoader = {
    val previousKCL = getKCLInternals(du)
    val res = if (previousKCL != null) {
      logger.debug("Take already installed {}", buildKEY(du))
      previousKCL
    } else {
      logger.debug("Install {} , file {}", buildKEY(du), file)
      val cleankey = buildKEY(du).replace(File.separator, "_")
      val newcl = new AndroidKevoreeJarClassLoader(cleankey, ctx, parent)
      if (du.getVersion.contains("SNAPSHOT")) {
        newcl.setLazyLoad(false)
      }
      logger.debug("Before add the jar {} to classloader",file.getAbsolutePath)
      try {
        newcl.add(file.getAbsolutePath)
      } catch {
        case _ @ e => {
          logger.error("Can't add Jar to class path",e)
          return null
        }
      }
      logger.debug("After Adding the jar ")
      kcl_cache.put(buildKEY(du), newcl)
      kcl_cache_file.put(buildKEY(du), file)
      logger.debug("Add KCL for {}->{}", du.getUnitName, buildKEY(du))

      du.getRequiredLibs.foreach {
        rLib =>
          val kcl = getKCLInternals(rLib)
          if (kcl != null) {
            logger.debug("Link KCL for {}->{}", du.getUnitName, rLib.getUnitName)
            newcl.addSubClassLoader(kcl)

            du.getRequiredLibs.filter(rLibIn => rLib != rLibIn).foreach(rLibIn => {
              val kcl2 = getKCLInternals(rLibIn)
              if (kcl2 != null) {
                kcl.addWeakClassLoader(kcl2)
                logger.debug("Link Weak for {}->{}", rLib.getUnitName, rLibIn.getUnitName)
              }
            })


          }
      }
      newcl
    }
    if (logger.isDebugEnabled) {
      printDumpInternals()
    }
    res
  }

  private def getKCLInternals(du: DeployUnit): KevoreeJarClassLoader = {
    kcl_cache.get(buildKEY(du))
  }

  private val failedLinks = new java.util.HashMap[DeployUnit, KevoreeJarClassLoader]()

  def clearFailedLinks() {
    failedLinks.clear()
  }

  private def removeDeployUnitInternals(du: DeployUnit) {
    if (failedLinks.containsKey(du)) {
      failedLinks.remove(du)
    }

    val key = buildKEY(du)
    val kcl_to_remove = kcl_cache.get(key)
    if (!lockedDu.contains(key)) {
      if (kcl_cache.containsKey(key)) {
        logger.debug("Remove KCL for {}->{}", du.getUnitName, buildKEY(du))
        //logger.debug("Cache To cleanuip size"+kcl_cache.values().size()+"-"+kcl_cache.size()+"-"+kcl_cache.keySet().size())
        kcl_cache.values().foreach {
          vals => {
            vals.cleanupLinks(kcl_to_remove)
            //logger.debug("Cleanup {} from {}",vals.toString(),du.getUnitName)
          }
        }
        kcl_cache.get(key).unload()
        kcl_cache.remove(key)
      }
      if (kcl_cache_file.containsKey(key)) {
        kcl_cache_file.remove(key)
      }
    }
  }


  private def buildKEY(du: DeployUnit): String = {
    du.getName + "/" + buildQuery(du, None)
  }

  private def buildQuery(du: DeployUnit, repoUrl: Option[String]): String = {
    val query = new StringBuilder
    query.append("mvn:")
    repoUrl match {
      case Some(r) => query.append(r); query.append("!")
      case None =>
    }
    query.append(du.getGroupName)
    query.append("/")
    query.append(du.getUnitName)
    du.getVersion match {
      case "default" =>
      case "" =>
      case _ => query.append("/"); query.append(du.getVersion)
    }
    query.toString()
  }


  def installDeployUnit(du: DeployUnit, file: File): KevoreeJarClassLoader = {
    (this !? INSTALL_DEPLOYUNIT_FILE(du, file)).asInstanceOf[KevoreeJarClassLoader]
  }

  def getKevoreeClassLoader(du: DeployUnit): KevoreeJarClassLoader = {
    (this !? GET_KCL(du)).asInstanceOf[KevoreeJarClassLoader]
  }

  def removeDeployUnitClassLoader(du: DeployUnit) {
    this ! REMOVE_DEPLOYUNIT(du)
  }

  def installDeployUnitNoFileInternals(du: DeployUnit): KevoreeJarClassLoader = {
    val resolvedFile = AetherUtil.resolveDeployUnit(du)
    if (resolvedFile != null) {
      installDeployUnitInternals(du, resolvedFile)
    } else {
      logger.error("Error while resolving deploy unit " + du.getUnitName)
      null
    }
  }

  def installDeployUnit(du: DeployUnit): KevoreeJarClassLoader = {
    (this !? INSTALL_DEPLOYUNIT(du)).asInstanceOf[KevoreeJarClassLoader]
  }

  def getKCLDump: String = {
    val buffer = new StringBuffer
    kcl_cache.foreach {
      k =>
        buffer.append("KCL KEY name=" + k._1.substring(0, k._1.indexOf("/")) + "\n")
        buffer.append(k._2.getKCLDump + "\n")
    }

    buffer.toString
  }

}