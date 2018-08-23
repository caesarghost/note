package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IDescription;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.ITeamArea;
import com.ibm.team.process.common.ITeamAreaHandle;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.common.IContent;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;

public class CaptureTeamArea {
	private static class LoginHandler implements ILoginHandler, ILoginHandler.ILoginInfo {
		
		private String fUserId;
		private String fPassword;
		
		private LoginHandler(String userId, String password) {
			fUserId= userId;
			fPassword= password;
		}
		
		public String getUserId() {
			return fUserId;
		}
		
		public String getPassword() {
			return fPassword;
		}
		
		public ILoginInfo challenge(ITeamRepository repository) {
			return this;
		}
	}
	/**
	* Dump the details of the contributors
	*
	* @param teamRepository
	* @param processArea
	* @param handle
	* @throws TeamRepositoryException
	*/
	private static void dumpContributor(ITeamRepository teamRepository,
			IProcessArea processArea, IContributorHandle handle,HashMap<String, String> roleList)
			throws TeamRepositoryException {
		IContributor contributor = (IContributor) teamRepository.itemManager()
			.fetchCompleteItem(handle, IItemManager.DEFAULT, null);
		//System.out.print(": " + contributor.getUserId() + "\t"
		//	+ contributor.getName() + "\t" + contributor.getEmailAddress()
		//	+ "\t");
		//IProcessItemService processService = (IProcessItemService) teamRepository
		//	.getClientLibrary(IProcessItemService.class);
		//IClientProcess process = processService.getClientProcess(processArea, null);
		
		String[] rolesList = processArea.getRoleAssignmentIds(handle);
		
		for (String roleID : rolesList){
			//System.out.print(roleID + " ");
			if ("Product Owner".equals(roleID)){
				String productOwnerList = roleList.get("productOwner");
				if("".equals(productOwnerList)){
					productOwnerList = contributor.getUserId();
				}else{
					productOwnerList = productOwnerList + ";" + contributor.getUserId();
				}
				//roleList.replace("productOwner", productOwnerList);
				roleList.put("productOwner", productOwnerList);
				
			}
			if ("ScrumMaster".equals(roleID)){
				String productOwnerList = roleList.get("scrumMaster");
				if("".equals(productOwnerList)){
					productOwnerList = contributor.getUserId();
				}else{
					productOwnerList = productOwnerList + ";" + contributor.getUserId();
				}
				//roleList.replace("scrumMaster", productOwnerList);
				roleList.put("scrumMaster", productOwnerList);
				
			}
		}
		//IRole[] contributorRoles = process.getContributorRoles(contributor, processArea, null);
		//for (int j = 0; j < contributorRoles.length; j++) {
		//	IRole role = (IRole) contributorRoles[j];
		//	System.out.print(role.getId() + " ");
			
		//}
		//System.out.println();
	}
	/**
	 * @param teamRepository
	 * @param processArea
	 * @param contributors
	 * @throws TeamRepositoryException
	 */
	private static HashMap<String, String> dumpContributors(ITeamRepository teamRepository,
			IProcessArea processArea, IContributorHandle[] contributors)
			throws TeamRepositoryException {
		HashMap<String, String> rolesList = new HashMap<String, String>();
		rolesList.put("productOwner", "");
		rolesList.put("scrumMaster", "");
		for (int i = 0; i < contributors.length; i++) {
			IContributorHandle handle = (IContributorHandle) contributors[i];
			dumpContributor(teamRepository, processArea, handle,rolesList);
			
		}
		return rolesList;
	}
	/**
	 * Iterate over the contributors of the process area and print them sorted
	 * as admins and as team members
	 *
	 * @param teamRepository
	 * @param processArea
	 * @throws TeamRepositoryException
	 */
	private static HashMap<String, String> dumpContributors(ITeamRepository teamRepository,
			IProcessArea processArea ) throws TeamRepositoryException {
		//System.out.println("Process: " + processArea.getName());
		//System.out.println(AdministratorsdumpContributors(teamRepository, processArea, processArea.getAdministrators());
		//System.out.println("Teamers");
		HashMap<String, String> rolesList = dumpContributors(teamRepository, processArea, processArea.getMembers());
		return rolesList;
	}
	 /**
	  * Print the description of the process area
	  *
	  * @param teamRepository
	  * @param pa
	  * @throws TeamRepositoryException
	  */
	 public static void printProcessAreaDescription(ITeamRepository teamRepository, IProcessArea pa)
	 		throws TeamRepositoryException {
	 	IDescription desc = pa.getDescription();
	 	IContent content = desc.getDetails();
	 	String description = "";
	 	if (content != null) {
	 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
	 		teamRepository.contentManager().retrieveContent(content, stream, null);
	 		try {
	 			description = stream.toString(content.getCharacterEncoding());
	 		} catch (UnsupportedEncodingException exception) {
	 			description = stream.toString();
	 		}
	 	}
	 	String summary = desc.getSummary();
	 	System.out.println(summary + "\n\nDescription:\n" + description);
	 }
	/**
	  * Analyze a project area
	  *
	  * @param teamRepository
	  * @param projectArea
	  * @throws TeamRepositoryException
	 * @throws IOException 
	  */
	 public static void analyzeProjectArea(ITeamRepository teamRepository,
	 		IProjectArea projectArea,String filePath,String resultFile) throws TeamRepositoryException, IOException {
	 	//printProcessAreaDescription(teamRepository, projectArea);
	 	//dumpContributors(teamRepository, projectArea);
		
	 	List teamAreas = projectArea.getTeamAreas();
	 	HashMap<String, HashMap<String, String>> teamRoles = new HashMap<String, HashMap<String,String>>();
	 	for (Iterator iterator = teamAreas.iterator(); iterator.hasNext();) {
	 		ITeamAreaHandle handle = (ITeamAreaHandle) iterator.next();
	 		ITeamArea teamArea = (ITeamArea) teamRepository.itemManager()
	 			.fetchCompleteItem(handle, IItemManager.DEFAULT, null);

	 		//printProcessAreaDescription(teamRepository, teamArea);
	 		
	 		HashMap<String, String> rolesList = dumpContributors(teamRepository, teamArea);
	 		teamRoles.put(teamArea.getName(), rolesList);
	 		
	 	}
	 	
	 	File f = new File(filePath + resultFile);
	 	Iterator<Entry<String, HashMap<String, String>>> iter = teamRoles.entrySet().iterator();
	 	while (iter.hasNext()){
	 		Entry<String, HashMap<String, String>> entry = iter.next();
	 		Object key = entry.getKey();
	 		HashMap<String, String> value = entry.getValue();
	 		System.out.println(key); 	 		
	 		System.out.println("Product Owner : " + value.get("productOwner"));
	 		System.out.println("Scrum Master : " + value.get("scrumMaster"));
	 		String str = key + "#" +  value.get("productOwner") + "#" + value.get("scrumMaster");
	 		if(f.exists() && !f.isDirectory()) { 
	 	    	BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + resultFile, true));
		        writer.append('\n');
		        writer.append(str);		             
		        writer.close();
	 	    }else{
	 	        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + resultFile));
		        writer.write(str);		             
		        writer.close();
		            
		    }
	 	}        
 	    
	 	
	 }
	
	 public static void getPorjectInfo(String projectAreaName,ITeamRepository teamRepository,String filePath,String resultFile) throws TeamRepositoryException, IOException{
		 
			IProcessClientService processClient = (IProcessClientService) teamRepository
				.getClientLibrary(IProcessClientService.class);

			URI uri = URI.create(projectAreaName.replaceAll(" ", "%20"));
			IProjectArea projectArea = (IProjectArea) processClient.findProcessArea(uri, null, null);
			if (projectArea == null) {
				System.out.println("Project not found.");
			}
			
			analyzeProjectArea(teamRepository, projectArea,filePath,resultFile);
			
	 }
	  
	 public static boolean run(String[] args) throws TeamRepositoryException, IOException {
		 
			String repositoryURI= args[0];
			String userId= args[1];
			String password= args[2];
			String projectAreaName= args[3];
			String filePath = args[4];
	    	String resultFile = args[5];
			System.out.println(projectAreaName);
			
			ITeamRepository teamRepository= TeamPlatform.getTeamRepositoryService().getTeamRepository(repositoryURI);
			teamRepository.registerLoginHandler(new LoginHandler(userId, password));
			teamRepository.login(null);
			getPorjectInfo(projectAreaName,teamRepository,filePath,resultFile);
			return true;
	}
	 public static void main(String[] args) throws IOException {
			
			boolean result;
			TeamPlatform.startup();
			try {
				result= run(args);
			} catch (TeamRepositoryException x) {
				x.printStackTrace();
				result= false;
			} finally {
				TeamPlatform.shutdown();
			}
			
			if (!result)
				System.exit(1);
			
		}
	

}
