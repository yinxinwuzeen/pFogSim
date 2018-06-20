/**
 * Node Class for modeling a particular location in the network
 * @author jih0007
 */
package edu.auburn.pFogSim.netsim;

import java.util.ArrayList;
import java.util.List;
import edu.auburn.pFogSim.netsim.Link;
import edu.auburn.pFogSim.Exceptions.BadLinkException;
import javafx.util.Pair;

public class NodeSim {
	
	private ArrayList<Link> edges;
	private Pair<Integer, Integer> myLocation;
	private int level;
	private boolean wifiAccess;
	private boolean moving;
	private Pair<Integer, Integer> vector;
	private int wlan_id;
	/**
	 * Constructor
	 * @param inputEdges
	 * @param coord
	 */
	public NodeSim (List<Link> inputEdges, Pair<Integer, Integer> coord) {
		myLocation = coord;
		for (Link edge : inputEdges) {
			addLink(edge);
		}
	}
	public NodeSim() {
		edges = new ArrayList<Link>();
		myLocation = new Pair<Integer, Integer>(null, null);
	}
	public NodeSim(int xin, int yin) {
		edges = new ArrayList<Link>();
		myLocation = new Pair<Integer, Integer>(xin, yin);
	}
	
	public NodeSim(int xin, int yin, int level, int id, boolean isAccessPoint) {
		wlan_id = id;
		edges = new ArrayList<Link>();
		myLocation = new Pair<Integer, Integer>(xin, yin);
		this.level = level;
		this.wifiAccess = isAccessPoint;
	}
	
	public NodeSim(int xin, int yin, int level, int id, boolean isAccessPoint, boolean isMoving) {
		wlan_id = id;
		edges = new ArrayList<Link>();
		myLocation = new Pair<Integer, Integer>(xin, yin);
		this.level = level;
		this.wifiAccess = isAccessPoint;
		if(isMoving == true)
			throw new IllegalArgumentException("Please use the correct NodeSim constructor for a moving object. This one assumes the FOG Device is stationary");
		this.moving = isMoving;
		this.vector = new Pair<Integer, Integer>(0,0);
	}
	
	public NodeSim(int xin, int yin, int level, int id, boolean isAccessPoint, boolean isMoving, Pair<Integer, Integer> _vector) {
		wlan_id = id;
		edges = new ArrayList<Link>();
		myLocation = new Pair<Integer, Integer>(xin, yin);
		this.level = level;
		this.wifiAccess = isAccessPoint;
		//There is a way better way of doing this construction but I will return to fix it later
		if(isMoving == false)
			throw new IllegalArgumentException("Please use the correct NodeSim constructor for a moving object. This one assumes the FOG Device is moving and needs the vector.");
		this.moving = isMoving;
		this.vector = _vector;
	}
	
