package org.kevoree.kevscript;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.tools.KevoreeConfig;

import java.io.File;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * Created by leiko on 12/15/16.
 */
public abstract class AbstractKevScriptTest {

    private static int PORT = 3000;
    private KevoreeFactory factory = new DefaultKevoreeFactory();
    private WireMockConfiguration conf = options().port(PORT);

    protected KevScriptEngine kevs;
    protected ContainerRoot model;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(conf, false);

    @Before
    public void init() {
        Log.set(Log.LEVEL_TRACE);
        KevoreeConfig config = new KevoreeConfig.Builder().useDefault().build();
        config.set("registry.host", "localhost");
        config.set("registry.port", PORT);
        config.set("registry.ssl", false);
        this.kevs = new KevScriptEngine(config);
        this.model = factory.createContainerRoot();
        factory.root(this.model);
    }
}
