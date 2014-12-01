/*
 * ******************************************************************************
 *
 * INTEL CONFIDENTIAL
 *
 * Copyright 2013 - 2016 Intel Corporation All Rights Reserved.
 *
 * The source code contained or described herein and all documents related to
 * the source code ("Material") are owned by Intel Corporation or its suppliers
 * or licensors. Title to the Material remains with Intel Corporation or its
 * suppliers and licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and licensors. The
 * Material is protected by worldwide copyright and trade secret laws and treaty
 * provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed
 * in any way without Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery
 * of the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise. Any license under such intellectual property rights must be
 * express and approved by Intel in writing.
 *
 * Unless otherwise agreed by Intel in writing, you may not remove or alter this
 * notice or any other notice embedded in Materials by Intel or Intel's
 * suppliers or licensors in any way.
 *
 * ******************************************************************************
 */
package com.intel.icecp.core.misc;

import com.intel.icecp.common.TestMessage;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ResolvedFutureTest {

    private final ResolvedFuture<TestMessage> instance;
    private final TestMessage message;

    public ResolvedFutureTest() {
        message = TestMessage.buildRandom(10);
        instance = new ResolvedFuture<>(message);
    }

    @Test
    public void testCancel() {
        instance.cancel(true);
    }

    @Test
    public void testIsCancelled() {
        assertFalse(instance.isCancelled());
        instance.cancel(false);
        instance.cancel(true);
        assertFalse(instance.isCancelled());
    }

    @Test
    public void testIsDone() {
        assertTrue(instance.isDone());
    }

    @Test
    public void testGet_0args() throws Exception {
        assertEquals(message, instance.get());
    }

    @Test
    public void testGet_long_TimeUnit() throws Exception {
        assertEquals(message, instance.get(100, TimeUnit.DAYS));
    }

}
