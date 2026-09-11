package com.nnp.envrep.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.nnp.envrep.exception.GitException;

import lombok.extern.slf4j.Slf4j;

/**
 * GitlabActionUtil.java
 *
 * @author AC
 * @date 21-Apr-2025
 */
@Slf4j
public class GitlabActionUtil {
	
	private static List<String> REJECTECTED_CODES = Arrays.asList(new String[]{
			RemoteRefUpdate.Status.NON_EXISTING.name(),
			RemoteRefUpdate.Status.REJECTED_NODELETE.name(),
			RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD.name(),
			RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED.name(),
			RemoteRefUpdate.Status.REJECTED_OTHER_REASON.name(),
	});
	
	public static final String COMMIT_MSG = "nnp : ENV REPLICATION Generated ARGO Yamls for all requested componenets";
	

	public static boolean cloneFromGitOpsRepo(String gitUrl,String dirPath,String gitUserId, String gitPassword) throws GitException {
		Git git = null;
		try {
            git = Git.cloneRepository().setURI(gitUrl).setDirectory(new File(dirPath))
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserId, gitPassword)).call();
			return git!=null;
		} catch (GitAPIException e) {
			log.error("exception in GitlabActionUtil->cloneFromGitOpsRepo()", e);
			throw new GitException(e.getLocalizedMessage(),e);
		} finally {
			if(git != null)
				git.close();
		}
	}
	
	public static void pushProject(String localRepoPath, String gitUrl, String gitUserId,String gitPassword) throws GitException {
		Git git = null;
        try {
			git = Git.open(new File(localRepoPath));
			
			git.add().addFilepattern(".").call();
			StringBuffer sb = new StringBuffer(COMMIT_MSG);
			sb.append("[#");
			sb.append(", @");
			sb.append(gitUserId);
			sb.append("]");
			git.commit().setAll(true).setMessage(sb.toString()).call();

			// push to remote:
			PushCommand pushCommand = git.push();
			pushCommand.setRemote(gitUrl);
			pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserId, gitPassword));
			// you can add more settings here if needed
			Iterable<PushResult> resultItr = pushCommand.call();
            resultItr.forEach(GitlabActionUtil::analyzeRemoteUpdates);

		} catch (IOException | GitAPIException e) {
			log.error("exception in GitActionUtil->pushProject()", e);
			throw new GitException(e.getLocalizedMessage(),e);
		} finally {
			if (git != null)
				git.close();
		}
	}
	
	private static void analyzeRemoteUpdates(PushResult pushRes) {
		pushRes.getRemoteUpdates().forEach(u->{
			if (REJECTECTED_CODES.indexOf(u.getStatus().name()) >= 0) {
				throw new RuntimeException("[" + u.getStatus().name() + "] " + pushRes.getMessages());
			}
		});
	}
}
