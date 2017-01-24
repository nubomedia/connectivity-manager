package org.nubomedia.qosmanager.interfaces;

import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;

import java.util.List;

/**
 * Created by maa on 22.02.16.
 */
public interface QoSInterface {

    public boolean addQoS(List<QoSAllocation> vnfc_instances, FlowAllocation vnfc_flows, String nsrId);
    public boolean removeQoS(List<String> vnfc_instances,String nsrID);
}
