/*-
 * MIT License
 *
 * Copyright (c) 2025 Iwao AVE!
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package it;

import static org.junit.Assume.*;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

public abstract class IntegrationTestBase {

  protected static SqlSessionFactory sqlSessionFactory;

  public IntegrationTestBase() {
    super();
  }

  @Test
  void testInsert() {
    Integer id = null;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setLastName("Lund");
      user.setFirstName("Ilsa");
      mapper.insertUser(user);
      id = user.getId();
      assertNotNull(id);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(id);
      assertNull(user.getLastName());
      assertEquals("Ilsa", user.getFirstName());
    }
  }

  @Test
  void testUpdate_SingleArgWithoutParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(3);
      user.setLastName("Cruise");
      user.setFirstName("Tom");
      assertEquals(1, mapper.updateUserById(user));
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(3);
      assertEquals("Cruise", user.getLastName());
      assertEquals("Tom", user.getFirstName());
    }
  }

  @Test
  void testUpdate_MultiArgsWithoutParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setLastName("Dreadful");
      user.setFirstName("Penny");
      assertEquals(1, mapper.updateUser(user, 2));
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(2);
      assertEquals("Dreadful", user.getLastName());
      assertEquals("Penny", user.getFirstName());
    }
  }

  @Test
  void testUpdate() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Company company = new Company();
      company.setId(1);
      company.setName("Adobe");
      mapper.updateCompanyById(company);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Company company = mapper.getCompany(1);
      assertEquals("Adobe", company.getName());
      assertNull(company.getAddress());
    }
  }

  @Test
  void testUpsert() {
    assumeFalse(this.getClass() == OracleTest.class);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Company company = new Company();
      company.setId(99);
      company.setName("Sun Microsystems");
      company.setAddress("California");
      mapper.upsertCompanyOnId(company);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Company company = mapper.getCompany(99);
      assertEquals("Sun Microsystems", company.getName());
      assertEquals("California", company.getAddress());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Company company = new Company();
      company.setId(99);
      company.setName("Connectix");
      company.setAddress("California");
      mapper.upsertCompanyOnId(company);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Company company = mapper.getCompany(99);
      assertEquals("Connectix", company.getName());
      assertEquals("California", company.getAddress());
    }
  }

}