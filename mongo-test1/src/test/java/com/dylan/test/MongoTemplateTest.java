package com.dylan.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoTemplateTest {
	private static final Logger log = LoggerFactory.getLogger(MongoTemplateTest.class);

	private MongoDatabase db;
	private MongoCollection<Document> doc;
	private MongoClient client;

	@Before
	public void init() {
		client = new MongoClient("39.108.79.138", 27017);
		db = client.getDatabase("test");
		doc = db.getCollection("users");
	}

	/**
	 * 测试插入数据
	 */
	@Test
	public void testInsert() {
		Document doc1 = new Document();
		doc1.append("username", "tom");
		doc1.append("contry", "Englan");
		doc1.append("age", 18);
		doc1.append("lenght", 1.77f);
		doc1.append("salary", new BigDecimal("6585.23"));
		Map<String, String> address1 = new HashMap<String, String>();
		address1.put("aCode", "0000");
		address1.put("add", "xxx000");
		doc1.append("address", address1);
		Map<String, Object> favorites1 = new HashMap<String, Object>();
		favorites1.put("movies", Arrays.asList("aa", "bb"));
		favorites1.put("cities", Arrays.asList("广州", "深圳"));
		doc1.append("favorites", favorites1);

		Document doc2 = new Document();
		doc2.append("username", "tony");
		doc2.append("contry", "USA");
		doc2.append("age", 28);
		doc2.append("lenght", 1.77f);
		doc2.append("salary", new BigDecimal("6585.23"));
		Map<String, String> address2 = new HashMap<String, String>();
		address2.put("aCode", "0000");
		address2.put("add", "xxx000");
		doc2.append("address", address2);
		Map<String, Object> favorites2 = new HashMap<String, Object>();
		favorites2.put("movies", Arrays.asList("cc", "dd"));
		favorites2.put("cities", Arrays.asList("南宁", "衡阳"));
		doc2.append("favorites", favorites2);

		doc.insertMany(Arrays.asList(doc1, doc2));
	}

	@Test
	public void testDelete() {
		// delete from users where username = 'iuv'
		DeleteResult deleteMany = doc.deleteMany(Filters.eq("username", "iuv"));
		log.info("删除的行数为：{}", deleteMany.getDeletedCount());

		// delete from users where age >8 and age <25
		DeleteResult deleteMany2 = doc.deleteMany(and(gt("age", 8), lt("age", 25)));
		log.info("删除的行数为：{}", deleteMany2.getDeletedCount());
	}

	@Test
	public void testUpdate() {
		// update users set age = 6 where username = 'tom'
		UpdateResult updateNum = doc.updateMany(eq("username", "tom"), new Document("$set", new Document("age", 6)));
		log.info("更新的行数为：{}", updateNum.getModifiedCount());

		// update users set favorites.movies add "蜘蛛侠","钢铁侠" where
		// favorites.cities has "深圳"
		UpdateResult updateNum2 = doc.updateMany(eq("favorites.cities", "深圳"),
				addEachToSet("favorites.movies", Arrays.asList("蜘蛛侠", "钢铁侠")));
		log.info("更新的行数为：{}", updateNum2.getModifiedCount());
	}

	@Test
	public void testFind() {
		final List<Document> ret = new ArrayList<>();
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				log.info("{}", t.toJson());
				ret.add(t);
			}
		};

		// select * from users where favorites.cities has "深圳"、"广州"
		FindIterable<Document> find = doc.find(all("favorites.cities", Arrays.asList("深圳", "广州")));
		find.forEach(printBlock);
		log.info(String.valueOf(ret.size()));
		ret.removeAll(ret);// 将集合清空

		// select * from users where username like '%om%' and (contry=Englan or
		// contry=USA)
		String regexStr = ".*om.*";
		Bson regex = regex("username", regexStr);
		Bson or = or(eq("contry", "Englan"), eq("contry", "USA"));
		FindIterable<Document> find2 = doc.find(and(regex, or));
		find2.forEach(printBlock);
		log.info(String.valueOf(ret.size()));
	}
}
