/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.simulator.sample;

import com.consol.citrus.endpoint.adapter.mapping.MappingKeyExtractor;
import com.consol.citrus.simulator.annotation.*;
import com.consol.citrus.simulator.config.SimulatorConfigurationProperties;
import com.consol.citrus.simulator.http.HttpScenarioMappingKeyExtractor;
import com.consol.citrus.simulator.http.HttpScenarioGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Christoph Deppisch
 */
@SpringBootApplication
@SimulatorApplication
@EnableRest
public class Simulator extends SimulatorRestAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Simulator.class, args);
    }

    @Override
    public MappingKeyExtractor mappingKeyExtractor() {
        return new HttpScenarioMappingKeyExtractor();
    }

    @Override
    public String urlMapping(SimulatorRestConfigurationProperties simulatorRestConfiguration) {
        return "/petstore/v2/**";
    }

    @Bean
    public static HttpScenarioGenerator scenarioGenerator(SimulatorConfigurationProperties simulatorConfiguration) {
        HttpScenarioGenerator generator = new HttpScenarioGenerator(new ClassPathResource("swagger/petstore-api.json"));
        generator.setSimulatorConfiguration(simulatorConfiguration);
        generator.setContextPath("/petstore");
        return generator;
    }
}