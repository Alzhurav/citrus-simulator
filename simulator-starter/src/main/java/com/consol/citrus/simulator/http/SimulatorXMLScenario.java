package com.consol.citrus.simulator.http;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SimulatorXMLScenario
 *
 * @author ext_azhuravlev2
 * @date 05.10.2017
 */
public class SimulatorXMLScenario extends AbstractSimulatorScenario {

    private String path;

    private HttpMethod method;

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getPath() {
        if (path == null) {
            fillRestAttributes();
        }
        return path;
    }

    public HttpMethod getMethod() {
        if (method == null) {
            fillRestAttributes();
        }
        return method;
    }

    private void fillRestAttributes() {
        RequestMapping requestMapping = this.getClass().getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            method = HttpMethod.resolve(requestMapping.method()[0].name());
            path = requestMapping.value()[0];
        }
    }
}
