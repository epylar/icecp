/*
 * Copyright (c) 2017 Intel Corporation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.icecp.node.channels.ndn;

import com.intel.icecp.common.TestMessage;
import com.intel.icecp.core.channels.OnLatest;
import com.intel.icecp.core.event.EventObservable;
import com.intel.icecp.core.metadata.formats.JsonFormat;
import com.intel.icecp.core.pipeline.Pipeline;
import com.intel.icecp.node.pipeline.implementations.MessageFormattingPipeline;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test MessageRequestHandler
 *
 */
public class MessageRequestHandlerTest {

    private static final int MESSAGE_SIZE = 10000;
    private static final int MARKER = 42;
    private Name prefix;
    private EventObservable observable;
    private MessageRequestHandler instance;
    private Face face;
    private ArgumentCaptor<Data> dataCaptor;
    private OnLatest onLatest;

    @Before
    public void beforeTest() {
        prefix = new Name("/test/name");
        Data template = new Data(prefix);
        observable = mock(EventObservable.class);
        face = mock(Face.class);
        dataCaptor = ArgumentCaptor.forClass(Data.class);
        Pipeline pipeline = MessageFormattingPipeline.create(TestMessage.class, new JsonFormat<>(TestMessage.class));
        onLatest = mock(OnLatest.class);
        when(onLatest.onLatest()).thenReturn(null);

        MessageCache cache = new MessageCache(Long.MAX_VALUE, 5);
        cache.add(1, TestMessage.buildRandom(MESSAGE_SIZE));
        cache.add(2, TestMessage.buildRandom(MESSAGE_SIZE));
        cache.add(3, TestMessage.buildRandom(MESSAGE_SIZE));

        ExecutorService pool = mock(ExecutorService.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(pool).submit(any(Runnable.class));

        instance = new MessageRequestHandler(template, cache, MARKER, pipeline, pool, observable);
        instance.setOnLatest(onLatest);
    }

    @Test
    public void testRetrieveEarliest() throws Exception {
        Interest interest = new Interest(prefix);
        interest.setChildSelector(Interest.CHILD_SELECTOR_LEFT);

        instance.onInterest(prefix, interest, face, 0, null);

        verify(observable, times(2)).notifyApplicableObservers(any());
        verify(face, atLeast(2)).putData(dataCaptor.capture());
        assertEquals(1, dataCaptor.getAllValues().get(0).getName().get(-2).toNumberWithMarker(MARKER));
    }

    @Test
    public void testRetrieveLatest() throws Exception {
        Interest interest = new Interest(prefix);
        interest.setChildSelector(Interest.CHILD_SELECTOR_RIGHT);

        instance.onInterest(prefix, interest, face, 0, null);

        verify(observable, times(2)).notifyApplicableObservers(any());
        verify(face, atLeast(2)).putData(dataCaptor.capture());
        assertEquals(3, dataCaptor.getAllValues().get(0).getName().get(-2).toNumberWithMarker(MARKER));
    }

    @Test
    public void testRetrieveLatestWithGeneration() throws Exception {
        Interest interest = new Interest(prefix);
        interest.setChildSelector(Interest.CHILD_SELECTOR_RIGHT);
        when(onLatest.onLatest()).thenReturn(new OnLatest.Response(TestMessage.buildRandom(MESSAGE_SIZE)));

        instance.onInterest(prefix, interest, face, 0, null);

        verify(onLatest, times(1)).onLatest();
        verify(face, atLeast(2)).putData(dataCaptor.capture());
        assertEquals(4, dataCaptor.getAllValues().get(0).getName().get(-2).toNumberWithMarker(MARKER));
    }

    @Test
    public void testRetrieveSpecificId() throws Exception {
        Interest interest = new Interest(prefix);
        interest.getName().append(Name.Component.fromNumberWithMarker(2, MARKER));

        instance.onInterest(prefix, interest, face, 0, null);

        verify(observable, times(2)).notifyApplicableObservers(any());
        verify(face, atLeast(2)).putData(dataCaptor.capture());
        assertEquals(2, dataCaptor.getAllValues().get(0).getName().get(-2).toNumberWithMarker(MARKER));
    }

    @Test
    public void testRetrieveSpecificIdAndSegment() throws Exception {
        Interest interest = new Interest(prefix);
        interest.getName().append(Name.Component.fromNumberWithMarker(3, MARKER));
        interest.getName().appendSegment(1);

        instance.onInterest(prefix, interest, face, 0, null);

        verify(observable, times(2)).notifyApplicableObservers(any());
        verify(face, atLeast(1)).putData(dataCaptor.capture()); // TODO optimize to send only the requested segment
        assertEquals(3, dataCaptor.getAllValues().get(0).getName().get(-2).toNumberWithMarker(MARKER));
    }

    @Test
    public void testErroneousRetrieval() throws Exception {
        Interest interest = new Interest(prefix);
        interest.getName().appendSegment(999);

        instance.onInterest(prefix, interest, face, 0, null);

        verify(observable, times(0)).notifyApplicableObservers(any());
        verify(face, atLeast(0)).putData(any());
    }

    @Test
    public void testMessageNotFound() throws Exception {
        Interest interest = new Interest(prefix);
        interest.getName().append(Name.Component.fromNumberWithMarker(999, MARKER));

        instance.onInterest(prefix, interest, face, 0, null);

        verify(observable, times(1)).notifyApplicableObservers(any());
        verify(face, atLeast(0)).putData(any());
    }

}