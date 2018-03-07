import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;


public class SNMPClient {


    private String address = null;
    private HashMap<String, Long> previous;
    private HashMap<String, Long> previousOut;
    private static final Logger log = Logger.getLogger(SNMPClient.class);

    public SNMPClient(String add) {
        address = add;
        previous = new HashMap<String, Long>();
        previousOut = new HashMap<String, Long>();
        //SNMP4JSettings.
    }

    public void scanAllPorts(String ip, int howManyPorts) throws IOException, NoSuchAlgorithmException, InterruptedException {
        /**
         * Port 161 is used for Read and Other operations Port 162 is used for
         * the trap generation
         */
        Long in = new Long(0);
        Long out = new Long(0);

        log.info("Creating the restclient ...");
        System.setProperty("javax.net.debug", "ssl");

                /*SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            HttpGet httpGet = new HttpGet("https://some-server");
            CloseableHttpResponse response = httpclient.execute(httpGet);
            try {
                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
            }
            finally {
                response.close();
            }*/

        //SSLContext.getDefault();

        RestClient restClient = RestClient.builder(new HttpHost("kibana", 9302, "http")).build();
        log.info("CREATED the restclient ************");

        for (int i = 0; i < 2; i++) {
            // for (String ip : ips) {
            for (int j = 1; j <= howManyPorts; j++) {
                try {
                    log.info("Scanning IP: " + ip + " on port: " + j);
                    boolean thereIsDeltaIn = false;
                    boolean thereIsDeltaOut = false;
                    System.out.println("IP: " + ip + " port:" + j);

                   // this.start();

                    // https://docstore.mik.ua/orelly/networking_2ndEd/snmp/appa_01.htm
                    // http://www.oidview.com/mibs/0/IF-MIB.html
                    // IF-MIB::ifOutOctets
                    // http://www.oidview.com/mibs/0/IF-MIB.html
                    // https://qbox.io/blog/rest-calls-new-java-elasticsearch-client-tutorial
                    //https://blog.jayway.com/2010/05/21/introduction-to-snmp4j/

                    /*String sysDescr = this.getAsString(new OID("1.3.6.1.2.1.2.2.1.10.10" + (j > 9 ? j : "0" + j)));

                    Long max = 4294967294l;
                    Long myIN = (Long.parseLong(sysDescr)
                            - (previous.containsKey(ip + "IN" + j + "IN") ? previous.get(ip + "IN" + j + "IN") : 0));
                    if (Long.parseLong(sysDescr) < (previous.containsKey(ip + "IN" + j + "IN")
                            ? previous.get(ip + "IN" + j + "IN") : 0))
                        myIN = max
                                - (previous.containsKey(ip + "IN" + j + "IN") ? previous.get(ip + "IN" + j + "IN") : 0)
                                + Long.parseLong(sysDescr);

                    in = new Long(Long.parseLong(sysDescr));
                    thereIsDeltaIn = previous.containsKey(ip + "IN" + j + "IN");
                    previous.put(ip + "IN" + j + "IN", in);

                    String sysDescrOut = this.getAsString(new OID("1.3.6.1.2.1.2.2.1.16.10" + (j > 9 ? j : "0" + j)));
                    long myOut = (Long.parseLong(sysDescrOut) - (previousOut.containsKey(ip + "OUT" + j + "OUT")
                            ? previousOut.get(ip + "OUT" + j + "OUT") : 0));
                    if (Long.parseLong(sysDescrOut) < (previousOut.containsKey(ip + "OUT" + j + "OUT")
                            ? previousOut.get(ip + "OUT" + j + "OUT") : 0))
                        myOut = max - (previousOut.containsKey(ip + "OUT" + j + "OUT")
                                ? previousOut.get(ip + "OUT" + j + "OUT") : 0) + Long.parseLong(sysDescrOut);
                    out = new Long(Long.parseLong(sysDescrOut));
                    thereIsDeltaOut = previousOut.containsKey(ip + "OUT" + j + "OUT");
                    previousOut.put(ip + "OUT" + j + "OUT", out);*/

                    long id = this.uniqueCurrentTimeMS();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // your
                    // format
                    format.setTimeZone(TimeZone.getTimeZone("GMT"));
                    String dateAsString = format.format(new Date());
                    SimpleDateFormat formatForIndexName = new SimpleDateFormat("yyyy-MM-dd");
                    String dateAsStringForIndexName = formatForIndexName.format(new Date());

                    if (thereIsDeltaIn && thereIsDeltaOut) {
                        log.info("Sending data to Elasticsearch ...");
                        String myIN = "";
                        String myOut = "";
                        HttpEntity entity = new NStringEntity(
                                "{\n" + "    \"port\" : " + j + ",\n" + "    \"in\" : " + myIN + ",\n"
                                        + "    \"out\" : " + myOut + ",\n" + "    \"ip\" : \"" + ip + "\",\n"
                                        + "    \"date\" : \"" + dateAsString + "\"\n" + "}",
                                ContentType.APPLICATION_JSON);
                        Response indexResponse = restClient.performRequest("PUT",
                                "/snmp-" + dateAsStringForIndexName + "/post/" + String.valueOf(id), Collections.<String, String>emptyMap(),
                                entity);
                        log.info("Elasticsearch response: " + indexResponse.getEntity());
                    }

                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    log.error("Error generic: " + e.getMessage());

                } finally {
                    //log.info("stopping snmp client ...");
                    //this.stop();
                    //log.info("stoppED snmp client ...");
                }
                Thread.sleep(100);
            }
        }
        log.info("Closing rest client ...");
        restClient.close();
        log.info("ClosiED rest client ...");

    }



    private static final AtomicLong LAST_TIME_MS = new AtomicLong();

    public long uniqueCurrentTimeMS() {
        long now = System.currentTimeMillis();
        while (true) {
            long lastTime = LAST_TIME_MS.get();
            if (lastTime >= now)
                now = lastTime + 1;
            if (LAST_TIME_MS.compareAndSet(lastTime, now))
                return now;
        }
    }

}
