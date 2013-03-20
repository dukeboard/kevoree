package org.kevoree.kompare

import org.kevoree.*
import org.kevoreeAdaptation.*
import org.kevoree.kompare.sub.Kompare2
import org.kevoree.impl.DefaultKevoreeFactory
import org.slf4j.LoggerFactory

class KevoreeKompareBean: Kompare2, KevoreeScheduler {

    val logger = LoggerFactory.getLogger(this.javaClass)!!

    override var adaptationModelFactory: KevoreeAdaptationFactory = org.kevoreeAdaptation.impl.DefaultKevoreeAdaptationFactory()

    fun kompare(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {

        val factory = DefaultKevoreeFactory()
        val adaptationModelFactory = org.kevoreeAdaptation.impl.DefaultKevoreeAdaptationFactory()
        val adaptationModel = adaptationModelFactory.createAdaptationModel()
        //STEP 0 - FOUND LOCAL NODE
        var actualLocalNode = actualModel.findByPath("nodes[" + nodeName + "]", javaClass<ContainerNode>())
        var updateLocalNode = targetModel.findByPath("nodes[" + nodeName + "]", javaClass<ContainerNode>())

        if(actualLocalNode == null && updateLocalNode == null){
            logger.warn("Empty Kompare because "+nodeName+" not found in current nor in target model")
            return adaptationModel
        }
        var dropActualNode = false
        var dropNewNode = false

        //case empty Model
        if(actualLocalNode == null){
            actualLocalNode = factory.createContainerNode()
            actualLocalNode!!.setName(nodeName)
            actualModel.addNodes(actualLocalNode!!)
            dropActualNode = true
            actualLocalNode!!.setTypeDefinition(updateLocalNode!!.getTypeDefinition())

        }
        //case empty Model

        if(updateLocalNode == null){
            updateLocalNode = factory.createContainerNode()
            updateLocalNode!!.setName(nodeName)
            targetModel.addNodes(updateLocalNode!!)
            dropNewNode = true
            updateLocalNode!!.setTypeDefinition(actualLocalNode!!.getTypeDefinition())

        }

        val currentAdaptModel = getUpdateNodeAdaptationModel(actualLocalNode!!, updateLocalNode!!)

        if(dropActualNode){
            actualModel.removeNodes(actualLocalNode!!)
        }
        if(dropNewNode){
            targetModel.removeNodes(updateLocalNode!!)
        }

        //TRANSFORME UPDATE
        for(adaptation in currentAdaptModel.getAdaptations()){
            when(adaptation.getPrimitiveType()!!.getName()) {
                JavaSePrimitive.UpdateType -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType))
                    rcmd.setRef(adaptation.getRef())
                    currentAdaptModel.removeAdaptations(adaptation)
                    currentAdaptModel.addAdaptations(rcmd)
                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType))
                    acmd.setRef(adaptation.getRef())
                    currentAdaptModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding))
                    rcmd.setRef(adaptation.getRef())
                    currentAdaptModel.removeAdaptations(adaptation)
                    currentAdaptModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding))
                    acmd.setRef(adaptation.getRef())
                    currentAdaptModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateFragmentBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding))
                    rcmd.setRef(adaptation.getRef())
                    rcmd.setTargetNodeName(adaptation.getTargetNodeName())
                    currentAdaptModel.removeAdaptations(adaptation)
                    currentAdaptModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding))
                    acmd.setRef(adaptation.getRef())
                    acmd.setTargetNodeName(adaptation.getTargetNodeName())
                    currentAdaptModel.addAdaptations(acmd)
                }

                JavaSePrimitive.UpdateInstance -> {
                    val stopcmd = adaptationModelFactory.createAdaptationPrimitive()
                    stopcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance))
                    stopcmd.setRef( (adaptation.getRef() as Array<Any>).get(0))
                    currentAdaptModel.removeAdaptations(adaptation)
                    currentAdaptModel.addAdaptations(stopcmd)

                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance))
                    rcmd.setRef( (adaptation.getRef() as Array<Any>).get(0))
                    currentAdaptModel.removeAdaptations(adaptation)
                    currentAdaptModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance))
                    acmd.setRef( (adaptation.getRef() as Array<Any>).get(1))
                    currentAdaptModel.addAdaptations(acmd)

                    val uDiccmd = adaptationModelFactory.createAdaptationPrimitive()
                    uDiccmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance))
                    uDiccmd.setRef( (adaptation.getRef() as Array<Any>).get(1))
                    currentAdaptModel.addAdaptations(uDiccmd)

                    val startcmd = adaptationModelFactory.createAdaptationPrimitive()
                    startcmd.setPrimitiveType(actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance))
                    startcmd.setRef( (adaptation.getRef() as Array<Any>).get(1))
                    currentAdaptModel.addAdaptations(startcmd)

                }
                else -> {
                }
            }
        }

        //logger.debug("after Hara Kiri detect")
        val afterPlan = plan(currentAdaptModel, nodeName)
        return afterPlan
    }

}
