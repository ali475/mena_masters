package com.master.algorithms;

// imports //////////////////////////////////////

import ch.qos.logback.classic.Level;
import com.master.config.ConfigReader;

import com.master.utl.GlobalMappings;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.power.models.PowerAware;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.UtilizationHistory;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
////////////////////////////////////////////////


public class CloudRunner {

    private static final int SCHEDULING_INTERVAL = 10;
    /**
     * Defines the minimum percentage of power a Host uses,
     * even it it's idle.
     */
    private static final double STATIC_POWER_PERCENT = 0.7;
    /**
     * The max number of watt-second (Ws) of power a Host uses.
     */
    private static final int MAX_POWER_WATTS_SEC = 50;
    private static final ConfigReader CONFIG_READER = new ConfigReader();
    private static GlobalMappings MAPPING = new GlobalMappings();
    private final boolean showAllHostUtilizationHistoryEntries;
    private final CloudSim simulation;
    private DatacenterBroker broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    private List<Host> hostList;


    public CloudRunner(boolean showAllHostUtilizationHistoryEntries) {
        Log.setLevel(Level.WARN);
        this.showAllHostUtilizationHistoryEntries = showAllHostUtilizationHistoryEntries;
        simulation = new CloudSim();
        hostList = createHostsFromConfig();
        System.out.println("host size is " + hostList.size());
        datacenter0 = createDataCenter();
        broker0 = new DatacenterBrokerSimple(simulation);
        vmList = createVmsFromConfig();
        System.out.println("vm size is " + vmList.size());
        cloudletList = createCloudLetsFromConfig();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);
        simulation.start();
        System.out.println("------------------------------- SIMULATION FOR SCHEDULING INTERVAL = " + SCHEDULING_INTERVAL + " -------------------------------");
        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets).build();
        printHostsCpuUtilizationAndPowerConsumption();
        printVmsCpuUtilizationAndPowerConsumption();
    }

    @NotNull
    private List<Vm> createVmsFromConfig() {
        List<Vm> list = new ArrayList<>();
        JSONArray Vms = (JSONArray) CONFIG_READER.getProperty("VMS");
        for (Object VmAsObj : Vms) {
            JSONObject VmJSON = (JSONObject) VmAsObj;
            long id = (long) ((JSONObject) VmAsObj).get("id");
            long mipsCapacity = (long) ((JSONObject) VmAsObj).get("mips");
            long numberOfPes = (long) ((JSONObject) VmAsObj).get("cpus");
            long Ram = (long) ((JSONObject) VmAsObj).get("ram");
            long BW = (long) ((JSONObject) VmAsObj).get("bw");
            long Size = (long) ((JSONObject) VmAsObj).get("image-size");
            Vm vm = new VmSimple(id, mipsCapacity, numberOfPes);

            //create cloudletScheduler form config

            String cloudletSchedulerStr = (String) ((JSONObject) VmAsObj).get("cloudletScheduler");

            CloudletScheduler cloudletScheduler = GlobalMappings.getCloudLetScheduler(cloudletSchedulerStr);
            vm.setRam(Ram).setBw(BW).setSize(Size)
                    .setCloudletScheduler(cloudletScheduler); // todo get from config
            vm.getUtilizationHistory().enable();
            list.add(vm);
        }

        return list;
    }

    @NotNull
    private List<Host> createHostsFromConfig() {
        List<Host> hostlist = new ArrayList<>();
        JSONArray hosts = (JSONArray) CONFIG_READER.getProperty("Hosts");
        for (Object hostAsObj : hosts) {
            JSONObject hostAsJson = (JSONObject) hostAsObj;
            Host host = createPowerHost(hostAsJson);
            hostlist.add(host);
        }

        return hostlist;
    }

    @NotNull
    private Host createPowerHost(@NotNull JSONObject hostAsJson) {
        List<Pe> peList = new ArrayList<>();
        JSONArray peJsonArray = (JSONArray) hostAsJson.get("PEs");
        for (Object peAsObj : peJsonArray) {
            JSONObject pe = (JSONObject) peAsObj;
            long mipsCapacity = (long) pe.get("mips");
            peList.add(new PeSimple(mipsCapacity, new PeProvisionerSimple()));
        }
        final PowerModel powerModel = new PowerModelLinear(MAX_POWER_WATTS_SEC, STATIC_POWER_PERCENT);
        final long ram = (long) hostAsJson.get("ram"); //in Megabytes
        final long bw = (long) hostAsJson.get("bw"); //in Megabits/s
        final long storage = (long) hostAsJson.get("storage"); //in Megabytes
        final ResourceProvisioner ramProvisioner = new ResourceProvisionerSimple();
        final ResourceProvisioner bwProvisioner = new ResourceProvisionerSimple();

        // creating VmScheduler from config
        final String vmSchedulerStr = (String) hostAsJson.get("VmScheduler");
        final VmScheduler vmScheduler = GlobalMappings.getVmScheduler(vmSchedulerStr);
        final Host host = new HostSimple(ram, bw, storage, peList);
        // creating power model
        host.setPowerModel(powerModel);
        host
                .setRamProvisioner(ramProvisioner)
                .setBwProvisioner(bwProvisioner)
                .setVmScheduler(vmScheduler);
        return host;
    }

    @NotNull
    private List<Cloudlet> createCloudLetsFromConfig() {
        final List<Cloudlet> list = new ArrayList<>();
        final UtilizationModel utilization = new UtilizationModelDynamic(0.2);
        JSONArray CLOUDLETS = (JSONArray) CONFIG_READER.getProperty("Cloudlets");
        for (Object CloudLetAsObject : CLOUDLETS) {
            //Sets half of the cloudlets with the defined length and the other half with the double of it
            JSONObject CloudLet = (JSONObject) CloudLetAsObject;
            long id = (long) CloudLet.get("id");
            long length = (long) CloudLet.get("length");
            long FileSize = (long) CloudLet.get("fileSize");
            long OutputSize = (long) CloudLet.get("outputSize");
            long CLOUDLET_PES = (long) CloudLet.get("CLOUDLET_PES");
            Cloudlet cloudlet =
                    new CloudletSimple(id, length, CLOUDLET_PES)
                            .setFileSize(FileSize)
                            .setOutputSize(OutputSize)
                            .setUtilizationModelCpu(new UtilizationModelFull())
                            .setUtilizationModelRam(utilization)
                            .setUtilizationModelBw(utilization);
            list.add(cloudlet);
        }
        return list;
    }

    private void printHostsCpuUtilizationAndPowerConsumption() {
        System.out.println();
        for (final Host host : hostList) {
            printHostCpuUtilizationAndPowerConsumption(host);
        }
    }

    private void printHostCpuUtilizationAndPowerConsumption(@NotNull final Host host) {
        System.out.printf("Host %d CPU utilization and power consumption%n", host.getId());
        System.out.println("----------------------------------------------------------------------------------------------------------------------");
        final Map<Double, DoubleSummaryStatistics> utilizationPercentHistory = host.getUtilizationHistory();
        double totalWattsSec = 0;
        double prevUtilizationPercent = -1, prevWattsSec = -1;
        //time difference from the current to the previous line in the history
        double utilizationHistoryTimeInterval;
        double prevTime = 0;
        for (Map.Entry<Double, DoubleSummaryStatistics> entry : utilizationPercentHistory.entrySet()) {
            utilizationHistoryTimeInterval = entry.getKey() - prevTime;
            //The total Host's CPU utilization for the time specified by the map key
            final double utilizationPercent = entry.getValue().getSum();
            final double watts = host.getPowerModel().getPower(utilizationPercent);
            //Energy consumption in the time interval
            final double wattsSec = watts * utilizationHistoryTimeInterval;
            //Energy consumption in the entire simulation time
            totalWattsSec += wattsSec;
            //only prints when the next utilization is different from the previous one, or it's the first one
            if (showAllHostUtilizationHistoryEntries || prevUtilizationPercent != utilizationPercent || prevWattsSec != wattsSec) {
                System.out.printf(
                        "\tTime %8.1f | Host CPU Usage: %6.1f%% | Power Consumption: %8.0f Watts * %6.0f Secs = %10.2f Watt-Sec%n",
                        entry.getKey(), utilizationPercent * 100, watts, utilizationHistoryTimeInterval, wattsSec);
            }
            prevUtilizationPercent = utilizationPercent;
            prevWattsSec = wattsSec;
            prevTime = entry.getKey();
        }

        System.out.printf(
                "Total Host %d Power Consumption in %.0f secs: %.0f Watt-Sec (%.5f KWatt-Hour)%n",
                host.getId(), simulation.clock(), totalWattsSec, PowerAware.wattsSecToKWattsHour(totalWattsSec));
        final double powerWattsSecMean = totalWattsSec / simulation.clock();
        System.out.printf(
                "Mean %.2f Watt-Sec for %d usage samples (%.5f KWatt-Hour)%n",
                powerWattsSecMean, utilizationPercentHistory.size(), PowerAware.wattsSecToKWattsHour(powerWattsSecMean));
        System.out.printf("----------------------------------------------------------------------------------------------------------------------%n%n");
    }

    private void printVmsCpuUtilizationAndPowerConsumption() {
        for (Vm vm : vmList) {
            System.out.println("Vm " + vm.getId() + " at Host " + vm.getHost().getId() + " CPU Usage and Power Consumption");
            System.out.println("----------------------------------------------------------------------------------------------------------------------");
            double vmPower; //watt-sec
            double utilizationHistoryTimeInterval, prevTime = 0;
            final UtilizationHistory history = vm.getUtilizationHistory();
            for (final double time : history.getHistory().keySet()) {
                utilizationHistoryTimeInterval = time - prevTime;
                vmPower = history.powerConsumption(time);
                final double wattsPerInterval = vmPower * utilizationHistoryTimeInterval;
                System.out.printf(
                        "\tTime %8.1f | Host CPU Usage: %6.1f%% | Power Consumption: %8.0f Watt-Sec * %6.0f Secs = %10.2f Watt-Sec%n",
                        time, history.getHostCpuUtilization(time) * 100, vmPower, utilizationHistoryTimeInterval, wattsPerInterval);
                prevTime = time;
            }
            System.out.println();
        }
    }


    @NotNull
    private Datacenter createDataCenter() {
        // get settings from config file
        JSONObject DataCenterJSON = (JSONObject) CONFIG_READER.getProperty("Data-center");
        // creating allocation policy
        String allocationPolicyStr = (String) DataCenterJSON.get("VmAllocationPolicy");
        VmAllocationPolicy allocationPolicy = GlobalMappings.getVmAllocationPolicy(allocationPolicyStr);
        // creating data center
        final Datacenter dc = new DatacenterSimple(simulation, hostList, allocationPolicy);
        dc.setSchedulingInterval(SCHEDULING_INTERVAL);
        return dc;
    }
}