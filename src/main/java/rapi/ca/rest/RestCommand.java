package rapi.ca.rest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class RestCommand<Request, Response> {
    private static Logger log = LoggerFactory.getLogger(RestCommand.class);
    private RestTemplate restTemplate;// = new RestTemplate();
    private String url;
    private Request requestModel;
    private ResponseEntity<Response> responseEntity;
    private HttpStatus httpStatus;
    private Class<Response> responseModel;
    private HttpHeaderDelegate headerDelegate = new NoAuthHeaderDelegate();
    private static SchemeRegistry schemeRegistry = new SchemeRegistry();
    private static PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);

    static {
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(
                new Scheme("https", 443, buildSSLSocketFactory()));
        // Increase max total connection to 200
        cm.setMaxTotal(100);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
    }


    private static SSLSocketFactory buildSSLSocketFactory() {
        TrustStrategy ts = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true; // heck yea!
            }
        };

        SSLSocketFactory sf = null;

        try {
            /* build socket factory with hostname verification turned off. */
            sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to initialize SSL handling.", e);
        } catch (KeyManagementException e) {
            log.error("Failed to initialize SSL handling.", e);
        } catch (KeyStoreException e) {
            log.error("Failed to initialize SSL handling.", e);
        } catch (UnrecoverableKeyException e) {
            log.error("Failed to initialize SSL handling.", e);
        }

        return sf;
    }

    public RestCommand() {
        restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(new DefaultHttpClient(cm)));
    }

    public RestCommand(final String aUserName, final String aPassword, final String anAuthDomain, int aPort) {
        DefaultHttpClient client = new DefaultHttpClient(cm);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(aUserName, aPassword);
        client.getCredentialsProvider().setCredentials(new AuthScope(anAuthDomain, aPort, AuthScope.ANY_REALM), credentials);
        HttpComponentsClientHttpRequestFactory commons = new HttpComponentsClientHttpRequestFactory(client);
        restTemplate = new RestTemplate(commons);
    }

    public RestCommand(final String aUrl, Request aRequest, Class<Response> aResponse) {
        this();
        url = aUrl;
        requestModel = aRequest;
        responseModel = aResponse;
    }

    public RestCommand(final String aUrl, Class<Response> aResponse) {
        this();
        url = aUrl;
        responseModel = aResponse;
    }

    public RestCommand(final String aUrl, Class<Response> aResponse, HttpHeaderDelegate aHeaderDelegate) {
        this(aUrl, aResponse);
        headerDelegate = aHeaderDelegate;
    }

    public RestCommand(final String aUrl, Request aRequest, Class<Response> aResponse, HttpHeaderDelegate aHeaderDelegate) {
        this(aUrl, aRequest, aResponse);
        headerDelegate = aHeaderDelegate;
    }

    public void delete() {
        try {
            if (getRequestModel() == null) {
                this.responseEntity = restTemplate.exchange(getUrl(), HttpMethod.DELETE, new HttpEntity<String>(getHttpHeaders()), responseModel);
            } else {
                this.responseEntity = restTemplate.exchange(getUrl(), HttpMethod.DELETE, new HttpEntity<Request>(getRequestModel(), getHttpHeaders()), responseModel);
            }
        }catch(HttpStatusCodeException hsce){
            this.httpStatus=hsce.getStatusCode();
        }catch (RestClientException rce){
            this.httpStatus=HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Response get() {
        this.responseEntity = restTemplate.exchange(getUrl(), HttpMethod.GET, new HttpEntity<String>(getHttpHeaders()), getResponseModel());
        return responseEntity.getBody();
    }

    public HttpHeaders getHttpHeaders() {
        return headerDelegate.getHttpHeaders();
    }

    public String getUrl() {
        return url;
    }

    public Request getRequestModel() {
        return requestModel;
    }

    public Class<Response> getResponseModel() {
        return responseModel;
    }

    public Response post() {
        return post(1);
    }

    public Response post(int retries) {
        return exchange(HttpMethod.POST, retries);
    }

    private Response exchange(HttpMethod method, int retries) {
        for (int i = 0; i < retries; i++) {
            try {
                this.responseEntity = restTemplate.exchange(getUrl(), method, new HttpEntity<Request>(getRequestModel(), getHttpHeaders()), getResponseModel());
                HttpStatus statusCode = responseEntity.getStatusCode();
                if (statusCode == HttpStatus.OK || statusCode == HttpStatus.ACCEPTED || statusCode == HttpStatus.CREATED) {
                    return this.responseEntity.getBody();
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) { }
                }
            }catch(HttpStatusCodeException hsce){
                log.error("Response error:", hsce.getMessage());
                this.httpStatus=hsce.getStatusCode();
            }catch (RestClientException rce){
                this.httpStatus=HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return null;
    }

    public Response put() {
        return put(1);
    }

    public Response put(int retries) {
        return exchange(HttpMethod.PUT, retries);
    }

    public void setUrl(final String path) {
        this.url = path;
    }

    public void setRequestModel(final Request requestModel) {
        this.requestModel = requestModel;
    }

    public void setResponseModel(final Class<Response> responseModel) {
        this.responseModel = responseModel;
    }

    public HttpHeaderDelegate getHeaderDelegate() {
        return headerDelegate;
    }

    public void setHeaderDelegate(HttpHeaderDelegate headerDelegate) {
        this.headerDelegate = headerDelegate;
    }

    public HttpStatus getHttpStatus() {
        return (null==this.responseEntity)?this.httpStatus:this.responseEntity.getStatusCode();
    }
}
