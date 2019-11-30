package com.master.utl;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyRandom;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyRoundRobin;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyWorstFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerFirstFit;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;


import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.distributions.NormalDistr;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerCompletelyFair;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;


public class GlobalMappings {

    public static VmAllocationPolicy getVmAllocationPolicy(String allocationPolicy) {
        if (allocationPolicy == null) {
            return null;
        } else if (allocationPolicy == "VmAllocationPolicySimple") {
            return new VmAllocationPolicySimple();
        } else if (allocationPolicy == "VmAllocationPolicyBestFit") {
            return new VmAllocationPolicyBestFit();
        } else if (allocationPolicy == "VmAllocationPolicyFirstFit") {
            return new VmAllocationPolicyFirstFit();
        } else if (allocationPolicy == "VmAllocationPolicyRandom") {
            double mean = 50.0;
            double standardDeviation = 2.0;
            ContinuousDistribution continuousDistribution = new NormalDistr(mean, standardDeviation);
            return new VmAllocationPolicyRandom(continuousDistribution);
        } else if (allocationPolicy == "VmAllocationPolicyRoundRobin") {
            return new VmAllocationPolicyRoundRobin();
        } else if (allocationPolicy == "VmAllocationPolicyWorstFit") {
            return new VmAllocationPolicyWorstFit();
        } else {
            return new VmAllocationPolicySimple();
        }
    }

    public static CloudletScheduler getCloudLetScheduler(String CloudletScheduler) {
        if (CloudletScheduler == null) {
            return null;
        } else if (CloudletScheduler == "CloudletSchedulerTimeShared") {
            return new CloudletSchedulerTimeShared();
        } else if (CloudletScheduler == "CloudletSchedulerCompletelyFair") {
            return new CloudletSchedulerCompletelyFair();
        } else if (CloudletScheduler == "CloudletSchedulerSpaceShared") {
            return new CloudletSchedulerSpaceShared();
        } else {
            return new CloudletSchedulerSpaceShared();
        }
    }

    public static DatacenterBroker getDataCenterBroker(String DataCenterBroker, CloudSim simulation) {
        // todo add new algorithms here after implementation
        if (DataCenterBroker == null) {
            return null;
        } else if (DataCenterBroker == "DatacenterBrokerBestFit") {
            return new DatacenterBrokerBestFit(simulation);
        } else if (DataCenterBroker == "DatacenterBrokerFirstFit") {
            return new DatacenterBrokerFirstFit(simulation);
        } else if (DataCenterBroker == "DatacenterBrokerSimple") {
            return new DatacenterBrokerSimple(simulation);
        } else if (DataCenterBroker == "DatacenterBrokerPracticalSwarm") {
            // todo return new DatacenterBrokerPracticalSwarm(simulation);
            return new DatacenterBrokerSimple(simulation);
        } else if (DataCenterBroker == "DatacenterBrokerSecondImplementation") {
            // todo return new DatacenterBrokerSecondImplementation(simulation);
            return new DatacenterBrokerSimple(simulation);
        } else if (DataCenterBroker == "DatacenterBrokerHybrid") {
            // todo return new DatacenterBrokerHybrid(simulation);
            return new DatacenterBrokerSimple(simulation);
        } else {
            return new DatacenterBrokerSimple(simulation);
        }
    }


}
