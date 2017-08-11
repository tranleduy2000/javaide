package kellinwood.sigblock;

import java.io.IOException;
import java.io.OutputStream;

import java.math.BigInteger;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public class SignatureBlockWriter {
    
    /** Write a .RSA file with a digital signature. */
    public static void writeSignatureBlock( byte[] signatureBytes, X509Certificate publicKey, OutputStream out)
            throws IOException, GeneralSecurityException 
    {
    	X500Principal x500Principal = publicKey.getIssuerX500Principal();
    	String x500PrincipalName = x500Principal.getName();
    	X500Name x500Name = null;
    	x500Name = new X500Name( x500PrincipalName);
    	
    	BigInteger serialNumber = publicKey.getSerialNumber();
    	
    	AlgorithmId SHA1_Id = AlgorithmId.get("SHA1");
    	AlgorithmId RSA_Id = AlgorithmId.get("RSA");
    	
        SignerInfo signerInfo = new SignerInfo(
                x500Name,
                serialNumber,
                SHA1_Id,
                RSA_Id,
                signatureBytes);

        PKCS7 pkcs7 = new PKCS7(
                new AlgorithmId[] { AlgorithmId.get("SHA1") },
                new ContentInfo(ContentInfo.DATA_OID, null),
                new X509Certificate[] { publicKey },
                new SignerInfo[] { signerInfo });

        pkcs7.encodeSignedData(out);
    }
}

    
