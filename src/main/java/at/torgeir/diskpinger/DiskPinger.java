package at.torgeir.diskpinger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class DiskPinger implements Runnable {

	private static final String COMMAND_LS = "ls";
	private ResourceBundle resources;
	
	protected boolean running;

	public DiskPinger() {
		resources = ResourceBundle.getBundle("diskpinger");
		running = true;
	}
	

	protected boolean writeFile(File file) throws IOException {
		return file.createNewFile();
	}

	protected boolean deleteFile(File file) {
		return file.delete();
	}

	public List<File> ping() throws IOException {
		Process p = Runtime.getRuntime().exec(COMMAND_LS);
		
		InputStream in = p.getInputStream();
		InputStreamReader reader = new InputStreamReader(in);
		BufferedReader breader = new BufferedReader(reader);
		
		ArrayList<File> files = new ArrayList<File>();
		
		String line;
		while ((line = breader.readLine()) != null) {
			files.add(new File(line));
		}
		return files;
	}

	public void run() {
		while(running) {
			try {
				tick();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void tick() throws IOException {
		try {
			ping();
			if(getShouldLog()) {
				log();
			}
			Thread.sleep(getInterval());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Long getInterval() {
		return Long.valueOf(resources.getString("interval"));
	}

	private void log() {
		PrintWriter p = null;
		
		try {
			File file = new File(getLogFileName());
			if(!file.exists()) {
				file.createNewFile();
			}
				
			p = new PrintWriter(new FileWriter(file, true)); 
			p.println(String.format("%s: Pinged folder %s", getTime(), getPath()));
			p.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(p != null) { 
				p.close();
			}
		}
	}

	private String getTime() {
		return DateFormat.getTimeInstance().format(new Date());
	}


	protected void stop() {
		running = false;
	}

	public String getLogFileName() {
		return resources.getString("logfile");
	}
	
	public boolean getShouldLog() {
		return Boolean.valueOf(resources.getString("log"));
	}
	
	public String getPath() {
		return resources.getString("path");
	}
	
	public static void main(String[] args) {
		new Thread(new DiskPinger()).start();
	}
}
