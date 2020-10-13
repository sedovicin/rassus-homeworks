package hr.fer.rassus.homework1.homework1_Client;

/**
 * Class that represents data acquired by the sensor
 *
 * @author Sebastian
 *
 */
public class SensorData {

	private Double temperature = null;
	private Double pressure = null;
	private Double humidity = null;
	private Double CO = null;
	private Double NO2 = null;
	private Double SO2 = null;

	/**
	 * Creates new instance of object by parsing the data string
	 *
	 * @param entry String to be parsed
	 */
	public SensorData(final String entry) {
		if (entry != null) {
			String[] splittedEntry = entry.split(",", -1);
			if (!((splittedEntry[0] == null) || splittedEntry[0].isEmpty())) {
				temperature = Double.valueOf(splittedEntry[0]);
			}
			if (!((splittedEntry[1] == null) || splittedEntry[1].isEmpty())) {
				pressure = Double.valueOf(splittedEntry[1]);
			}
			if (!((splittedEntry[2] == null) || splittedEntry[2].isEmpty())) {
				humidity = Double.valueOf(splittedEntry[2]);
			}
			if (!((splittedEntry[3] == null) || splittedEntry[3].isEmpty())) {
				CO = Double.valueOf(splittedEntry[3]);
			}
			if (!((splittedEntry[4] == null) || splittedEntry[4].isEmpty())) {
				NO2 = Double.valueOf(splittedEntry[4]);
			}
			if (!((splittedEntry[5] == null) || splittedEntry[5].isEmpty())) {
				SO2 = Double.valueOf(splittedEntry[5]);
			}
		}

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

	/**
	 * Does the average of all data with another set of data. Modifies this object
	 *
	 * @param anotherData another data to be averaged with
	 */
	public void calibrate(final SensorData anotherData) {
		if (this.temperature != null) {
			if (anotherData.temperature != null) {
				this.temperature = (this.temperature + anotherData.temperature) / 2;
			}
		}
		if (this.pressure != null) {
			if (anotherData.pressure != null) {
				this.pressure = (this.pressure + anotherData.pressure) / 2;
			}
		}
		if (this.humidity != null) {
			if (anotherData.humidity != null) {
				this.humidity = (this.humidity + anotherData.humidity) / 2;
			}
		}
		if (this.CO != null) {
			if (anotherData.CO != null) {
				this.CO = (this.CO + anotherData.CO) / 2;
			}
		}
		if (this.NO2 != null) {
			if (anotherData.NO2 != null) {
				this.NO2 = (this.NO2 + anotherData.NO2) / 2;
			}
		}
		if (this.SO2 != null) {
			if (anotherData.SO2 != null) {
				this.SO2 = (this.SO2 + anotherData.SO2) / 2;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(temperature == null ? "" : temperature).append(",").append(pressure == null ? "" : pressure)
				.append(",").append(humidity == null ? "" : humidity).append(",").append(CO == null ? "" : CO)
				.append(",").append(NO2 == null ? "" : NO2).append(",").append(SO2 == null ? "" : SO2).toString();
	}

}
