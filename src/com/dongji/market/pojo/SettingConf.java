package com.dongji.market.pojo;

public class SettingConf {

	private int _id;
	private String name;
	private int value;
	
	public SettingConf() {}
	
	public SettingConf(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "SettingConf [_id=" + _id + ", name=" + name + ", value="
				+ value + "]";
	}
	
}