	/**
	 * tests to make sure that at least on of the endpoints for the given link is at this node
	 * @param in
	 * @return true/false
	 */
	public boolean validateLink(Link in) {
		if (in.getLeftLink().equals(myLocation) || in.getRightLink().equals(myLocation)) {
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * set myLocation by coords
	 * @param xin
	 * @param yin
	 */
	public void setLocation(int xin, int yin) {
		myLocation = new Pair<Integer, Integer>(xin, yin);
	}
	/**
	 * set myLocation by designating a coordinate
	 * @param in
	 */
	public void setLocation(Pair<Integer, Integer> in) {
		myLocation = in;
	}
	/**
	 * get the coordinate for this node
	 * @return
	 */
	public Pair<Integer, Integer> getLocation() {
		return myLocation;
	}
	/**
	 * get the edges for this node
	 * @return
	 */
	public ArrayList<Link> getEdges() {
		return edges;
	}
	/**
	 * make sure that all edges on this node are connected to this node
	 * @return the offending invalid link if one is found<br>
	 * returns null if all links are valid
	 */
	public Link validateNode() {
		for (int i = 0; i < edges.size(); i++) {
			if (!validateLink(edges.get(i))) {
				return edges.get(i);
			}
		}
		//SimLogger.printLine("returned null");
		return null;
	}
	/**
	 * remove a link from this node
	 * @param victim
	 * @return true if the link was removed<br>
	 * false if the input is null
	 * throw IllegalArgumentException if the link does not exist
	 */
	public boolean removeLink(Link victim) {
		if (victim == null) {
			//SimLogger.printLine("removeLink return false");
			return false;
		}
		for (Link edge : edges) {
			if (edge.equals(victim)) {
				edges.remove(victim);
				//SimLogger.printLine("removeLink return true");
				return true;
			}
		}
		throw new IllegalArgumentException();
	}
	/**
	 * add a link to this node<br>
	 * if the link is already on this node do nothing<br>
	 * if there exists a link on this node with the same exact endpoints but different latencies throw IllegalArgumentException<br>
	 * if the link is invalid for this node throw BadLinkException
	 * @param in
	 */
	public void addLink(Link in) {
		if (validateLink(in)) {
			for (int i = 0; i < edges.size(); i++) {
				if (in.equals(edges.get(i))) {
					return;//link already exists
				}
				else if (in.equalEndPoints(edges.get(i))) {
					//adding a link whose endpoints exist but with different latencies
					throw new IllegalArgumentException();
				}
			}
			edges.add(in);
		}
		else {
			throw new BadLinkException();
		}
	}
	/**
	 * travel from this node to an adjacent node
	 * @param dest
	 * @return the latency to travel to the dest node<br>
	 * -1 if the dest node is not adjacent
	 */
	public double traverse(NodeSim dest) {
		for (Link edge : edges) {
			if (edge.getOutgoingLink(getLocation()).equals(dest.getLocation())) {
				return edge.getOutgoingLat(getLocation());
			}
		}
		return -1.0;
	}
	/**
	 * tests nodes for equality based on location and links
	 * @param in
	 * @return
	 */
	public boolean equals(NodeSim in) {
		if(getLocation() != in.getLocation()) {
			return false;
		}
		else if (getEdges().size() != in.getEdges().size()) {
			return false;
		}
		else {
			for (int i = 0; i < getEdges().size(); i++) {
				if (getEdges().get(i) != in.getEdges().get(i) ) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * set the puddle level
	 * @param _level
	 */
	public void setLevel(int _level) {
		this.level = _level;
	}
	/**
	 * get the puddle level
	 * @return
	 */
	public int getLevel() {
		return this.level;
	}
	/**
	 * is this node a designated wifi access point
	 * @return
	 */
	public boolean isWifiAcc() {
		return wifiAccess;
	}
	/**
	 * is this a mobile fog device
	 * @return
	 */
	public boolean isMoving() {
		return moving;
	}
	/**
	 * set the motion vector for this device, if it is mobile
	 * @param _vector
	 */
	public void setVector(Pair<Integer, Integer> _vector)
	{
		this.vector = _vector;
	}
	/**
	 * get the motion vector for this device, if it is mobile
	 * @return
	 */
	public Pair<Integer, Integer> getVector() 
	{
		return this.vector;
	}
	/**
	 * set whether this node is a wifi access point
	 * @param wifi
	 */
	public void setWifi(boolean wifi) {
		wifiAccess = wifi;
	}
	/**
	 * set the wlanid associated with this node
	 * @param id
	 */
	public void setWlanId(int id)
	{
		this.wlan_id = id;
	}
	/**
	 * get the wlanid associated with this node
	 * @return
	 */
	public int getWlanId()
	{
		return this.wlan_id;
	}
	/**
	 * toString() for node returns the x and y coordinate of the node as a string of the form "x, y"
	 */
	public String toString() {
		return getLocation().getKey() + ", " + getLocation().getValue();
	}
}
