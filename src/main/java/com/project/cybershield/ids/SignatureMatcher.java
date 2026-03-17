package com.project.cybershield.ids;

import com.project.cybershield.decode.DecodedPacket;
import com.project.cybershield.entities.Protocol;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class SignatureMatcher {

    public boolean matches(SignaturePayload signature, DecodedPacket dp){

        //proto filter
        Protocol sp = signature.getProtocol();
        if(sp != null && dp.l4Proto() != sp){
            return false;
        }

        // port filters (null = any)
        if(signature.getSrcPort() != null && (dp.srcPort() == null || !signature.getSrcPort().equals(dp.srcPort())) ){
            return false;
        }
        if(signature.getDstPort() != null && (dp.dstPort() == null || !signature.getDstPort().equals(dp.dstPort())) ){
            return false;
        }

        byte[] payload = dp.payload();
        if((signature.getContains() != null && !signature.getContains().isBlank()) && signature.getRegex() == null){
            return true;
        }

        String payloadText = new String(payload, StandardCharsets.ISO_8859_1);

        if(signature.getContains() != null && !signature.getContains().isBlank()){
            if(!payloadText.matches(signature.getContains())){
                return false;
            }
        }

        Pattern pattern = signature.getCompiledRegex();

        if(pattern != null){
            if (!pattern.matcher(payloadText).find()){
                return false;
            }
        }
        return true;
    }
}
