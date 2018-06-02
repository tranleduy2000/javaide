/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.builder.signing;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.signing.SignedJarBuilder.IZipEntryFilter.ZipAbortException;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A Jar file builder with signature support.
 */
public class SignedJarBuilder {
    private static final String DIGEST_ALGORITHM = "SHA1";
    private static final String DIGEST_ATTR = "SHA1-Digest";
    private static final String DIGEST_MANIFEST_ATTR = "SHA1-Digest-Manifest";

    /** Write to another stream and track how many bytes have been
     *  written.
     */
    private static class CountOutputStream extends FilterOutputStream {
        private int mCount = 0;

        public CountOutputStream(OutputStream out) {
            super(out);
            mCount = 0;
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            mCount++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            mCount += len;
        }

        public int size() {
            return mCount;
        }
    }

    private JarOutputStream mOutputJar;
    private PrivateKey mKey;
    private X509Certificate mCertificate;
    private Manifest mManifest;
    private MessageDigest mMessageDigest;

    private byte[] mBuffer = new byte[4096];

    /**
     * Classes which implement this interface provides a method to check whether a file should
     * be added to a Jar file.
     */
    public interface IZipEntryFilter {

        /**
         * An exception thrown during packaging of a zip file into APK file.
         * This is typically thrown by implementations of
         * {@link IZipEntryFilter#checkEntry(String)}.
         */
        class ZipAbortException extends Exception {
            private static final long serialVersionUID = 1L;

            public ZipAbortException() {
                super();
            }

            public ZipAbortException(String format, Object... args) {
                super(String.format(format, args));
            }

            public ZipAbortException(Throwable cause, String format, Object... args) {
                super(String.format(format, args), cause);
            }

            public ZipAbortException(Throwable cause) {
                super(cause);
            }
        }


        /**
         * Checks a file for inclusion in a Jar archive.
         * @param archivePath the archive file path of the entry
         * @return <code>true</code> if the file should be included.
         * @throws ZipAbortException if writing the file should be aborted.
         */
        boolean checkEntry(String archivePath) throws ZipAbortException;
    }


    /**
     * Classes which implement this interface provides a method to check whether a file should
     * be merged and extracted from the zip.
     */
    public interface ZipEntryExtractor {

        boolean checkEntry(String archivePath);

        void extract(String archivePath, InputStream zis) throws IOException;
    }

    /**
     * Creates a {@link SignedJarBuilder} with a given output stream, and signing information.
     * <p/>If either <code>key</code> or <code>certificate</code> is <code>null</code> then
     * the archive will not be signed.
     * @param out the {@link OutputStream} where to write the Jar archive.
     * @param key the {@link PrivateKey} used to sign the archive, or <code>null</code>.
     * @param certificate the {@link X509Certificate} used to sign the archive, or
     * <code>null</code>.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public SignedJarBuilder(@NonNull OutputStream out,
                            @Nullable PrivateKey key,
                            @Nullable X509Certificate certificate,
                            @Nullable String builtBy,
                            @Nullable String createdBy)
            throws IOException, NoSuchAlgorithmException {
        mOutputJar = new JarOutputStream(new BufferedOutputStream(out));
        mOutputJar.setLevel(9);
        mKey = key;
        mCertificate = certificate;

        if (mKey != null && mCertificate != null) {
            mManifest = new Manifest();
            Attributes main = mManifest.getMainAttributes();
            main.putValue("Manifest-Version", "1.0");
            if (builtBy != null) {
                main.putValue("Built-By", builtBy);
            }
            if (createdBy != null) {
                main.putValue("Created-By", createdBy);
            }

            mMessageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        }
    }

    /**
     * Writes a new {@link File} into the archive.
     * @param inputFile the {@link File} to write.
     * @param jarPath the filepath inside the archive.
     * @throws IOException
     */
    public void writeFile(File inputFile, String jarPath) throws IOException {
        // Get an input stream on the file.
        FileInputStream fis = new FileInputStream(inputFile);
        try {

            // create the zip entry
            JarEntry entry = new JarEntry(jarPath);
            entry.setTime(inputFile.lastModified());

            writeEntry(fis, entry);
        } finally {
            // close the file stream used to read the file
            fis.close();
        }
    }

