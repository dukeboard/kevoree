package org.kevoree.core.impl

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.ContainerRoot
import org.kevoree.cloner.ModelCloner
import java.util.Date
import org.kevoree.core.basechecker.RootChecker
import java.util.concurrent.ExecutorService
import java.util.UUID
import java.util.ArrayList
import org.kevoree.api.Bootstraper
import java.util.concurrent.Callable
import org.kevoree.api.service.core.handler.UUIDModel
import org.kevoree.api.service.core.handler.ModelUpdateCallback
import org.kevoree.api.service.core.handler.ModelUpdateCallBackReturn
import org.kevoree.api.service.core.handler.ModelHandlerLockCallBack
import org.kevoree.ContainerNode
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.kevoree.api.service.core.handler.KevoreeModelUpdateException
import org.kevoree.api.service.core.handler.ModelListener
import org.kevoree.context.ContextRoot
import org.kevoree.framework.HaraKiriHelper
import org.kevoree.impl.DefaultKevoreeFactory
import java.util.concurrent.atomic.AtomicReference
import org.kevoree.log.Log

class PreCommand(newmodel: ContainerRoot, modelListeners: KevoreeListeners, oldModel: ContainerRoot){
    var alreadyCall = false
    val preRollbackTest: ()->Boolean = {()->
        if (!alreadyCall) {
            modelListeners.preRollback(oldModel, newmodel)
            alreadyCall = true
        }
        true
    }
}

class KevoreeCoreBean(): KevoreeModelHandlerService {

    val modelListeners = KevoreeListeners()
    var _kevsEngineFactory: KevScriptEngineFactory? = null
    var _bootstraper: Bootstraper? = null
    var _nodeName: String = ""
    var nodeInstance: org.kevoree.api.NodeType? = null
    var models: MutableList<UUIDModel> = ArrayList<UUIDModel>()
    val kevoreeFactory = org.kevoree.impl.DefaultKevoreeFactory()
    val model: AtomicReference<UUIDModel> = AtomicReference<UUIDModel>()
    var lastDate: Date = Date(System.currentTimeMillis())
    val modelCloner = ModelCloner()
    val modelChecker = RootChecker()
    var selfActorPointer = this
    private var scheduler: ExecutorService? = null
    private var lockWatchDog: ScheduledExecutorService? = null
    private var futurWatchDog: ScheduledFuture<out Any?>? = null
    private var currentLock: LockCallBack? = null

    var factory = DefaultKevoreeFactory()

    data class LockCallBack(val uuid: UUID, val callback: ModelHandlerLockCallBack)


    override fun getNodeName(): String {
        return _nodeName
    }

    fun setNodeName(nn: String) {
        _nodeName = nn
    }

    override fun getLastModification(): Date {
        return lastDate
    }

    fun setKevsEngineFactory(k: KevScriptEngineFactory) {
        _kevsEngineFactory = k
    }

    fun setBootstraper(b: Bootstraper) {
        _bootstraper = b
    }

    private fun switchToNewModel(c: ContainerRoot) {
        var cc: ContainerRoot? = c
        if(!c.isReadOnly()){
            Log.error("It is not safe to store ReadWrite model")
            cc = modelCloner.clone(c, true)
        } /*else {
            cc = c
        }*/

        //current model is backed-up
        val previousModel = model.get()
        if(previousModel != null){
            models.add(previousModel)
        }
        // TODO : MAGIC NUMBER ;-) , ONLY KEEP 10 PREVIOUS MODEL
        if (models.size > 15) {
            models = models.drop(5) as MutableList<UUIDModel>
            Log.debug("Garbage old previous model")
        }
        //Changes the current model by the new model
        if(cc != null){
            val uuidModel = UUIDModelImpl(UUID.randomUUID(), cc!!)
            model.set(uuidModel)
            lastDate = Date(System.currentTimeMillis())
            //Fires the update to listeners
            modelListeners.notifyAllListener()
        }
    }

    override fun getLastModel(): ContainerRoot {
        return model.get()!!.getModel()!!
    }

    override fun getLastUUIDModel(): UUIDModel {
        return model.get()!!
    }

