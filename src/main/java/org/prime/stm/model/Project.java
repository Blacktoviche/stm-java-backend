package org.prime.stm.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.prime.security.model.User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Entity
@Table(name = "PROJECT")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Project implements Serializable{

	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Long id;
	    private String name;
	    private String description;
	    private Date dateCreated = new Date();
	    private Date lastModefied = new Date();
	    private Long progress = 0L;
	    
	    //@JsonIgnore
	    @ManyToOne
	    private User createdBy;
	    //@JsonIgnore
	    @ManyToOne
	    private User modefiedBy;
	    
	    /*@Transient
	    private String createdByName;
	    @Transient
	    private String modefiedByName;*/
	    
	    @Transient
	    private ObjectNode statistics;
	    @Transient
	    private List<Task> tasks;
	    
	    public Project() {}
	    
	    
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
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
		
		public Long getProgress() {
			return progress;
		}
		public void setProgress(Long progress) {
			this.progress = progress;
		}
		public User getCreatedBy() {
			return createdBy;
		}
		public void setCreatedBy(User createdBy) {
			this.createdBy = createdBy;
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

		public List<Task> getTasks() {
			return tasks;
		}

		public void setTasks(List<Task> tasks) {
			this.tasks = tasks;
		}
	   
}
