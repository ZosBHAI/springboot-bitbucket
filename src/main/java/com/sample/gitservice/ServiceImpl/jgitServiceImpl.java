package com.sample.gitservice.ServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.sample.gitservice.Service.jgitService;

@Service
public class jgitServiceImpl implements jgitService {
	
	RestTemplate restTemplate = new RestTemplate();
	ResponseEntity<String> response = null;
	
	
	@Value("${bitbucket.admin.user}")
	private String bitbucketAdmin;
	//private final String bitbucketAdmin="admin";
	
	@Value("${bitbucket.admin.password}")
	private String bitbucketAdminPass;
	//private final String bitbucketAdminPass="admin";
	
	@Value("${bitbucket.projectID}")
	private  String projectID;
	//private  String projectID="mas";
	
	
	@Value("${bitbucket.server.url}")
	private String bitbucketURL;
	//private final String bitbucketURL= "http://localhost:7990/";
	
	@Value("${bitbucket.bitbucketUserCreationAPI}")
	private String bitbucketUserCreationAPI;
	//private String bitbucketUserCreationAPI = "http://localhost:7990/rest/api/1.0/admin/users?";
	
	@Value("${bitbucketUserAddGroupAPI}")
	private String bitbucketUserAddGroupAPI;
	//private String bitbucketUserAddGroupAPI = "http://localhost:7990/rest/api/1.0/admin/groups/add-user";
	
	@Value("${bitbucketProjectAPI}")
	private String bitbucketProjectAPI;
	private String bitbucketUserRepoCreateAPI = bitbucketProjectAPI + projectID + "/repos";
	private String bitbucketUserGrantPermissionToRepo = bitbucketProjectAPI + projectID + "/repos/";
	
	@Value("${bitbucketGroupName}")
	private String bitbucketGroupName;
	//private String bitbucketGroupName = "projectuser"	;
	
	@Value("${localrepo}")
	private String localRepo;
	//private String localRepo = "C:\\TechBlog\\JAVA\\jgit\\user\\user1\\";
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private boolean gitInitLocalfolder(String localpath)
	{
		boolean status = false;
		File dir = null;
		Git git = null;
		try{
			dir = new File(localpath);
			git = Git.init().setDirectory(dir).call();
			status = true;
			}
		
		  catch (IllegalStateException e) {
			logger.debug("IllegalStateException: " + e.getMessage());
			} catch (GitAPIException e) {
			logger.debug("GitAPIException: " + e.getMessage());
			} finally {
				if (git != null) {
					git.getRepository().close();
				}
			}
		return status;
	}
				
				
	@Override			
	public String createremoteRepo(String username,String password,String email,String localPath,String reponame)
	
	{
		Git git = null;
		JSONObject msg = new JSONObject();
		//URI uri = new URI(localPath);
		String localrepo = Paths.get(localPath).toString();
		System.out.println(" Local repository " + localrepo);
		logger.debug("local Repo" + localrepo);
		
		try {
		gitInitLocalfolder(localrepo);
		File repoDir = new File(localrepo);
		git = Git.open(repoDir);
		System.out.println("Git add");
		git.add().addFilepattern(".").call();
		
		logger.debug("GIT commmit");
		git.commit().setMessage("Created GIT project . Added files from " + localrepo + "").
		setAuthor(username,email).call();
		String remoteRepoURL = "http://localhost:7990/scm/mas/" + reponame + ".git";
		URIish uri = new URIish(remoteRepoURL);
		git.remoteAdd().setUri(uri).setName("origin").call();

		git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username,password))
				.setRemote("origin").add("master").call();

