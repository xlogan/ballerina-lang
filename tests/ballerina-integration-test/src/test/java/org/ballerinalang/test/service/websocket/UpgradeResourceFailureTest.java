/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.websocket;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test whether upgrade resource failure after handshake causes a close frame to be sent.
 */
public class UpgradeResourceFailureTest extends WebSocketIntegrationTest {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/simple";

    @BeforeClass(description = "Initializes the Ballerina server with the resource_failure.bal file")
    public void setup() throws BallerinaTestException, URISyntaxException {
        initBallerinaServer("resource_failure.bal");
        client = new WebSocketTestClient(URL);
    }

    @Test(description = "Tests failure of upgrade resource")
    public void testUpgradeResourceFailure() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.handshake();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceivedCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1011);
        Assert.assertEquals(closeWebSocketFrame.reasonText(), "Unexpected condition");

        closeWebSocketFrame.release();
    }

    @AfterClass(description = "Stops the Ballerina server")
    public void cleanup() throws BallerinaTestException, InterruptedException {
        client.shutDown();
        stopBallerinaServerInstance();
    }
}
