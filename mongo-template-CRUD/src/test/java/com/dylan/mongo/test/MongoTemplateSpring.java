package com.dylan.mongo.test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import com.dylan.mongo.pojo.Address;
import com.dylan.mongo.pojo.Favorites;
import com.dylan.mongo.pojo.User;
import com.mongodb.WriteResult;

/**
 * spring pojo的操作测试
 * 
 * @author dylan
 * @date 2018年4月13日下午11:43:55
 */
@RunWith(SpringRunner.class)
@SpringBootTest // springboot项目的加载方式
// @ContextConfiguration("classpath:applicationContext.xml") //spring加载配置的方式
public class MongoTemplateSpring {
	private static final Logger log = LoggerFactory.getLogger(MongoTemplateSpring.class);

	@Resource
	private MongoOperations tempelate;

	/**
	 * 测试插入数据
	 */
	@Test
	public void testInsert() {
		User user1 = new User();
		user1.setUsername("tom");
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
		user2.setUsername("tom");
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
		tempelate.insertAll(Arrays.asList(user1, user2));
	}

	/**
	 * 删除测试
	 */
	@Test
	public void testDelete() {
		// delete from users where username = 'dylan1'
		WriteResult remove = tempelate.remove(Query.query(Criteria.where("username").is("tony")), User.class);
		log.info("remove.getN()====={}", remove.getN());
		// delete from users where age >8 and age <25
		WriteResult remove2 = tempelate.remove(
				Query.query(new Criteria().andOperator(Criteria.where("age").gt(8), Criteria.where("age").lt(25))),
				User.class);
		log.info("remove2.getN()====={}", remove2.getN());
	}

	/**
	 * 更新测试
	 */
	@Test
	public void testUpdate() {
		// update users set age = 10 where username = 'tom'
		WriteResult updateResult = tempelate.updateMulti(Query.query(Criteria.where("username").is("tom")),
				Update.update("age", 10), User.class);
		log.info("updateResult=：{}", updateResult);
		log.info("updateResult.getN()={}", updateResult.getN());

		// update users set favorites.movies add "蜘蛛侠","钢铁侠" where
		// favorites.cities has "深圳"
		Query query = Query.query(Criteria.where("favorites.cities").is("深圳"));
		Update update = new Update().addToSet("favorites.movies").each("钢铁侠", "蜘蛛侠");
		WriteResult updateResult2 = tempelate.updateMulti(query, update, User.class);
		log.info("updateResult2={}", updateResult2);
		log.info("updateResult2.getN()={}", updateResult2.getN());
	}

	/**
	 * 查询测试-------将Document修改为User即可，其它不需要修改
	 */
	@Test
	public void testFind() {
		// select * from users where favorites.cities has "深圳"、"广州"
		List<User> find = tempelate.find(Query.query(Criteria.where("favorites.cities").all(Arrays.asList("深圳", "广州"))),
				User.class);
		log.info("find.size()={}", find.size());

		// select * from users where username like '%om%' and (contry=Englan or
		// contry=USA)
		String regexStr = ".*om.*";
		Criteria regex = Criteria.where("username").regex(regexStr);
		Criteria or1 = Criteria.where("country").is("Englan");
		Criteria or2 = Criteria.where("country").is("USA");
		Criteria or = new Criteria().orOperator(or1, or2);
		Query query = Query.query(new Criteria().andOperator(regex, or));
		List<User> find2 = tempelate.find(query, User.class);
		for (User user : find2) {
			log.info("user==={}", user.toString());
		}
	}
}
