package com.dylan.mongo.test;

import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Updates.addEachToSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dylan.mongo.pojo.Address;
import com.dylan.mongo.pojo.Favorites;
import com.dylan.mongo.pojo.User;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * 原生java驱动pojo的操作测试
 * 
 * @author dylan
 * @date 2018年4月13日下午11:43:55
 */
public class MongoTemplateTest2 {
	private static final Logger log = LoggerFactory.getLogger(MongoTemplateTest2.class);

	private MongoDatabase db;
	private MongoCollection<User> doc;
	private MongoClient client;

	@Before
	public void init() {
		// 编解码器的list
		List<CodecRegistry> codecRegisties = new ArrayList<CodecRegistry>();
		// 将默认的编解码器加入到list
		codecRegisties.add(MongoClient.getDefaultCodecRegistry());
		// 生成一个pojo的编解码器注册中心
		CodecRegistry pojoProviders = CodecRegistries
				.fromProviders(PojoCodecProvider.builder().automatic(true).build());
		codecRegisties.add(pojoProviders);
		// 通过编解码器list生成编解码器注册中心
		CodecRegistry register = CodecRegistries.fromRegistries(codecRegisties);
		// 把编解码器注册中心放到MongoClientOptions中去（MongoClientOption可以看成是封装了配置文件的类）
		MongoClientOptions build = MongoClientOptions.builder().codecRegistry(register).build();
		// 创建服务器连接
		ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
		client = new MongoClient(serverAddress, build);
		db = client.getDatabase("test");
		doc = db.getCollection("users", User.class);
	}

	/**
	 * 测试插入数据
	 */
	@Test
	public void testInsert() {
		User user1 = new User();
		user1.setUsername("dylan1");
		user1.setAge(18);
		user1.setCountry("中国");
		user1.setLenght(1.70f);
		user1.setSalary(new BigDecimal(10000));
		Address address1 = new Address();
		address1.setaCode("0000");
		address1.setAdd("XXXXXXXXX");
		user1.setAddress(address1);
		Favorites favorites1 = new Favorites();
		favorites1.setCities(Arrays.asList("广州", "深圳"));
		favorites1.setMovies(Arrays.asList("西游记", "水浒传"));
		user1.setFavorites(favorites1);

		User user2 = new User();
		user2.setUsername("dylan2");
		user2.setAge(18);
		user2.setCountry("美国");
		user2.setLenght(1.70f);
		user2.setSalary(new BigDecimal(10000));
		Address address2 = new Address();
		address2.setaCode("1111");
		address2.setAdd("YYYYYYYYYY");
		user2.setAddress(address2);
		Favorites favorites2 = new Favorites();
		favorites2.setCities(Arrays.asList("南宁", "衡阳"));
		favorites2.setMovies(Arrays.asList("红楼梦", "三国演义"));
		user2.setFavorites(favorites2);

		doc.insertMany(Arrays.asList(user1, user2));// 插入方式与document调用同一个方法
	}

	/**
	 * 删除测试 -----与Document的方式一摸一样，代码完全不需要修改
	 */
	@Test
	public void testDelete() {
		// delete from users where username = 'dylan1'
		DeleteResult deleteMany = doc.deleteMany(Filters.eq("username", "dylan1"));
		log.info("删除的行数为：{}", deleteMany.getDeletedCount());

		// delete from users where age >8 and age <25
		DeleteResult deleteMany2 = doc.deleteMany(and(gt("age", 8), lt("age", 25)));
		log.info("删除的行数为：{}", deleteMany2.getDeletedCount());
	}

	/**
	 * 更新测试 -----与Document的方式一摸一样，代码完全不需要修改
	 */
	@Test
	public void testUpdate() {
		// update users set age = 10 where username = 'tom'
		UpdateResult updateNum = doc.updateMany(eq("username", "tom"), new Document("$set", new Document("age", 10)));
		log.info("更新的行数为：{}", updateNum.getModifiedCount());

		// update users set favorites.movies add "蜘蛛侠","钢铁侠" where
		// favorites.cities has "深圳"
		UpdateResult updateNum2 = doc.updateMany(eq("favorites.cities", "深圳"),
				addEachToSet("favorites.movies", Arrays.asList("蜘蛛侠", "钢铁侠")));
		log.info("更新的行数为：{}", updateNum2.getModifiedCount());
	}

	/**
	 * 查询测试-------将Document修改为User即可，其它不需要修改
	 */
	@Test
	public void testFind() {
		final List<User> ret = new ArrayList<>();
		Block<User> printBlock = new Block<User>() {
			@Override
			public void apply(User user) {
				log.info("{}", user.toString());
				ret.add(user);
			}
		};

		// select * from users where favorites.cities has "深圳"、"广州"
		FindIterable<User> find = doc.find(all("favorites.cities", Arrays.asList("深圳", "广州")));
		find.forEach(printBlock);
		log.info(String.valueOf(ret.size()));
		ret.removeAll(ret);// 将集合清空

		// select * from users where username like '%om%' and (contry=Englan or
		// contry=USA)
		String regexStr = ".*om.*";
		Bson regex = regex("username", regexStr);
		Bson or = or(eq("contry", "Englan"), eq("contry", "USA"));
		FindIterable<User> find2 = doc.find(and(regex, or));
		find2.forEach(printBlock);
		log.info(String.valueOf(ret.size()));
	}
}
