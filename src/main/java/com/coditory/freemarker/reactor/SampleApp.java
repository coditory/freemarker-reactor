package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader;
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.blockhound.BlockHound;
import reactor.tools.agent.ReactorDebugAgent;

import java.util.Map;

import static reactor.core.scheduler.Schedulers.parallel;

class SampleApp {
    private static final Logger log = LoggerFactory.getLogger(SampleApp.class);

    public static void main(String[] args) {
        BlockHound.install();
        ReactorDebugAgent.init();

        ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
                .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("templates"))
                .build();

        ReactiveFreeMarkerTemplate template = engine
                .createTemplate("template")
                .subscribeOn(parallel())
                .block();

        String result = template.process(Map.of("name", "John"))
                .subscribeOn(parallel())
                .block();

        log.info("Result:\n" + result);
    }
}
