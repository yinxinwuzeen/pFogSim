/*
 * Title:        pFogSim derived from EdgeCloudSim VectorMobility Model
 * 
 * Description: 
 * Simulates where the lowest-level devices (such as mobile devices) will be in the simulation space
 * which extends from (-1 * (MAX_WIDTH / 2) to MAX_WIDTH / 2 to make it MAX_WIDTH wide and permit
 * negative coordinates to resemble GPS coordinates as much as possible.
 * Devices are placed at a random Wireless Access Point (WAP) and given a random vector to move in.
 * It updates which access point a device is connected to by using the Voronoi Diagram to organize
 * the simulation space.
 * 
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 */

package edu.auburn.pFogSim.mobility;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.auburn.pFogSim.Voronoi.src.kn.uni.voronoitreemap.diagram.PowerDiagram;
import edu.auburn.pFogSim.Voronoi.src.kn.uni.voronoitreemap.j2d.Site;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.orchestrator.PuddleOrchestrator;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class VectorMobility extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private int MAX_WIDTH;
	private int MAX_HEIGHT;
	private NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();

	
	public VectorMobility(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initialize() {
		this.MAX_HEIGHT = SimManager.MAX_HEIGHT;
		this.MAX_WIDTH = SimManager.MAX_WIDTH;
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
				
		//Go through network's list of nodes and pick out just the wireless access points
		ArrayList<NodeSim> accessPoints = new ArrayList<NodeSim>();
		for(NodeSim node : network.getNodes())
		{
			if(node.isWifiAcc()) 
				accessPoints.add(node);
		}
			
		//initialize tree maps and position of mobile devices
		for(int i=0; i<numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());
			
			//Picks a random wireless access point to start at
			int randDatacenterId = SimUtils.getRandomNumber(0, accessPoints.size()-1);
			int wlan_id = accessPoints.get(randDatacenterId).getWlanId();
			double x_pos = accessPoints.get(randDatacenterId).getLocation().getXPos();
			double y_pos = accessPoints.get(randDatacenterId).getLocation().getYPos();
			
			//start locating user from 10th seconds
			treeMapArray.get(i).put((double)10, new Location(wlan_id, x_pos, y_pos));
		}

		for(int i=0; i<numberOfMobileDevices; i++) {
			TreeMap<Double, Location> treeMap = treeMapArray.get(i);
			//Make random numbers to make the vectors
			double up = 5 * (Math.random() - 0.5);
			double right = 5 * (Math.random() - 0.5);

			while(treeMap.lastKey() < SimSettings.getInstance().getSimulationTime()) {		
				
				
				double x_pos = treeMap.lastEntry().getValue().getXPos();
				double y_pos = treeMap.lastEntry().getValue().getYPos();				
				int wlan_id = treeMap.lastEntry().getValue().getServingWlanId();
				  
				if(x_pos + right > this.MAX_WIDTH / 2.0) right = right * -1;
				if(y_pos + up > this.MAX_HEIGHT / 2.0) up = up * -1;
				
				//If we are still in the same polygon, don't change (We haven't moved out of range of the wap)
				PowerDiagram diagram = SimManager.getInstance().getVoronoiDiagramAtLevel(0);

				if (SimManager.getInstance().getEdgeOrchestrator() instanceof PuddleOrchestrator) {
					for(Site site : diagram.getSites())
					{
						if(site.getPolygon().contains(x_pos, y_pos))
						{
							//We know that the site.getX and Y pos is location of WAP
							//Find wlan id to assign
							wlan_id = (network.findNode(new Location(site.getX(), site.getY()), true)).getWlanId();
						}
					}
				}
				//This first argument kind of dictates the speed at which the device moves, higher it is, slower the devices are
				//	smaller value in there, the more it updates
				//As it is now, allows devices to change wlan_ids around 600 times in an hour
				treeMap.put(treeMap.lastKey() + 50.0, new Location(wlan_id, x_pos + right, y_pos + up));		
			}
		}
	}

	

	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
		
		Entry<Double, Location> e = treeMap.floorEntry(time);
	    
	    if(e == null){
	    	SimLogger.printLine("impossible is occured! no location is found for the device!");
	    	System.exit(0);
	    }
	    
		return e.getValue();
	}
	public int getWlanId(int deviceId, double time) 
	{
		int wlan_id = -1;
		
		if(time >= 0 && deviceId >= 0)
		{	
			TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
			
			Entry<Double, Location> e = treeMap.floorEntry(time);
			
			try {
				wlan_id = e.getValue().getServingWlanId();
			} catch (NullPointerException exce)
			{
				SimLogger.printLine("NullPointerException at time : " + time + "\n\tFor Device #: " + deviceId);
				throw new NullPointerException();
			}
		}
		else throw new IllegalArgumentException();
		return wlan_id;
	}
	
	public int getWlanId(int deviceId) 
	{
		int wlan_id = -1;
		
		if(deviceId >= 0)
		{	
			TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
			Entry<Double, Location> e = treeMap.floorEntry(20.0); //This 20.0 is rather arbitrary, just gives 'starting' WlanId connection
			wlan_id = e.getValue().getServingWlanId();
		}
		else throw new IllegalArgumentException();
		return wlan_id;
	}
	
	public int getSize()
	{
		return treeMapArray.size();
	}
}