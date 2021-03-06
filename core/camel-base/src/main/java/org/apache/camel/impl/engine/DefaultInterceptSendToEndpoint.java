/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl.engine;

import java.util.Map;

import org.apache.camel.AsyncProducer;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.ShutdownableService;
import org.apache.camel.spi.InterceptSendToEndpoint;
import org.apache.camel.support.service.ServiceHelper;

/**
 * This is an endpoint when sending to it, is intercepted and is routed in a detour
 */
public class DefaultInterceptSendToEndpoint implements InterceptSendToEndpoint, ShutdownableService {

    private final Endpoint delegate;
    private Processor detour;
    private boolean skip;

    /**
     * Intercepts sending to the given endpoint
     *
     * @param destination  the original endpoint
     * @param skip <tt>true</tt> to skip sending after the detour to the original endpoint
     */
    public DefaultInterceptSendToEndpoint(final Endpoint destination, boolean skip) {
        this.delegate = destination;
        this.skip = skip;
    }

    public void setDetour(Processor detour) {
        this.detour = detour;
    }

    @Override
    public Processor getDetour() {
        return detour;
    }

    @Override
    public Endpoint getOriginalEndpoint() {
        return delegate;
    }

    @Override
    public boolean isSkip() {
        return skip;
    }

    public String getEndpointUri() {
        return delegate.getEndpointUri();
    }

    public String getEndpointKey() {
        return delegate.getEndpointKey();
    }

    public Exchange createExchange() {
        return delegate.createExchange();
    }

    public Exchange createExchange(ExchangePattern pattern) {
        return delegate.createExchange(pattern);
    }

    public CamelContext getCamelContext() {
        return delegate.getCamelContext();
    }

    public Producer createProducer() throws Exception {
        return createAsyncProducer();
    }

    @Override
    public AsyncProducer createAsyncProducer() throws Exception {
        AsyncProducer producer = delegate.createAsyncProducer();
        return new InterceptSendToEndpointProcessor(this, delegate, producer, skip);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return delegate.createConsumer(processor);
    }

    public PollingConsumer createPollingConsumer() throws Exception {
        return delegate.createPollingConsumer();
    }

    public void configureProperties(Map<String, Object> options) {
        delegate.configureProperties(options);
    }

    public void setCamelContext(CamelContext context) {
        delegate.setCamelContext(context);
    }

    public boolean isLenientProperties() {
        return delegate.isLenientProperties();
    }

    public boolean isSingleton() {
        return delegate.isSingleton();
    }

    public void start() {
        ServiceHelper.startService(detour, delegate);
    }

    public void stop() {
        ServiceHelper.stopService(delegate, detour);
    }

    @Override
    public void shutdown() {
        ServiceHelper.stopAndShutdownServices(delegate, detour);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
