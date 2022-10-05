/*
 * Copyright and related rights waived via CC0
 *
 * You should have received a copy of the CC0 legalcode along with this
 * work. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package com.ecwid.consul;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.coordinate.CoordinateConsulClient;
import com.ecwid.consul.v1.coordinate.model.Datacenter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ecwid.consul.utils.ConsulTestUtils.getFreePort;
import static com.ecwid.consul.utils.ConsulTestUtils.initTomcat;
import static org.assertj.core.api.Assertions.assertThat;

class CoordinateConsulClientTests {
    private Tomcat tomcat;
    private CoordinateConsulClient consulClient;
    private String requestUri;

    @BeforeEach
    void setUp() {
        int port = getFreePort();
        try {
            tomcat = initTomcat(port, new TestServlet());
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        ConsulRawClient consulRawClient = new ConsulRawClient("localhost", port);
        consulClient = new CoordinateConsulClient(consulRawClient);
    }

    @AfterEach
    void tearDownAll() {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldRetrieveDatacenters() {
        Response<List<Datacenter>> response = consulClient.getDatacenters();

        assertThat(response).isNotNull();
        List<Datacenter> datacenters = response.getValue();
        assertThat(datacenters).hasSize(1);
        assertThat(requestUri).endsWith("/v1/coordinate/datacenters");
    }

    private class TestServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            requestUri = req.getRequestURI();
            resp.setStatus(200);
            resp.setContentType("JSON/UTF-8");
            try (Writer writer = resp.getWriter()) {
                writer.write("[{\"ID\":\"test\"}]");
            }
        }
    }
}
