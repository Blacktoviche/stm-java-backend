package org.prime.security.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.prime.security.core.JwtTokenUtil;
import org.prime.security.core.JwtUser;
import org.prime.security.model.Role;
import org.prime.security.model.RoleName;
import org.prime.security.model.User;
import org.prime.security.repository.RoleRepository;
import org.prime.security.repository.UserRepository;
import org.prime.stm.model.Project;
import org.prime.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@CrossOrigin
@RestController
@RequestMapping("/api/")
public class UserRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${jwt.header}")
	private String tokenHeader;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	@Qualifier("jwtUserDetailsService")
	private UserDetailsService userDetailsService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@RequestMapping(value = "user", method = RequestMethod.GET)
	public User getAuthenticatedUser(HttpServletRequest request) {
		String token = request.getHeader(tokenHeader).substring(7);
		String username = jwtTokenUtil.getUsernameFromToken(token);
		// JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
		// System.out.println("currentUser::: " +
		// SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return userRepository.findByUsername(username).get();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = "enabledusers", method = RequestMethod.GET)
	public List<User> getEnabledUsers() {
		return userRepository.findByEnabledTrue();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = "users", method = RequestMethod.GET)
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "user", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> addUser(@RequestBody User user) {

		logger.info("user to be saved {} , {} , {} , {}", user.getUsername(), user.getEmail(), user.getUserPassword(),
				user.getBeautifyRoleName());

		if (user.getUsername().isEmpty() || user.getUserPassword().isEmpty() || user.getEmail().isEmpty()
				|| user.getBeautifyRoleName().isEmpty()) {
			return new ResponseEntity<Message>(new Message("Username info not completed!"), HttpStatus.NOT_ACCEPTABLE);
		}

		Optional<User> existUser = userRepository.findByUsername(user.getUsername());
		if (!existUser.isPresent()) {

			if (user.getBeautifyRoleName().equalsIgnoreCase("Admin")) {
				logger.info(".....................................");
				user.setRole(roleRepository.findByRoleName(RoleName.ROLE_ADMIN));
			} else {
				user.setRole(roleRepository.findByRoleName(RoleName.ROLE_USER));
			}
			user.setPassword(new BCryptPasswordEncoder().encode(user.getUserPassword()));
			userRepository.save(user);
			logger.info("user added {} ", user.getUsername());
			return new ResponseEntity<Message>(new Message("User add success"), HttpStatus.OK);
		} else {
			return new ResponseEntity<Message>(new Message("Username already taken!"), HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "user", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<?> updateUser(@RequestBody User user) {

		if (user.getUsername().isEmpty() || user.getEmail().isEmpty() || user.getBeautifyRoleName().isEmpty()) {
			return new ResponseEntity<Message>(new Message("User info not completed!"), HttpStatus.NOT_ACCEPTABLE);
		}

		Optional<User> existUser = userRepository.findByUsernameExept(user.getUsername(), user.getId());

		if (!existUser.isPresent()) {

			Optional<User> oldOptUser = userRepository.findById(user.getId());
			if (oldOptUser.isPresent()) {

				User oldUser = oldOptUser.get();
				if (user.getBeautifyRoleName().equalsIgnoreCase("Admin".trim())) {
					logger.info(".....................................");
					oldUser.setRole(roleRepository.findByRoleName(RoleName.ROLE_ADMIN));
				} else {
					oldUser.setRole(roleRepository.findByRoleName(RoleName.ROLE_USER));
				}

				oldUser.setUsername(user.getUsername());
				oldUser.setEmail(user.getEmail());
				oldUser.setFirstname(user.getFirstname());
				oldUser.setLastname(user.getLastname());
				oldUser.setEnabled(user.getEnabled());

				jwtTokenUtil.invalidateToken(oldUser.getUsername());

				userRepository.save(oldUser);
				logger.info("user updated {} ", user.getUsername());
				return new ResponseEntity<Message>(new Message("User update success"), HttpStatus.OK);

			} else {
				return new ResponseEntity<Message>(new Message("User not found!"), HttpStatus.NOT_FOUND);
			}

		} else {
			return new ResponseEntity<Message>(new Message("Username already taken!"), HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@RequestMapping(path = "myuser", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<?> updateMyUser(@RequestBody User user) {

		if (user.getEmail().isEmpty()) {
			return new ResponseEntity<Message>(new Message("User info not completed!"), HttpStatus.NOT_ACCEPTABLE);
		} else {

			Optional<User> userOpt = userRepository.findById(
					((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());

			if (userOpt.isPresent()) {

				User oldUser = userOpt.get();
				oldUser.setEmail(user.getEmail());
				oldUser.setFirstname(user.getFirstname());
				oldUser.setLastname(user.getLastname());
				logger.info("my user updated {} {} {} {} {}", oldUser.getId(), oldUser.getUsername(),
						oldUser.getFirstname(), oldUser.getLastname(), oldUser.getEmail());
				userRepository.save(oldUser);
				logger.info("my user updated {} ", oldUser.getUsername());

				return new ResponseEntity<Message>(new Message("User update success"), HttpStatus.OK);

			} else {
				return new ResponseEntity<Message>(new Message("User not found!"), HttpStatus.NOT_FOUND);
			}
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "user/resetpwd/{id}", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> resetUserPassword(@PathVariable Long id, @RequestBody String password) {

		if (password.isEmpty()) {
			return new ResponseEntity<Message>(new Message("Password is empty!"), HttpStatus.NOT_ACCEPTABLE);
		}

		Optional<User> oldOptUser = userRepository.findById(id);
		if (oldOptUser.isPresent()) {

			User oldUser = oldOptUser.get();
			oldUser.setPassword(new BCryptPasswordEncoder().encode(password));

			userRepository.save(oldUser);
			jwtTokenUtil.invalidateToken(oldUser.getUsername());

			logger.info("password updated for user {} |" + password + "|", oldUser.getUsername());
			return new ResponseEntity<Message>(new Message("Password update success"), HttpStatus.OK);

		} else {
			return new ResponseEntity<Message>(new Message("User not found!"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(path = "user/resetmypwd", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> resetMyPassword(@RequestBody String passwords) {

		logger.info("passwords {} ", passwords);

		if (passwords.isEmpty()) {
			return new ResponseEntity<Message>(new Message("Password is empty!"), HttpStatus.NOT_ACCEPTABLE);
		}

		List<String> pwds = List.of(passwords.split(","));

		logger.info("pwds {} {} ", pwds, pwds.size());

		if (pwds.get(0).isEmpty() || pwds.get(1).isEmpty()) {
			return new ResponseEntity<Message>(new Message("Password is empty!"), HttpStatus.NOT_ACCEPTABLE);
		}

		Optional<User> optUser = userRepository
				.findById(((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());

		if (optUser.isPresent()) {

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			if (encoder.matches(pwds.get(0), optUser.get().getPassword())) {

				User oldUser = optUser.get();
				oldUser.setPassword(new BCryptPasswordEncoder().encode(pwds.get(1)));

				userRepository.save(oldUser);
				jwtTokenUtil.invalidateToken(oldUser.getUsername());

				logger.info("my password updated {} ", oldUser.getUsername());
				return new ResponseEntity<Message>(new Message("My Password update success"), HttpStatus.OK);
			} else {
				return new ResponseEntity<Message>(new Message("Old password incorrect!"), HttpStatus.NOT_ACCEPTABLE);
			}

		} else {
			return new ResponseEntity<Message>(new Message("User not found!"), HttpStatus.NOT_FOUND);
		}

	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(path = "user/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<?> deleteUser(@PathVariable Long id) {
		Optional<User> loggedinOptUser = userRepository
				.findById(((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());

		Optional<User> userOpt = userRepository.findById(id);

		if (userOpt.isPresent()) {

			// avoid deleting the last admin
			if (loggedinOptUser.get().getId() == userOpt.get().getId()) {
				return new ResponseEntity<Message>(new Message("You can't delete yourself!"),
						HttpStatus.NOT_ACCEPTABLE);
			}

			jwtTokenUtil.invalidateToken(userOpt.get().getUsername());
			userRepository.deleteById(id);
			logger.info("user deleted {} ", id);

			return new ResponseEntity<Message>(new Message("User delete success"), HttpStatus.OK);

		} else {
			return new ResponseEntity<Message>(new Message("User not found!"), HttpStatus.NOT_FOUND);
		}

	}

}
