package com.project.cybershield.ids;

import ch.qos.logback.core.util.SimpleInvocationGate;
import com.project.cybershield.decode.DecodedPacket;

import java.util.ArrayList;
import java.util.List;

public class SignatureEngine {

    private final List<SignaturePayload> signatures;
    private final SignatureMatcher signatureMatcher = new SignatureMatcher();

    public SignatureEngine(List<SignaturePayload> signatures) {
        this.signatures = signatures;
    }

    public List<SignaturePayload> matchAll(DecodedPacket p){
        List<SignaturePayload> hits = new ArrayList<>();
        for(SignaturePayload s : signatures){
            if(signatureMatcher.matches(s, p)) hits.add(s);
        }
        return hits;
    }

}
