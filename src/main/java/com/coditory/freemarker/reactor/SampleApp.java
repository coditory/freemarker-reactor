package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader;
import reactor.blockhound.BlockHound;
import reactor.tools.agent.ReactorDebugAgent;

import java.util.Map;

import static reactor.core.scheduler.Schedulers.parallel;

class SampleApp {
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

        String result = template.process(Map.of("a", true))
                .subscribeOn(parallel())
                .block();

        System.out.println("Result:\n" + result);
    }
}
