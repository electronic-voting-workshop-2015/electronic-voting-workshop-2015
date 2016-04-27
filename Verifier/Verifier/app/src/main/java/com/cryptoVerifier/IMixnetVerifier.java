package com.cryptoVerifier;

import com.cryptoVerifier.MixnetVerifierUtils.MixnetProofs;

/**
 * Created by Maor Elias on 21/02/16.
 */
public interface IMixnetVerifier {

    boolean verifyMixnetFully(MixnetProofs proofs);

    boolean verifyMixnetRandomlyByPercentage(MixnetProofs proofs, int percentage);

    String getComplaint();

    int getVerificationProgressPercentage();

}