    inline fun cloneCurrentModel(pmodel: ContainerRoot?): ContainerRoot {
        return modelCloner.clone(pmodel!!, true)!!
    }

    override fun updateModel(pmodel: ContainerRoot?): Unit {
        scheduler!!.submit(UpdateModelCallable(cloneCurrentModel(pmodel), null))
    }

    inner class UpdateModelCallable(val model: ContainerRoot, val callback: ModelUpdateCallback?): Callable<Boolean> {
        override fun call(): Boolean {
            var res: Boolean = false
            if (currentLock == null) {
                val internalRes = internal_update_model(model)
                callCallBack(callback, internalRes, null)
                res = internalRes
            } else {
                Log.debug("Core Locked , UUID mandatory")
                callCallBack(callback, false, ModelUpdateCallBackReturn.CAS_ERROR)
                res = false
            }
            return res
        }
    }

    fun callCallBack(callback: ModelUpdateCallback?, sucess: Boolean, res: ModelUpdateCallBackReturn?) {
        if (callback != null) {
            object : Thread(){
                override fun run() {
                    if (res == null) {
                        callback?.modelProcessed(if (sucess) {
                            ModelUpdateCallBackReturn.UPDATED
                        } else {
                            ModelUpdateCallBackReturn.DEPLOY_ERROR
                        })
                    } else {
                        callback?.modelProcessed(res)
                    }
                }
            }.start()
        }
    }

    fun start() {
        if (getNodeName() == "") {
            setNodeName("node0")
        }
        modelListeners.start(getNodeName())
        Log.info("Kevoree Start event : node name = {}", getNodeName())
        scheduler = java.util.concurrent.Executors.newSingleThreadExecutor(KevoreeCoreThreadFactory(getNodeName()))
        val uuidModel = UUIDModelImpl(UUID.randomUUID(), factory.createContainerRoot())
        model.set(uuidModel)
    }

    fun stop() {
        Log.warn("Kevoree Core will be stopped !")
        modelListeners.stop()
        scheduler?.shutdownNow()
        scheduler = null
        if (nodeInstance != null) {
            try {
                val modelCurrent = model.get()!!.getModel()!!
                val stopModel = modelCloner.clone(modelCurrent)!!
                val currentNode = stopModel.findNodesByID(getNodeName())!!
                for(childNode in currentNode.getHosts()){
                    childNode.setStarted(false)
                }
                //TEST only stop local
                for(group in stopModel.getGroups()){
                    group.setStarted(false)
                }
                for(hub in stopModel.getHubs()){
                    hub.setStarted(false)
                }
                for(childComponent in currentNode.getComponents()){
                    childComponent.setStarted(false)
                }

                //val stopModel = factory.createContainerRoot()
                val adaptationModel = nodeInstance!!.kompare(modelCurrent, stopModel)
                adaptationModel.setInternalReadOnly()
                val afterUpdateTest: () -> Boolean = {() -> true }
                val rootNode = modelCurrent.findByPath("nodes[" + getNodeName() + "]", javaClass<ContainerNode>())
                if (rootNode != null) {
                    org.kevoree.framework.deploy.PrimitiveCommandExecutionHelper.execute(rootNode, adaptationModel, nodeInstance!!, afterUpdateTest, afterUpdateTest, afterUpdateTest)
                } else {
                    Log.error("Node is not defined into the model so unbootstrap cannot be correctly done")
                }
            } catch (e: Exception) {
                Log.error("Error while unbootstrap ", e)
            }
            try {
                Log.debug("Call instance stop")
                nodeInstance?.stopNode()
                nodeInstance == null
                _bootstraper?.clear()
            } catch(e: Exception) {
                Log.error("Error while stopping node instance ", e)
            }
        }
        Log.debug("Kevoree core stopped ")
    }

    private fun lockTimeout() {
        scheduler?.submit(LockTimeoutCallable())
    }

    inner class LockTimeoutCallable(): Runnable {
        override fun run() {
            if (currentLock != null) {
                currentLock!!.callback.lockTimeout()
                currentLock = null
                lockWatchDog?.shutdownNow()
                lockWatchDog = null
                futurWatchDog = null
            }
        }
    }

