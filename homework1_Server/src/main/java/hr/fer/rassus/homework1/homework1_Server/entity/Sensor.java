package hr.fer.rassus.homework1.homework1_Server.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Sensor {

	@Id
	@GeneratedValue
	private Long id;

	private String username;

	private Double latitude, longitude;

	private String IPAddress;

	private Integer port;

	public Sensor() {

	}

	public Sensor(final String username, final Double latitude, final Double longitude, final String IPAddress,
			final Integer port) {
		this.username = username;
		this.latitude = latitude;
		this.longitude = longitude;
		this.IPAddress = IPAddress;
		this.port = port;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(final Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(final Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the iPAddress
	 */
	public String getIPAddress() {
		return IPAddress;
	}

	/**
	 * @param iPAddress the iPAddress to set
	 */
	public void setIPAddress(final String IPAddress) {
		this.IPAddress = IPAddress;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(final Integer port) {
		this.port = port;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(username).append(" ").append(latitude).append(" ").append(longitude).append(" ")
				.append(IPAddress).append(":").append(port).toString();
	}

}
