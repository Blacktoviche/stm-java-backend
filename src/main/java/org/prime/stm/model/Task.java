package org.prime.stm.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prime.security.model.User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
@Table(name = "TASK")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Task.class)
public class Task implements Serializable{
			
	     
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long status = IN_PROGRESS_STATUS;
    private Date dateCreated = new Date();
    private Date lastModefied = new Date();
    private Date duDate;
    private Long progress = 0L;
    
    @ManyToOne
    private Project project;
    @ManyToOne
    private User createdBy;
    @ManyToOne
    private User modefiedBy;
    @ManyToOne
    private Task parent;
    
    /*@LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany
    @JoinTable(
            name = "USER_TASK",
            joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "TASK_ID", referencedColumnName = "ID")})*/
    @Transient
    private ObjectNode statistics;
    @Transient
    private List<Long> assignedToUsersIds;
    @Transient
    private List<Attachment> attachments;
    
    public static final Long IN_PROGRESS_STATUS = 0L;
    public static final Long COMPLETED_STATUS = 1L;
    //public static final Long CLOSED_STATUS = 2L;
    /*@Transient
    private String createdByName;
    @Transient
    private String modefiedByName;*/
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getStatus() {
		return status;
	}
	public void setStatus(Long status) {
		this.status = status;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getLastModefied() {
		return lastModefied;
	}
	public void setLastModefied(Date lastModefied) {
		this.lastModefied = lastModefied;
	}
	public Date getDuDate() {
		return duDate;
	}
	public void setDuDate(Date duDate) {
		this.duDate = duDate;
	}
	public Long getProgress() {
		return progress;
	}
	public void setProgress(Long progress) {
		this.progress = progress;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public User getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	public Task getParent() {
		return parent;
	}
	public void setParent(Task parent) {
		this.parent = parent;
	}
	
	public List<Long> getAssignedToUsersIds() {
		return assignedToUsersIds;
	}
	public void setAssignedToUsersIds(List<Long> assignedToUsersIds) {
		this.assignedToUsersIds = assignedToUsersIds;
	}
	public List<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	public User getModefiedBy() {
		return modefiedBy;
	}
	public void setModefiedBy(User modefiedBy) {
		this.modefiedBy = modefiedBy;
	}
	public ObjectNode getStatistics() {
		return statistics;
	}
	public void setStatistics(ObjectNode statistics) {
		this.statistics = statistics;
	}
	
	/*public String getCreatedByName() {
		return createdBy.getUsername();
	}
	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}
	public String getModefiedByName() {
		return modefiedBy.getUsername();
	}
	public void setModefiedByName(String modefiedByName) {
		this.modefiedByName = modefiedByName;
	}*/
}
