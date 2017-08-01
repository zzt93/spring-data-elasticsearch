package org.springframework.data.elasticsearch.repositories.highlight;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.entities.File;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author zzt
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/repository-test-highlight-annotation.xml")
public class FileRepoTests {

	@Autowired private FileRepo fileRepo;

	@Autowired private ElasticsearchTemplate elasticsearchTemplate;

	@Before
	public void before() {
		elasticsearchTemplate.deleteIndex(File.class);
		elasticsearchTemplate.createIndex(File.class);
		elasticsearchTemplate.putMapping(File.class);
		elasticsearchTemplate.refresh(File.class);
	}

	@Test
	public void hasHighlight() {
		// given
		String content0 = "elastic search highlight annotation test";
		String content1 = "simple content here";
		fileRepo.save(new File("1", "a test here", content0));
		fileRepo.save(new File("2", "content summary here", content0));
		fileRepo.save(new File("3", "test again", content1));
		fileRepo.save(new File("4", "again summary", content1));
		String em = "<em>";

		// when
		List<File> file1 = fileRepo.findByContent("search", PageRequest.of(0, 10)).getContent();
		// then
		assertThat(file1.size(), equalTo(2));
		for (File file : file1) {
			assertTrue(file.getContent().contains(em));
		}
		// when
		List<File> file2 = fileRepo.findByTitle("test");
		// then
		assertThat(file2.size(), equalTo(2));
		for (File file : file2) {
			assertFalse(file.getTitle().contains(em));
		}
		// when
		List<File> file3 = fileRepo.findByTitleButHighlightContent("test");
		// then
		assertThat(file3.size(), equalTo(2));
		for (File file : file3) {
			assertFalse(file.getTitle().contains(em));
		}
		// when
		List<File> f3 = fileRepo.findByTitleOrContentHighlightTitle("test", PageRequest.of(0, 10)).getContent();
		// then
		assertThat(f3.size(), equalTo(3));
		for (File file : f3) {
			assertTrue(file.getTitle().contains(em + "test") || file.getContent().contains("test"));
		}
		// when
		List<File> f4 = fileRepo.findByTitleOrContentInListHighlightContent("here");
		// then
		assertThat(f4.size(), equalTo(4));
		for (File file : f4) {
			assertTrue(file.getTitle().contains("here") || file.getContent().contains(em + "here"));
		}
		// when
		List<File> f5 = fileRepo.findByTitleOrContentInListHighlightBoth("here");
		// then
		assertThat(f5.size(), equalTo(4));
		for (File file : f5) {
			assertTrue(file.getTitle().contains(em) || file.getContent().contains(em));
		}
	}
}
