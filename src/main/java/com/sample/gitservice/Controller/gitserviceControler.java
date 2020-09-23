package com.sample.gitservice.Controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sample.gitservice.Service.jgitService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@RestController
public class gitserviceControler {
	
	@Autowired
	private jgitService jgit;
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PostMapping(value="/createRemoteRepo")
	@ResponseBody
	String createRemoteRepo(@RequestParam("reponame") final String repoName,
			@RequestParam("localpath") final String localpath,
			@RequestHeader(value="username") final String username,
			@RequestHeader(value="epassword") final String password,
			@RequestHeader(value="email") final String email
			)
	{
		logger.debug("Entering createRemoteRepo");
		String message = null;
		if (StringUtils.isNotEmpty(repoName) && StringUtils.isNotEmpty(localpath)){
			System.out.println("Not empty");
		logger.debug("Creating remote repo");
		message = 	jgit.createremoteRepo(username, password, email, localpath, repoName);
			
		}
		
		return message;
	}
	
	@PostMapping(value="/onboard")
	String onboardUser(@RequestHeader(value="username") final String username,
			@RequestHeader(value="epassword") final String password,
			@RequestHeader(value="email") final String email
			){
		String message = null;
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(email) ){
			
		logger.debug("Creating user");
		message = jgit.startOnboarding(username, password, email);
			
		}
		else {
			logger.debug("Pass the valid user details");
		}
		
		return message;
	}
	
   
	@PostMapping(value="/crepo")
	String createBitbucketRepo(@RequestHeader(value="username") final String username,
			@RequestHeader(value="epassword") final String password,
			@RequestHeader(value="repo") final String repo
			){
		String message = null;
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(repo) ){
			
		logger.debug("Creating Bitbucket Repository");
		message = jgit.createBitbucketRepo(username, password, repo);
			
		}
		else {
			logger.debug("Pass the valid user details");
		}
		
		return message;
	}
	
	
	@PostMapping(value="/push")
	String pushtoBitbucketRepo(@RequestHeader(value="username") final String username,
			@RequestHeader(value="epassword") final String password,
			@RequestHeader(value="repo") final String repo
			){
		String message = null;
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(repo) ){
			
		logger.debug("Pushing the Artifacts  to Bitbucker Repo");
		message = jgit.pushArtifacts(username, password, repo);
			
		}
		else {
			logger.debug("Pass the valid user details");
		}
		
		return message;
	}
	

}
