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
package com.intel.icecp.core.mock;

import com.intel.icecp.core.Message;
import com.intel.icecp.core.metadata.Format;
import com.intel.icecp.core.metadata.formats.FormatEncodingException;
import com.intel.icecp.core.pipeline.Operation;
import com.intel.icecp.core.pipeline.exception.OperationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Operation that encodes/decodes a {@link Message} using a give {@link Format}
 *
 */
public class MockFormattingOperation extends Operation<Message, InputStream> {

    /** Format to use to encode/decode the message */
    private final Format format;

    public MockFormattingOperation(Format format) {
        super(Message.class, InputStream.class);
        this.format = format;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public InputStream execute(Message input) throws OperationException {
        try {
            return format.encode(input);
        } catch (FormatEncodingException ex) {
            throw new OperationException("FormattingOperation direct operation failed.", ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Message executeInverse(InputStream input) throws OperationException {
        try {
            return format.decode(input);
        } catch (IOException | FormatEncodingException ex) {
            throw new OperationException("FormattingOperation inverse operation failed.", ex);
        }
    }
}
