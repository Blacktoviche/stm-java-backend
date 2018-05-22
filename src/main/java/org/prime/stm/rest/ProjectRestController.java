package org.prime.stm.rest;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.prime.security.core.JwtUser;
import org.prime.security.model.User;
import org.prime.security.repository.UserRepository;
import org.prime.stm.model.Project;
import org.prime.stm.model.Task;
import org.prime.stm.model.UserTask;
import org.prime.stm.repository.ProjectRepository;
import org.prime.stm.repository.TaskRepository;
import org.prime.stm.repository.UserTaskRepository;
import org.prime.util.ControllerUtils;
import org.prime.util.Message;
import org.prime.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@CrossOrigin
@RestController
@RequestMapping("/api/")
public class ProjectRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserTaskRepository userTaskRepository;
	@Autowired
	private ControllerUtils controllerUtils;

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "projects/{count}", method = RequestMethod.GET)
	public List<Project> getLatestProjects(@PathVariable int count) {
		if (count > 0) {
			return projectRepository.findAll(PageRequest.of(0, count, Sort.Direction.DESC, "dateCreated")).getContent();
		} else {
			return projectRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreated"));
		}
	}
	
	@RequestMapping(path = "project/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getProject(@PathVariable Long id) {
		Optional<Project> optProject = projectRepository.findById(id);
		if (optProject.isPresent()) {
			return new ResponseEntity<Project>(optProject.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Project not found!"), HttpStatus.NOT_FOUND);
		}
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "project/{id}", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project project) {
		Optional<Project> projOptional = projectRepository.findById(id);
		if (projOptional.isPresent()) {
			project.setId(id);
			project.setCreatedBy(projOptional.get().getCreatedBy());
			project.setDateCreated(projOptional.get().getDateCreated());
			project.setLastModefied(new Date());
			project.setProgress(projOptional.get().getProgress());
			project.setModefiedBy(userRepository.findByUsername(
					((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).get());
			projectRepository.save(project);
			return new ResponseEntity<Message>(new Message("Project update success"), HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Project not found!", HttpStatus.NOT_FOUND);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "project", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> addProject(@RequestBody Project project) {

		User currentUser = userRepository.findByUsername(
				((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).get();
		project.setId(null);
		project.setCreatedBy(currentUser);
		project.setDateCreated(new Date());
		project.setLastModefied(new Date());
		projectRepository.save(project);

		project.getTasks().forEach(task -> {
			task.setProject(project);
			task.setCreatedBy(currentUser);
			task.setDateCreated(new Date());
			task.setLastModefied(new Date());
			task.setProgress(0L);
			taskRepository.save(task);
			task.getAssignedToUsersIds().forEach(userId -> {
				Optional<User> userOptional = userRepository.findById(userId);
				if (userOptional.isPresent()) {
					UserTask userTask = new UserTask();
					userTask.setTask(task);
					userTask.setUser(userOptional.get());
					userTaskRepository.save(userTask);
				}
			});

		});

		return new ResponseEntity<Message>(new Message("Project add success"), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "project/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<?> deleteProject(@PathVariable Long id) {
		Optional<Project> projOptional = projectRepository.findById(id);
		if (projOptional.isPresent()) {
			projectRepository.deleteById(id);
			logger.info("project deleted {} ", id);
			return new ResponseEntity<Message>(new Message("Project delete success"), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Project not found!"), HttpStatus.NOT_FOUND);
		}

	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "projectstatistics/{id}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getProjectStatistics(@PathVariable Long id) {
		Optional<Project> projOptional = projectRepository.findById(id);
		if (projOptional.isPresent()) {

			Project project = projOptional.get();
			project.setTasks(taskRepository.findAllByProjectId(id));
			
			ObjectNode bodyNode = JsonNodeFactory.instance.objectNode();

			Long completedTasks = taskRepository.countTasksByStatus(id, Task.COMPLETED_STATUS);
			Long inProgressTasks = taskRepository.countTasksByStatus(id, Task.IN_PROGRESS_STATUS);
			//Long closedTasks = taskRepository.countTasksByStatus(id, Task.CLOSED_STATUS);

			bodyNode.put(Utils.COMPLETED_TASKS, completedTasks);
			bodyNode.put(Utils.INP_ROGRESS_TASKS, inProgressTasks);
			

			bodyNode.set(Utils.USER_PROGRESS, getProjectUsersProgressDetail(id));

			project.setStatistics(bodyNode);

			return new ResponseEntity<Project>(project, HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Project not found!"), HttpStatus.NOT_FOUND);
		}
	}

	private ArrayNode getProjectUsersProgressDetail(Long projectId) {

		ArrayNode usersProgressArray = JsonNodeFactory.instance.arrayNode();
		List<Task> tasks = taskRepository.findAllByProjectId(projectId);
		Set<User> projectUsers = new HashSet();

		for (Task task : tasks) {
			projectUsers.addAll(userRepository.getUsersByTask(task.getId()));
		}

		logger.info("Users: " + projectUsers.size());
		for (User user : projectUsers) {
			logger.info("User: " + user.getUsername());
			ObjectNode userNode = JsonNodeFactory.instance.objectNode();
			userNode.put(Utils.USERNAME, user.getUsername());
			userNode.put(Utils.PROGRESS, controllerUtils.getUserProgressByProject(projectId, user.getId()));
			usersProgressArray.add(userNode);
		}

		return usersProgressArray;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "projectdetail/{id}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getProjectDetail(@PathVariable Long id) {
		Optional<Project> projOptional = projectRepository.findById(id);
		if (projOptional.isPresent()) {

			Project project = projOptional.get();
			project.setTasks(taskRepository.findAllByProjectId(id));

			return new ResponseEntity<Project>(project, HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Project not found"), HttpStatus.NOT_FOUND);
		}
	}

	//
	@RequestMapping(path = "myprojects", method = RequestMethod.GET)
	public List<Project> getUserProjects() {
		User currentUser = userRepository.findByUsername(
				((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).get();
		
		List<Project> userProjects = projectRepository.getUserProjects(currentUser.getId());
		
		userProjects.forEach(project -> {
			ObjectNode bodyNode = JsonNodeFactory.instance.objectNode();

			logger.info("myProjectsProgress : {} ", controllerUtils.getUserProgressByProject(project.getId(), currentUser.getId()));

			bodyNode.put("myProgress", controllerUtils.getUserProgressByProject(project.getId(), currentUser.getId()));
			project.setStatistics(bodyNode);
		});

		return userProjects;
	}

	@RequestMapping(path = "mylatestprojects/{count}", method = RequestMethod.GET)
	public List<Project> getMyLatestProjects(@PathVariable int count) {
		User currentUser = userRepository.findByUsername(
				((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).get();
		if (count > 0) {
			return projectRepository.getUserProjects(currentUser.getId(), PageRequest.of(0, count, Sort.Direction.DESC, "dateCreated")).getContent();
		} else {
			return projectRepository.getUserProjects(currentUser.getId(), Sort.by(Sort.Direction.DESC, "dateCreated"));
		}
	}

}
