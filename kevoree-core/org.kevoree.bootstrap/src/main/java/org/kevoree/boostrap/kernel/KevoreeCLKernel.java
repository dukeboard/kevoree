package org.kevoree.boostrap.kernel;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.Repository;
import org.kevoree.api.BootstrapService;
import org.kevoree.boostrap.reflect.KevoreeInjector;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 19:41
 */
public class KevoreeCLKernel implements KevoreeCLFactory, BootstrapService {

    private HashMap<String, KevoreeJarClassLoader> cache = new HashMap<String, KevoreeJarClassLoader>();

    private MavenResolver resolver = new MavenResolver();

    private Boolean offline = false;

    public void setKevoreeCLFactory(KevoreeCLFactory kevoreeCLFactory) {
        this.kevoreeCLFactory = kevoreeCLFactory;
    }

    public void setResolver(MavenResolver resolver) {
        this.resolver = resolver;
    }

    private KevoreeCLFactory kevoreeCLFactory = this;

    public void setInjector(KevoreeInjector injector) {
        this.injector = injector;
    }

    private KevoreeInjector injector = null;

    @Override
    public KevoreeJarClassLoader get(DeployUnit deployUnit) {
        return cache.get(deployUnit.path());
    }

    public KevoreeJarClassLoader installDeployUnit(DeployUnit deployUnit) {
        String path = deployUnit.path();
        if (cache.containsKey(path)) {
            return cache.get(path);
        } else {
            List<String> urls = new ArrayList<String>();
            if (!offline) {
                ContainerRoot root = (ContainerRoot) deployUnit.eContainer();
                for (Repository repo : root.getRepositories()) {
                    urls.add(repo.getUrl());
                }
                if (deployUnit.getVersion().contains("SNAPSHOT") || deployUnit.getVersion().contains("LATEST")) {
                    urls.add("http://oss.sonatype.org/content/groups/public/");
                } else {
                    urls.add("http://repo1.maven.org/maven2");
                }
                File resolved = resolver.resolve(deployUnit.getGroupName(), deployUnit.getName(), deployUnit.getVersion(), deployUnit.getType(), urls);
                if (resolved != null) {
                    KevoreeJarClassLoader kcl = createClassLoader(deployUnit, resolved);
                    cache.put(path, kcl);
                    return kcl;
                }
            }

        }
        return null;
    }

    @Override
    public void removeDeployUnit(DeployUnit deployUnit) {
        cache.remove(deployUnit.path());
    }

    @Override
    public void manualAttach(DeployUnit deployUnit, KevoreeJarClassLoader kevoreeJarClassLoader) {
        cache.put(deployUnit.path(), kevoreeJarClassLoader);
    }

    public KevoreeJarClassLoader recursiveInstallDeployUnit(DeployUnit deployUnit) {
        String path = deployUnit.path();
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        KevoreeJarClassLoader kcl = installDeployUnit(deployUnit);
        if(kcl == null){
           Log.error("Can install {}",deployUnit.path());
        } else {
            for (DeployUnit child : deployUnit.getRequiredLibs()) {
                kcl.addSubClassLoader(recursiveInstallDeployUnit(child));
            }
        }

        return kcl;
    }

    @Override
    public void setOffline(boolean b) {
        offline = b;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Object createInstance(Instance instance) {
        KevoreeJarClassLoader classLoader = recursiveInstallDeployUnit(instance.getTypeDefinition().getDeployUnit());
        Class clazz = classLoader.loadClass(instance.getTypeDefinition().getBean());
        try {
            Object newInstance = clazz.newInstance();
            injector.process(newInstance);
            //TODO inject dictionary
            return newInstance;
        } catch (Exception e) {
            Log.error("Error while creating instance ", e);
        }
        return null;
    }

    @Override
    public KevoreeJarClassLoader createClassLoader(DeployUnit du, File file) {
        KevoreeJarClassLoader classLoader = new KevoreeJarClassLoader();
        try {
            classLoader.add(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            Log.error("Error while opening JAR {} : ", file.getAbsolutePath());
        } finally {
            return classLoader;
        }
    }
}