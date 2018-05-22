package org.prime.util;

import java.util.List;

import org.prime.stm.model.Project;
import org.prime.stm.model.Task;
import org.prime.stm.repository.CommentRepository;
import org.prime.stm.repository.ProjectRepository;
import org.prime.stm.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class Utils
{
	
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private CommentRepository commentRepository;

	public static final String API = "/api/";
	public static final String PROJECT_PROGRESS = "projectProgress";
	
	public static final String COMPLETED_TASKS = "completedTasks";
	public static final String INP_ROGRESS_TASKS = "inProgressTasks";
	public static final String CLOSED_TASKS = "closedTasks";
	
	
	public static final String USER_PROGRESS = "userProgress";
	public static final String USERNAME = "username";
	public static final String PROGRESS = "progress";
	
	public static final int LATEST_COUNT = 3;
	
}
