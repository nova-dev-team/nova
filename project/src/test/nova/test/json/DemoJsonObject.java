package nova.test.json;

class DemoJsonObject {

	public String text;

	public Integer value;

	public DemoJsonObject() {
		this.text = null;
		this.value = null;
	}

	public DemoJsonObject(String text, Integer value) {
		this.text = text;
		this.value = value;
	}

	@Override
	public String toString() {
		return this.text + ", " + value;
	}

}