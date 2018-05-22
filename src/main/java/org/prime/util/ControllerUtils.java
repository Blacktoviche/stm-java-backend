package org.prime.util;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.prime.security.repository.UserRepository;
import org.prime.stm.model.Project;
import org.prime.stm.model.Task;
import org.prime.stm.repository.CommentRepository;
import org.prime.stm.repository.ProjectRepository;
import org.prime.stm.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ControllerUtils {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private UserRepository userRepository;

	public void updateProjectProgress(Long projectId) {
		Optional<Project> optionalProject = projectRepository.findById(projectId);
		if (optionalProject.isPresent()) {
			optionalProject.get().setProgress(calculateProjectProgress(projectId));
			projectRepository.save(optionalProject.get());
		}
	}

	public void updateTaskProgress(Long taskId) {
		Optional<Task> optTask = taskRepository.findById(taskId);
		if (optTask.isPresent()) {
			optTask.get().setProgress(calculateTaskProgress(taskId));
			if(optTask.get().getProgress() == 100) {
				optTask.get().setStatus(Task.COMPLETED_STATUS);
			}else{
				optTask.get().setStatus(Task.IN_PROGRESS_STATUS);
			}
			taskRepository.save(optTask.get());
			updateProjectProgress(optTask.get().getProject().getId());
		}
	}

	private Long calculateProjectProgress(Long projectId) {
		Long tasksCount = taskRepository.countTasksByProject(projectId);
		Long tasksProgressSum = taskRepository.sumProgressByProject(projectId);

		if (tasksProgressSum == null || tasksProgressSum == 0 || tasksCount == null|| tasksCount == 0) {
			return 0L;
		}
		return tasksProgressSum / tasksCount;
	}

	private Long calculateTaskProgress(Long taskId) {
		Long commentsProgressSum = commentRepository.sumProgressByTask(taskId);
		logger.info("commentsProgSum:: {} ", commentsProgressSum);
		if (commentsProgressSum != null ) {
			return commentsProgressSum;
		}else {
			return 0L;
		}
	}

	
	public String getUserProgressByProject(Long projectId, Long userId) {

		Long tasksCount = taskRepository.countTasksByProject(projectId);
		if (tasksCount == null || tasksCount == 0) {
			return "0.0";
		}

		// Tasks
		Double taskPercent = (double) (100 / tasksCount);
		
		Long myTaskProgress = 0L;

		List<Task> myTasks = taskRepository.getUserTasksByProject(userId, projectId);
		logger.info("taskPercent : {} ", taskPercent);
		
		for (int i = 0; i < myTasks.size(); i++) {
			Long prog = taskRepository.sumTaskProgressByUser(myTasks.get(i).getId(), userId);
			
			if(prog != null) {
				myTaskProgress += prog;
			}
		}
		
		logger.info("myTaskProgress : {} ", myTaskProgress);
		DecimalFormat dec = new DecimalFormat("#0.00");
		
		return dec.format((taskPercent / 100 ) * myTaskProgress);
	}

	public Long getUserProgressByTask(Long taskId, Long userId) {

		Long userTaskProgress = commentRepository.sumUserProgressByTask(taskId, userId);
		if (userTaskProgress == null) {
			return 0L;
		}else {
			return userTaskProgress;
		}

	}
	
	public Long getUserProgressByMonth(Long userId, int month ) {
		
		Long userProgress = 0L;
		
		Calendar dateFrom = Calendar.getInstance();
		dateFrom.set(Calendar.MONTH, month);
		dateFrom.set(Calendar.DAY_OF_MONTH, 1);
		dateFrom.set(Calendar.HOUR_OF_DAY, 0);
		
		
		Calendar dateTo = Calendar.getInstance();
		dateTo.set(Calendar.MONTH, month );
		dateTo.set(Calendar.HOUR_OF_DAY, 23);
		
		if( (month % 2) == 0 ) {
			if(month == 2 ) {
				dateTo.set(Calendar.DAY_OF_MONTH, 28);
			}else {
				dateTo.set(Calendar.DAY_OF_MONTH, 30);
			}
		}else {
			dateTo.set(Calendar.DAY_OF_MONTH, 31);
		}
		
		userProgress = commentRepository.sumUserProgressByMonth(userId, dateFrom.getTime(), dateTo.getTime());
		
		if (userProgress == null) {
			return 0L;
		}else {
			return userProgress;
		}
	}
}
