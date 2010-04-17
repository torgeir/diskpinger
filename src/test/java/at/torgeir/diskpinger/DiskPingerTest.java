package at.torgeir.diskpinger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.torgeir.diskpinger.DiskPinger;

public class DiskPingerTest {

	private static final String TMP_FILE = "tmp";
	private DiskPinger pinger;
	private File file;
	
	private int tickCount;
	private int pingCount;

	public DiskPingerTest() {
		pinger = new DiskPinger();
		file = new File(TMP_FILE);
	}
	
	@Before
	public void before() {
		tickCount = 0;
		pingCount = 0;
	}
	
	@Test 
	public void shouldReadPathFromProperties() throws IOException {
		assertEquals("/test/path", pinger.getPath());
	}
	
	@Test 
	public void shouldWriteFile() throws IOException {
		boolean wrote = pinger.writeFile(file);
		assertTrue(wrote);
		assertTrue(file.exists());
	}
	
	@Test
	public void shouldDeleteFile() throws IOException {
		pinger.writeFile(file);
		boolean deleted = pinger.deleteFile(file);
		assertTrue(deleted);
		assertFalse(file.exists());
	}
	
	@Test
	public void shouldNotFailOnDeletingFileThatDoesNotExist() {
		assertFalse(file.exists());
		assertFalse(pinger.deleteFile(file));
	}
	
	@Test
	public void shouldListFilesInDirectory() throws IOException {
		pinger.writeFile(file);
		assertTrue(pinger.ping().contains(file));
	}

	@Test
	public void shouldRunAtAnInterval() {
		final int expectedNumberOfRuns = 4;
		pinger = new DiskPinger() {
			
			public List<File> ping() throws IOException {
				pingCount++;
				return super.ping();
			}
			
			@Override
			public void tick() {
				try {
					if(++tickCount == expectedNumberOfRuns) {
						stop();
					}
					super.tick();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		pinger.run();
		assertEquals(expectedNumberOfRuns, pingCount);
	}
	
	@Test
	public void shouldLog() throws Exception {
		final Integer numberOfRuns = 2;
		pinger = new DiskPinger() {
			
			@Override
			protected void tick() throws IOException {
				if(++tickCount == numberOfRuns) {
					stop();
				}
				super.tick();
			}
		};
		
		pinger.run();
		
		FileReader reader = new FileReader(new File(pinger.getLogFileName()));
		BufferedReader breader = new BufferedReader(reader);
		
		String line;
		Integer lines = 0;
		while((line = breader.readLine()) != null) {
			lines++;
		}
		assertEquals(numberOfRuns, lines);
	}
	
	@After
	public void deleteTmpFile() {
		if(file.exists()) {
			file.delete();
		}
		assert(file.exists() == false);
		
		File log = new File(pinger.getLogFileName());
		if(log.exists()) {
			log.delete();
		}
	}
}
