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

package org.kevoree.kompare.sub

import org.kevoree._
import kompare.JavaSePrimitive
import org.kevoreeAdaptation._
import org.kevoree.framework.aspects.KevoreeAspects._

trait UpdateNodeKompare extends AbstractKompare with UpdateChannelKompare {

  def getUpdateNodeAdaptationModel(actualNode: ContainerNode, updateNode: ContainerNode): AdaptationModel = {
    val adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel
    logger.debug("UPDATE NODE " + actualNode.getName)

    val actualRoot = actualNode.eContainer.asInstanceOf[ContainerRoot]
    val updateRoot = updateNode.eContainer.asInstanceOf[ContainerRoot]

    //Update Type Step
    updateNode.getUsedTypeDefinition.foreach {
      uct =>
        actualNode.getUsedTypeDefinition.find({
          act => act.isModelEquals(uct)
        }) match {
          case Some(ct) => {
            //CHECK IF TYPE IS UPDATE
            if (ct.isUpdated(uct)) {
              val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

              adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateType,
                actualNode.eContainer.asInstanceOf[ContainerRoot]))

              adaptcmd.setRef(uct)
              adaptationModel.addAdaptations(adaptcmd)

              //ADD UPDATE DEPLOY UNIT IF NECESSARY
              val uctDeployUnit = uct.foundRelevantDeployUnit(updateNode)

              if (uctDeployUnit != null) {
                adaptationModel.getAdaptations
                  .filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.UpdateDeployUnit)
                  .find(adaptation => adaptation.getRef.asInstanceOf[DeployUnit].isModelEquals(uctDeployUnit)) match {
                  case None => {
                    val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

                    ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDeployUnit,
                      actualNode.eContainer.asInstanceOf[ContainerRoot]))

                    ctcmd.setRef(uctDeployUnit)
                    adaptationModel.addAdaptations(ctcmd)
                  }
                  case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                }
              } else {
                throw new Exception("Deploy Unit not found for " + uct.getName + " from " + updateNode.getName)
              }

            }
          }
          case None => {
            //ADD TYPE
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

            ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddType,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ctcmd.setRef(uct)
            adaptationModel.addAdaptations(ctcmd)

            /* add deploy unit if necessary */
            //CHECK IF A PREVIOUS INSTALLED TYPE DEFINITION USE THIS DEPLOY UNIT

            val uctDeployUnit = uct.foundRelevantDeployUnit(updateNode)

            actualNode.getUsedTypeDefinition
              .find(typeDef => typeDef.foundRelevantDeployUnit(actualNode).isModelEquals(uctDeployUnit)) match {
              case None => {
                //CHECK IF THIS DEPLOY UNIT IS ALREADY MARK AS TO BE INSTALLED
                adaptationModel.getAdaptations
                  .filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit
                  || adaptation.getPrimitiveType.getName == JavaSePrimitive.AddThirdParty)
                  .find(adaptation => adaptation.getRef.asInstanceOf[DeployUnit].isModelEquals(uctDeployUnit)) match {
                  case None => {
                    val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
                    ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddDeployUnit,
                      actualNode.eContainer.asInstanceOf[ContainerRoot]))
                    ctcmd.setRef(uctDeployUnit)
                    adaptationModel.addAdaptations(ctcmd)
                  }
                  case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                }
              }
              case Some(_) => // TYPE DEFINITION ALREADY USE DEPLOY UNIT IN PREVIOUS MODEL
            }


            //ADD USED THIRDPARTY
            uctDeployUnit.getRequiredLibs.foreach {
              tp =>
              //CHECK IF THIS DEPLOY UNIT IS ALREADY MARK AS TO BE INSTALLED
                adaptationModel.getAdaptations
                  .filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.AddThirdParty
                  || adaptation.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit)
                  .find(adaptation => adaptation.getRef.asInstanceOf[DeployUnit].isModelEquals(tp)) match {
                  case None => {
                    val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
                    adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddThirdParty,
                      actualNode.eContainer
                        .asInstanceOf[ContainerRoot]))
                    adaptcmd.setRef(tp)
                    adaptationModel.addAdaptations(adaptcmd)
                  }
                  case Some(e) => {
                    //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                    if (e.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit) {
                      e.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddThirdParty,
                        actualNode.eContainer
                          .asInstanceOf[ContainerRoot]))
                    }
                  }
                }

            }
          }
        }
    }

    actualNode.getUsedTypeDefinition.foreach {
      act =>
        updateNode.getUsedTypeDefinition.find({
          uct => uct.isModelEquals(act)
        }) match {
          case Some(ct) => //OK CHECK ALEADY DONE IN PREVIOUS STEP
          case None => {

            //Remove TYPE
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

            ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveType,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))

            ctcmd.setRef(act)
            adaptationModel.addAdaptations(ctcmd)

            val actualDeployU = act.foundRelevantDeployUnit(actualNode)

            /* remove deploy unit if necessary */
            updateNode.getUsedTypeDefinition.find(typeDef => (typeDef != act) &&
              typeDef.foundRelevantDeployUnit(updateNode).isModelEquals(actualDeployU)) match {
              case Some(_) => // DO NOT UNINSTALL DEPLOY UNIT
              case None => {
                adaptationModel.getAdaptations
                  .filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.RemoveDeployUnit)
                  .find(adaptation => adaptation.getRef.asInstanceOf[DeployUnit].isModelEquals(actualDeployU)) match {
                  case None => {
                    val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
                    ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveDeployUnit,
                      actualNode.eContainer.asInstanceOf[ContainerRoot]))
                    ctcmd.setRef(actualDeployU)
                    adaptationModel.addAdaptations(ctcmd)

                    //TRY TO CLEANUP UNUSED DU
                    def recCleanUp(du: DeployUnit) {
                      if (!updateNode.isDeployUnitUsed(du)) {
                        adaptationModel.getAdaptations
                          .filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.RemoveDeployUnit)
                          .find(adaptation => adaptation.getRef.asInstanceOf[DeployUnit].isModelEquals(du)) match {
                          case None => {
                            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
                            ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveDeployUnit,
                              actualNode.eContainer.asInstanceOf[ContainerRoot]))
                            ctcmd.setRef(du)
                            adaptationModel.addAdaptations(ctcmd)
                            du.getRequiredLibs.foreach(rlib => recCleanUp(rlib))
                          }
                          case _ => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                        }
                      }
                    }

                    actualDeployU.getRequiredLibs.foreach(rlib => recCleanUp(rlib))


                  }
                  case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                }

              }
            }



            //TODO REMOVE THIRDPARTY

          }
        }
    }

    //SELF DICTIONARY CHECK
    if (updateNode.getDictionary.getOrElse(KevoreeFactory.createDictionary)
      .isUpdated(actualNode.getDictionary.getOrElse(KevoreeFactory.createDictionary))) {
      val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
      adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance,
        actualNode.eContainer.asInstanceOf[ContainerRoot]))
      adaptcmd.setRef(updateNode)
      adaptationModel.addAdaptations(adaptcmd)
    }

    //INSTANCE STEP
    updateNode.getInstances.foreach {
      uc =>
        actualNode.getInstances.find({
          ac => ac.isModelEquals(uc)
        }) match {
          case Some(c) => {

            // println("check instance "+c)
            //CHECK IF INSTANCE TYPE DEFINITION IS NOT UPDATED
            if (c.getTypeDefinition.isUpdated(uc.getTypeDefinition)) {
              val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
              adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateInstance,
                actualNode.eContainer.asInstanceOf[ContainerRoot]))
              adaptcmd.setRef((c, uc))
              adaptationModel.addAdaptations(adaptcmd)


              //UPDATE BINDING IF NECESSARY
              uc match {
                case i: ComponentInstance => {
                  i.getRelatedBindings.foreach(b => {
                    adaptationModel.getAdaptations
                      .filter(p => p.getPrimitiveType.getName == JavaSePrimitive.UpdateBinding)
                      .find(adaptation => adaptation.getRef.asInstanceOf[MBinding].isModelEquals(b)) match {
                      case None => {
                        val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

                        adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateBinding,
                          actualNode.eContainer
                            .asInstanceOf[ContainerRoot]))

                        adaptcmd.setRef(b)
                        adaptationModel.addAdaptations(adaptcmd)
                      }
                      case Some(e) => //UPDATE BINDING ALREADY RESGISTERED
                    }
                  })
                }
                case i: Channel => {
                  i.getRelatedBindings.foreach {
                    b =>

                      if (b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == updateNode.getName) {
                        adaptationModel.getAdaptations
                          .filter(p => p.getPrimitiveType.getName == JavaSePrimitive.UpdateBinding)
                          .find(adaptation => adaptation.getRef.asInstanceOf[MBinding].isModelEquals(b)) match {
                          case None => {
                            val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

                            adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateBinding,
                              actualNode.eContainer
                                .asInstanceOf[ContainerRoot]))

                            adaptcmd.setRef(b)
                            adaptationModel.addAdaptations(adaptcmd)
                          }
                          case Some(e) => //UPDATE BINDING ALREADY RESGISTERED
                        }
                      } else {
                        adaptationModel.getAdaptations
                          .filter(p => p.getPrimitiveType.getName == JavaSePrimitive.UpdateFragmentBinding)
                          .find(adaptation => adaptation.getRef.asInstanceOf[Channel].isModelEquals(b.getHub)) match {
                          case None => {
                            val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

                            adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateFragmentBinding,
                              actualNode.eContainer
                                .asInstanceOf[ContainerRoot]))

                            adaptcmd.setRef(b.getHub)
                            adaptcmd
                              .setTargetNodeName(b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
                            adaptationModel.addAdaptations(adaptcmd)
                          }
                          case Some(e) => //UPDATE BINDING ALREADY RESGISTERED
                        }
                      }


                  }
                }
                case _ =>
              }


            } else {
              //CHECK IS DICTIONARY IS UPDATED
              if (uc.getDictionary.getOrElse(KevoreeFactory.createDictionary)
                .isUpdated(c.getDictionary.getOrElse(KevoreeFactory.createDictionary))) {
                val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
                adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance,
                  actualNode.eContainer.asInstanceOf[ContainerRoot]))
                adaptcmd.setRef(uc)
                adaptationModel.addAdaptations(adaptcmd)
              } else {
                /* SPECIAL CASE UPDATE GROUP BINDING AS DICTIONARY */
                if (uc.isInstanceOf[Group] && c.isInstanceOf[Group]) {
                  val previousGroup = c.asInstanceOf[Group]
                  val updateGroup = uc.asInstanceOf[Group]

                  var modified = false
                  if (previousGroup.getSubNodes.size != updateGroup.getSubNodes.size) {
                    modified = true
                  }
                  //DEEP CHECK
                  if (!modified) {
                    if (!previousGroup.getSubNodes
                      .forall(sub => updateGroup.getSubNodes.exists(usub => usub.getName == sub.getName))) {
                      modified = true
                    }
                    if (!updateGroup.getSubNodes
                      .forall(sub => previousGroup.getSubNodes.exists(usub => usub.getName == sub.getName))) {
                      modified = true
                    }
                  }

                  if (modified) {
                    val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive

                    adaptcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance,
                      actualNode.eContainer
                        .asInstanceOf[ContainerRoot]))
                    adaptcmd.setRef(uc)
                    adaptationModel.addAdaptations(adaptcmd)
                  }


                }

              }

            }
          }
          case None => {
            val ccmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddInstance,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ccmd.setRef(uc)
            adaptationModel.addAdaptations(ccmd)

            val ccmd2 = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ccmd2.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StartInstance,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ccmd2.setRef(uc)
            adaptationModel.addAdaptations(ccmd2)

            val ccmd3 = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ccmd3.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ccmd3.setRef(uc)
            adaptationModel.addAdaptations(ccmd3)
          }
        }
    }
    actualNode.getInstances.foreach {
      ac =>
        updateNode.getInstances.find({
          uc => uc.isModelEquals(ac)
        }) match {
          case Some(c) => //OK , CASE ALREADY PROCESS BY PREVIOUS STEP
          case None => {
            val ccmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveInstance,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ccmd.setRef(ac)
            adaptationModel.addAdaptations(ccmd)

            val ccmd2 = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ccmd2.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StopInstance,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ccmd2.setRef(ac)
            adaptationModel.addAdaptations(ccmd2)

          }
        }
    }

    //Binding Step
    updateRoot.getMBindings
      .filter(mb =>
      mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName ==
        actualNode.getName)
      .foreach {
      uct =>
        actualRoot.getMBindings
          .filter(mb =>
          mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName ==
            updateNode.getName)
          .find({
          act => act.isModelEquals(uct)
        }) match {
          case Some(ct) => //OK
          case None => {
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddBinding,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ctcmd.setRef(uct)
            adaptationModel.addAdaptations(ctcmd)
          }
        }
    }
    actualRoot.getMBindings
      .filter(mb =>
      mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName ==
        actualNode.getName)
      .foreach {
      act =>
        updateRoot.getMBindings
          .filter(mb =>
          mb.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerNode].getName ==
            updateNode.getName)
          .find({
          uct => uct.isModelEquals(act)
        }) match {
          case Some(ct) => //OK
          case None => {
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveBinding,
              actualNode.eContainer.asInstanceOf[ContainerRoot]))
            ctcmd.setRef(act)
            adaptationModel.addAdaptations(ctcmd)
          }
        }
    }




    //FRAGMENT BINDING STEP
    //ONLY CHECK FOR HUB NO UNINSTALL
    updateRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName)).foreach {
      newhub =>
        actualRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName))
          .find(hub => newhub.getName == hub.getName) match {
          case None => {
            //NEW HUB INIT BINDING
            newhub.getOtherFragment(updateNode.getName).foreach {
              remoteName =>
                val addccmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
                addccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddFragmentBinding,
                  actualNode.eContainer.asInstanceOf[ContainerRoot]))
                addccmd.setRef(newhub)
                addccmd.setTargetNodeName(remoteName)
                adaptationModel.addAdaptations(addccmd)
            }
          }

          case Some(previousHub) => {
            //adaptationModel.addAllAdaptations(getUpdateChannelAdaptationModel(previousHub, newhub, updateNode.getName).getAdaptations)
          }
        }
    }
    actualRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName)).foreach {
      actualHub =>
        updateRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName))
          .find(hub => actualHub.getName == hub.getName) match {
          case None => // NOTHING TO DO HUB WILL BE UNINSTALL, NO UNBIND IS NECESSARY
          case Some(updateHub) => {
            //CHECK AND UPDATE MBINDING
            adaptationModel.addAllAdaptations(getUpdateChannelAdaptationModel(actualHub, updateHub, updateNode.getName)
              .getAdaptations)
          }
        }
    }


    //GROUP STEP
    /*
           updateRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).foreach {
           newgroup =>
           actualRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).find(group => newgroup.getName == group.getName) match {
           case None => {
           //NEW GROUP INIT BINDING
           val addgroup = KevoreeAdaptationFactory.eINSTANCE.createAddInstance
           addgroup.setRef(newgroup)
           adaptationModel.getAdaptations.add(addgroup)
           }

           case Some(previousGroup) => {
           //SEARCH SOME MODIFICATION

           // adaptationModel.addAllAdaptations(getUpdateChannelAdaptationModel(previousHub, newhub, updateNode.getName).getAdaptations)
           }
           }
           }
           actualRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).foreach {
           newGroup =>
           updateRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).find(group => newGroup.getName == group.getName) match {
           case None => // NOTHING TO DO HUB WILL BE UNINSTALL, NO UNBIND IS NECESSARY
           case Some(previousHub) => {
           //CHECK AND UPDATE MBINDING
           adaptationModel.addAllAdaptations(getUpdateChannelAdaptationModel(previousHub, newhub, updateNode.getName).getAdaptations)
           }
           }
           } */



    adaptationModel
  }

}
