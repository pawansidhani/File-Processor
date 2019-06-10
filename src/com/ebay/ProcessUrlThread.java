package com.ebay;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
* The class extends Thread class
* It will process list of all urls passed to it
* It uses http client get method to process them
* @version 1.0
* @author Pawan Sidhani
*/

public class ProcessUrlThread extends Thread {
	
	private int passedCount;
	private int failedCount;
	private CountDownLatch latch;
	private List<String> urls;
	public int getPassedCount() {
		return passedCount;
	}
	public void setPassedCount(int passedCount) {
		this.passedCount = passedCount;
	}
	public int getFailedCount() {
		return failedCount;
	}
	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}
	
	public ProcessUrlThread(CountDownLatch latch, List<String> urls) {
		this.latch = latch;
		this.urls = urls;
		this.passedCount = 0;
		this.failedCount = 0;
	}
	@Override
	public void run() {
		HttpClient httpClient = HttpClientBuilder.create().build();// initializes http client
		for(String url: this.urls) {
			HttpGet request = new HttpGet(url);
			try {
				HttpResponse response = httpClient.execute(request);// this is synchronous way to send http get
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200) {
					this.passedCount ++;
				}
				else {
					this.failedCount ++;
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				this.failedCount++;
				e.printStackTrace();
				System.out.println(e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getMessage());
				this.failedCount++;
			}
		}
		this.latch.countDown();// for each thread finishing processing it, updates the latch
	}

}
