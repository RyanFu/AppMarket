package com.dongji.market.pojo;

import java.io.Serializable;
import java.util.List;

public class SubjectInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int subjectId;
	public String title;
	public String subjectIconUrl;
	public String contents;
	public List<SubjectItem> subjectItems;
	public int width;
	public final int what = 1;
}
