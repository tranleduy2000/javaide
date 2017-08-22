/*
 * Copyright (c) 2003, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.security.provider.certpath;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import sun.misc.HexDumpEncoder;
import sun.security.util.*;

/**
 * This class can be used to generate an OCSP request and send it over
 * an outputstream. Currently we do not support signing requests
 * The OCSP Request is specified in RFC 2560 and
 * the ASN.1 definition is as follows:
 * <pre>
 *
 * OCSPRequest     ::=     SEQUENCE {
 *      tbsRequest                  TBSRequest,
 *      optionalSignature   [0]     EXPLICIT Signature OPTIONAL }
 *
 *   TBSRequest      ::=     SEQUENCE {
 *      version             [0]     EXPLICIT Version DEFAULT v1,
 *      requestorName       [1]     EXPLICIT GeneralName OPTIONAL,
 *      requestList                 SEQUENCE OF Request,
 *      requestExtensions   [2]     EXPLICIT Extensions OPTIONAL }
 *
 *  Signature       ::=     SEQUENCE {
 *      signatureAlgorithm      AlgorithmIdentifier,
 *      signature               BIT STRING,
 *      certs               [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL
 *   }
 *
 *  Version         ::=             INTEGER  {  v1(0) }
 *
 *  Request         ::=     SEQUENCE {
 *      reqCert                     CertID,
 *      singleRequestExtensions     [0] EXPLICIT Extensions OPTIONAL }
 *
 *  CertID          ::= SEQUENCE {
 *       hashAlgorithm  AlgorithmIdentifier,
 *       issuerNameHash OCTET STRING, -- Hash of Issuer's DN
 *       issuerKeyHash  OCTET STRING, -- Hash of Issuers public key
 *       serialNumber   CertificateSerialNumber
 * }
 *
 * </pre>
 *
 * @author      Ram Marti
 */

class OCSPRequest {

    private static final Debug debug = Debug.getInstance("certpath");
    private static final boolean dump = debug.isOn("ocsp");

    // List of request CertIds
    private final List<CertId> certIds;

    /*
     * Constructs an OCSPRequest. This constructor is used
     * to construct an unsigned OCSP Request for a single user cert.
     */
    OCSPRequest(CertId certId) {
        this.certIds = Collections.singletonList(certId);
    }

    OCSPRequest(List<CertId> certIds) {
        this.certIds = certIds;
    }

    byte[] encodeBytes() throws IOException {

        // encode tbsRequest
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream requestsOut = new DerOutputStream();
        for (CertId certId : certIds) {
            DerOutputStream certIdOut = new DerOutputStream();
            certId.encode(certIdOut);
            requestsOut.write(DerValue.tag_Sequence, certIdOut);
        }

        tmp.write(DerValue.tag_Sequence, requestsOut);
        // No extensions supported
        DerOutputStream tbsRequest = new DerOutputStream();
        tbsRequest.write(DerValue.tag_Sequence, tmp);

        // OCSPRequest without the signature
        DerOutputStream ocspRequest = new DerOutputStream();
        ocspRequest.write(DerValue.tag_Sequence, tbsRequest);

        byte[] bytes = ocspRequest.toByteArray();

        if (dump) {
            HexDumpEncoder hexEnc = new HexDumpEncoder();
            debug.println("\nOCSPRequest bytes... ");
            debug.println(hexEnc.encode(bytes) + "\n");
        }

        return bytes;
    }

    List<CertId> getCertIds() {
        return certIds;
    }
}
