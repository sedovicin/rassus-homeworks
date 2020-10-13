/**
 *
 */
package hr.fer.rassus.homework1.homework1_Server.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Sebastian
 *
 */
@Entity
public class SensorData {

	@Id
	private Long id;

	private Double temperature, pressure, humidity, CO, NO2, SO2;

	public SensorData() {

	}

	public SensorData(final Long id, final Double temperature, final Double pressure, final Double humidity,
			final Double CO, final Double NO2, final Double SO2) {
		this.id = id;
		this.temperature = temperature;
		this.pressure = pressure;
		this.humidity = humidity;
		this.CO = CO;
		this.NO2 = NO2;
		this.SO2 = SO2;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(final Double temperature) {
		this.temperature = temperature;
	}

	public Double getPressure() {
		return pressure;
	}

	public void setPressure(final Double pressure) {
		this.pressure = pressure;
	}

	public Double getHumidity() {
		return humidity;
	}

	public void setHumidity(final Double humidity) {
		this.humidity = humidity;
	}

	public Double getCO() {
		return CO;
	}

	public void setCO(final Double CO) {
		this.CO = CO;
	}

	public Double getNO2() {
		return NO2;
	}

	public void setNO2(final Double NO2) {
		this.NO2 = NO2;
	}

	public Double getSO2() {
		return SO2;
	}

	public void setSO2(final Double SO2) {
		this.SO2 = SO2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(temperature == null ? "" : temperature).append(",").append(pressure == null ? "" : pressure)
				.append(",").append(humidity == null ? "" : humidity).append(",").append(CO == null ? "" : CO)
				.append(",").append(NO2 == null ? "" : NO2).append(",").append(SO2 == null ? "" : SO2).toString();
	}

}
