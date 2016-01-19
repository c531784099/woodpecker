package cn.bidlink.framework.search.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * 属性总分记录
 * */
@Entity
@Table(name="indexer_field_score")
public class FieldScore {
	
	private Long id;
	private Long indexId;
	private Long fieldId;
	private String score;
	private FieldScoreComputer fieldScoreComputer;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="indexerId")
	public Long getIndexId() {
		return indexId;
	}
	public void setIndexId(Long indexId) {
		this.indexId = indexId;
	}
	@Column(name="indexerFieldId")
	public Long getFieldId() {
		return fieldId;
	}
	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}
	@Column(name="score")
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="computerId")
	public FieldScoreComputer getFieldScoreComputer() {
		return fieldScoreComputer;
	}
	public void setFieldScoreComputer(FieldScoreComputer fieldScoreComputer) {
		this.fieldScoreComputer = fieldScoreComputer;
	}
}
