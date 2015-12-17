package org.nubomedia.qosmanager.connectivitymanageragent.beans;

import com.google.gson.Gson;
import org.nubomedia.qosmanager.connectivitymanageragent.json.AddQueue;
import org.nubomedia.qosmanager.connectivitymanageragent.json.*;
import org.nubomedia.qosmanager.utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Carlo on 10/11/2015.
 */
@Service
public class ConnectivityManagerRequestor {

    @Autowired private Gson mapper;
    private Properties cmProp;
    private Logger logger;
    private RestTemplate template;

    @PostConstruct
    private void init() throws IOException {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.template = new RestTemplate();
        this.cmProp = ConfigReader.readProperties();
    }

    public Host getHost(){
        String url = cmProp.getProperty("cmUrl") + "/hosts";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> getEntity = new HttpEntity<>(headers);
        ResponseEntity<String> hosts = template.exchange(url, HttpMethod.GET,getEntity,String.class);

        logger.debug("hosts " + hosts.getBody());

        if(!hosts.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else{
            return mapper.fromJson(hosts.getBody(),Host.class);
        }
    }

    public QosAdd setQoS(QosAdd qosRequest){

        logger.debug("SENDING REQUEST FOR " + mapper.toJson(qosRequest,QosAdd.class));
        String url = cmProp.getProperty("cmUrl") + "/qoses";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> setEntity = new HttpEntity<>(mapper.toJson(qosRequest,QosAdd.class),headers);
        ResponseEntity<String> insert = template.exchange(url,HttpMethod.POST,setEntity,String.class);

        logger.debug("Setting of QoS has produced http status:" + insert.getStatusCode() + " with body: " + insert.getBody());

        if(!insert.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else {
            QosAdd result = mapper.fromJson(insert.getBody(),QosAdd.class);
            logger.debug("RESULT IS " + insert.getStatusCode() + " with body " + mapper.toJson(result,QosAdd.class));
            return result;
        }
    }

    public Server getServerData(String hypervisorName, String serverName){

        logger.debug("Getting data for server " + serverName + " that belong to " + hypervisorName);
        String url = cmProp.getProperty("cmUrl") + "/server/" + hypervisorName + "/" + serverName;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> getEntity = new HttpEntity<>(headers);
        ResponseEntity<String> server = template.exchange(url,HttpMethod.GET,getEntity,String.class);

        logger.debug("Setting of QoS has produced http status:" + server.getStatusCode() + " with body: " + server.getBody());

        if(!server.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else{
            Server result = mapper.fromJson(server.getBody(),Server.class);
            logger.debug("Request produced " + server.getStatusCode() + " with data " + mapper.toJson(result,Server.class));
            return result;
        }
    }

    public HttpStatus delQos(String hypervisorName,String qosId){

        String url = cmProp.getProperty("cmUrl") + "/qoses/" + hypervisorName + "/" + qosId;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> delentity = new HttpEntity<>(headers);
        ResponseEntity<String> delete = template.exchange(url,HttpMethod.DELETE,delentity,String.class);

        if(delete.getStatusCode().is5xxServerError()){
            logger.debug("The port is still here, returned " + delete.getStatusCode() + " with body " + delete.getBody());
            return delete.getStatusCode();
        }

        logger.debug("deleting qos " + qosId + " has returned " + delete.getStatusCode());

        return delete.getStatusCode();
    }

    public HttpStatus delQueue(String hypervisorName, String qosId, String queueId, String queueNumber){

        String url = cmProp.getProperty("cmUrl") + "/queue/" + hypervisorName + "/" + queueId + "/" + queueNumber + "/" + qosId;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> delQueueEntity = new HttpEntity<>(headers);
        ResponseEntity<String> delete = template.exchange(url,HttpMethod.DELETE,delQueueEntity,String.class);

        logger.debug("deleting queue " + queueId + " with qosID " + qosId + " has returned " + delete.getStatusCode());

        return delete.getStatusCode();
    }

    public AddQueue addQueue (AddQueue add){

        String url = cmProp.getProperty("cmUrl") + "/queue";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> postEntity = new HttpEntity<>(mapper.toJson(add,AddQueue.class),headers);
        ResponseEntity<String> addResp = template.exchange(url,HttpMethod.POST,postEntity,String.class);

        logger.debug("posted " + add.toString() + " and returned " + addResp.getBody());

        if(!addResp.getStatusCode().is2xxSuccessful()){
            return null;
        }
        else{
            return mapper.fromJson(addResp.getBody(),AddQueue.class);
        }
    }

    public RequestFlows setFlow(RequestFlows flow){

        String url = cmProp.getProperty("cmUrl") + "/flow";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> flowEntity = new HttpEntity<>(mapper.toJson(flow,RequestFlows.class),headers);
        ResponseEntity<String> addFlow = template.exchange(url,HttpMethod.POST,flowEntity,String.class);

        logger.debug("FLOW RESPONSE: sent flow configuration " + flow.toString() + " and received " + addFlow.getBody());

        if (!addFlow.getStatusCode().is2xxSuccessful()){
            logger.debug("Status code is " + addFlow.getStatusCode());
            return null;
        }
        else {
            return mapper.fromJson(addFlow.getBody(), RequestFlows.class);
        }
    }

    public HttpStatus deleteFlow(String hypervisor_name, String flow_protocol,String flow_ip){

        String url = cmProp.getProperty("cmUrl") + "/flow/" + hypervisor_name + "/" + flow_protocol + "/" + flow_ip;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> deleteFlowEntity = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = template.exchange(url,HttpMethod.DELETE,deleteFlowEntity,String.class);

        logger.debug("Deleted flow with result " + deleteResponse.getStatusCode());

        return deleteResponse.getStatusCode();
    }

}
