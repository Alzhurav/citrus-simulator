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

package com.consol.citrus.simulator.http;

import com.consol.citrus.endpoint.adapter.mapping.AbstractMappingKeyExtractor;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.simulator.config.SimulatorConfigurationProperties;
import com.consol.citrus.simulator.scenario.mapper.ScenarioMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario mapper performs mapping logic on request mapping annotations on given scenarios. Scenarios match on request method as well as
 * request path pattern matching.
 *
 * @author Christoph Deppisch
 */
public class HttpXMLScenarioMapper extends AbstractMappingKeyExtractor implements ScenarioMapper {

    @Autowired(required = false)
    private List<SimulatorXMLScenario> scenarios = new ArrayList<>();

    @Autowired
    private SimulatorConfigurationProperties configuration;

    @Override
    protected String getMappingKey(Message request) {
        if (!(request instanceof HttpMessage)) {
            throw new CitrusRuntimeException("Not support. Error message type");
        }

        for (SimulatorXMLScenario scenario : scenarios) {
            if (((HttpMessage) request).getPath().equals(scenario.getPath())
                    && (((HttpMessage) request).getRequestMethod().compareTo(scenario.getMethod()) == 0)) {
                return scenario.toString();
            }
        }

        return configuration.getDefaultScenario();
    }

    /**
     * Gets the scenarios.
     *
     * @return
     */
    public List<SimulatorXMLScenario> getScenarios() {
        return scenarios;
    }

    /**
     * Sets the scenarios.
     *
     * @param scenarios
     */
    public void setScenarios(List<SimulatorXMLScenario> scenarios) {
        this.scenarios = scenarios;
    }

    /**
     * Gets the configuration.
     *
     * @return
     */
    public SimulatorConfigurationProperties getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration
     */
    public void setConfiguration(SimulatorConfigurationProperties configuration) {
        this.configuration = configuration;
    }
}
