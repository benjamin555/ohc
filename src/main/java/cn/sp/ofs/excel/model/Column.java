package cn.sp.ofs.excel.model;

import java.util.List;


/**
* @author 陈嘉镇
* @version 创建时间：2014-6-12 下午2:43:44
* @email benjaminchen555@gmail.com
*/
public class Column {
	/**
	 * 列名
	 */
	private String name;
	/**
	 * 注释
	 */
	private String comment ;
	
	private String type="varchar";
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	private List<String> datas ;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
	public String getDDL() {
		
		String pattern = name+" "+type ;
		if (comment!=null&&comment.trim().length()>0) {
			pattern+=" COMMENT '"+ comment +"'";
		}
		return pattern;
	}

	public List<String> getDatas() {
		return datas;
	}

	public void setDatas(List<String> datas) {
		this.datas = datas;
	}
	
	
	

}
