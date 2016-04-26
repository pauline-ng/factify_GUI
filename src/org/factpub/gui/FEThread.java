package org.factpub.gui;

import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.factpub.core.FEWrapper;
import org.factpub.network.PostFile;
import org.factpub.utility.FEConstants;
import org.factpub.utility.Utility;

public class FEThread implements Runnable {
	
	private Semaphore semaphore; 
	private int row;
	private File file;
		
	public FEThread(Semaphore semaphore, File file, int row){
		this.semaphore = semaphore;
		this.file = file;
		this.row = row;
	}
	
	private synchronized void updateStatusColumn(String status, int row_thread_sync){
		MainFrame.tableModel.setValueAt(status, row_thread_sync, FEConstants.TABLE_COLUMN_STATUS);
	}
	
	public void release(){
		this.semaphore.release();
	}
	
	// Thread process
	@Override
	public void run(){
		
		try{
			
			this.semaphore.acquire();

			String status = "Now Extracting...";			
			Thread.sleep((long) (Math.random() * 1000)); //1秒以下のランダムな時間
			updateStatusColumn(status, row);
			
			//MainFrame.fileTable getCellEditor(row, FEConstants.TABLE_COLUMN_STATUS);

			status = FEWrapper.GUI_Wrapper(file);  // <--------------------------- where FactExtractor is executed!
			// If success
			updateStatusColumn(status, row);
			
			if(status == FEConstants.FE_STATUS_CODE_1){
				// Fact Extractor
				try{    		    		
		    		// Uploading Facts
		    		if(status.equals(FEConstants.STATUS_UPLOADING)){
		        		
	        			System.out.println(Utility.getFileNameMD5(file));
	        			
	        			// File name must be MD5!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! otherwise it does get error.
	        			File json = new File(Utility.getFileNameMD5(file));
	        				
		        		try{
	        				List<String> res = PostFile.uploadToFactpub(json);
	        				System.out.println("JSON can be written.");
		        			
		        			// If the server returns page title, put it into the array so browser can open the page when user click it.
		        			if(res.get(0).contains(FEConstants.SERVER_RES_TITLE_BEGIN)){
		        				//Embedding HyperLink
		        				String pageTitle = (String) res.get(0).subSequence(res.get(0).indexOf(FEConstants.SERVER_RES_TITLE_BEGIN) + FEConstants.SERVER_RES_TITLE_BEGIN.length(), res.get(0).indexOf(FEConstants.SERVER_RES_TITLE_END));
		        				pageTitle = pageTitle.replace(" ", "_");
		        				System.out.println(pageTitle);
		        				DNDListener.setPageTitle(row, pageTitle);
		        				
		        				status = "<html><u><font color=\"blue\">Upload Success!</font></u></html>";
		        				updateStatusColumn(status, row);
		        				
		        				//change table color        				
		        			}else{
		        				
		        				status = "Upload Success!";
		        				updateStatusColumn(status, row);
		        				
		        			}
		        			
		        			// embed HTML to the label
		        			}catch(Exception e){
		        				
			        			status = FEConstants.STATUS_UPLOAD_FAILED;
			        			updateStatusColumn(status, row);
			        			
		        		}
		    		}	                    		
				}catch(Exception e){
					// If not success
					status = "Failed to upload.";
					updateStatusColumn(status, row);
					
				}
			}
		}catch (InterruptedException e){
			System.out.println("Waiting error occured.");
			e.printStackTrace();
		}finally{
			release();
		}
	}
}