    override fun checkModel(tModel: ContainerRoot?): Boolean {
        val checkResult = modelChecker.check(tModel)
        return if (checkResult != null && checkResult.isEmpty()!!) {
            modelListeners.preUpdate(model.get()!!.getModel()!!, cloneCurrentModel(tModel))
        } else {
            false
        }
    }

    override fun updateModel(tmodel: ContainerRoot?, callback: ModelUpdateCallback?): jet.Unit {
        scheduler!!.submit(UpdateModelCallable(cloneCurrentModel(tmodel), callback))
    }

    public override fun updateModel(p0: org.kevoree.ContainerRoot?, p1: ((org.kevoree.api.service.core.handler.ModelUpdateCallBackReturn?) -> jet.Unit)?): jet.Unit {
        throw Exception()
    }


    override fun compareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?, callback: ModelUpdateCallback?) {
        scheduler?.submit(CompareAndSwapCallable(previousModel!!, cloneCurrentModel(targetModel), callback))
    }

    public override fun compareAndSwapModel(p0: org.kevoree.api.service.core.handler.UUIDModel?, p1: org.kevoree.ContainerRoot?, p2: ((org.kevoree.api.service.core.handler.ModelUpdateCallBackReturn?) -> jet.Unit)?): jet.Unit {
        throw Exception()
    }

    inner class AcquireLock(val callBack: ModelHandlerLockCallBack, val timeout: Long): Runnable {
        override fun run() {
            if (currentLock != null) {
                callBack.lockRejected()
            } else {
                val lockUUID = UUID.randomUUID()
                currentLock = LockCallBack(lockUUID, callBack)
                lockWatchDog = java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
                futurWatchDog = lockWatchDog?.schedule(WatchDogCallable(), timeout, TimeUnit.MILLISECONDS)
                callBack.lockAcquired(lockUUID)
            }
        }
    }

    inner class WatchDogCallable(): Runnable {
        override fun run() {
            lockTimeout()
        }
    }

    override fun releaseLock(uuid: UUID?) {
        if(uuid != null){
            scheduler?.submit(ReleaseLockCallable(uuid))
        } else {
            Log.error("Null UUID , can release any lock")
        }
    }

    inner class ReleaseLockCallable(val uuid: UUID): Runnable {
        override fun run() {
            if (currentLock != null) {
                if (currentLock!!.uuid.compareTo(uuid) == 0) {
                    currentLock = null
                    futurWatchDog?.cancel(true)
                    futurWatchDog = null
                    lockWatchDog?.shutdownNow()
                    lockWatchDog = null
                }
            }
        }
    }

    inner class CompareAndSwapCallable(val previousModel: UUIDModel, val targetModel: ContainerRoot, val callback: ModelUpdateCallback?): Callable<Boolean> {
        override fun call(): Boolean {
            val res: Boolean = if (currentLock != null) {
                if (previousModel?.getUUID()?.compareTo(currentLock!!.uuid) == 0) {
                    val internalRes = internal_update_model(targetModel)
                    callCallBack(callback, internalRes, null)
                    internalRes
                } else {
                    Log.debug("Core Locked , bad UUID {}", previousModel.getUUID())
                    callCallBack(callback, false, ModelUpdateCallBackReturn.CAS_ERROR)
                    false //LOCK REFUSED !
                }
            } else {
                //COMMON CHECK
                if (previousModel.getUUID()?.compareTo(model.get()!!.getUUID()!!) == 0) {
                    //TODO CHECK WITH MODEL SHA-1 HASHCODE
                    val internalRes = internal_update_model(targetModel)
                    callCallBack(callback, internalRes, null)
                    internalRes
                } else {
                    callCallBack(callback, false, ModelUpdateCallBackReturn.CAS_ERROR)
                    false
                }
            }
            //System.gc()
            return res
        }
    }

    override fun compareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?): Unit {
        scheduler?.submit(CompareAndSwapCallable(previousModel!!, cloneCurrentModel(targetModel), null))
    }

    override fun atomicUpdateModel(tmodel: ContainerRoot?): Date? {
        scheduler?.submit(UpdateModelCallable(cloneCurrentModel(tmodel), null))?.get()
        return lastDate
    }

    override fun getPreviousModel(): MutableList<ContainerRoot> {
        return scheduler?.submit(GetPreviousModelCallable())?.get() as MutableList<ContainerRoot>
    }

    private inner class GetPreviousModelCallable(): Callable<List<ContainerRoot>> {
        override fun call(): List<ContainerRoot> {
            val previousM = ArrayList<ContainerRoot>()
            for(mds in models){
                previousM.add(mds.getModel()!!)
            }
            return previousM
        }
    }

    override fun atomicCompareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?): Date? {
        val result = scheduler?.submit(CompareAndSwapCallable(previousModel!!, cloneCurrentModel(targetModel), null))?.get()!!
        if (!result) {
            throw KevoreeModelUpdateException() //SEND AND EXCEPTION - Compare&Swap fail !
        }
        return lastDate
    }

    override fun registerModelListener(listener: ModelListener?) {
        modelListeners.addListener(listener!!)
    }

    override fun unregisterModelListener(listener: ModelListener?) {
        modelListeners.removeListener(listener!!)
    }

    override fun getContextModel(): ContextRoot {
        return nodeInstance?.getContextModel()!!
    }


    class RELEASE_LOCK(uuid: UUID){
    }

    override fun acquireLock(callBack: ModelHandlerLockCallBack?, timeout: Long?) {
        scheduler?.submit(AcquireLock(callBack!!, timeout!!))
    }

    private fun checkBootstrapNode(currentModel: ContainerRoot): Unit {
        try {
            if (nodeInstance == null) {
                val foundNode = currentModel.findNodesByID(getNodeName())
                if(foundNode != null){
                    nodeInstance = _bootstraper?.bootstrapNodeType(currentModel, getNodeName(), this, _kevsEngineFactory!!)
                    if(nodeInstance != null){
                        nodeInstance?.startNode()
                        val uuidModel = UUIDModelImpl(UUID.randomUUID(), factory.createContainerRoot())
                        model.set(uuidModel)
                    } else {
                        Log.error("TypeDef installation fail !")
                    }
                } else {
                    Log.error("Node instance name {} not found in bootstrap model !", getNodeName())
                }
            }
        } catch(e: Throwable) {
            Log.error("Error while bootstraping node instance ", e)
            Log.debug(_bootstraper?.getKevoreeClassLoaderHandler()?.getKCLDump())
            try {
                nodeInstance?.stopNode()
            } catch(e: Throwable) {
            } finally {
                _bootstraper?.clear()
            }
            nodeInstance = null
        }
    }

    fun internal_update_model(proposedNewModel: ContainerRoot): Boolean {
        if (proposedNewModel.findNodesByID(getNodeName()) == null) {
            Log.error("Asking for update with a NULL model or node name ({}) was not found in target model !", getNodeName())
            return false
        }
        try {
            val readOnlyNewModel = proposedNewModel
            val checkResult = modelChecker.check(readOnlyNewModel)!!
            if ( checkResult.size > 0) {
                Log.error("There is check failure on update model, update refused !")
                for(cr in checkResult) {
                    Log.error("error=> " + cr?.getMessage() + ",objects" + cr?.getTargetObjects())
                }
                return false
            } else {
                //Model check is OK.
                val currentModel = model.get()!!.getModel()!!
                Log.debug("Before listeners PreCheck !")
                val preCheckResult = modelListeners.preUpdate(currentModel, readOnlyNewModel)
                Log.debug("PreCheck result = " + preCheckResult)
                Log.debug("Before listeners InitUpdate !")
                val initUpdateResult = modelListeners.initUpdate(currentModel, readOnlyNewModel)
                Log.debug("InitUpdate result = " + initUpdateResult)
                if (preCheckResult && initUpdateResult) {
                    var newmodel = readOnlyNewModel
                    //CHECK FOR HARA KIRI
                    var previousHaraKiriModel: ContainerRoot? = null
                    val hkh = HaraKiriHelper()
                    if (hkh.detectNodeHaraKiri(currentModel, readOnlyNewModel, getNodeName())) {
                        Log.warn("HaraKiri detected , flush platform")
                        previousHaraKiriModel = currentModel
                        // Creates an empty model, removes the current node (harakiri)
                        newmodel = factory.createContainerRoot()
                        try {
                            // Compare the two models and plan the adaptation
                            val adaptationModel = nodeInstance!!.kompare(currentModel, newmodel)
                            adaptationModel.setInternalReadOnly()
                            if (Log.DEBUG){
                                //Avoid the loop if the debug is not activated
                                Log.debug("Adaptation model size {}", adaptationModel.getAdaptations().size())
                                for(adaptation in adaptationModel.getAdaptations()) {
                                    Log.debug("primitive {} ", adaptation.getPrimitiveType()?.getName())
                                }
                            }
                            //Executes the adaptation
                            val afterUpdateTest: ()->Boolean = {()-> true }
                            val rootNode = currentModel.findNodesByID(getNodeName())
                            org.kevoree.framework.deploy.PrimitiveCommandExecutionHelper.execute(rootNode!!, adaptationModel, nodeInstance!!, afterUpdateTest, afterUpdateTest, afterUpdateTest)
                            nodeInstance?.stopNode()
                            //end of harakiri
                            nodeInstance = null
                            _bootstraper?.clear() //CLEAR
                            //place the current model as an empty model (for backup)

                            val backupEmptyModel = kevoreeFactory.createContainerRoot()
                            backupEmptyModel.setInternalReadOnly()
                            switchToNewModel(backupEmptyModel)

                            //prepares for deployment of the new system
                            newmodel = readOnlyNewModel
                        } catch(e: Exception) {
                            Log.error("Error while update ", e);return false
                        }
                        Log.debug("End HaraKiri")
                    }


                    //Checks and bootstrap the node
                    checkBootstrapNode(newmodel)
                    val milli = System.currentTimeMillis()
                    if(Log.DEBUG){
                        Log.debug("Begin update model {}", milli)
                    }
                    var deployResult: Boolean// = true
                    try {
                        if (nodeInstance != null) {
                            // Compare the two models and plan the adaptation
                            Log.info("Comparing models and planning adaptation.")

                            val adaptationModel = nodeInstance!!.kompare(currentModel, newmodel)
                            adaptationModel.setInternalReadOnly()
                            //Execution of the adaptation
                            Log.info("Launching adaptation of the system.")
                            val  afterUpdateTest: ()-> Boolean = {()-> modelListeners.afterUpdate(currentModel, newmodel) }

                            val preCmd = PreCommand(newmodel, modelListeners, currentModel)
                            val postRollbackTest: ()->Boolean = {() -> modelListeners.postRollback(currentModel, newmodel);true }
                            val rootNode = newmodel.findNodesByID(getNodeName())!!
                            deployResult = org.kevoree.framework.deploy.PrimitiveCommandExecutionHelper.execute(rootNode, adaptationModel, nodeInstance!!, afterUpdateTest, preCmd.preRollbackTest, postRollbackTest)
                        } else {
                            Log.error("Node is not initialized")
                            deployResult = false
                        }
                    } catch(e: Exception) {
                        Log.error("Error while update ", e)
                        deployResult = false
                    }
                    if (deployResult) {
                        switchToNewModel(newmodel)
                        Log.info("Update sucessfully completed.")
                    } else {
                        //KEEP FAIL MODEL, TODO
                        Log.warn("Update failed")
                        //IF HARAKIRI
                        if (previousHaraKiriModel != null) {
                            internal_update_model(previousHaraKiriModel!!)
                            previousHaraKiriModel = null //CLEAR
                        }
                    }
                    val milliEnd = System.currentTimeMillis() - milli
                    Log.debug("End deploy result={}-{}", deployResult, milliEnd)
                    return deployResult

                } else {
                    Log.debug("PreCheck or InitUpdate Step was refused, update aborded !")
                    return false
                }

            }
        } catch (e: Throwable) {
            Log.error("Error while update", e)
            return false
        }
    }

}

