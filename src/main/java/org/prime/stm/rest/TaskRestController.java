package org.prime.stm.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@CrossOrigin
@RestController
@RequestMapping("/api/")
public class TaskRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserTaskRepository userTaskRepository;
	@Autowired
	private ControllerUtils controllerUtils;

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "tasks/{count}", method = RequestMethod.GET)
	public List<Task> getLatestTasks(@PathVariable int count) {
		if (count > 0) {
			return taskRepository.findAll(PageRequest.of(0, count, Sort.Direction.DESC, "dateCreated")).getContent();
		} else {
			return taskRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreated"));
		}
	}

	@RequestMapping(path = "mylatesttasks/{count}", method = RequestMethod.GET)
	public List<Task> getMyLatestTasks(@PathVariable int count) {
		User currentUser = userRepository.findByUsername(
				((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()).get();
		if (count > 0) {
			return taskRepository.getUserTasks(currentUser.getId(), PageRequest.of(0, count, Sort.Direction.DESC, "task.dateCreated")).getContent();
		} else {
			return taskRepository.getUserTasks(currentUser.getId(), Sort.by(Sort.Direction.DESC, "task.dateCreated"));
		}
	}
	
	@RequestMapping(path = "tasks/project/{id}", method = RequestMethod.GET)
	public List<Task> getTasksByProject(@PathVariable Long id) {
		return taskRepository.findAllByProjectId(id);
	}

	@RequestMapping(path = "task/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getTask(@PathVariable Long id) {
		Optional<Task> optTask = taskRepository.findById(id);
		if (optTask.isPresent()) {
			return new ResponseEntity<Task>(optTask.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Task not found!"), HttpStatus.NOT_FOUND);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "task/{id}", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task task) {

		Optional<Task> taskOptional = taskRepository.findById(task.getId());
		if (taskOptional.isPresent()) {
			Optional<Project> projectOptional = projectRepository.findById(id);
			if (projectOptional.isPresent()) {
				task.setProject(projectOptional.get());
				task.setCreatedBy(taskOptional.get().getCreatedBy());
				task.setLastModefied(new Date());
				task.setProgress(taskOptional.get().getProgress());
				task.setModefiedBy(userRepository.findByUsername(
						((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
						.get());
				taskRepository.save(task);
				logger.info("task updated {} ", task.getTitle());

				userTaskRepository.findAllByTaskId(task.getId()).forEach(userTaskId -> {
					userTaskRepository.deleteById(userTaskId);
				});

				task.getAssignedToUsersIds().forEach(userId -> {
					UserTask userTask = new UserTask();
					userTask.setTask(task);
					userTask.setUser(userRepository.findById(userId).get());
					userTaskRepository.save(userTask);
				});

			} else {
				return new ResponseEntity<Message>(new Message("Project not found!"), HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<Message>(new Message("Task update success"), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Task not found!"), HttpStatus.NOT_FOUND);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "task/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Message> addTask(@PathVariable Long id, @RequestBody Task task) {

		Optional<Project> optionalProject = projectRepository.findById(id);
		if (optionalProject.isPresent()) {
			task.setId(null);
			task.setCreatedBy(userRepository.findByUsername(
					((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
					.get());
			task.setDateCreated(new Date());
			task.setLastModefied(new Date());
			task.setProject(optionalProject.get());
			taskRepository.save(task);
			logger.info("assigned users Ids  {}", task.getAssignedToUsersIds());
			task.getAssignedToUsersIds().forEach(userId -> {
				UserTask userTask = new UserTask();
				userTask.setTask(task);
				userTask.setUser(userRepository.findById(userId).get());
				userTask = userTaskRepository.save(userTask);
				logger.info("usertask added  {}", userTask);
			});
			controllerUtils.updateProjectProgress(id);
			logger.info("task added {} ", task.getTitle());
			return new ResponseEntity<Message>(new Message("Task add success"), HttpStatus.OK);

		} else {
			return new ResponseEntity<Message>(new Message("Project not found!"), HttpStatus.NOT_FOUND);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "task/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<?> deleteTask(@PathVariable Long id) {
		Optional<Task> taskOptional = taskRepository.findById(id);
		if (taskOptional.isPresent()) {
			taskRepository.deleteById(id);
			logger.info("task deleted {} ", id);
			controllerUtils.updateProjectProgress(taskOptional.get().getProject().getId());
			return new ResponseEntity<Message>(new Message("Task delete success"), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Task not found!"), HttpStatus.NOT_FOUND);
		}

	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "task/{id}/users", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getTaskUsers(@PathVariable Long id) {
		Optional<Task> taskOptional = taskRepository.findById(id);
		if (taskOptional.isPresent()) {

			ObjectNode usersNode = JsonNodeFactory.instance.objectNode();
			ArrayNode selectedTaskUsers = JsonNodeFactory.instance.arrayNode();
			ArrayNode notSelectedTaskUsers = JsonNodeFactory.instance.arrayNode();
			ArrayNode allUsers = JsonNodeFactory.instance.arrayNode();

			taskRepository.getTaskUsers(id).forEach(user -> {
				selectedTaskUsers.add(JsonNodeFactory.instance.pojoNode(user));
			});
			taskRepository.getNotTaskUsers(id).forEach(user -> {
				notSelectedTaskUsers.add(JsonNodeFactory.instance.pojoNode(user));
			});
			userRepository.findByEnabledTrue().forEach(user -> {
				allUsers.add(JsonNodeFactory.instance.pojoNode(user));
			});

			usersNode.set("selectedTaskUsers", selectedTaskUsers);
			usersNode.set("notSelectedTaskUsers", notSelectedTaskUsers);
			usersNode.set("allUsers", allUsers);

			return new ResponseEntity<ObjectNode>(usersNode, HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Task not found!"), HttpStatus.NOT_FOUND);
		}

	}

	@RequestMapping(path = "mytasks/project/{id}", method = RequestMethod.GET)
	public List<Task> getUserTasksByProject(@PathVariable Long id) {
		User currentUser = userRepository
				.findByUsername(
						((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
				.get();
		List<Task> userTasks = taskRepository.getUserTasksByProject(currentUser.getId(), id);
		userTasks.forEach(task -> {
			ObjectNode bodyNode = JsonNodeFactory.instance.objectNode();
			bodyNode.put("myProgress", controllerUtils.getUserProgressByTask(task.getId(), currentUser.getId()));
			task.setStatistics(bodyNode);
		});

		return userTasks;
	}

	/*
	 * @RequestMapping(path = "/mytasks", method = RequestMethod.GET) public
	 * List<Task> getUserTasks() { User currentUser = userRepository.findByUsername(
	 * ((JwtUser)
	 * SecurityContextHolder.getContext().getAuthentication().getPrincipal()).
	 * getUsername()); List<Task> userTasks =
	 * taskRepository.getUserTasks(currentUser.getId()); userTasks.forEach(task -> {
	 * ObjectNode bodyNode = JsonNodeFactory.instance.objectNode();
	 * bodyNode.put("myProgress",
	 * controllerUtils.getUserProgressByTask(task.getId(), currentUser.getId()));
	 * task.setStatistics(bodyNode); });
	 * 
	 * return userTasks; }
	 */

}
