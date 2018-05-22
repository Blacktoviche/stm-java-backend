package org.prime.stm.rest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.prime.security.core.JwtUser;
import org.prime.security.model.User;
import org.prime.security.repository.UserRepository;
import org.prime.stm.model.Comment;
import org.prime.stm.model.Project;
import org.prime.stm.model.Task;
import org.prime.stm.repository.CommentRepository;
import org.prime.stm.repository.ProjectRepository;
import org.prime.stm.repository.TaskRepository;
import org.prime.util.ControllerUtils;
import org.prime.util.Message;
import org.prime.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

@CrossOrigin()
@RestController
@RequestMapping("/api/")
public class CommentRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ControllerUtils controllerUtils;

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "comments/{count}", method = RequestMethod.GET)
	public List<Comment> getComments(@PathVariable int count) {
		if (count > 0) {
			return commentRepository.findAll(PageRequest.of(0, count, Sort.Direction.DESC, "dateCreated")).getContent();
		} else {
			return commentRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreated"));
		}
	}

	@RequestMapping(path = "mylatestcomments/{count}", method = RequestMethod.GET)
	public List<Comment> getMyComments(@PathVariable int count) {
		User currentUser = userRepository
				.findByUsername(
						((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
				.get();
		if (count > 0) {
			return commentRepository
					.findByCreatedBy(currentUser.getId(), PageRequest.of(0, count, Sort.Direction.DESC, "dateCreated"))
					.getContent();
		} else {
			return commentRepository.findByCreatedBy(currentUser.getId(), Sort.by(Sort.Direction.DESC, "dateCreated"));
		}
	}
	

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "comments/task/{id}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getCommentsByTask(@PathVariable Long id) {

		logger.info("comments count {} for task {} ", commentRepository.findAllByTaskId(id).size(), id);

		return new ResponseEntity<List>(commentRepository.findAllByTaskId(id), HttpStatus.OK);
	}

	@RequestMapping(path = "comment/{id}", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Comment comment) {

		Optional<Task> optTask = taskRepository.findById(id);

		if (optTask.get().getStatus() == Task.COMPLETED_STATUS) {
			return new ResponseEntity<Message>(new Message("Task is completed!"), HttpStatus.NOT_ACCEPTABLE);
		}

		if (optTask.isPresent()) {
			logger.info("comment progress: {} ", comment.getProgress());
			comment.setId(null);
			comment.setCreatedBy(userRepository.findByUsername(
					((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
					.get());
			comment.setDateCreated(new Date());
			comment.setTask(optTask.get());
			comment.setProgress(comment.getProgress() - optTask.get().getProgress());
			commentRepository.save(comment);

			controllerUtils.updateTaskProgress(comment.getTask().getId());

			return new ResponseEntity<Message>(new Message("Comment add success"), HttpStatus.OK);
		}

		return new ResponseEntity<Message>(new Message("Task not found!"), HttpStatus.NOT_FOUND);
	}

	@RequestMapping(path = "comment/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<?> deleteComment(@PathVariable Long id) {
		Optional<Comment> commentOpt = commentRepository.findById(id);
		if (commentOpt.isPresent()) {
			commentRepository.deleteById(id);
			logger.info("comment deleted {} ", id);

			controllerUtils.updateTaskProgress(commentOpt.get().getTask().getId());
			Optional<Task> optTask = taskRepository.findById(commentOpt.get().getTask().getId());
			optTask.get().setStatus(Task.IN_PROGRESS_STATUS);
			taskRepository.save(optTask.get());

			return new ResponseEntity<Message>(new Message("Comment delete success"), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Comment not found!"), HttpStatus.NOT_FOUND);
		}

	}

	@RequestMapping(path = "mycomments/task/{id}", method = RequestMethod.GET)
	public List<Comment> getUserCommentsByTask(@PathVariable Long id) {
		User currentUser = userRepository
				.findByUsername(
						((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
				.get();
		controllerUtils.updateTaskProgress(id);
		return commentRepository.findUserCommentsByTask(currentUser.getId(), id);
	}

	@RequestMapping(path = "myprogress", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getMyProgress() {

		User currentUser = userRepository
				.findByUsername(
						((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername())
				.get();
		ArrayNode progressNode = JsonNodeFactory.instance.arrayNode();
		Long progress = 0L;
		
		for (int i = 0; i < 12; i++) {
			progress = controllerUtils.getUserProgressByMonth(currentUser.getId(), i);
			progressNode.add(progress);
			//logger.info("User {}  progress for month {} is {}", currentUser.getUsername(), i , progress);
		}

		return new ResponseEntity<ArrayNode>(progressNode, HttpStatus.OK);
	}
}
