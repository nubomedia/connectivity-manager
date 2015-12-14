package org.nubomedia.qosmanager.openbaton;

import java.util.*;

/**
 * Created by maa on 09.12.15.
 */
public class FlowAllocation {

    private Map<String,List<FlowReference>> virtualLinkRecord;

    public FlowAllocation(Map<String, List<FlowReference>> virtualLinkRecord) {
        this.virtualLinkRecord = virtualLinkRecord;
    }

    public FlowAllocation() {
        this.virtualLinkRecord = new LinkedHashMap<>();
    }

    public Map<String, List<FlowReference>> getVirtualLinkRecord() {
        return virtualLinkRecord;
    }

    public void setVirtualLinkRecord(Map<String, List<FlowReference>> virtualLinkRecord) {
        this.virtualLinkRecord = virtualLinkRecord;
    }

    public void addVirtualLink (String virtualLink, List<FlowReference> ips){
        this.virtualLinkRecord.put(virtualLink,ips);
    }

    public Set<String> getAllVlr(){
        return this.virtualLinkRecord.keySet();
    }

    public List<FlowReference> getIpsForVlr(String vlr){
        return this.virtualLinkRecord.get(vlr);
    }

    public List<String> getAllIpsForVlr(String vlr){
        List<String> res = new ArrayList<>();
        for(FlowReference ref : this.getIpsForVlr(vlr)){
            res.add(ref.getIp());
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "";

        for (String vlrid : virtualLinkRecord.keySet()){
            res +=  vlrid + ": {\n";
            for (FlowReference allocation : virtualLinkRecord.get(vlrid)){
                res+="\tserver:" + allocation.getHostname() + "\n" + "ip:" + allocation.getIp() + "\n";
            }

        }

        return res;
    }
}
