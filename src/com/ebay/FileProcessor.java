package com.ebay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
* The class is the entry point to start processing
* all files generated by File generator
* @version 1.0
* @author Pawan Sidhani
*/
public class FileProcessor {
	
	private int passedCount = 0;
	private int failedCount = 0;
	public static void main(String args[]) {
		FileProcessor obj = new FileProcessor();
		String path = args[0];
		System.out.println(path);
		File folder = new File(path);// get folder where all files are kept generated by File Generator
		String[] fileNames = folder.list();
		//iterate through all files inside the directory
		for(String fileName: fileNames) {
			System.out.println("processing File: "+fileName);
			obj.processFiles(path+"\\"+fileName);//process each File
		}
	}
	/*
	 * This is the method to process file based on name provided
	 * @param String path is the full path of file with file name
	 */
	void processFiles(String path) {
		//System.out.println("Complete path: "+path);
		FileInputStream inputStream = null;
		Scanner sc = null;
		Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
		
		try {
			inputStream = new FileInputStream(path);// loads the input stream from the file
			sc = new Scanner(inputStream, "UTF-8");
			int count = 0;
			List<String> urls = new ArrayList<String>();
			boolean flag = true;
			// Loads one line from file at a time
			//makes sure that it doesn't load the entire file at once
			// due to which if file size is huge, entire memory is not consumed at once
			while(sc.hasNextLine()) {
				String url = sc.nextLine();
				urls.add(url);
				if(count == 9999) {
					this.processUrls(urls);
					urls.clear();
					flag = false;
					
				}
				if(flag) {
					count ++;
				}
				else {
					count = 0;
					flag = true;
				}
				
			}
			this.processUrls(urls);// calls the multi threaded implementation to process urls
			Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
			long milleseconds = timestamp2.getTime() - timestamp1.getTime();
			long seconds = milleseconds/(1000);
			System.out.println("completed in seconds: "+seconds);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	    
	    
	}
	
	/*
	 * This is the method to process urls generated
	 * It creates available threads to distribute load to process urls simultaneously
	 * The main method will wait for all threads to complete processing
	 * @param List<String> urls List of all urls which need to be proccessed
	 */
	void processUrls(List<String> urls)  {
		int numThreads = Runtime.getRuntime().availableProcessors();// gets the number of threads available for the machine
		CountDownLatch latch = new CountDownLatch(numThreads);// initializing countdown latch
		List<ProcessUrlThread> threadList = new ArrayList<ProcessUrlThread>();
		
		for(int i = 0; i < numThreads; i++) {
			int startIndex = i*urls.size()/numThreads;
			int endIndex = (i+1)*urls.size()/numThreads - 1;
			ProcessUrlThread processUrlThread = new ProcessUrlThread(latch,urls.subList(startIndex,endIndex+1));
			threadList.add(processUrlThread);
			processUrlThread.start();
		}
		try {
			latch.await();// a mechanism to wait for multiple threads to complete their processing
			for(ProcessUrlThread thread:threadList) {
				this.passedCount = this.passedCount + thread.getPassedCount();// gets passed count from each thread
				this.failedCount = this.failedCount + thread.getFailedCount();// gets failed count from each thread
				//this make sure each thread has their own copy of counts
				//so that no thread is sharing any variable to prevent race condition
			}
			System.out.println("passed count: "+passedCount +" failed count: "+failedCount);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		
		
		
	}

}
