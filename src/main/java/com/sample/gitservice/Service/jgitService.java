package com.sample.gitservice.Service;

public interface jgitService {
	
	String createremoteRepo(String username, String password,String email,String localPath, String reponame);
	String startOnboarding(String username,String password, String email);
	String createBitbucketRepo(String username,String password, String repo);
	String pushArtifacts(String username,String password, String repo);

}