		msg.put("success", true);
		msg.put("message", "Successfully remote repository");
  
		}
		catch (IOException | InvalidRemoteException | TransportException | URISyntaxException
				| IllegalStateException e){
			logger.error("Exception Message : " + e.getMessage());
			msg.put("success", false);
			msg.put("exception" ,e.getMessage());
		}catch (GitAPIException e){
			msg.put("success", false);
			logger.error("Exception Message : " + e.getMessage());
			msg.put("exception" ,e.getMessage());
		}
		finally{
			if (git != null){
				git.getRepository().close();
			}
		}
		return msg.toString();
		}


	@Override
	public String startOnboarding(String username, String password, String email) {
		JSONObject msg = new JSONObject();
		String url = bitbucketUserCreationAPI + "name=" + username + "&password=" + password + "&displayName=" + username
				+ "&emailAddress=" + email + "&addToDefualtGroup=true&notify=false";
		
		System.out.println("URL that is calling is    " + url);
		
		
		String auth = bitbucketAdmin + ":" + bitbucketAdminPass;
		byte [] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		
		System.out.println("Creatign the Http Header");
		try {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + new String(encodedAuth));
		headers.add("X-Atlassian-Token", "no-check");
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		
		System.out.println("Building the Http Request");
		HttpEntity<String> request = new HttpEntity<String>("",headers);
		response = restTemplate.postForEntity(url.trim(), request, String.class);
		if (response != null && response.getStatusCodeValue() == 204) {
			logger.debug("Successfully created user in Bitbucket Server");
			msg.put("success", true);
			msg.put("message0", "Successfully create Bitbucket user");
			
		}
		}
		catch (HttpStatusCodeException ex)
		{logger.error("Http exceeption occured while creating the user" + ex.getMessage());
		}
		finally {
			logger.debug("Exiting startOnboarding");
		}
		// TODO Auto-generated method stub
		
		if (addUsertoGroup(username)){
			msg.put("message1", "User is added to Group");
		}
		else{
			msg.put("message1", "User is not added to Group");
		}
		return msg.toString();
		
	}
	
	 private boolean addUsertoGroup(String username)
	 {
		 
		 String auth = bitbucketAdmin + ":" + bitbucketAdminPass;
		 byte [] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		 boolean status = false;
		 System.out.println("Creatign the Http Header");
			try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Basic " + new String(encodedAuth));
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			
			System.out.println("Building the Http Request");
			JSONObject requestHeader = new JSONObject();
			requestHeader.put("context", bitbucketGroupName);
			requestHeader.put("itemName", username);
			
			HttpEntity<String> request = new HttpEntity<String>(requestHeader.toString(),headers);
			System.out.println("Passing the below Header" + requestHeader.toString());
			
			response = restTemplate.postForEntity(bitbucketUserAddGroupAPI.trim(), request, String.class);
			if (response != null && response.getStatusCodeValue() == 200) {
				logger.debug("Successfully added user to Bitbucket group");
				status = true;
				
				
			}
			}
			catch (HttpStatusCodeException ex)
			{logger.error("Http exceeption occured while creating the user" + ex.getMessage());
			}
			finally {
				logger.debug("Exiting startOnboarding");
			}
		 
		 
		 
		 return status;
	 }


	@Override
	public String createBitbucketRepo(String username, String password, String repo) {
		// TODO Auto-generated method stub
		System.out.println("API called is " + bitbucketUserRepoCreateAPI);
		JSONObject msg = new JSONObject();
		
		String auth = bitbucketAdmin + ":" + bitbucketAdminPass;
		byte [] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		
		 System.out.println("Creatign the Http Header");
			try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Basic " + new String(encodedAuth));
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			
			System.out.println("Building the Http Request");
			JSONObject requestHeader = new JSONObject();
			requestHeader.put("name", repo);
			requestHeader.put("scmId", "git");
			requestHeader.put("forkable", true);
			
			HttpEntity<String> request = new HttpEntity<String>(requestHeader.toString(),headers);
			System.out.println("Passing the below Header" + requestHeader.toString());
			
			response = restTemplate.postForEntity(bitbucketUserRepoCreateAPI.trim(), request, String.class);
			if (response != null && response.getStatusCodeValue() == 201) {
				logger.debug("Successfully added user to Bitbucket group");
				msg.put("success", true);
				msg.put("message01", "Successfully create Bitbucket repository");
				permissionToRepo(username,"REPO_WRITE",repo);
				
			}
			}
			catch (HttpStatusCodeException ex)
			{logger.error("Http exceeption occured while creating the user" + ex.getMessage());
			}
			finally {
				logger.debug("Exiting startOnboarding");
			}
		
		return msg.toString();
	}
	 
	 
	
	private boolean permissionToRepo(String username,String permission, String reposlug)
	
	{
		String fullUrl = bitbucketUserGrantPermissionToRepo + reposlug + "/permissions/users?name=" + username + 
				
				"&permission=" + permission;
		String auth = bitbucketAdmin + ":" + bitbucketAdminPass;
		byte [] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		boolean status = false;
		 System.out.println("Creatign the Http Header");
			try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Basic " + new String(encodedAuth));
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			
			System.out.println("Building the Http Request" + fullUrl.toString());
			
			
			HttpEntity<String> request = new HttpEntity<String>("",headers);
			
			
			//restTemplate.put(fullUrl.trim(), request);
			response = restTemplate.exchange(fullUrl.trim(), HttpMethod.PUT,request,String.class);
			System.out.println("Response code  is " + response.getStatusCodeValue());
			if (response != null && response.getStatusCodeValue() == 204) {
				logger.debug("Given necessary Permission to " + username);
				status=true;
				
				
				
			}
			}
			catch (HttpStatusCodeException ex)
			{logger.error("Http exceeption occured while creating the user" + ex.getMessage());
			}
			finally {
				logger.debug("Exiting startOnboarding");
			}
		
		return status;
	}


	@Override
	public String pushArtifacts(String username, String password, String repo) {
		// TODO Auto-generated method stub
		Git git = null;
		JSONObject msg = new JSONObject();
		String responseMessage = null;
		boolean status = false;
		
		//Reading all the files from the particular folder
		File localDir = new File(localRepo);
		File[] listOfFiles = localDir.listFiles();
		List<String> fileList = new ArrayList<String>();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				fileList.add(file.getName().toString());
				}
		}
		try {
					
			     git = Git.open(localDir);
				
					
					if (fileList != null && fileList.size() != 0 )
					{
						
						AddCommand addCmd = git.add();
						for (String s : fileList){
							logger.info("Adding " + s);
							addCmd.addFilepattern(s);
						}
						addCmd.call();
						git.commit().setMessage("commiting").call();
								
					}
					
					
					PushCommand pushC = git.push();
					pushC.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));

					logger.info("GIT pushing files");
					pushC.setRemote("origin").add("master").call();

					Status files = git.status().call();
					Set<String> missing = files.getMissing(); 
					if (missing != null && !missing.isEmpty()) {
						logger.info("GIT Removing missing files");
						RmCommand deleteFiles = git.rm();
						for (String miss : missing) {
							logger.info("Missing: " + miss);
							deleteFiles.addFilepattern(miss);
						}
						deleteFiles.call();
						git.commit().setMessage("Removed missing files").call();
						pushC.setRemote("origin").add("master").call();
													
						}
					status=true;
					responseMessage="Successfully staged and psushed";
					
		}
		catch (GitAPIException e){
			logger.error("GitAPIexception " + e.getMessage());
			}
		catch (IOException e1){
			logger.error("GitAPIexception " + e1.getMessage());
		}
		finally{
			if (git != null){
				git.getRepository().close();
			}
		}
		msg.put("status", status);
		msg.put("message", responseMessage);
		
			
		
		
		return msg.toString();
	}
	 
		
	}


