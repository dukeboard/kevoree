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
package org.kevoree.tools.aether.framework

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

import java.io.File
import org.slf4j.LoggerFactory
import org.kevoree.DeployUnit
import actors.DaemonActor
import scala.collection.JavaConversions._
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler
import org.kevoree.kcl.KevoreeJarClassLoader
import scala.Predef._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 14:29
 */

class JCLContextHandler extends DaemonActor with KevoreeClassLoaderHandler {

  protected val kcl_cache = new java.util.HashMap[String, KevoreeJarClassLoader]()
  protected val kcl_cache_file = new java.util.HashMap[String, File]()
  protected var lockedDu: List[String] = List()
  protected val logger = LoggerFactory.getLogger(this.getClass)

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


  protected def clearInternals() {
    logger.debug("Clear Internal")
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
    /*
    if (logger.isDebugEnabled) {
      logger.debug("-----------------------------DUMP after clear-------------------------")
      printDumpInternals()
      logger.debug("-----------------------------END DUMP after clear-------------------------")
    }*/
  }

  protected def getCacheFileInternals(du: DeployUnit): File = {
    kcl_cache_file.get(buildKEY(du))
  }

  private def manuallyAddToCacheInternals(du: DeployUnit, kcl: KevoreeJarClassLoader) {
    kcl_cache.put(buildKEY(du), kcl)
    lockedDu = lockedDu ++ List(buildKEY(du))
    // kcl_cache_file.put(buildKEY(du), f)
  }

  protected def printDumpInternals() {
    logger.debug("------------------ KCL Dump -----------------------")
    kcl_cache.foreach {
      k =>
        logger.debug("Dump = {}", k._1)
        k._2.printDump()
    }
    logger.debug("================== End KCL Dump ===================")
  }


  /* Temp Zone for temporary unresolved KCL links */
  protected val failedLinks = new java.util.HashMap[String, KevoreeJarClassLoader]()

  def clearFailedLinks() {
    failedLinks.clear()
  }

  protected def installDeployUnitInternals(du: DeployUnit, file: File): KevoreeJarClassLoader = {
    val previousKCL = getKCLInternals(du)
    val res = if (previousKCL != null) {
      logger.debug("Take already installed {}", buildKEY(du))
      previousKCL
    } else {
      logger.debug("Install {} , file {}", buildKEY(du), file)
      val newcl = new KevoreeJarClassLoader

      //if (du.getVersion.contains("SNAPSHOT")) {
      newcl.setLazyLoad(false)
      // }

      newcl.add(file.getAbsolutePath)
      kcl_cache.put(buildKEY(du), newcl)
      kcl_cache_file.put(buildKEY(du), file)
      logger.debug("Add KCL for {}->{}", du.getUnitName, buildKEY(du))

      //TRY TO RECOVER FAILED LINK
      if (failedLinks.containsKey(buildKEY(du))) {
        failedLinks.get(buildKEY(du)).addSubClassLoader(newcl)
        newcl.addWeakClassLoader(failedLinks.get(buildKEY(du)))
        failedLinks.remove(buildKEY(du))
        logger.debug("Failed Link {} remain size : {}", du.getUnitName, failedLinks.size())
      }

      du.getRequiredLibs.foreach {
        rLib =>
          val kcl = getKCLInternals(rLib)
          if (kcl != null) {
            logger.debug("Link KCL for {}->{}", du.getUnitName, rLib.getUnitName)
            newcl.addSubClassLoader(kcl)
            kcl.addWeakClassLoader(newcl)

            du.getRequiredLibs.filter(rLibIn => rLib != rLibIn).foreach(rLibIn => {
              val kcl2 = getKCLInternals(rLibIn)
              if (kcl2 != null) {
                kcl.addWeakClassLoader(kcl2)
                // logger.debug("Link Weak for {}->{}", rLib.getUnitName, rLibIn.getUnitName)
              }
            })
          } else {
            logger.debug("Fail link ! Warning ")
            failedLinks.put(buildKEY(du), newcl)
          }
      }


      newcl
    }
    /*
    if (logger.isDebugEnabled) {
      printDumpInternals()
    }*/
    res
  }

  protected def getKCLInternals(du: DeployUnit): KevoreeJarClassLoader = {
    kcl_cache.get(buildKEY(du))
  }

  protected def removeDeployUnitInternals(du: DeployUnit) {

    // println("DEBUG REMOVE DU "+du.getUnitName)

    val key = buildKEY(du)
    if (failedLinks.containsKey(key)) {
      failedLinks.remove(key)
    }
    if (!lockedDu.contains(key)) {
      val kcl_to_remove = kcl_cache.get(key)
      failedLinks.filter(fl => fl._2 == kcl_to_remove).toList.foreach {
        k =>
          failedLinks.remove(k._1)
      }
      if (!lockedDu.contains(key)) {
        if (kcl_cache.containsKey(key)) {
          logger.debug("Try to remove KCL for {}->{}", du.getUnitName, buildKEY(du))
          logger.debug("Cache To cleanuip size" + kcl_cache.values().size() + "-" + kcl_cache.size() + "-" + kcl_cache.keySet().size())
          kcl_cache.values().foreach {
            vals => {
              if (vals.getSubClassLoaders().contains(kcl_to_remove)) {
                failedLinks.put(key, vals)
                logger.debug("Pending Fail link " + key)
              }
              vals.cleanupLinks(kcl_to_remove)
              logger.debug("Cleanup {} from {}", vals.toString(), du.getUnitName)
            }
          }

          val toRemoveKCL = kcl_cache.get(key)

          //GET
          var rootGroup = Thread.currentThread().getThreadGroup
          var parentGroup: ThreadGroup = null
          while ( {
            parentGroup = rootGroup.getParent; parentGroup
          } != null) {
            rootGroup = parentGroup
          }
          val numThreads = rootGroup.activeCount()
          val listOfThreads = new Array[Thread](numThreads)
          rootGroup.enumerate(listOfThreads)
          for (i <- 0 until numThreads) {
            val tloop = listOfThreads(i)
            if (tloop.getContextClassLoader == toRemoveKCL) {
              logger.warn("Change Thread " + tloop.getName + " currentClassLoader to avoid memory leak")
              tloop.setContextClassLoader(getClass.getClassLoader)
            }

          }
          toRemoveKCL.unload()
          kcl_cache.remove(key)
        }
        if (kcl_cache_file.containsKey(key)) {
          logger.debug("Cleanup Cache File" + kcl_cache_file.get(key).getAbsolutePath)
          kcl_cache_file.get(key).delete()
          kcl_cache_file.remove(key)
          logger.debug("Remove File Cache " + key)
        }
      }
    }
  }


  protected def buildKEY(du: DeployUnit): String = {
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
        buffer.append("KCL KEY name=" + k._1 + "\n")
        buffer.append(k._2.getKCLDump + "\n")
    }

    buffer.toString
  }


}