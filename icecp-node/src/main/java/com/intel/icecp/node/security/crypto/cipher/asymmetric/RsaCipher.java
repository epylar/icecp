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
package com.intel.icecp.node.security.crypto.cipher.asymmetric;

import com.intel.icecp.node.security.SecurityConstants;
import com.intel.icecp.core.security.crypto.cipher.Cipher;
import com.intel.icecp.core.security.crypto.exception.cipher.CipherDecryptionError;
import com.intel.icecp.core.security.crypto.exception.cipher.CipherEncryptionError;
import com.intel.icecp.core.security.crypto.key.asymmetric.PrivateKey;
import com.intel.icecp.core.security.crypto.key.asymmetric.PublicKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Thin wrapper around RSA crypto APIs provided by {@link javax.crypto.Cipher}
 *
 */
public class RsaCipher implements Cipher<PublicKey, PrivateKey> {

    /**
     * {@inheritDoc }
     */
    @Override
    public byte[] encrypt(byte[] dataToEncrypt, PublicKey key, Object... other) throws CipherEncryptionError {
        try {
            // Obtain a RSA cipher instance from javax.crypto
            final javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(SecurityConstants.RSA_ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key.getPublicKey());
            // Finally return the ciphertext
            return cipher.doFinal(dataToEncrypt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            throw new CipherEncryptionError("Error during encryption.", ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public byte[] decrypt(byte[] dataToDecrypt, PrivateKey key, Object... other) throws CipherDecryptionError {
        try {
            // Obtain a RSA cipher instance from javax.crypto
            final javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(SecurityConstants.RSA_ALGORITHM);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key.getKey());
            // Finally return
            return cipher.doFinal(dataToDecrypt);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            throw new CipherDecryptionError("Error during decryption.", ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String id() {
        return SecurityConstants.RSA_ALGORITHM;
    }

}