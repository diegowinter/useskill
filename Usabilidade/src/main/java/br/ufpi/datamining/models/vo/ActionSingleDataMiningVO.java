package br.ufpi.datamining.models.vo;

import java.util.List;

import br.ufpi.datamining.models.ActionSingleDataMining;
import br.ufpi.datamining.models.FieldSearchTupleDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.enums.ActionTypeDataMiningEnum;
import br.ufpi.datamining.models.enums.MomentTypeActionDataMiningEnum;

public class ActionSingleDataMiningVO {

	private Long id;
	private TaskDataMining task;
	private ActionTypeDataMiningEnum actionType;
	private MomentTypeActionDataMiningEnum momentType;
	private List<FieldSearchTupleDataMining> elementFiedlSearch;
	private List<FieldSearchTupleDataMining> urlFieldSearch;
	
	public ActionSingleDataMiningVO(ActionSingleDataMining action) {
		super();
		this.id = action.getId();
		this.task = action.getTask();
		this.actionType = action.getActionType();
		this.momentType = action.getMomentType();
		this.elementFiedlSearch = action.getElementFiedlSearch();
		this.urlFieldSearch = action.getUrlFieldSearch();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public TaskDataMining getTask() {
		return task;
	}
	public void setTask(TaskDataMining task) {
		this.task = task;
	}
	public ActionTypeDataMiningEnum getActionType() {
		return actionType;
	}
	public void setActionType(ActionTypeDataMiningEnum actionType) {
		this.actionType = actionType;
	}
	public MomentTypeActionDataMiningEnum getMomentType() {
		return momentType;
	}
	public void setMomentType(MomentTypeActionDataMiningEnum momentType) {
		this.momentType = momentType;
	}
	public List<FieldSearchTupleDataMining> getElementFiedlSearch() {
		return elementFiedlSearch;
	}
	public void setElementFiedlSearch(
			List<FieldSearchTupleDataMining> elementFiedlSearch) {
		this.elementFiedlSearch = elementFiedlSearch;
	}
	public List<FieldSearchTupleDataMining> getUrlFieldSearch() {
		return urlFieldSearch;
	}
	public void setUrlFieldSearch(List<FieldSearchTupleDataMining> urlFieldSearch) {
		this.urlFieldSearch = urlFieldSearch;
	}
	
}