    /**
     * Copies the content of a Jar/Zip archive into the receiver archive.
     * @param input the {@link InputStream} for the Jar/Zip to copy.
     * @throws IOException
     * @throws ZipAbortException if the {@link IZipEntryFilter} filter indicated that the write
     *                           must be aborted.
     */
    public void writeZip(InputStream input) throws IOException, ZipAbortException {
        writeZip(input, null, null);
    }

    /**
     * Copies the content of a Jar/Zip archive into the receiver archive.
     * <p/>An optional {@link IZipEntryFilter} allows to selectively choose which files
     * to copy over.
     * @param input the {@link InputStream} for the Jar/Zip to copy.
     * @param filter the filter or <code>null</code>
     * @throws IOException
     * @throws ZipAbortException if the {@link IZipEntryFilter} filter indicated that the write
     *                           must be aborted.
     */
    public void writeZip(InputStream input, IZipEntryFilter filter, ZipEntryExtractor extractor)
            throws IOException, ZipAbortException {
        ZipInputStream zis = new ZipInputStream(input);

        try {
            // loop on the entries of the intermediary package and put them in the final package.
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // do not take directories or anything inside a potential META-INF folder.
                if (entry.isDirectory()) {
                    continue;
                }

                // ignore some of the content in META-INF/ but not all
                if (name.startsWith("META-INF/")) {
                    // ignore the manifest file.
                    String subName = name.substring(9);
                    if ("MANIFEST.MF".equals(subName)) {
                        continue;
                    }

                    // special case for Maven meta-data because we really don't care about them in apks.
                    if (name.startsWith("META-INF/maven/")) {
                        continue;
                    }


                    // check for subfolder
                    int index = subName.indexOf('/');
                    if (index == -1) {
                        // no sub folder, ignores signature files.
                        if (subName.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA")) {
                            continue;
                        }
                    }
                }

                // if we have a filter, we check the entry to see if it's a file that should be extracted.
                if (extractor != null && extractor.checkEntry(name)) {
                    extractor.extract(name, zis);
                    continue;
                }

                // if we have a filter, we check the entry against it
                if (filter != null && !filter.checkEntry(name)) {
                    continue;
                }

                JarEntry newEntry;

                // Preserve the STORED method of the input entry.
                if (entry.getMethod() == JarEntry.STORED) {
                    newEntry = new JarEntry(entry);
                } else {
                    // Create a new entry so that the compressed len is recomputed.
                    newEntry = new JarEntry(name);
                }

                writeEntry(zis, newEntry);

                zis.closeEntry();
            }
        } finally {
            zis.close();
        }
    }

    /**
     * Closes the Jar archive by creating the manifest, and signing the archive.
     * @throws IOException
     * @throws SigningException
     */
    public void close() throws IOException, SigningException {
        if (mManifest != null) {
            // write the manifest to the jar file
            mOutputJar.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
            mManifest.write(mOutputJar);

            try {
                // CERT.SF
                Signature signature = Signature.getInstance("SHA1with" + mKey.getAlgorithm());
                signature.initSign(mKey);
                mOutputJar.putNextEntry(new JarEntry("META-INF/CERT.SF"));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writeSignatureFile(baos);
                byte[] signedData = baos.toByteArray();
                mOutputJar.write(signedData);

                // CERT.*
                mOutputJar.putNextEntry(new JarEntry("META-INF/CERT." + mKey.getAlgorithm()));
                writeSignatureBlock(new CMSProcessableByteArray(signedData), mCertificate, mKey);
            } catch (Exception e) {
                throw new SigningException(e);
            }
        }

        mOutputJar.close();
        mOutputJar = null;
    }

    /**
     * Clean up of the builder for interrupted workflow.
     * This does nothing if {@link #close()} was called successfully.
     */
    public void cleanUp() {
        if (mOutputJar != null) {
            try {
                mOutputJar.close();
            } catch (IOException e) {
                // pass
            }
        }
    }

    /**
     * Adds an entry to the output jar, and write its content from the {@link InputStream}
     * @param input The input stream from where to write the entry content.
     * @param entry the entry to write in the jar.
     * @throws IOException
     */
    private void writeEntry(InputStream input, JarEntry entry) throws IOException {
        // add the entry to the jar archive
        mOutputJar.putNextEntry(entry);

        // read the content of the entry from the input stream, and write it into the archive.
        int count;
        while ((count = input.read(mBuffer)) != -1) {
            mOutputJar.write(mBuffer, 0, count);

            // update the digest
            if (mMessageDigest != null) {
                mMessageDigest.update(mBuffer, 0, count);
            }
        }

        // close the entry for this file
        mOutputJar.closeEntry();

        if (mManifest != null) {
            // update the manifest for this entry.
            Attributes attr = mManifest.getAttributes(entry.getName());
            if (attr == null) {
                attr = new Attributes();
                mManifest.getEntries().put(entry.getName(), attr);
            }
            attr.putValue(DIGEST_ATTR, 
                          new String(Base64.encode(mMessageDigest.digest()), "ASCII"));
        }
    }

    /** Writes a .SF file with a digest to the manifest. */
    private void writeSignatureFile(OutputStream out)
            throws IOException, GeneralSecurityException {
        Manifest sf = new Manifest();
        Attributes main = sf.getMainAttributes();
        main.putValue("Signature-Version", "1.0");
        main.putValue("Created-By", "1.0 (Android)");

        MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        PrintStream print = new PrintStream(
                new DigestOutputStream(new ByteArrayOutputStream(), md),
                true, SdkConstants.UTF_8);

        // Digest of the entire manifest
        mManifest.write(print);
        print.flush();
        main.putValue(DIGEST_MANIFEST_ATTR, new String(Base64.encode(md.digest()), "ASCII"));

        Map<String, Attributes> entries = mManifest.getEntries();
        for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
            // Digest of the manifest stanza for this entry.
            print.print("Name: " + entry.getKey() + "\r\n");
            for (Map.Entry<Object, Object> att : entry.getValue().entrySet()) {
                print.print(att.getKey() + ": " + att.getValue() + "\r\n");
            }
            print.print("\r\n");
            print.flush();

            Attributes sfAttr = new Attributes();
            sfAttr.putValue(DIGEST_ATTR, new String(Base64.encode(md.digest()), "ASCII"));
            sf.getEntries().put(entry.getKey(), sfAttr);
        }
        CountOutputStream cout = new CountOutputStream(out);
        sf.write(cout);

        // A bug in the java.util.jar implementation of Android platforms
        // up to version 1.6 will cause a spurious IOException to be thrown
        // if the length of the signature file is a multiple of 1024 bytes.
        // As a workaround, add an extra CRLF in this case.
        if ((cout.size() % 1024) == 0) {
            cout.write('\r');
            cout.write('\n');
        }
    }

    /** Write the certificate file with a digital signature. */
    private void writeSignatureBlock(CMSTypedData data, X509Certificate publicKey,
            PrivateKey privateKey)
                        throws IOException,
                        CertificateEncodingException,
                        OperatorCreationException,
                        CMSException {

        ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(publicKey);
        JcaCertStore certs = new JcaCertStore(certList);

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha1Signer = new JcaContentSignerBuilder(
                                       "SHA1with" + privateKey.getAlgorithm())
                                   .build(privateKey);
        gen.addSignerInfoGenerator(
            new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder()
                .build())
            .setDirectSignature(true)
            .build(sha1Signer, publicKey));
        gen.addCertificates(certs);
        CMSSignedData sigData = gen.generate(data, false);

        ASN1InputStream asn1 = new ASN1InputStream(sigData.getEncoded());
        DEROutputStream dos = new DEROutputStream(mOutputJar);
        dos.writeObject(asn1.readObject());

        dos.flush();
        dos.close();
        asn1.close();
    }
}
