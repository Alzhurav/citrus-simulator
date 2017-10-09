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

package com.consol.citrus.simulator.endpoint;

import com.consol.citrus.endpoint.adapter.RequestDispatchingEndpointAdapter;
import com.consol.citrus.message.Message;
import com.consol.citrus.simulator.config.SimulatorConfigurationProperties;
import com.consol.citrus.simulator.correlation.CorrelationHandler;
import com.consol.citrus.simulator.correlation.CorrelationHandlerRegistry;
import com.consol.citrus.simulator.exception.SimulatorException;
import com.consol.citrus.simulator.scenario.SimulatorScenario;
import com.consol.citrus.simulator.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * @author Christoph Deppisch
 */
public class SimulatorEndpointAdapter extends RequestDispatchingEndpointAdapter implements ApplicationContextAware {

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(SimulatorEndpointAdapter.class);

    @Autowired
    private CorrelationHandlerRegistry handlerRegistry;

    @Autowired
    private SimulatorConfigurationProperties configuration;

    @Autowired
    private ScenarioService scenarioService;

    /** Spring application context */
    private ApplicationContext applicationContext;

    /** When adapter is asynchronous response handling is skipped */
    private boolean handleResponse = true;

    @Override
    protected Message handleMessageInternal(Message request) {
        CorrelationHandler handler = handlerRegistry.findHandlerFor(request);
        if (handler != null) {
            CompletableFuture<Message> responseFuture = new CompletableFuture<>();
            handler.getScenarioEndpoint().add(request, responseFuture);

            try {
                if (handleResponse) {
                    return responseFuture.get(configuration.getDefaultTimeout(), TimeUnit.MILLISECONDS);
                } else {
                    return null;
                }
            } catch (TimeoutException e) {
                LOG.warn(String.format("No response for scenario '%s'", handler.getScenarioEndpoint().getName()));
                return null;
            } catch (InterruptedException | ExecutionException e) {
                throw new SimulatorException(e);
            }
        } else {
            return super.handleMessageInternal(request);
        }
    }

    @Override
    public Message dispatchMessage(Message request, String mappingName) {
        String scenarioName = mappingName;
        CompletableFuture<Message> responseFuture = new CompletableFuture<>();
        SimulatorScenario scenario;
        if (StringUtils.hasText(scenarioName) && applicationContext.containsBean(scenarioName)) {
            scenario = applicationContext.getBean(scenarioName, SimulatorScenario.class);
        } else {
            scenarioName = configuration.getDefaultScenario();
            LOG.info(String.format("Unable to find scenario for mapping '%s' - " +
                    "using default scenario '%s'", mappingName, scenarioName));
            getTestContext().getGlobalVariables().put("scenario_mapping_name", mappingName);
            scenario = applicationContext.getBean(scenarioName, SimulatorScenario.class);
        }

        scenario.getScenarioEndpoint().setName(scenarioName);
        scenario.getScenarioEndpoint().add(request, responseFuture);
        scenarioService.run(scenario, scenarioName, Collections.EMPTY_LIST);

        try {
            if (handleResponse) {
                return responseFuture.get(configuration.getDefaultTimeout(), TimeUnit.MILLISECONDS);
            } else {
                return null;
            }
        } catch (TimeoutException e) {
            LOG.warn(String.format("No response for scenario '%s'", scenarioName));
            return null;
        } catch (InterruptedException | ExecutionException e) {
            throw new SimulatorException(e);
        }
    }

    /**
     * Sets the applicationContext.
     *
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        this.applicationContext = applicationContext;
    }

    /**
     * Gets the handleResponse.
     *
     * @return
     */
    public boolean isHandleResponse() {
        return handleResponse;
    }

    /**
     * Sets the handleResponse.
     *
     * @param handleResponse
     */
    public void setHandleResponse(boolean handleResponse) {
        this.handleResponse = handleResponse;
    }
}
