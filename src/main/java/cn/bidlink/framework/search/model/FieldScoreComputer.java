package cn.bidlink.framework.search.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="indexer_field_score_computer")
public class FieldScoreComputer {
	
	private Long id;
	private String computerName;
	private String computerClassName;
	private String params;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="computerName")
	public String getComputerName() {
		return computerName;
	}
	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}
	@Column(name="computerClassName")
	public String getComputerClassName() {
		return computerClassName;
	}
	public void setComputerClassName(String computerClassName) {
		this.computerClassName = computerClassName;
	}
	@Column(name="params")
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
}
