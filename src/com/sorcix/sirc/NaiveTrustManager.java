package com.sorcix.sirc;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

/**
 * This Trust Manager is "naive" because it trusts everyone.
 * Found at http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 * Added for MinecraftBot because many popular IRC servers appear to use self-signed certificates.
 **/
public class NaiveTrustManager implements X509TrustManager
{
  /**
   * Doesn't throw an exception, so this is how it approves a certificate.
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
   **/
    @Override
  public void checkClientTrusted ( X509Certificate[] cert, String authType )
              throws CertificateException 
  {
  }

  /**
   * Doesn't throw an exception, so this is how it approves a certificate.
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
   **/
    @Override
  public void checkServerTrusted ( X509Certificate[] cert, String authType ) 
     throws CertificateException 
  {
  }

  /**
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   **/
    @Override
  public X509Certificate[] getAcceptedIssuers ()
  {
    return null;  // I've seen someone return new X509Certificate[ 0 ]; 
  }
    

/**
 * Returns a SSL Factory instance that accepts all server certificates.
 * <pre>SSLSocket sock =
 *     (SSLSocket) getSocketFactory.createSocket ( host, 443 ); </pre>
 * @return  An SSL-specific socket factory. 
 **/
public static SSLSocketFactory getSocketFactory()
{
  if ( sslSocketFactory == null ) {
    try {
      TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
      SSLContext context = SSLContext.getInstance ("SSL");
      context.init( new KeyManager[0], tm, new SecureRandom( ) );

      sslSocketFactory = (SSLSocketFactory) context.getSocketFactory ();

    } catch (Exception e) {
    }
  }
  return sslSocketFactory;
}
private static SSLSocketFactory sslSocketFactory;
}